package ni.edu.uam.UAM_LIFT.services;

import ni.edu.uam.UAM_LIFT.models.Destino;
import ni.edu.uam.UAM_LIFT.repositories.RepoDestino;
import org.springframework.stereotype.Service;

@Service
public class DestinoServicio {
    private final RepoDestino repoDestino;

    public DestinoServicio(RepoDestino repoDestino) {
        this.repoDestino = repoDestino;
    }

    public boolean existeDestino(String nombre) {
        return repoDestino.existsByNombre(nombre);
    }

    public boolean agregarDestino(String nombre) {
        if (existeDestino(nombre)) {
            return false; // El destino ya existe
        }
        try {
            Destino destino = new Destino();
            destino.setNombre(nombre);
            destino.setUniversidad(false);
            repoDestino.save(destino);
            return true;
        } catch (Exception e) {
            System.out.println("Error al agregar destino: " + e.getMessage());
            return false;
        }
    }

        public boolean eliminarDestino(String nombre) {
            try {
                Destino destino = repoDestino.findByNombre(nombre).orElseThrow(() -> new RuntimeException("Destino no encontrado con nombre: " + nombre));
                repoDestino.delete(destino);
                return true;
            } catch (Exception e) {
                System.out.println("Error al eliminar destino: " + e.getMessage());
                return false;
            }
        }
}
