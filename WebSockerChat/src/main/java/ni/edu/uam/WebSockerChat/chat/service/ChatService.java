package ni.edu.uam.WebSockerChat.chat.service;

import lombok.RequiredArgsConstructor;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeRequest;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeResponse;
import ni.edu.uam.WebSockerChat.chat.model.Mensaje;
import ni.edu.uam.WebSockerChat.chat.model.Usuario;
import ni.edu.uam.WebSockerChat.chat.model.Viaje;
import ni.edu.uam.WebSockerChat.chat.repository.MensajeRepository;
import ni.edu.uam.WebSockerChat.chat.repository.UsuarioRepository;
import ni.edu.uam.WebSockerChat.chat.repository.ViajeRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MensajeRepository mensajeRepository;
    private final ViajeRepository viajeRepository;
    private final UsuarioRepository usuarioRepository;

    public List<MensajeResponse> obtenerHistorial(Long viajeId) {

        List<Mensaje> mensajes =
                mensajeRepository.findByViajeIdOrderByFechaEnvioAsc(viajeId);

        return mensajes.stream()
                .map(mensaje -> new MensajeResponse(
                        mensaje.getId(),
                        mensaje.getViaje().getId(),
                        mensaje.getUsuario().getId(),
                        mensaje.getContenido(),
                        mensaje.getFechaEnvio()
                ))
                .toList();
    }

    public MensajeResponse guardarMensaje(MensajeRequest mensajeRequest) {

        Viaje viaje = viajeRepository
                .findById(mensajeRequest.viajeId())
                .orElseThrow(() ->
                        new RuntimeException("Viaje no encontrado"));

        Usuario usuario = usuarioRepository
                .findById(mensajeRequest.usuarioId())
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        Mensaje mensaje = new Mensaje();
        mensaje.setContenido(mensajeRequest.contenido());
        mensaje.setUsuario(usuario);
        mensaje.setViaje(viaje);

        Mensaje mensajeGuardado =
                mensajeRepository.save(mensaje);

        return new MensajeResponse(
                mensajeGuardado.getId(),
                mensajeGuardado.getViaje().getId(),
                mensajeGuardado.getUsuario().getId(),
                mensajeGuardado.getContenido(),
                mensajeGuardado.getFechaEnvio()
        );
    }

    public boolean usuariopertenceAlViaje(Long viajeId, Long usuarioId) {
        return viajeRepository.usuarioPerteneceAlViaje(usuarioId, viajeId);
    }

}