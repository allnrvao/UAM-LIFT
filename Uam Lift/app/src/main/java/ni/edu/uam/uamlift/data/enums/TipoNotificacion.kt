package ni.edu.uam.uamlift.data.enums

import com.google.gson.annotations.SerializedName

enum class TipoNotificacion {
    @SerializedName("CANCELACION_VIAJE") CANCELACION_VIAJE,
    @SerializedName("INICIO_VIAJE") INICIO_VIAJE,
    @SerializedName("GENERAL") GENERAL
}
