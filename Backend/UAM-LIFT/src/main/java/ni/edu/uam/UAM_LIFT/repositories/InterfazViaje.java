package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.Viaje;

public interface InterfazViaje {
    Viaje crearViaje(Viaje viaje, String conductorCif);

    void agregarPasajero(Long viajeId, Long usuarioCif);

    void cancelarParticipacion(Long viajeId, Long usuarioCif);

    void finalizarViaje(Long viajeId);
}
