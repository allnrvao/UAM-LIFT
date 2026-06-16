package ni.edu.uam.WebSockerChat.chat.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeRequest;
import ni.edu.uam.WebSockerChat.chat.dto.MensajeResponse;
import ni.edu.uam.WebSockerChat.chat.service.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Long, Set<WebSocketSession>> salasViajes = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long viajeId = extraerViajeId(session);
        salasViajes.computeIfAbsent(viajeId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("Nueva conexión establecida en el viaje ID: {}", viajeId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long viajeId = extraerViajeId(session);

        try {
            MensajeRequest request = objectMapper.readValue(message.getPayload(), MensajeRequest.class);
            MensajeResponse response = chatService.procesarYGuardarMensaje(request);
            TextMessage mensajeSalida = new TextMessage(objectMapper.writeValueAsString(response));

            Set<WebSocketSession> sala = salasViajes.getOrDefault(viajeId, Collections.emptySet());
            for (WebSocketSession s : sala) {
                if (s.isOpen()) {
                    s.sendMessage(mensajeSalida);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error procesando JSON: {}", e.getMessage());
            session.sendMessage(new TextMessage("{\"error\": \"JSON inválido. Asegúrate de enviar remitenteNombre.\"}"));
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long viajeId = extraerViajeId(session);
        Set<WebSocketSession> sala = salasViajes.get(viajeId);
        if (sala != null) {
            sala.remove(session);
            if (sala.isEmpty()) {
                salasViajes.remove(viajeId);
            }
        }
        log.info("Conexión cerrada en el viaje ID: {}", viajeId);
    }

    private Long extraerViajeId(WebSocketSession session) {
        String uri = session.getUri().getPath();
        String[] segmentos = uri.split("/");
        return Long.parseLong(segmentos[segmentos.length - 1]);
    }
}