package ni.edu.uam.uamlift.data

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.dto.Suscripcion
import ni.edu.uam.uamlift.data.dto.Ubicacion
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket

class ViajeWebSocketManager(
    private val client: OkHttpClient
) {

    private val gson = Gson()
    private var socket: WebSocket? = null
    private var currentViajeId: Long? = null

    // Usamos replay = 1 para que el pasajero reciba la última ubicación conocida al conectarse
    private val _ubicacion = MutableSharedFlow<Ubicacion>(replay = 1)
    val ubicacion = _ubicacion.asSharedFlow()

    fun conectar(idViaje: Long) {
        // Si ya estamos conectados al mismo viaje, no hacemos nada
        if (socket != null && currentViajeId == idViaje) return

        // Si estamos conectados a otro viaje, cerramos primero
        if (socket != null) {
            cerrar()
        }

        currentViajeId = idViaje

        val request = Request.Builder()
            .url("ws://192.168.0.13:8082/ws/viaje")
            .build()

        socket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ViajeWebSocket", "Conectado al servidor para el viaje: $idViaje")
                val mensaje = Suscripcion(
                    idViaje = idViaje,
                    tipo = "SUSCRIBIR"
                )
                webSocket.send(gson.toJson(mensaje))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val ubicacionRecibida = gson.fromJson(text, Ubicacion::class.java)
                    // Validamos que la ubicación pertenezca al viaje que estamos visualizando
                    if (ubicacionRecibida.idViaje == idViaje) {
                        CoroutineScope(Dispatchers.IO).launch {
                            _ubicacion.emit(ubicacionRecibida)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ViajeWebSocket", "Error al procesar ubicación: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ViajeWebSocket", "Fallo en conexión: ${t.message}")
                socket = null
                currentViajeId = null
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                socket = null
                currentViajeId = null
            }
        })
    }

    fun enviarUbicacion(
        idViaje: Long,
        lat: Double,
        lng: Double
    ) {
        val mensaje = Ubicacion(
            idViaje = idViaje,
            tipo = "ACTUALIZAR",
            latitud = lat,
            longitud = lng
        )
        val json = gson.toJson(mensaje)
        val enviado = socket?.send(json) ?: false
        if (!enviado) {
            Log.e("ViajeWebSocket", "No se pudo enviar la ubicación. ¿Está conectado?")
        }
    }

    fun cerrar() {
        socket?.close(1000, "Cierre de conexión")
        socket = null
        currentViajeId = null
    }
}
