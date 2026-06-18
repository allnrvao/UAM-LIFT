package ni.edu.uam.UAM_LIFT.controller;


import ni.edu.uam.UAM_LIFT.models.Viaje;
import ni.edu.uam.UAM_LIFT.services.ViajeServicio;
import org.springframework.web.bind.annotation.*;

<<<<<<< Updated upstream
import java.util.List;

=======

import java.util.List;


>>>>>>> Stashed changes
@RestController
@RequestMapping("/api/viajes")
public class ViajeController {

<<<<<<< Updated upstream
    private final ViajeServicio viajeServicio;

=======

    private final ViajeServicio viajeServicio;


>>>>>>> Stashed changes
    public ViajeController(ViajeServicio viajeServicio) {
        this.viajeServicio = viajeServicio;
    }

<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
    /**
     * Obtener todos los viajes
     */
    @GetMapping
    public List<Viaje> obtenerTodosLosViajes() {
        return viajeServicio.obtenerTodosLosViajes();
    }

<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
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