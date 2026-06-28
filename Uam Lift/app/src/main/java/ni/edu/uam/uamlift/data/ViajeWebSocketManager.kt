package ni.edu.uam.uamlift.data

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

    private val _ubicacion = MutableSharedFlow<Ubicacion>()

    val ubicacion = _ubicacion.asSharedFlow()

    fun conectar(idViaje: Long) {
        // Usando la IP definida en RetrofitClient
        val request = Request.Builder()
            .url("ws://192.168.0.7:8080/ws/viaje")
            .build()

        socket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                val mensaje = Suscripcion(
                    idViaje = idViaje,
                    tipo = "SUSCRIBIR"
                )
                webSocket.send(gson.toJson(mensaje))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val ubicacion = gson.fromJson(text, Ubicacion::class.java)
                    CoroutineScope(Dispatchers.IO).launch {
                        _ubicacion.emit(ubicacion)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
        socket?.send(gson.toJson(mensaje))
    }

    fun cerrar() {
        socket?.close(1000, "Cierre de conexión")
        socket = null
    }
}
