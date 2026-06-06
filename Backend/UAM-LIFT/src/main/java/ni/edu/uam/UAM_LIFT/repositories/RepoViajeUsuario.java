package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.enums.EstadoViajeUsuario;
import ni.edu.uam.UAM_LIFT.models.ViajeUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepoViajeUsuario extends JpaRepository<ViajeUsuario, Long>
{
    boolean existsByViajeIdAndUsuarioId(
            Long viajeId,
            Long usuarioId
    );

    long countByViajeIdAndEstado(
            Long viajeId,
            EstadoViajeUsuario estado
    );

    Optional<ViajeUsuario>
    findByViajeIdAndUsuarioCif(
            Long viajeId,
            String usuarioCif
    );
}
