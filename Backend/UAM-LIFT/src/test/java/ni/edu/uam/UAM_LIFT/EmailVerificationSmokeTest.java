package ni.edu.uam.UAM_LIFT;

import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoEmailVerification;
import ni.edu.uam.UAM_LIFT.repositories.RepoUsuario;
import ni.edu.uam.UAM_LIFT.services.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationSmokeTest {

    @Mock
    private RepoUsuario repoUsuario;

    @Mock
    private RepoEmailVerification repoEmailVerification;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void requestVerificationRejectsInvalidDomain() {
        EmailVerificationService service = new EmailVerificationService(
                repoUsuario,
                repoEmailVerification,
                mailSender,
                10
        );

        boolean result = service.requestVerification("test@gmail.com");
        assertFalse(result);
    }

    @Test
    void requestVerificationAcceptsValidDomainAndUserExists() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("alumno@uamv.edu.ni");

        when(repoUsuario.findByCorreo("alumno@uamv.edu.ni")).thenReturn(Optional.of(usuario));
        when(repoEmailVerification.findByUsuario(usuario)).thenReturn(Optional.empty());
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        EmailVerificationService service = new EmailVerificationService(
                repoUsuario,
                repoEmailVerification,
                mailSender,
                10
        );

        boolean result = service.requestVerification("alumno@uamv.edu.ni");
        assertTrue(result);
    }

    @Test
    void requestVerificationReturnsFalseWhenUserMissing() {
        when(repoUsuario.findByCorreo(anyString())).thenReturn(Optional.empty());

        EmailVerificationService service = new EmailVerificationService(
                repoUsuario,
                repoEmailVerification,
                mailSender,
                10
        );

        boolean result = service.requestVerification("alumno@uamv.edu.ni");
        assertFalse(result);
    }
}
