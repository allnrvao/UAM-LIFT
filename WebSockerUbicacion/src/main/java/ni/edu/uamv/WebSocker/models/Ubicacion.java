package ni.edu.uamv.WebSocker.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion {
    private Long idViaje;
    private double lat;
    private double lon;
    private String tipo; // "SUSCRIBIR" o "ACTUALIZAR"
}