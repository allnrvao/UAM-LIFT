package ni.edu.uam.UAM_LIFT.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EstadoViaje {
    @JsonProperty("PROPUESTO")
    PROPUESTO,

    @JsonProperty("PROGRAMADO")
    PROGRAMADO,

    @JsonProperty("EN_CURSO")
    EN_CURSO,

    @JsonProperty("FINALIZADO")
    FINALIZADO,

    @JsonProperty("CANCELADO")
    CANCELADO
}