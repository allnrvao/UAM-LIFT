package ni.edu.uam.UamLiftWebSocket.model;

public record Mensaje(
        String contenido,
        String remitente,
        String chatId
) {
}