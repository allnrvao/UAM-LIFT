package ni.edu.uam.UAM_LIFT.controller;

import ni.edu.uam.UAM_LIFT.models.Destino;
import ni.edu.uam.UAM_LIFT.services.DestinoServicio;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/destinos")
public class DestinoController {

    private final DestinoServicio destinoServicio;

    public DestinoController(DestinoServicio destinoServicio) {
        this.destinoServicio = destinoServicio;
    }

    @PostMapping("/{nombre}")
    public boolean agregarDestinosPredeterminados(@PathVariable String nombre) {
        return destinoServicio.agregarDestino(nombre);
    }

    @DeleteMapping("/{nombre}")
    public boolean eliminarDestino(@PathVariable String nombre) {
        return destinoServicio.eliminarDestino(nombre);
    }

    @GetMapping("/defecto")
    public Destino obtenerDestinoDefecto() {
        return destinoServicio.darDestinoDefecto();
    }
}