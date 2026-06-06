package ni.edu.uam.UAM_LIFT.controller;

import ni.edu.uam.UAM_LIFT.models.Viaje;
import ni.edu.uam.UAM_LIFT.services.ViajeServicio;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {
    private final ViajeServicio viajeServicio;
    public ViajeController(ViajeServicio viajeServicio) {
        this.viajeServicio = viajeServicio;
    }

    @PostMapping("/{conductorCif}")
    public boolean crearViaje(@PathVariable String conductorCif,@RequestBody Viaje viaje) {
        try {
            viajeServicio.crearViaje(viaje, conductorCif);
            return true;
        } catch (Exception e) {
            System.out.println("Error al crear viaje: " + e.getMessage());
            return false;
        }

    }

    @PutMapping("/{viajeId}/pasajeros/{usuarioCif}")
    public boolean agregarPasajero(@PathVariable Long viajeId, @PathVariable String usuarioCif) {
        try {
            viajeServicio.agregarPasajero(viajeId, usuarioCif);
            return true;
        } catch (Exception e) {
            System.out.println("Error al agregar pasajero: " + e.getMessage());
            return false;
        }
    }

    @PutMapping("/{viajeId}/cancelar/{usuarioCif}")
    public boolean cancelarParticipacion(@PathVariable Long viajeId, @PathVariable String usuarioCif) {
        try {
            viajeServicio.cancelarParticipacion(viajeId, usuarioCif);
            return true;
        } catch (Exception e) {
            System.out.println("Error al cancelar participación: " + e.getMessage());
            return false;
        }
    }

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
