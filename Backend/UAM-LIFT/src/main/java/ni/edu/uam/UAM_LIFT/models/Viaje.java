package ni.edu.uam.UAM_LIFT.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.*;
import ni.edu.uam.UAM_LIFT.enums.EstadoViaje;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "viajes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origen_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Destino origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Destino destino;

    // 💡 CORREGIDO: Se removió @FutureOrPresent para permitir consultas de registros existentes
    @Column(nullable = false)
    private LocalDateTime fechaHoraSalida;

    // 💡 CORREGIDO: Se removió @FutureOrPresent para permitir consultas de registros existentes
    @Column(nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @Column(nullable = false)
    @Min(value = 1, message = "El número de asientos disponibles debe ser al menos 1")
    private int numeroAsientosDisponibles;

    @Column(nullable = false)
    @Min(value = 1, message = "El precio del viaje debe ser al menos 1")
    private double precioPorPersona;

    // 🔥 CORRECCIÓN: Se configuró correctamente la relación Uno a Muchos con la intermedia.
    @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Evita bucles infinitos en Jackson al serializar a JSON
    private List<ViajeUsuario> pasajeros = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "conductor_id", nullable = false)
    @JsonIgnoreProperties({"viajesAsignados", "password", "viajes", "hibernateLazyInitializer", "handler"})
    private Usuario conductor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carro_id", nullable = false)
    @JsonIgnoreProperties({"propietario", "hibernateLazyInitializer", "handler"})
    private Carro carro;

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