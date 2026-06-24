package ni.edu.uamv.WebSocker.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import ni.edu.uamv.WebSocker.models.Ubicacion;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ViajeHandler extends TextWebSocketHandler {

    // IMPORTANTE: Se corrigió la importación de Jackson
    private final ObjectMapper mapper = new ObjectMapper();

    // Mapas concurrentes para soportar múltiples conexiones simultáneas sin crashear
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>> viajes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> ultimasUbicaciones = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("✅ Nueva conexión física abierta: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        try {
            Ubicacion ubicacion = mapper.readValue(payload, Ubicacion.class);
            Long idViaje = ubicacion.getIdViaje();
            String tipo = ubicacion.getTipo();

            if (idViaje == null || idViaje == 0 || tipo == null) return;

            // Asociar la sesión a este idViaje
            session.getAttributes().put("idViaje", idViaje);
            CopyOnWriteArrayList<WebSocketSession> usuarios = viajes.computeIfAbsent(idViaje, k -> new CopyOnWriteArrayList<>());

            if (!usuarios.contains(session)) {
                usuarios.add(session);
            }

            if ("SUSCRIBIR".equalsIgnoreCase(tipo)) {
                System.out.println("👤 Pasajero " + session.getId() + " suscrito al viaje: " + idViaje);

                // Si ya hay una ubicación guardada, se la mandamos de inmediato
                if (ultimasUbicaciones.containsKey(idViaje)) {
                    String ultimaCoordenadaJson = ultimasUbicaciones.get(idViaje);
                    session.sendMessage(new TextMessage(ultimaCoordenadaJson));
                    System.out.println("📍 Enviando última ubicación retenida al nuevo pasajero.");
                }

            } else if ("ACTUALIZAR".equalsIgnoreCase(tipo)) {
                // Guardar la última posición en memoria
                ultimasUbicaciones.put(idViaje, payload);

                // Reenviar a los demás (Broadcast)
                String idEmisor = session.getId();
                for (WebSocketSession s : usuarios) {
                    if (s.isOpen() && !s.getId().equals(idEmisor)) {
                        s.sendMessage(message);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error procesando el mensaje JSON: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long idViaje = (Long) session.getAttributes().get("idViaje");

        if (idViaje != null && viajes.containsKey(idViaje)) {
            CopyOnWriteArrayList<WebSocketSession> usuarios = viajes.get(idViaje);
            usuarios.remove(session);

            if (usuarios.isEmpty()) {
                viajes.remove(idViaje);
                ultimasUbicaciones.remove(idViaje);
                System.out.println("🗑️ Viaje " + idViaje + " completamente vacío. Memoria RAM liberada.");
            }
        }
        System.out.println("🔴 Conexión cerrada para el usuario: " + session.getId());
    }
}