package ni.edu.uam.UAM_LIFT.services;

import ni.edu.uam.UAM_LIFT.models.Carro;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import ni.edu.uam.UAM_LIFT.repositories.RepoCarro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarroService {

    @Autowired
    private RepoCarro repoCarro;

    public Carro guardarCarro(Carro carro) {
        return repoCarro.save(carro);
    }

    public List<Carro> obtenerTodosLosCarros() {
        return repoCarro.findAll();
    }

    public Optional<Carro> obtenerCarroPorId(Long id) {
        return repoCarro.findById(id);
    }

    public Carro actualizarCarro(Carro carro) {
        // Asegúrate de que el carro exista antes de actualizar
        if (repoCarro.existsById(carro.getId())) {
            return repoCarro.save(carro);
        }
        return null; // O lanzar una excepción
    }

    public void eliminarCarro(Long id) {
        repoCarro.deleteById(id);
    }

    public List<Carro> obtenerCarrosPorPropietario(Usuario propietario) {
        return repoCarro.findByPropietario(propietario);
    }

}