package ni.edu.uam.uamlift.data

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    
    // Coroutine scope dedicado para emitir mensajes de forma asíncrona sin bloquear hilos de red de OkHttp
    private val socketScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _mensajes = MutableSharedFlow<MensajeResponse>(replay = 0, extraBufferCapacity = 64)
    val mensajes = _mensajes.asSharedFlow()

    private var isConnected = false
    private var currentViajeId: Long? = null

    fun conectar(viajeId: Long) {
        if (isConnected && currentViajeId == viajeId) {
            Log.d("ChatWS", "Ya conectado a este viaje ($viajeId)")
            return
        }

        desconectar()
        currentViajeId = viajeId

        val host = RetrofitClient2.chatHost
        val url = "ws://$host:8081/ws/chat?viajeId=$viajeId"

        val request = Request.Builder()
            .url(url)
            .build()

        Log.d("ChatWS", "Conectando a: $url")

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    isConnected = true
                    Log.d("ChatWS", "🟢 Conectado al WebSocket con éxito")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d("ChatWS", "📥 Mensaje recibido: $text")
                    try {
                        val mensaje = gson.fromJson(text, MensajeResponse::class.java)
                        // Cambiamos tryEmit por emit dentro de una corrutina para asegurar al 100% que el mensaje no se pierda
                        socketScope.launch {
                            _mensajes.emit(mensaje)
                        }
                    } catch (e: Exception) {
                        Log.e("ChatWS", "❌ Error al parsear mensaje: ${e.message}")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    isConnected = false
                    Log.e("ChatWS", "🔴 Error WebSocket: ${t.message}")
                    retryConnection()
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    isConnected = false
                    Log.d("ChatWS", "🔌 WebSocket cerrado: $reason")
                }
            }
        )
    }

    fun enviarMensaje(viajeId: Long, usuarioId: Long, contenido: String) {
        val socket = webSocket
        if (socket == null || !isConnected) {
            Log.e("ChatWS", "No se puede enviar mensaje: No conectado")
            return
        }

        val mensaje = MensajeRequest(
            viajeId = viajeId,
            usuarioId = usuarioId,
            contenido = contenido
        )

        val json = gson.toJson(mensaje)
        val enviado = socket.send(json)

        if (enviado) {
            Log.d("ChatWS", "📤 Mensaje enviado exitosamente: $json")
        } else {
            Log.e("ChatWS", "❌ Error al enviar mensaje por el socket")
        }
    }

    fun desconectar() {
        isConnected = false
        currentViajeId = null
        webSocket?.close(1000, "Cierre manual")
        webSocket = null
        Log.d("ChatWS", "🔌 Desconectado voluntariamente")
    }

    private fun retryConnection() {
        val viajeId = currentViajeId ?: return
        Log.d("ChatWS", "Reintentando conexión en 3 segundos...")
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            conectar(viajeId)
        }, 3000)
    }
}
