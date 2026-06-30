package ni.edu.uam.UAM_LIFT.controller;

import ni.edu.uam.UAM_LIFT.models.Notificacion;
import ni.edu.uam.UAM_LIFT.services.NotificacionServicio;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionServicio notificacionServicio;

    public NotificacionController(NotificacionServicio notificacionServicio) {
        this.notificacionServicio = notificacionServicio;
    }

    /**
     * Obtiene las notificaciones de un usuario ordenadas de la más reciente
     * a la más antigua.
     */
    @GetMapping("/usuario/{usuarioId}")
    public List<Notificacion> obtenerPorUsuario(@PathVariable Long usuarioId) {
        try {
            return notificacionServicio.obtenerPorUsuario(usuarioId);
        } catch (Exception e) {
            System.out.println("Error al obtener notificaciones: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Cuenta cuántas notificaciones sin leer tiene un usuario (usado para
     * mostrar el círculo rojo sobre el botón de notificaciones).
     */
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public long contarNoLeidas(@PathVariable Long usuarioId) {
        try {
            return notificacionServicio.contarNoLeidas(usuarioId);
        } catch (Exception e) {
            System.out.println("Error al contar notificaciones no leídas: " + e.getMessage());
            return 0L;
        }
    }

    @PutMapping("/{id}/leer")
    public boolean marcarComoLeida(@PathVariable Long id) {
        try {
            return notificacionServicio.marcarComoLeida(id);
        } catch (Exception e) {
            System.out.println("Error al marcar notificación como leída: " + e.getMessage());
            return false;
        }
    }

    @PutMapping("/usuario/{usuarioId}/leer-todas")
    public boolean marcarTodasComoLeidas(@PathVariable Long usuarioId) {
        try {
            notificacionServicio.marcarTodasComoLeidas(usuarioId);
            return true;
        } catch (Exception e) {
            System.out.println("Error al marcar notificaciones como leídas: " + e.getMessage());
            return false;
        }
    }
}
