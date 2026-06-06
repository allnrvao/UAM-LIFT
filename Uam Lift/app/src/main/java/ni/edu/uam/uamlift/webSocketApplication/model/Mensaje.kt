package ni.edu.uam.uamlift.webSocketApplication.model

data class Mensaje(
    val contenido: String,
    val remitente: String,
    val chatId: String
)