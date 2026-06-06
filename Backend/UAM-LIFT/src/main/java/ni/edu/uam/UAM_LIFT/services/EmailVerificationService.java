package ni.edu.uam.UAM_LIFT.services;

import ni.edu.uam.UAM_LIFT.models.EmailVerification;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoEmailVerification;
import ni.edu.uam.UAM_LIFT.repositories.RepoUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class EmailVerificationService {

    private static final String DOMAIN = "@uamv.edu.ni";

    private final RepoUsuario repoUsuario;
    private final RepoEmailVerification repoEmailVerification;
    private final JavaMailSender mailSender;
    private final int ttlMinutes;

    public EmailVerificationService(RepoUsuario repoUsuario,
                                    RepoEmailVerification repoEmailVerification,
                                    JavaMailSender mailSender,
                                    @Value("${app.email.verification.ttl-minutes:10}") int ttlMinutes) {
        this.repoUsuario = repoUsuario;
        this.repoEmailVerification = repoEmailVerification;
        this.mailSender = mailSender;
        this.ttlMinutes = ttlMinutes;
    }

    public boolean requestVerification(String correo) {
        if (correo == null || !correo.endsWith(DOMAIN)) {
            return false;
        }
        Optional<Usuario> optionalUsuario = repoUsuario.findByCorreo(correo);
        if (optionalUsuario.isEmpty()) {
            return false;
        }
        Usuario usuario = optionalUsuario.get();
        if (usuario.isCorreoVerificado()) {
            return true;
        }

        EmailVerification verification = repoEmailVerification.findByUsuario(usuario)
                .orElseGet(() -> new EmailVerification(null, usuario, null, null, null));

        String code = generateCode();
        verification.setCode(code);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        verification.setVerifiedAt(null);
        repoEmailVerification.save(verification);

        sendEmail(correo, code);
        return true;
    }

    public boolean confirmVerification(String correo, String code) {
        if (correo == null || code == null || !correo.endsWith(DOMAIN)) {
            return false;
        }
        Optional<Usuario> optionalUsuario = repoUsuario.findByCorreo(correo);
        if (optionalUsuario.isEmpty()) {
            return false;
        }
        Usuario usuario = optionalUsuario.get();
        if (usuario.isCorreoVerificado()) {
            return true;
        }
        Optional<EmailVerification> optionalVerification = repoEmailVerification.findByUsuario(usuario);
        if (optionalVerification.isEmpty()) {
            return false;
        }

        EmailVerification verification = optionalVerification.get();
        if (verification.getExpiresAt() == null || verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        if (!code.equals(verification.getCode())) {
            return false;
        }

        verification.setVerifiedAt(LocalDateTime.now());
        repoEmailVerification.save(verification);

        usuario.setCorreoVerificado(true);
        repoUsuario.save(usuario);
        return true;
    }

    private void sendEmail(String correo, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(correo);
        message.setSubject("Codigo de verificacion UAM-LIFT");
        message.setText("Tu codigo de verificacion es: " + code + "\nExpira en " + ttlMinutes + " minutos.");
        mailSender.send(message);
    }

    private String generateCode() {
        int value = 100000 + new Random().nextInt(900000);
        return String.valueOf(value);
    }
}

