package ni.edu.uam.uamlift.models

import com.google.gson.annotations.SerializedName

data class ViajeUsuario(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("viaje")
    val viaje: Viaje? = null,

    @SerializedName("usuario")
    val usuario: Usuario? = null,

    @SerializedName("estado")
    val estado: EstadoViajeUsuario? = null
)

enum class EstadoViajeUsuario {
    @SerializedName("PENDIENTE") PENDIENTE,
    @SerializedName("ACEPTADO") ACEPTADO,
    @SerializedName("RECHAZADO") RECHAZADO,
    @SerializedName("CANCELADO") CANCELADO
}
