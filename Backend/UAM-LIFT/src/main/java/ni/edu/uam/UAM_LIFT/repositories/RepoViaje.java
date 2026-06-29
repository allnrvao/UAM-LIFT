package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.enums.EstadoViaje;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.models.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface RepoViaje extends JpaRepository<Viaje, Long> {

    List<Viaje> findByConductorAndEstadoViaje(Usuario conductor, EstadoViaje estadoViaje);
    List<Viaje> findByConductor(Usuario conductor);
    @Modifying
    @Transactional
    @Query("""
    UPDATE Viaje v
    SET v.estadoViaje = ni.edu.uam.UAM_LIFT.enums.EstadoViaje.CANCELADO
    WHERE v.estadoViaje = ni.edu.uam.UAM_LIFT.enums.EstadoViaje.PROPUESTO 
    AND v.fechaHoraLlegada <= :fechaHoraActual
""")
    int cancelarViajesVencidos(@Param("fechaHoraActual") LocalDateTime fechaHoraActual);
}