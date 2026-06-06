package ni.edu.uam.UAM_LIFT.repositories;

public interface ValidacionViaje {
    boolean validarAsientoDisponible(Long viajeId);
    void ValidarUsuarioNoParticipante(Long viajeId, String usuarioCif);
    void ValidarConductorNoEsPasajero(Long viajeId, String usuarioCif);
}
