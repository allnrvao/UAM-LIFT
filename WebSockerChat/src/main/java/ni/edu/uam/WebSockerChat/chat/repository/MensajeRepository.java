package ni.edu.uam.WebSockerChat.chat.repository;

import ni.edu.uam.WebSockerChat.chat.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByViajeIdOrderByFechaEnvioAsc(Long viajeId);
}