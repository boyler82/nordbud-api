package pl.nordbud.nordbud_api.mail;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import pl.nordbud.nordbud_api.LeadRequest;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}") private String ownerEmail;

    public MailService(JavaMailSender mailSender) { this.mailSender = mailSender; }

    public void sendAutoReplyToClient(LeadRequest req) throws Exception {
        String html = """
      <div style="font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif;line-height:1.6;color:#111;">
        <h2 style="margin:0 0 8px;">Dziękujemy za kontakt, %s!</h2>
        <p>Otrzymaliśmy Twoje zapytanie i wkrótce się odezwiemy.</p>
        <p style="margin-top:16px;"><strong>Twoja wiadomość:</strong></p>
        <div style="white-space:pre-wrap;border-left:3px solid #ddd;padding-left:12px;margin-top:6px;">%s</div>
        <p style="margin-top:24px;color:#888;font-size:12px;">Nordbud — wiadomość automatyczna. Nie odpowiadaj na tego maila.</p>
      </div>
    """.formatted(esc(req.name()), esc(req.message()));

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name()
        );
        helper.setTo(req.email());                    // adres klienta
        helper.setSubject("Nordbud — potwierdzenie otrzymania zapytania");
        helper.setFrom(ownerEmail, "Nordbud – www");  // nadawca = Twoje konto
        helper.setText(html, true);                   // HTML
        mailSender.send(msg);
    }

    public void sendLeadToOwner(LeadRequest req, HttpServletRequest http) throws Exception {
        String subject = "Valheimbygg – nowe zapytanie od: " + req.name();
        String when = ZonedDateTime.now(ZoneId.of("Europe/Warsaw"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        String ip = clientIp(http);

        String html = """
              <div style="font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif;line-height:1.6;color:#111;">
                <h2 style="margin:0 0 8px;">Nowe zapytanie ofertowe</h2>
                <p style="margin:0 0 16px;color:#555;">Poniżej szczegóły przesłane z formularza na stronie.</p>
                <table style="border-collapse:collapse;width:100%%;max-width:640px;">
                  <tbody>
                    <tr><td style="padding:8px 0;width:160px;color:#666;">Data</td><td style="padding:8px 0;">%s</td></tr>
                    <tr><td style="padding:8px 0;color:#666;">Imię i nazwisko</td><td style="padding:8px 0;"><strong>%s</strong></td></tr>
                    <tr><td style="padding:8px 0;color:#666;">E-mail</td><td style="padding:8px 0;"><a href="mailto:%s">%s</a></td></tr>
                    <tr><td style="padding:8px 0;color:#666;">Telefon</td><td style="padding:8px 0;">%s</td></tr>
                    <tr><td style="padding:8px 0;color:#666;vertical-align:top;">Wiadomość</td><td style="padding:8px 0;white-space:pre-wrap;">%s</td></tr>
                    <tr><td style="padding:8px 0;color:#666;">Zgoda RODO</td><td style="padding:8px 0;">%s</td></tr>
                    <tr><td style="padding:8px 0;color:#666;">IP</td><td style="padding:8px 0;">%s</td></tr>
                  </tbody>
                </table>
                <p style="margin-top:24px;color:#888;font-size:12px;">Wiadomość wygenerowana automatycznie przez serwis nordbud-api.</p>
              </div>
            """.formatted(
                esc(when),
                esc(req.name()),
                esc(req.email()), esc(req.email()),
                esc(empty(req.phone())),
                esc(req.message()),
                req.consent() ? "TAK" : "NIE",
                esc(ip)
        );

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        helper.setTo(ownerEmail);
        helper.setSubject(subject);
        helper.setFrom(ownerEmail, "Valheimbygg – www");   // nadawca = Twoje konto Gmail
        helper.setReplyTo(req.email(), req.name());     // odpowiedz trafia do klienta
        helper.setText(html, true);                     // HTML
        mailSender.send(msg);
    }

    private static String clientIp(HttpServletRequest http) {
        String fwd = http.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) return fwd.split(",")[0].trim();
        return http.getRemoteAddr();
    }
    private static String empty(String s){ return s==null? "": s; }
    private static String esc(String s){
        if (s==null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}