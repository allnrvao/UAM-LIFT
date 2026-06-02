package ni.edu.uam.UAM_LIFT.services;

import ni.edu.uam.UAM_LIFT.enums.EstadoViaje;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.models.Viaje;
import ni.edu.uam.UAM_LIFT.repositories.InterfazViaje;
import ni.edu.uam.UAM_LIFT.repositories.RepoUsuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoViaje;
import ni.edu.uam.UAM_LIFT.repositories.RepoViajeUsuario;

public class ViajeServicio implements InterfazViaje {
    private final RepoViaje repoViaje;
    private final RepoUsuario repoUsuario;
    private final RepoViajeUsuario repoViajeUsuario;

    public ViajeServicio(RepoViaje repoViaje, RepoUsuario repoUsuario, RepoViajeUsuario repoViajeUsuario) {
        this.repoViaje = repoViaje;
        this.repoUsuario = repoUsuario;
        this.repoViajeUsuario = repoViajeUsuario;
    }

    @Override
    public Viaje crearViaje(Viaje viaje, String conductorCif) {
        Usuario conductor = repoUsuario.findByCif(conductorCif);
        if (conductor == null) {
            throw new RuntimeException("Conductor no encontrado con CIF: " + conductorCif);
        }
        viaje.setConductor(conductor);
        viaje.setEstadoViaje(EstadoViaje.PROPUESTO);
        return repoViaje.save(viaje);
    }

    @Override
    public void agregarPasajero(Long viajeId, String usuarioCif) {
        Viaje viaje = repoViaje.findById(viajeId).orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));
        Usuario pasajero = repoUsuario.findByCif(usuarioCif).orElseThrow(() -> new RuntimeException("Usuario no encontrado con CIF: " + usuarioCif));

    }
}
