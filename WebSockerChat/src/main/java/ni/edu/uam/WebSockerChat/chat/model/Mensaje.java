package ni.edu.uam.WebSockerChat.chat.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_chat")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long viajeId;

    @Column(nullable = false)
    private Long remitenteId;

    // ¡NUEVO! Guardamos el nombre directamente en la BD del chat
    @Column(nullable = false)
    private String remitenteNombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    @PrePersist
    protected void onCreate() {
        this.fechaEnvio = LocalDateTime.now();
    }
}