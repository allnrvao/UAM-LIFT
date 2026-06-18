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

<<<<<<< Updated upstream
import java.util.List;
=======

import java.util.List;

>>>>>>> Stashed changes

@Service
public class ViajeServicio implements InterfazViaje {
    private final RepoViaje repoViaje;
    private final RepoUsuario repoUsuario;
    private final RepoViajeUsuario repoViajeUsuario;
    private final ValidacionViaje validacionViaje;
    private final RepoDestino repoDestino;


<<<<<<< Updated upstream
=======


>>>>>>> Stashed changes
    @Override
    public List<Viaje> obtenerTodosLosViajes() {
        return repoViaje.findAll();
    }

<<<<<<< Updated upstream
    public ViajeServicio(RepoViaje repoViaje, RepoUsuario repoUsuario, RepoViajeUsuario repoViajeUsuario, ValidacionViaje validacionViaje) {
=======

    public ViajeServicio(RepoViaje repoViaje, RepoUsuario repoUsuario, RepoViajeUsuario repoViajeUsuario, ValidacionViaje validacionViaje, RepoDestino repoDestino) {
>>>>>>> Stashed changes
        this.repoViaje = repoViaje;
        this.repoUsuario = repoUsuario;
        this.repoViajeUsuario = repoViajeUsuario;
        this.validacionViaje = validacionViaje;
        this.repoDestino = repoDestino;
    }


    @Override
    public Viaje crearViaje(Viaje viaje, String conductorCif) {

        Usuario conductor = repoUsuario.findByCif(conductorCif)
                .orElseThrow(() ->
                        new RuntimeException("Conductor no encontrado con CIF: " + conductorCif));

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

<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
    @Override
    public void agregarPasajero(Long viajeId, String usuarioCif) {
        Viaje viaje = repoViaje.findById(viajeId).orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));

<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
        // 💡 CAMBIADO: Ahora permite agregar pasajeros si el viaje está PROGRAMADO
        if(viaje.getEstadoViaje() != EstadoViaje.PROPUESTO) {
            throw new RuntimeException("No se pueden agregar pasajeros a un viaje que no está en estado PROGRAMADO");
        }


        if (!validacionViaje.validarAsientoDisponible(viajeId)) {
            throw new RuntimeException("No hay asientos disponibles en el viaje");
        }


        validacionViaje.ValidarUsuarioNoParticipante(viajeId, usuarioCif);


        validacionViaje.ValidarConductorNoEsPasajero(viajeId, usuarioCif);


        Usuario pasajero = repoUsuario.findByCif(usuarioCif).orElseThrow(() -> new RuntimeException("Usuario no encontrado con CIF: " + usuarioCif));
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
        Viaje viaje = repoViaje.findById(viajeId).orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));
        viaje.setEstadoViaje(EstadoViaje.FINALIZADO);
        repoViaje.save(viaje);
    }
    @Override
    public void cancelarViaje(Long viajeId) {
        Viaje viaje = repoViaje.findById(viajeId).orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));
        viaje.setEstadoViaje(EstadoViaje.CANCELADO);
        repoViaje.save(viaje);
    }
}
