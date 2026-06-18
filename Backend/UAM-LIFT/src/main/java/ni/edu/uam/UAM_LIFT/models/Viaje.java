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

    // ✅ CORRECCIÓN: Se agregó cascade = CascadeType.ALL para evitar el error TransientPropertyValueException
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "origen_id", nullable = false)
    private Destino origen;

    // ✅ CORRECCIÓN: Se agregó cascade = CascadeType.ALL también aquí
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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