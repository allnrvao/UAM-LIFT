package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.Destino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepoDestino extends JpaRepository<Destino, Long> {
    boolean existsByNombre(String nombre);
    Optional<Destino> findByNombre(String nombre);
}
