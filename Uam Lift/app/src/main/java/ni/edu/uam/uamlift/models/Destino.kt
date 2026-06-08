package ni.edu.uam.uamlift.models

import com.google.gson.annotations.SerializedName

data class Destino(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("universidad")
    val universidad: Boolean = false,

    @SerializedName("estado")
    val estado: Boolean = true
)
