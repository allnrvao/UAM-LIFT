package ni.edu.uam.WebSockerChat.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeRequest;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeResponse;
import ni.edu.uam.WebSockerChat.chat.service.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Esto hace que la fecha sea un String (ISO-8601)

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>> salasDeChat = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message
    ) {

        try {

            MensajeRequest request =
                    mapper.readValue(
                            message.getPayload(),
                            MensajeRequest.class
                    );

            Long viajeId =
                    (Long) session.getAttributes()
                            .get("viajeId");

            if (viajeId == null) {
                throw new IllegalStateException(
                        "La sesión no está asociada a ningún viaje"
                );
            }

            // CORREGIDO: Se agregó el '!' para verificar si el usuario NO pertenece al viaje
            if (!chatService.usuariopertenceAlViaje(viajeId, request.usuarioId())) {
                throw new IllegalStateException(
                        "El usuario no pertenece al viaje"
                );
            }

            MensajeResponse response =
                    chatService.guardarMensaje(request);

            TextMessage mensajeDeSalida =
                    new TextMessage(
                            mapper.writeValueAsString(response)
                    );

            CopyOnWriteArrayList<WebSocketSession> sala =
                    salasDeChat.get(viajeId);

            if (sala == null) {
                return;
            }

            for (WebSocketSession s : sala) {

                if (!s.isOpen()) {
                    continue;
                }

                synchronized (s) {
                    s.sendMessage(mensajeDeSalida);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // MEJORA: Informa el error al cliente WebSocket para evitar fallos silenciosos en la app móvil
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage("{\"error\": \"" + e.getMessage() + "\"}"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String viajeIdParam =
                session.getUri()
                        .getQuery()
                        .split("=")[1];

        Long viajeId =
                Long.parseLong(viajeIdParam);

        session.getAttributes()
                .put("viajeId", viajeId);

        salasDeChat
                .computeIfAbsent(
                        viajeId,
                        k -> new CopyOnWriteArrayList<>()
                )
                .add(session);
    }
}