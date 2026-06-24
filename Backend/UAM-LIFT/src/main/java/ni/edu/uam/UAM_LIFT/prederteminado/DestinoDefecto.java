package ni.edu.uam.UAM_LIFT.prederteminado;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ni.edu.uam.UAM_LIFT.models.Destino;
import ni.edu.uam.UAM_LIFT.repositories.RepoDestino;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DestinoDefecto {
    private final RepoDestino repoDestino;
    @PostConstruct
    public void crearDestinoDefecto() {
        if (!repoDestino.existsByNombre("UAM")) {
            Destino destino = new Destino();
            destino.setNombre("UAM");
            destino.setLatitud(12.108502168418019);
            destino.setLongitud(-86.2569602969308);
            repoDestino.save(destino);
        }
    }
}