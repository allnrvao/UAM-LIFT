package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RepoUsuario extends JpaRepository<Usuario, Long> {
     Optional<Usuario> findByNombreUsuario(String nombreUsuario);
     Optional<Usuario> findByCorreo(String correo);
     Optional<Usuario> findByCif(String cif);
}
