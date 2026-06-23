package ni.edu.uam.uamlift.data.dto

data class MensajeResponse(
    val id: Long,
    val usuarioId: Long,
    val nombreUsuario: String,
    val contenido: String,
    val fechaEnvio: String
)
