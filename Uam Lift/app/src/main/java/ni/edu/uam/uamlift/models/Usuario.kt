package ni.edu.uam.uamlift.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("cif")
    val cif: String? = null,
    @SerializedName("nombreUsuario")
    val nombreUsuario: String? = null,
    @SerializedName("nombre")
    val nombre: String? = null,
    @SerializedName("apellido")
    val apellido: String? = null,
    @SerializedName("correo")
    val correo: String? = null,
    @SerializedName("contraseña")
    val contrasenia: String? = null,
    @SerializedName("imagenUrl")
    val imagenUrl: String? = null,
    @SerializedName("estado")
    val estado: Boolean = true,
    @SerializedName("correoVerificado")
    val correoVerificado: Boolean = false
)
