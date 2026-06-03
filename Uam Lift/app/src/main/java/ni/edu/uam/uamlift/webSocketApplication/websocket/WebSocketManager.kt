package ni.edu.uam.uamlift.webSocketApplication.websocket
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ni.edu.uam.uamlift.webSocketApplication.model.Mensaje
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

class WebSocketManager {

    private val gson = Gson()

    private val stompClient: StompClient =
        Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "ws://192.168.1.100:8080/websocket"
        )

    private val _mensajes = MutableSharedFlow<Mensaje>()
    val mensajes = _mensajes.asSharedFlow()

    fun conectar() {

        stompClient.connect()

        stompClient.lifecycle().subscribe {
            when (it.type) {

                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED ->
                    println("Conectado")

                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR ->
                    println("Error")

                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED ->
                    println("Desconectado")

                else -> {}
            }
        }
    }

    fun suscribirse(chatId: String) {

        stompClient
            .topic("/tema/$chatId")
            .subscribe {

                val mensaje =
                    gson.fromJson(
                        it.payload,
                        Mensaje::class.java
                    )

                _mensajes.tryEmit(mensaje)
            }
    }

    fun enviarMensaje(mensaje: Mensaje) {

        stompClient.send(
            "/app/envio/${mensaje.chatId}",
            gson.toJson(mensaje)
        ).subscribe()
    }

    fun desconectar() {
        stompClient.disconnect()
    }
}