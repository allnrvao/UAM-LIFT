package ni.edu.uam.UAM_LIFT.models;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;


@Entity
@Getter
@Setter
@Table(name = "chats")
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;
}
