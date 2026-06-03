package ni.edu.uam.uamlift.webSocketApplication.repository

import kotlinx.coroutines.flow.SharedFlow
import ni.edu.uam.uamlift.webSocketApplication.model.Mensaje
import ni.edu.uam.uamlift.webSocketApplication.websocket.WebSocketManager

class ChatRepository {

    private val socket = WebSocketManager()

    val mensajes: SharedFlow<Mensaje> = socket.mensajes

    fun conectar() {
        socket.conectar()
    }

    fun suscribirse(chatId: String) {
        socket.suscribirse(chatId)
    }

    fun enviarMensaje(mensaje: Mensaje) {
        socket.enviarMensaje(mensaje)
    }

    fun desconectar() {
        socket.desconectar()
    }
}