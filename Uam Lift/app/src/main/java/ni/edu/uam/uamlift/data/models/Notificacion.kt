package ni.edu.uam.uamlift.data.models

import com.google.gson.annotations.SerializedName
import ni.edu.uam.uamlift.data.enums.TipoNotificacion

data class Notificacion(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("viajeId")
    val viajeId: Long? = null,

    @SerializedName("tipo")
    val tipo: TipoNotificacion? = null,

    @SerializedName("titulo")
    val titulo: String = "",

    @SerializedName("mensaje")
    val mensaje: String = "",

    @SerializedName("leida")
    val leida: Boolean = false,

    @SerializedName("fechaCreacion")
    val fechaCreacion: String? = null
)
