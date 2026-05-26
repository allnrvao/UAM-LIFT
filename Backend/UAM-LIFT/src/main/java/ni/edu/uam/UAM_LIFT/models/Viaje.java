package ni.edu.uam.UAM_LIFT.models;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "viajes")
public class Viaje {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String origen;
        @Column(nullable = false, length = 100)
        @Default
        private String destino;
        private String fechaHoraSalida;
        private String fechaHoraLlegada;
        private int numeroAsientosDisponibles;
        private double precio;
}
