package ni.edu.uam.UAM_LIFT.models;

<<<<<<< Updated upstream
import com.fasterxml.jackson.annotation.JsonBackReference;
=======
import com.fasterxml.jackson.annotation.JsonBackReference; // <-- NUEVA IMPORTACIÓN
>>>>>>> Stashed changes
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ni.edu.uam.UAM_LIFT.enums.EstadoViajeUsuario;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ViajeUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id")
    @JsonBackReference
    private Viaje viaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private EstadoViajeUsuario estado;
}