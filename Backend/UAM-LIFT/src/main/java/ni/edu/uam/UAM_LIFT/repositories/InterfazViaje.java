package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.Viaje;

import java.util.List;

public interface InterfazViaje {
    List<Viaje> obtenerTodosLosViajes();

    Viaje crearViaje(Viaje viaje, String conductorCif);

    void agregarPasajero(Long viajeId, String usuarioCif);

    void cancelarParticipacion(Long viajeId, String usuarioCif);

    void finalizarViaje(Long viajeId);

    void cancelarViaje(Long viajeId, String motivo);
}