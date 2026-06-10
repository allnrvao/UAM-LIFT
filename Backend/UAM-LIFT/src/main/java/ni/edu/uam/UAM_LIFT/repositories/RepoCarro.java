package ni.edu.uam.UAM_LIFT.repositories;

import ni.edu.uam.UAM_LIFT.models.Carro;
import ni.edu.uam.UAM_LIFT.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoCarro extends JpaRepository<Carro, Long> {
    public Carro findByPlaca(String placa);
    public List<Carro>  findByPropietario(Usuario propietario);
}