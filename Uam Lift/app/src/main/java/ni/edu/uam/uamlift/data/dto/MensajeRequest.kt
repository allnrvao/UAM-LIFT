package ni.edu.uam.uamlift.data.dto

data class MensajeRequest(
    val viajeId: Long,
    val usuarioId: Long,
    val contenido: String
)
