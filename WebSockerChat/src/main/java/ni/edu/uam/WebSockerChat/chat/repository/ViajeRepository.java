package ni.edu.uam.WebSockerChat.chat.repository;

import ni.edu.uam.WebSockerChat.chat.model.Viaje;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ViajeRepository extends JpaRepository<Viaje, Long> {
    @Query("""
SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
FROM Viaje v JOIN v.pasajeros u
WHERE v.id = :viajeId AND u.id = :usuarioId
""")
    boolean usuarioPerteneceAlViaje(Long viajeId, Long usuarioId);
}
