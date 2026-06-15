package ni.edu.uam.WebSockerChat.chat.service;

import lombok.RequiredArgsConstructor;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeRequest;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeResponse;
import ni.edu.uam.WebSockerChat.chat.model.Mensaje;
import ni.edu.uam.WebSockerChat.chat.repository.MensajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MensajeRepository mensajeRepository;

    @Transactional
    public MensajeResponse procesarYGuardarMensaje(MensajeRequest request) {
        // Guardamos todo directo, sin consultar a ninguna otra API
        Mensaje nuevoMensaje = Mensaje.builder()
                .viajeId(request.viajeId())
                .remitenteId(request.remitenteId())
                .remitenteNombre(request.remitenteNombre()) // Tomado del JSON de Android
                .contenido(request.contenido())
                .build();

        Mensaje guardado = mensajeRepository.save(nuevoMensaje);

        return new MensajeResponse(
                guardado.getId(),
                guardado.getViajeId(),
                guardado.getRemitenteId(),
                guardado.getRemitenteNombre(),
                guardado.getContenido(),
                guardado.getFechaEnvio()
        );
    }

    @Transactional(readOnly = true)
    public List<MensajeResponse> obtenerHistorial(Long viajeId) {
        return mensajeRepository.findByViajeIdOrderByFechaEnvioAsc(viajeId)
                .stream()
                .map(msg -> new MensajeResponse(
                        msg.getId(),
                        msg.getViajeId(),
                        msg.getRemitenteId(),
                        msg.getRemitenteNombre(),
                        msg.getContenido(),
                        msg.getFechaEnvio()
                ))
                .collect(Collectors.toList());
    }
}