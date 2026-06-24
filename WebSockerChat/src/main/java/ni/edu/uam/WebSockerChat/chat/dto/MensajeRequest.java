package ni.edu.uam.WebSockerChat.chat.dto;

import ni.edu.uam.WebSockerChat.chat.model.Usuario;
import ni.edu.uam.WebSockerChat.chat.model.Viaje;

// Ahora exigimos que Android nos mande el nombre en el JSON
public record MensajeRequest(
         Long viajeId,
        Long usuarioId,
        String contenido
) {}