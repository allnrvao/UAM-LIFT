package ni.edu.uam.WebSockerChat.chat.dto;

import java.time.LocalDateTime;

public record MensajeResponse(
        Long id,
        Long viajeId,
        Long remitenteId,
        String remitenteNombre,
        String contenido,
        LocalDateTime fechaEnvio
) {}
