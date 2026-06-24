package ni.edu.uam.uamlift.data.dto

data class MensajeResponse(
    val id: Long,
    val viajeId: Long,
    val usuarioId: Long,
    val contenido: String,
    val fechaEnvio: String
)
