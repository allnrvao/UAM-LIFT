package ni.edu.uam.uamlift.data.models

import com.google.gson.annotations.SerializedName

data class Carro(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("placa")
    var placa: String,

    @SerializedName("marca")
    var marca: String,

    @SerializedName("modelo")
    var modelo: String,

    @SerializedName("color")
    var color: String,

    @SerializedName("propietario")
    var propietario: Usuario
)