package ni.edu.uam.UAM_LIFT.services;

import ni.edu.uam.UAM_LIFT.enums.TipoNotificacion;
import ni.edu.uam.UAM_LIFT.models.Notificacion;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.models.Viaje;
import ni.edu.uam.UAM_LIFT.repositories.RepoNotificacion;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class NotificacionServicio {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    private final RepoNotificacion repoNotificacion;

    public NotificacionServicio(RepoNotificacion repoNotificacion) {
        this.repoNotificacion = repoNotificacion;
    }

    public Notificacion crearNotificacion(Usuario usuario, Long viajeId, TipoNotificacion tipo, String titulo, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setViajeId(viajeId);
        notificacion.setTipo(tipo);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setLeida(false);
        return repoNotificacion.save(notificacion);
    }

    /**
     * Notifica a todos los destinatarios (pasajeros aceptados) que el viaje
     * fue cancelado, incluyendo el motivo proporcionado por el conductor.
     */
    public void notificarCancelacionViaje(Viaje viaje, String motivo, List<Usuario> destinatarios) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            return;
        }
        String ruta = construirRuta(viaje);
        String motivoFinal = (motivo == null || motivo.isBlank()) ? "No se especificó un motivo." : motivo.trim();
        String mensaje = "El viaje " + ruta + " fue cancelado por el conductor. Motivo: " + motivoFinal;

        for (Usuario destinatario : destinatarios) {
            crearNotificacion(destinatario, viaje.getId(), TipoNotificacion.CANCELACION_VIAJE, "Viaje cancelado", mensaje);
        }
    }

    /**
     * Notifica a todos los destinatarios (pasajeros aceptados) que el viaje
     * ha iniciado, incluyendo un saludo, datos del conductor, la ruta y la fecha.
     */
    public void notificarInicioViaje(Viaje viaje, List<Usuario> destinatarios) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            return;
        }
        String ruta = construirRuta(viaje);
        String fecha = (viaje.getFechaHoraSalida() != null) ? viaje.getFechaHoraSalida().format(FORMATO_FECHA) : "fecha no disponible";
        String nombreConductor = construirNombreConductor(viaje);

        String mensaje = "¡Hola! " + nombreConductor + " ha iniciado el viaje " + ruta +
                ", programado para el " + fecha + ". ¡Buen viaje!";

        for (Usuario destinatario : destinatarios) {
            crearNotificacion(destinatario, viaje.getId(), TipoNotificacion.INICIO_VIAJE, "Tu viaje ha iniciado", mensaje);
        }
    }

    /**
     * Notifica a todos los destinatarios (pasajeros aceptados) que el viaje
     * ha finalizado.
     */
    public void notificarFinalizacionViaje(Viaje viaje, List<Usuario> destinatarios) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            return;
        }
        String ruta = construirRuta(viaje);
        String mensaje = "El viaje " + ruta + " ha finalizado. ¡Gracias por usar UAM Lift!";

        for (Usuario destinatario : destinatarios) {
            crearNotificacion(destinatario, viaje.getId(), TipoNotificacion.FINALIZACION_VIAJE, "Viaje finalizado", mensaje);
        }
    }

    /**
     * Notifica al conductor que un pasajero se unió a su viaje.
     */
    public void notificarUsuarioUnido(Viaje viaje, Usuario pasajero) {
        Usuario conductor = viaje.getConductor();
        if (conductor == null || pasajero == null) {
            return;
        }
        String ruta = construirRuta(viaje);
        String fecha = (viaje.getFechaHoraSalida() != null) ? viaje.getFechaHoraSalida().format(FORMATO_FECHA) : "fecha no disponible";
        String nombrePasajero = ((pasajero.getNombre() != null ? pasajero.getNombre() : "") + " " +
                (pasajero.getApellido() != null ? pasajero.getApellido() : "")).trim();
        if (nombrePasajero.isEmpty()) nombrePasajero = "Un pasajero";
        String mensaje = nombrePasajero + " se unió a tu viaje " + ruta +
                ", programado para el " + fecha + ".";

        crearNotificacion(conductor, viaje.getId(), TipoNotificacion.USUARIO_UNIDO, "Nuevo pasajero", mensaje);
    }

    /**
     * Notifica al pasajero que fue eliminado (sacado) de un viaje por el conductor.
     */
    public void notificarUsuarioEliminado(Viaje viaje, Usuario pasajero) {
        if (pasajero == null) {
            return;
        }
        String ruta = construirRuta(viaje);
        String mensaje = "Fuiste eliminado del viaje " + ruta + " por el conductor.";

        crearNotificacion(pasajero, viaje.getId(), TipoNotificacion.USUARIO_ELIMINADO, "Fuiste removido del viaje", mensaje);
    }

    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return repoNotificacion.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    public long contarNoLeidas(Long usuarioId) {
        return repoNotificacion.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    public boolean marcarComoLeida(Long id) {
        return repoNotificacion.findById(id)
                .map(notificacion -> {
                    notificacion.setLeida(true);
                    repoNotificacion.save(notificacion);
                    return true;
                })
                .orElse(false);
    }

    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> noLeidas = repoNotificacion.findByUsuarioIdAndLeidaFalse(usuarioId);
        for (Notificacion notificacion : noLeidas) {
            notificacion.setLeida(true);
        }
        repoNotificacion.saveAll(noLeidas);
    }

    private String construirRuta(Viaje viaje) {
        String origen = (viaje.getOrigen() != null) ? viaje.getOrigen().getNombre() : "Origen";
        String destino = (viaje.getDestino() != null) ? viaje.getDestino().getNombre() : "Destino";
        return origen + " → " + destino;
    }

    private String construirNombreConductor(Viaje viaje) {
        Usuario conductor = viaje.getConductor();
        if (conductor == null) {
            return "Tu conductor";
        }
        String nombreCompleto = ((conductor.getNombre() != null ? conductor.getNombre() : "") + " " +
                (conductor.getApellido() != null ? conductor.getApellido() : "")).trim();
        return nombreCompleto.isEmpty() ? "Tu conductor" : nombreCompleto;
    }
}