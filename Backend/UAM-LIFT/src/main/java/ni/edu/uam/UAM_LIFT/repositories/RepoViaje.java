package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.models.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RepoViaje extends JpaRepository<Viaje, Long> {


}
