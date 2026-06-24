package ni.edu.uam.WebSockerChat.chat.dto;

import ni.edu.uam.WebSockerChat.chat.model.Usuario;
import ni.edu.uam.WebSockerChat.chat.model.Viaje;

import java.time.LocalDateTime;

public record MensajeResponse(
        Long id,
        Long viajeId,
        Long usuarioId,
        String contenido,
        LocalDateTime fechaEnvio
) {}
