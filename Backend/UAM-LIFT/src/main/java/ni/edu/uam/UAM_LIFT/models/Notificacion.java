package ni.edu.uam.UAM_LIFT.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ni.edu.uam.UAM_LIFT.enums.TipoNotificacion;

import java.time.LocalDateTime;

/**
 * Notificación dirigida a un usuario específico (pasajero o conductor) sobre
 * eventos de un viaje, por ejemplo: cancelación o inicio del viaje.
 *
 * Se guarda únicamente el ID del viaje (en vez de una relación @ManyToOne)
 * para evitar problemas de serialización circular y mantener la entidad
 * desacoplada del ciclo de vida del viaje.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notificaciones")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"viajesAsignados", "password", "viajes", "hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @Column(name = "viaje_id")
    private Long viajeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Column(nullable = false)
    private boolean leida = false;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
