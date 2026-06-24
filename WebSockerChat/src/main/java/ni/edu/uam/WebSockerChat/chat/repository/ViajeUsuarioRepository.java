package ni.edu.uam.WebSockerChat.chat.repository;

import ni.edu.uam.WebSockerChat.chat.model.Viaje;
import ni.edu.uam.WebSockerChat.chat.model.ViajeUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViajeUsuarioRepository extends JpaRepository<ViajeUsuario,Long>
{
    Optional<ViajeUsuario> findByViaje(Viaje viaje);
}
