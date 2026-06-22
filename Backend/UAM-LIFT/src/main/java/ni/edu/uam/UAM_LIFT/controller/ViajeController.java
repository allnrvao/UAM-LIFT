package ni.edu.uam.UAM_LIFT.controller;

import ni.edu.uam.UAM_LIFT.models.Usuario;
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

    /**
     * Cancelar un viaje
     */
    @PutMapping("/{viajeId}/cancelar")
    public boolean cancelarViaje(@PathVariable Long viajeId) {
        try {
            viajeServicio.cancelarViaje(viajeId);
            return true;
        } catch (Exception e) {
            System.out.println("Error al cancelar viaje: " + e.getMessage());
            return false;
        }
    }

    //Validar Viaje crecion Conductor, limite y fecha
        @GetMapping("/validar/{usuarioId}/{fechaSalida}/{fechaLlegada}")
        public boolean validarFechas(
                @PathVariable Long usuarioId,
                @PathVariable String fechaSalida,
                @PathVariable String fechaLlegada
        ) {
            try {
                return viajeServicio.validarPorFecha(fechaSalida, fechaLlegada, usuarioId) &&
                        viajeServicio.validarPorFechaConductor(fechaSalida,fechaLlegada,usuarioId);
            } catch (Exception e) {
                System.out.println("Error al validar viaje: " + e.getMessage());
                return false;
            }
        }

        @GetMapping("/validar/numviajes/{usuarioId}")
        public boolean validarNumViajes(@PathVariable Long usuarioId) {
            try {
                return viajeServicio.LimitesDeViaje(usuarioId) && viajeServicio.LimiteDeViajeConductor(usuarioId);
            } catch (Exception e) {
                System.out.println("Error al validar número de viajes: " + e.getMessage());
                return false;
            }
        }

        @GetMapping("/usuario/{usuarioId}")
        public List<Viaje> obtenerViajesPorUsuario(@PathVariable Long usuarioId) {
            try {
                return viajeServicio.viajesPorUsuario(usuarioId);
            } catch (Exception e) {
                System.out.println("Error al obtener viajes por usuario: " + e.getMessage());
                return null;
            }
    }

    @GetMapping("/conductor/{usuarioId}")
    public List<Viaje> obtenerViajesPorConductor(@PathVariable Long usuarioId) {
        try {
            return viajeServicio.viajesPorConductor(usuarioId);
        } catch (Exception e) {
            System.out.println("Error al obtener viajes por conductor: " + e.getMessage());
            return null;
        }
    }

    @GetMapping("/noconductor/{usuarioId}")
    public boolean usuarioEsConductor(@PathVariable Long usuarioId) {
        try {
            return viajeServicio.usuarioEsConductor(usuarioId);
        } catch (Exception e) {
            System.out.println("Error al verificar si el usuario es conductor: " + e.getMessage());
            return false;
        }


    }
    @GetMapping("/{viajeId}/pasajeros")
    public List<Usuario> obtenerPasajerosPorViaje(@PathVariable Long viajeId) {
        try {
            return viajeServicio.obtenerPasajerosPorViaje(viajeId);
        } catch (Exception e) {
            System.out.println("Error al obtener pasajeros por viaje: " + e.getMessage());
            return null;
        }
    }
}