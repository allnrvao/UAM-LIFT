package ni.edu.uam.uamlift.data.models

import com.google.gson.annotations.SerializedName

data class Destino(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("latitud")
    val latitud: Double? = null,

    @SerializedName("longitud")
    val longitud: Double? = null,

    @SerializedName("universidad")
    val universidad: Boolean = false,

    @SerializedName("estado")
    val estado: Boolean = true
)