package ni.edu.uam.uamlift.data

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.dto.MensajeRequest
import ni.edu.uam.uamlift.data.dto.MensajeResponse
import okhttp3.*



class ChatWebSocketManager {

    private val client = OkHttpClient()

    private var webSocket: WebSocket? = null

    private val gson = Gson()

    private val _mensajes =
        MutableSharedFlow<MensajeResponse>()

    val mensajes = _mensajes.asSharedFlow()

    fun conectar(
        viajeId: Long
    ) {

        val request = Request.Builder()
            .url("ws://192.168.1.5:8081/ws/chat?viajeId=$viajeId")
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {

                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response
                ) {
                    println("Chat conectado")
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    text: String
                ) {

                    try {

                        val mensaje =
                            gson.fromJson(
                                text,
                                MensajeResponse::class.java
                            )

                        CoroutineScope(Dispatchers.IO).launch {
                            _mensajes.emit(mensaje)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    t.printStackTrace()
                }
            }
        )
    }

    fun enviarMensaje(
        viajeId: Long,
        usuarioId: Long,
        contenido: String
    ) {

        val mensaje = MensajeRequest(
            viajeId = viajeId,
            usuarioId = usuarioId,
            contenido = contenido
        )

        webSocket?.send(
            gson.toJson(mensaje)
        )
    }

    fun desconectar() {
        webSocket?.close(1000, null)
    }
}