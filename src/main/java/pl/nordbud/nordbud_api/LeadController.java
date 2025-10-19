package pl.nordbud.nordbud_api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nordbud.nordbud_api.mail.MailService;
import pl.nordbud.nordbud_api.security.RateLimiter;
import io.github.bucket4j.Bucket;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LeadController {
    private static final Logger log = LoggerFactory.getLogger(LeadController.class);

    private final MailService mailService;
    private final RateLimiter rateLimiter;

    public LeadController(MailService mailService, RateLimiter rateLimiter) {
        this.mailService = mailService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/lead")
    public ResponseEntity<?> createLead(@Valid @RequestBody LeadRequest req, HttpServletRequest http) {

        // 1) HONEYPOT: odfiltruj boty
        if (req.website() != null && !req.website().isBlank()) {
            return ResponseEntity.noContent().build(); // 204 – udaj sukces, nie rób nic
        }

        // 2) RATE-LIMIT: klucz IP (z X-Forwarded-For lub remoteAddr)
        String ip = Optional.ofNullable(http.getHeader("X-Forwarded-For"))
                .map(s -> s.split(",")[0].trim())
                .orElseGet(http::getRemoteAddr);

        Bucket bucket = rateLimiter.resolveBucket(ip);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "Zbyt wiele prób, spróbuj za chwilę"
            ));
        }

        // 3) Walidacje biznesowe
        if (!req.consent()) {
            return ResponseEntity.badRequest().body(Map.of("error","Wymagana zgoda RODO"));
        }

        log.info("New lead: ip={}, name={}, email={}, phone={}, message={}",
                ip, req.name(), req.email(), req.phone(), req.message());

        try {
            mailService.sendLeadToOwner(req, http);
            try { mailService.sendAutoReplyToClient(req); } catch (Exception e) { log.error("Autoresponder error", e); }
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("Mail send error", e);
            return ResponseEntity.internalServerError().body(Map.of("error","Nie udało się wysłać e-maila"));
        }
    }
}