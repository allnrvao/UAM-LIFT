package ni.edu.uam.WebSockerChat.chat.dto;

// Ahora exigimos que Android nos mande el nombre en el JSON
public record MensajeRequest(
        Long viajeId,
        Long remitenteId,
        String remitenteNombre,
        String contenido
) {}