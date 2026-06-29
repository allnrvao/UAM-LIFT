package ni.edu.uam.uamlift.data.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("cif")
    var cif: String? = null,
    @SerializedName("nombreUsuario")
    var nombreUsuario: String? = null,
    @SerializedName("nombre")
    var nombre: String? = null,
    @SerializedName("apellido")
    var apellido: String? = null,
    @SerializedName("correo")
    var correo: String? = null,
    @SerializedName("contrasenia")
    var contrasenia: String? = null,
    @SerializedName(value = "imagenUrl", alternate = ["imagen_url"])
    var imagenUrl: String? = null,
    @SerializedName("estado")
    var estado: Boolean = true,
    @SerializedName("correoVerificado")
    var correoVerificado: Boolean = false,
    @SerializedName("numeroViajes")
    var numeroViajes: Int = 0,
    @SerializedName("kilometrosTotales")
    var kilometrosTotales: Double = 0.0,
    @SerializedName("co2Ahorrado")
    var co2Ahorrado: Double = 0.0
)