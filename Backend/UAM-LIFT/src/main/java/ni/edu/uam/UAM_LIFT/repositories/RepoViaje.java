package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.enums.EstadoViaje;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.models.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface RepoViaje extends JpaRepository<Viaje, Long> {

    List<Viaje> findByConductorAndEstadoViaje(Usuario conductor, EstadoViaje estadoViaje);
    List<Viaje> findByConductor(Usuario conductor);
}