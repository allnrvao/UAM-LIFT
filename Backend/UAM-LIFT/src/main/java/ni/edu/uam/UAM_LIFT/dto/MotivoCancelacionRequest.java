package ni.edu.uam.UAM_LIFT.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO usado al cancelar un viaje: transporta el motivo ingresado por el
 * conductor para que pueda ser incluido en la notificación enviada a los
 * pasajeros.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MotivoCancelacionRequest {
    private String motivo;
}
