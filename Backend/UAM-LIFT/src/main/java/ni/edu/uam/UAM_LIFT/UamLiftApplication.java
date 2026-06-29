package ni.edu.uam.UAM_LIFT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UamLiftApplication {

	public static void main(String[] args) {
		SpringApplication.run(UamLiftApplication.class, args);
	}

}
