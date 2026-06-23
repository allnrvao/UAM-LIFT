package ni.edu.uam.WebSockerChat.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    // IMPORTANTE: JavaTimeModule es necesario para que Jackson entienda el LocalDateTime de tu Response
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // Memoria RAM para las salas de chat
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>> salasDeChat = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 1. Recibimos el DTO (MensajeRequest) desde Android
            MensajeRequest request = mapper.readValue(message.getPayload(), MensajeRequest.class);
            Long viajeId = request.viajeId();

            // Guardamos al usuario en la "sala" si no estaba
            session.getAttributes().put("viajeId", viajeId);
            salasDeChat.computeIfAbsent(viajeId, k -> new CopyOnWriteArrayList<>()).addIfAbsent(session);

            // 2. Usamos tu Servicio para guardar en la BD y obtener la respuesta armada
            MensajeResponse response = chatService.procesarYGuardarMensaje(request);

            // 3. Convertimos la respuesta a JSON
            String jsonRespuesta = mapper.writeValueAsString(response);
            TextMessage mensajeDeSalida = new TextMessage(jsonRespuesta);

            // 4. Se lo mandamos a TODOS en la sala de ese viaje
            for (WebSocketSession s : salasDeChat.get(viajeId)) {
                if (s.isOpen()) {
                    s.sendMessage(mensajeDeSalida);
                }
            }

        } catch (Exception e) {
            System.err.println("Error procesando el mensaje de chat: " + e.getMessage());
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long viajeId = (Long) session.getAttributes().get("viajeId");
        if (viajeId != null && salasDeChat.containsKey(viajeId)) {
            salasDeChat.get(viajeId).remove(session);

            // Limpiamos la sala si ya no hay nadie chateando
            if (salasDeChat.get(viajeId).isEmpty()) {
                salasDeChat.remove(viajeId);
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