package ni.edu.uam.UAM_LIFT.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TipoNotificacion {
    @JsonProperty("CANCELACION_VIAJE")
    CANCELACION_VIAJE,

    @JsonProperty("INICIO_VIAJE")
    INICIO_VIAJE,

    @JsonProperty("FINALIZACION_VIAJE")
    FINALIZACION_VIAJE,

    @JsonProperty("USUARIO_UNIDO")
    USUARIO_UNIDO,

    @JsonProperty("USUARIO_ELIMINADO")
    USUARIO_ELIMINADO,

    @JsonProperty("GENERAL")
    GENERAL
}