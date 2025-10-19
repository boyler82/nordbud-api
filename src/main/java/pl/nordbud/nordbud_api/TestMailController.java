package pl.nordbud.nordbud_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMailController {

    private final JavaMailSender mail;
    @Value("${spring.mail.username}") String ownerEmail;

    public TestMailController(JavaMailSender mail) {
        this.mail = mail;
    }

    @GetMapping("/api/test-mail")
    public ResponseEntity<?> test() {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(ownerEmail);
            msg.setFrom(ownerEmail); // GMAIL: 'from' musi być tym samym kontem, którym się logujesz
            msg.setSubject("Nordbud — test SMTP");
            msg.setText("To jest testowy e-mail z Nordbud API.");
            mail.send(msg);
            return ResponseEntity.ok().body(java.util.Map.of("sent", true, "to", ownerEmail));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}