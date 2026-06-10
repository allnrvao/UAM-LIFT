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
    @SerializedName("contraseña")
    val contrasenia: String? = null,
    @SerializedName("imagenUrl")
    var imagenUrl: String? = null,
    @SerializedName("estado")
    val estado: Boolean = true,
    @SerializedName("correoVerificado")
    val correoVerificado: Boolean = false

)