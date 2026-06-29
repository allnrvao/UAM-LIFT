package ni.edu.uam.UAM_LIFT.schedulers;

import lombok.RequiredArgsConstructor;
import ni.edu.uam.UAM_LIFT.repositories.RepoViaje;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ViajeScheduler {

    private final RepoViaje repoViaje;

    @Scheduled(cron = "0 * * * * *") // Cada minuto, en el segundo 0
    public void cancelarViajesVencidos() {

        int cancelados = repoViaje.cancelarViajesVencidos(LocalDateTime.now());

        if (cancelados > 0) {
            System.out.println("Viajes cancelados automáticamente: " + cancelados);
        }
    }
}
