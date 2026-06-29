package ni.edu.uam.UAM_LIFT.services;

import ni.edu.uam.UAM_LIFT.enums.EstadoViaje;
import ni.edu.uam.UAM_LIFT.enums.EstadoViajeUsuario;
import ni.edu.uam.UAM_LIFT.models.Destino;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.models.Viaje;
import ni.edu.uam.UAM_LIFT.models.ViajeUsuario;
import ni.edu.uam.UAM_LIFT.repositories.*;
import ni.edu.uam.UAM_LIFT.repositories.ValidacionViaje;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ViajeServicio implements InterfazViaje {
    private final RepoViaje repoViaje;
    private final RepoUsuario repoUsuario;
    private final RepoViajeUsuario repoViajeUsuario;
    private final ValidacionViaje validacionViaje;
    private final RepoDestino repoDestino;

    public ViajeServicio(RepoViaje repoViaje, RepoUsuario repoUsuario, RepoViajeUsuario repoViajeUsuario, ValidacionViaje validacionViaje, RepoDestino repoDestino) {
        this.repoViaje = repoViaje;
        this.repoUsuario = repoUsuario;
        this.repoViajeUsuario = repoViajeUsuario;
        this.validacionViaje = validacionViaje;
        this.repoDestino = repoDestino;
    }

    @Override
    public List<Viaje> obtenerTodosLosViajes() {
        return repoViaje.findAll();
    }

    @Override
    public Viaje crearViaje(Viaje viaje, String conductorCif) {
        Usuario conductor = repoUsuario.findByCif(conductorCif)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado con CIF: " + conductorCif));

        viaje.setConductor(conductor);
        viaje.setEstadoViaje(EstadoViaje.PROPUESTO);

        // ORIGEN
        Destino origen = repoDestino.findByNombre(viaje.getOrigen().getNombre())
                .orElseGet(() -> repoDestino.save(viaje.getOrigen()));

        // DESTINO
        Destino destino = repoDestino.findByNombre(viaje.getDestino().getNombre())
                .orElseGet(() -> repoDestino.save(viaje.getDestino()));

        viaje.setOrigen(origen);
        viaje.setDestino(destino);

        return repoViaje.save(viaje);
    }
    public boolean iniciarViajeReal(Long viajeId, Long conductorId) {
        Optional<Viaje> viajeOpt = repoViaje.findById(viajeId);

        if (viajeOpt.isPresent()) {
            Viaje viaje = viajeOpt.get();


            if (viaje.getConductor().getId()== conductorId && viaje.getEstadoViaje() == EstadoViaje.PROPUESTO) {
                viaje.setEstadoViaje(EstadoViaje.EN_CURSO);
                repoViaje.save(viaje);
                return true;
            }
        }
        return false;
    }
    @Override
    public void agregarPasajero(Long viajeId, String usuarioCif) {
        Viaje viaje = repoViaje.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));

        if (viaje.getEstadoViaje() != EstadoViaje.PROPUESTO) {
            throw new RuntimeException("No se pueden agregar pasajeros a un viaje que no está en estado PROGRAMADO");
        }

        if (!validacionViaje.validarAsientoDisponible(viajeId)) {
            throw new RuntimeException("No hay asientos disponibles en el viaje");
        }

        validacionViaje.ValidarUsuarioNoParticipante(viajeId, usuarioCif);
        validacionViaje.ValidarConductorNoEsPasajero(viajeId, usuarioCif);

        Usuario pasajero = repoUsuario.findByCif(usuarioCif)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con CIF: " + usuarioCif));

        ViajeUsuario viajeUsuario = new ViajeUsuario();
        viajeUsuario.setViaje(viaje);
        viajeUsuario.setUsuario(pasajero);
        viajeUsuario.setEstado(EstadoViajeUsuario.ACEPTADO);
        repoViajeUsuario.save(viajeUsuario);
    }

    @Override
    public void cancelarParticipacion(Long viajeId, String usuarioCif) {
        ViajeUsuario viajeUsuario = repoViajeUsuario.findByViajeIdAndUsuarioCif(viajeId, usuarioCif)
                .orElseThrow(() -> new RuntimeException("Participación no encontrada para viaje ID: " + viajeId + " y usuario CIF: " + usuarioCif));
        viajeUsuario.setEstado(EstadoViajeUsuario.CANCELADO);
        repoViajeUsuario.save(viajeUsuario);
    }

    @Override
    public void finalizarViaje(Long viajeId) {
        Viaje viaje = repoViaje.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));
        viaje.setEstadoViaje(EstadoViaje.FINALIZADO);
        repoViaje.save(viaje);
    }

    @Override
    public void cancelarViaje(Long viajeId) {
        Viaje viaje = repoViaje.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));
        viaje.setEstadoViaje(EstadoViaje.CANCELADO);
        repoViaje.save(viaje);
    }

    public boolean LimitesDeViaje(Long usuarioId) {
        List<ViajeUsuario> viajesUsuario = repoViajeUsuario.findByUsuarioIdAndEstado(usuarioId, EstadoViajeUsuario.ACEPTADO);
        viajesUsuario.removeIf(viajeUsuario -> viajeUsuario.getViaje().getEstadoViaje()== EstadoViaje.CANCELADO || viajeUsuario.getViaje().getEstadoViaje() == EstadoViaje.FINALIZADO);
        return viajesUsuario .size() <=2;
    }

    public boolean LimiteDeViajeConductor(Long usuarioId) {
        Usuario usuario = repoUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        List<Viaje> viajesConductor = repoViaje.findByConductorAndEstadoViaje(usuario, EstadoViaje.PROPUESTO);
        return viajesConductor.size() <= 2;
    }

    public List<Viaje> viajesPorUsuario(Long usuarioId) {
        List<ViajeUsuario> viajesUsuario = repoViajeUsuario.findByUsuarioIdAndEstado(usuarioId, EstadoViajeUsuario.ACEPTADO);
        List<Viaje> viajes = new ArrayList<>();
        for (ViajeUsuario vu : viajesUsuario) {
            if (vu.getEstado() != EstadoViajeUsuario.CANCELADO) {
                viajes.add(vu.getViaje());
            }
        }
        return viajes;
    }

    public List<Viaje> viajesPorConductor(Long usuarioId) {
        Usuario usuario = repoUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        return repoViaje.findByConductor(usuario);
    }

    // Espera formato: "2026-06-20T15:30:00"
    public boolean validarPorFecha(String fechaSalida, String fechaLlegada, Long usuarioId) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime nuevaSalida = LocalDateTime.parse(fechaSalida, formatter);
        LocalDateTime nuevaLlegada = LocalDateTime.parse(fechaLlegada, formatter);


        if (nuevaSalida.isAfter(nuevaLlegada) || nuevaSalida.isEqual(nuevaLlegada)) {
            return false;
        }

        List<Viaje> viajes = viajesPorUsuario(usuarioId);
        viajes.removeIf(viaje -> viaje.getEstadoViaje() == EstadoViaje.CANCELADO || viaje.getEstadoViaje() == EstadoViaje.FINALIZADO);

        for (Viaje viaje : viajes) {
            LocalDateTime existenteSalida = viaje.getFechaHoraSalida();
            LocalDateTime existenteLlegada = viaje.getFechaHoraLlegada();

             if (nuevaSalida.isBefore(existenteLlegada) && nuevaLlegada.isAfter(existenteSalida)) {
                return false;
            }
        }

        return true;
    }

    public boolean validarPorFechaConductor(String fechaSalida, String fechaLlegada, Long usuarioId) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime nuevaSalida = LocalDateTime.parse(fechaSalida, formatter);
        LocalDateTime nuevaLlegada = LocalDateTime.parse(fechaLlegada, formatter);


        if (nuevaSalida.isAfter(nuevaLlegada) || nuevaSalida.isEqual(nuevaLlegada)) {
            return false;
        }

        List<Viaje> viajes = viajesPorConductor(usuarioId);
        viajes.removeIf(viaje -> viaje.getEstadoViaje() == EstadoViaje.CANCELADO || viaje.getEstadoViaje() == EstadoViaje.FINALIZADO);
        for (Viaje viaje : viajes) {
            LocalDateTime existenteSalida = viaje.getFechaHoraSalida();
            LocalDateTime existenteLlegada = viaje.getFechaHoraLlegada();

             if (nuevaSalida.isBefore(existenteLlegada) && nuevaLlegada.isAfter(existenteSalida)) {
                return false;
            }
        }

        return true;
    }

    public boolean usuarioEsConductor(Long usuarioId) {
        Usuario usuario = repoUsuario.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        List<Viaje> viajesConductor = repoViaje.findByConductorAndEstadoViaje(usuario, EstadoViaje.PROPUESTO);
        return !viajesConductor.isEmpty();
    }

    public List<Usuario> obtenerPasajerosPorViaje(Long viajeId) {
        List<ViajeUsuario> viajeUsuarios = repoViajeUsuario.findByViajeIdAndEstado(viajeId, EstadoViajeUsuario.ACEPTADO);
        List<Usuario> pasajeros = new ArrayList<>();
        for (ViajeUsuario vu : viajeUsuarios) {
            if (vu.getEstado() == EstadoViajeUsuario.ACEPTADO) {
                pasajeros.add(vu.getUsuario());
            }
        }
        return pasajeros;
    }

    public boolean quitarPasajero(Long viajeId, String usuarioCif) {
        Optional<ViajeUsuario> viajeUsuarioOpt = repoViajeUsuario.findByViajeIdAndUsuarioCif(viajeId, usuarioCif);
        if (viajeUsuarioOpt.isPresent()) {
            ViajeUsuario viajeUsuario = viajeUsuarioOpt.get();
            if (viajeUsuario.getEstado() == EstadoViajeUsuario.ACEPTADO) {
                viajeUsuario.setEstado(EstadoViajeUsuario.CANCELADO);
                repoViajeUsuario.save(viajeUsuario);
                return true;
            }
        }
        return false;
    }

}