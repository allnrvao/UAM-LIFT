package ni.edu.uam.UAM_LIFT.validators;

import ni.edu.uam.UAM_LIFT.enums.EstadoViajeUsuario;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.models.Viaje;
import ni.edu.uam.UAM_LIFT.repositories.RepoUsuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoViaje;
import ni.edu.uam.UAM_LIFT.repositories.RepoViajeUsuario;
import ni.edu.uam.UAM_LIFT.repositories.ValidacionViaje;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ValidacionViajeImp implements ValidacionViaje {
    private final RepoViaje repoViaje;
    private final RepoViajeUsuario repoViajeUsuario;
    private final RepoUsuario repoUsuario;

    public ValidacionViajeImp(RepoViaje repoViaje,
                               RepoViajeUsuario repoViajeUsuario,
                               RepoUsuario repoUsuario) {
        this.repoViaje = repoViaje;
        this.repoViajeUsuario = repoViajeUsuario;
        this.repoUsuario = repoUsuario;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validarAsientoDisponible(Long viajeId) {
        Viaje viaje = repoViaje.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));
        long aceptados = repoViajeUsuario.countByViajeIdAndEstado(viajeId, EstadoViajeUsuario.ACEPTADO);
        return aceptados < viaje.getNumeroAsientosDisponibles();
    }

    @Override
    @Transactional(readOnly = true)
    public void ValidarUsuarioNoParticipante(Long viajeId, String usuarioCif) {
        Usuario usuario = repoUsuario.findByCif(usuarioCif)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con CIF: " + usuarioCif));
        boolean yaParticipa = repoViajeUsuario.existsByViajeIdAndUsuarioId(viajeId, usuario.getId());
        if (yaParticipa) {
            throw new RuntimeException("El usuario ya está inscrito en el viaje");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void ValidarConductorNoEsPasajero(Long viajeId, String usuarioCif) {
        Viaje viaje = repoViaje.findById(viajeId)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado con ID: " + viajeId));
        if (viaje.getConductor() != null && viaje.getConductor().getCif() != null
                && viaje.getConductor().getCif().equalsIgnoreCase(usuarioCif)) {
            throw new RuntimeException("El conductor no puede ser agregado como pasajero en su propio viaje");
        }
    }
}
