package ni.edu.uam.UAM_LIFT.controller;

import ni.edu.uam.UAM_LIFT.models.Viaje;
import ni.edu.uam.UAM_LIFT.services.ViajeServicio;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {

    private final ViajeServicio viajeServicio;

    public ViajeController(ViajeServicio viajeServicio) {
        this.viajeServicio = viajeServicio;
    }

    /**
     * Obtener todos los viajes
     */
    @GetMapping
    public List<Viaje> obtenerTodosLosViajes() {
        return viajeServicio.obtenerTodosLosViajes();
    }

    /**
     * Crear un nuevo viaje
     */
    @PostMapping("/{conductorCif}")
    public Viaje crearViaje(
            @PathVariable String conductorCif,
            @RequestBody Viaje viaje
    ) {
        return viajeServicio.crearViaje(viaje, conductorCif);
    }

    /**
     * Agregar un pasajero a un viaje
     */
    @PutMapping("/{viajeId}/pasajeros/{usuarioCif}")
    public boolean agregarPasajero(
            @PathVariable Long viajeId,
            @PathVariable String usuarioCif
    ) {
        try {
            viajeServicio.agregarPasajero(viajeId, usuarioCif);
            return true;
        } catch (Exception e) {
            System.out.println("Error al agregar pasajero: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancelar participación de un pasajero
     */
    @PutMapping("/{viajeId}/cancelar/{usuarioCif}")
    public boolean cancelarParticipacion(
            @PathVariable Long viajeId,
            @PathVariable String usuarioCif
    ) {
        try {
            viajeServicio.cancelarParticipacion(viajeId, usuarioCif);
            return true;
        } catch (Exception e) {
            System.out.println("Error al cancelar participación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Finalizar un viaje
     */
    @PutMapping("/{viajeId}/finalizar")
    public boolean finalizarViaje(@PathVariable Long viajeId) {
        try {
            viajeServicio.finalizarViaje(viajeId);
            return true;
        } catch (Exception e) {
            System.out.println("Error al finalizar viaje: " + e.getMessage());
            return false;
        }
    }
}