package ni.edu.uam.UAM_LIFT.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;
    @Column(nullable = false, length = 50)
    private String apellido;
    @Column(nullable = false, length = 50, unique = true)
    private String correo;
    @Column(nullable = false, length = 100, unique = true)
    private String contraseña;
    @Column(nullable = false, length = 100)
    private String imagenUrl;

}
