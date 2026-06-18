package ni.edu.uam.uamlift.data.models

import com.google.gson.annotations.SerializedName
import ni.edu.uam.uamlift.data.enums.EstadoViajeUsuario

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
