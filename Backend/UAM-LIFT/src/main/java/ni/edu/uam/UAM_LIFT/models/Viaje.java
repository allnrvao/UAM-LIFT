package ni.edu.uam.UAM_LIFT.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.Date;
import java.util.List;

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

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "destino_id", nullable = false)
        private Destino destino;

        @Column(nullable = false)
        @FutureOrPresent
        private Date fechaHoraSalida;
        @Column(nullable = false)
        @FutureOrPresent
        private Date fechaHoraLlegada;
        @Column(nullable = false)
        @Min(value = 1, message = "El número de asientos disponibles debe ser al menos 1")
        private int numeroAsientosDisponibles;
        @Column(nullable = false)
        @Min(value = 1, message = "El precio del viaje debe ser al menos 1")
        private double precioPorPersona;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "viaje_usuario",
                joinColumns = @JoinColumn(name = "viaje_id"),
                inverseJoinColumns = @JoinColumn(name = "usuario_id")
        )
        private List<Usuario> usuarios;

        @AssertTrue(message = "La fecha y hora de llegada deben ser posteriores a la fecha y hora de salida")
        public boolean isFechaHoraLlegadaValida() {
            if (fechaHoraSalida == null || fechaHoraLlegada == null) {
                return true;
            }
            return fechaHoraLlegada.after(fechaHoraSalida);

        }

        @AssertTrue(message = "El número de usuarios inscritos no puede exceder el número de asientos disponibles")
        public boolean isNumeroAsientosSuficientes() {
            if (usuarios == null) {
                return true;
            }
            return usuarios.size() <= numeroAsientosDisponibles;
        }


}
