package ni.edu.uam.UAM_LIFT.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import lombok.*;
import ni.edu.uam.UAM_LIFT.enums.EstadoViaje;
import ni.edu.uam.UAM_LIFT.enums.EstadoViajeUsuario;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "viajes")
@SQLDelete(sql = "UPDATE viajes SET estado = false WHERE id = ?")
public class Viaje {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String origen;
        @Column(nullable = false, length = 100)

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "destino_id", nullable = false)
        private Destino destino;

        @Column(nullable = false)
        @FutureOrPresent
        private LocalDateTime fechaHoraSalida;
        @Column(nullable = false)
        @FutureOrPresent
        private LocalDateTime fechaHoraLlegada;
        @Column(nullable = false)
        @Min(value = 1, message = "El número de asientos disponibles debe ser al menos 1")
        private int numeroAsientosDisponibles;
        @Column(nullable = false)
        @Min(value = 1, message = "El precio del viaje debe ser al menos 1")
        private double precioPorPersona;

        @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL)
        private List<ViajeUsuario> pasajeros = new ArrayList<>();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "conductor_id", nullable = false)
        private Usuario conductor;

        @Column(nullable = false)
        private boolean estado = true;

        @Enumerated(EnumType.STRING)
        private EstadoViaje estadoViaje;

        @AssertTrue(message = "La fecha y hora de llegada deben ser posteriores a la fecha y hora de salida")
        public boolean isFechaHoraLlegadaValida() {
            if (fechaHoraSalida == null || fechaHoraLlegada == null) {
                return true;
            }
            return fechaHoraLlegada.isAfter(fechaHoraSalida);

        }

        @AssertTrue(message = "El número de usuarios inscritos no puede exceder el número de asientos disponibles")
        public boolean isNumeroAsientosSuficientes() {
            if (pasajeros == null) {
                return true;
            }
            return pasajeros.size() <= numeroAsientosDisponibles;
        }


}
