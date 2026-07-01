package ni.edu.uam.uamlift.data.enums

import com.google.gson.annotations.SerializedName

enum class TipoNotificacion {
    @SerializedName("CANCELACION_VIAJE") CANCELACION_VIAJE,
    @SerializedName("INICIO_VIAJE") INICIO_VIAJE,
    @SerializedName("FINALIZACION_VIAJE") FINALIZACION_VIAJE,
    @SerializedName("USUARIO_UNIDO") USUARIO_UNIDO,
    @SerializedName("USUARIO_ELIMINADO") USUARIO_ELIMINADO,
    @SerializedName("GENERAL") GENERAL
}