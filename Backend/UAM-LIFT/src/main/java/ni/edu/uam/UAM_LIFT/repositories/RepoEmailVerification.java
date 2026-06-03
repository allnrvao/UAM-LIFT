package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.EmailVerification;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepoEmailVerification extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByUsuario(Usuario usuario);
}

