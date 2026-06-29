package ni.edu.uamv.WebSocker.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ni.edu.uamv.WebSocker.models.Ubicacion;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ViajeHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    // Viaje -> sesiones conectadas
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> viajes = new ConcurrentHashMap<>();

    // Viaje -> última ubicación
    private final ConcurrentHashMap<Long, String> ultimasUbicaciones = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Nueva conexión: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {

        try {

            Ubicacion ubicacion = mapper.readValue(message.getPayload(), Ubicacion.class);

            if (!mensajeValido(ubicacion))
                return;

            registrarSesion(session, ubicacion.getIdViaje());

            switch (ubicacion.getTipo().toUpperCase()) {

                case "SUSCRIBIR":
                    procesarSuscripcion(session, ubicacion.getIdViaje());
                    break;

                case "ACTUALIZAR":
                    procesarActualizacion(session, ubicacion.getIdViaje(), message);
                    break;

                default:
                    log.warn("Tipo desconocido: {}", ubicacion.getTipo());

            }

        } catch (Exception e) {
            log.error("Error procesando mensaje", e);
        }

    }

    private boolean mensajeValido(Ubicacion u) {

        return u.getIdViaje() != null
                && u.getIdViaje() > 0
                && u.getTipo() != null;

    }

    private void registrarSesion(WebSocketSession session, Long idViaje) {

        session.getAttributes().put("idViaje", idViaje);

        viajes.computeIfAbsent(idViaje,
                k -> ConcurrentHashMap.newKeySet()).add(session);

    }

    private void procesarSuscripcion(WebSocketSession session, Long idViaje) throws Exception {

        log.info("Usuario {} suscrito al viaje {}", session.getId(), idViaje);

        enviarUltimaUbicacion(session, idViaje);

    }

    private void procesarActualizacion(WebSocketSession session,
                                       Long idViaje,
                                       TextMessage message) throws Exception {

        ultimasUbicaciones.put(idViaje, message.getPayload());

        reenviarUbicacion(session, idViaje, message);

    }

    private void enviarUltimaUbicacion(WebSocketSession session,
                                       Long idViaje) throws Exception {

        String ultima = ultimasUbicaciones.get(idViaje);

        if (ultima != null) {
            log.info("Enviando última ubicación a {}: {}", session.getId(), ultima);
            log.info("Latitud: {}, Longitud: {}", mapper.readTree(ultima).get("latitud"), mapper.readTree(ultima).get("longitud"));
            session.sendMessage(new TextMessage(ultima));
        }

    }

    private void reenviarUbicacion(WebSocketSession emisor,
                                   Long idViaje,
                                   TextMessage message) throws Exception {

        Set<WebSocketSession> sesiones = viajes.get(idViaje);

        if (sesiones == null)
            return;

        sesiones.removeIf(s -> !s.isOpen());

        for (WebSocketSession s : sesiones) {

            if (!s.getId().equals(emisor.getId())) {
                s.sendMessage(message);
            }

        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status) {

        Long idViaje = (Long) session.getAttributes().get("idViaje");

        if (idViaje == null)
            return;

        Set<WebSocketSession> sesiones = viajes.get(idViaje);

        if (sesiones == null)
            return;

        sesiones.remove(session);

        if (sesiones.isEmpty()) {

            viajes.remove(idViaje);
            ultimasUbicaciones.remove(idViaje);

            log.info("Viaje {} eliminado de memoria", idViaje);

        }

        log.info("Conexión cerrada {}", session.getId());

    }

}