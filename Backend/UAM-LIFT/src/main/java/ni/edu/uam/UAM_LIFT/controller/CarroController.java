package ni.edu.uam.UAM_LIFT.controller;

import ni.edu.uam.UAM_LIFT.models.Carro;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoUsuario;
import ni.edu.uam.UAM_LIFT.services.CarroService; // Importar CarroService
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carros")
public class CarroController {

    private final CarroService carroService; // Usar CarroService
    private final RepoUsuario repoUsuario; // Necesario para asociar el carro a un usuario

    public CarroController(CarroService carroService, RepoUsuario repoUsuario) { // Inyectar CarroService
        this.carroService = carroService;
        this.repoUsuario = repoUsuario;
    }

    @GetMapping
    public List<Carro> getAllCarros() {
        return carroService.obtenerTodosLosCarros(); // Usar CarroService
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carro> getCarroById(@PathVariable Long id) {
        Optional<Carro> carro = carroService.obtenerCarroPorId(id); // Usar CarroService
        return carro.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Carro> createCarro(@RequestBody Carro carro) {
        // Asegurarse de que el propietario (Usuario) exista antes de guardar el carro
        if (carro.getPropietario() != null && carro.getPropietario().getId() != null) {
            Optional<Usuario> propietarioOptional = repoUsuario.findById(carro.getPropietario().getId());
            if (propietarioOptional.isPresent()) {
                carro.setPropietario(propietarioOptional.get());
                Carro savedCarro = carroService.guardarCarro(carro); // Usar CarroService
                return ResponseEntity.status(HttpStatus.CREATED).body(savedCarro);
            } else {
                return ResponseEntity.badRequest().build(); // Propietario no encontrado
            }
        } else {
            return ResponseEntity.badRequest().build(); // Datos de propietario incompletos
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carro> updateCarro(@PathVariable Long id, @RequestBody Carro carroDetails) {
        Optional<Carro> carroOptional = carroService.obtenerCarroPorId(id); // Usar CarroService
        if (carroOptional.isPresent()) {
            Carro carro = carroOptional.get();
            carro.setPlaca(carroDetails.getPlaca());
            carro.setMarca(carroDetails.getMarca());
            carro.setModelo(carroDetails.getModelo());
            carro.setColor(carroDetails.getColor());

            // Si se intenta cambiar el propietario, verificar que el nuevo propietario exista
            if (carroDetails.getPropietario() != null && carroDetails.getPropietario().getId() != null) {
                Optional<Usuario> nuevoPropietarioOptional = repoUsuario.findById(carroDetails.getPropietario().getId());
                if (nuevoPropietarioOptional.isPresent()) {
                    carro.setPropietario(nuevoPropietarioOptional.get());
                } else {
                    return ResponseEntity.badRequest().build(); // Nuevo propietario no encontrado
                }
            }

            Carro updatedCarro = carroService.actualizarCarro(carro); // Usar CarroService
            return ResponseEntity.ok(updatedCarro);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarro(@PathVariable Long id) {
        if (carroService.obtenerCarroPorId(id).isPresent()) { // Usar CarroService para verificar existencia
            carroService.eliminarCarro(id); // Usar CarroService
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}