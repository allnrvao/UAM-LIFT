package ni.edu.uam.UAM_LIFT.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
@SQLDelete(sql = "UPDATE usuarios SET estado = false WHERE id = ?")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100, unique = true, nullable = false)
    private String cif;
    @Column(unique = true, nullable = false, length = 50)
    private String nombreUsuario;
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
    @Column(nullable = false)
    private boolean estado = true;
    @Column(nullable = false)
    private boolean correoVerificado = false;
    @Column(nullable = false)
    private int numeroViajes = 0;

}
