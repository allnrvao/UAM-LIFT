package ni.edu.uam.uamlift.data.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ni.edu.uam.uamlift.data.ChatWebSocketManager
import ni.edu.uam.uamlift.data.api.ChatApi
import ni.edu.uam.uamlift.data.api.UsuarioApiService

data class MensajeUI(
    val id: Long,
    val usuarioId: Long,
    val usuarioNombre: String,
    val contenido: String,
    val fechaEnvio: String,
    val isMe: Boolean
)

class ChatViewModel(
    private val api: ChatApi,
    private val usuarioApi: UsuarioApiService
) : ViewModel() {

    private val ws = ChatWebSocketManager()
    private val nombresCache = mutableMapOf<Long, String>()

    private val _mensajesUi = MutableStateFlow<List<MensajeUI>>(emptyList())
    val mensajesUi = _mensajesUi.asStateFlow()

    private var currentViajeId: Long? = null
    private var connectionJob: Job? = null

    fun iniciarChat(viajeId: Long, currentUserId: Long) {
        if (currentViajeId == viajeId) return
        
        connectionJob?.cancel()
        currentViajeId = viajeId
        _mensajesUi.value = emptyList()

        connectionJob = viewModelScope.launch {
            // 1. Conectar al WebSocket
            try {
                ws.conectar(viajeId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error al conectar WS: ${e.message}")
            }

            // 2. Lanzar la escucha en tiempo real en un job separado de inmediato
            launch {
                ws.mensajes
                    .filter { it.viajeId == viajeId }
                    .collect { nuevo ->
                        val nombre = obtenerNombreUsuario(nuevo.usuarioId)
                        val nuevoUi = MensajeUI(
                            id = nuevo.id,
                            usuarioId = nuevo.usuarioId,
                            usuarioNombre = nombre,
                            contenido = nuevo.contenido,
                            fechaEnvio = nuevo.fechaEnvio,
                            isMe = nuevo.usuarioId == currentUserId
                        )
                        
                        _mensajesUi.update { actual ->
                            // Evitar duplicados si el historial y el socket traen el mismo mensaje
                            if (actual.any { it.id == nuevoUi.id }) actual else actual + nuevoUi
                        }
                    }
            }

            try {
                val historial = api.obtenerHistorial(viajeId)
                val listaHistorialUi = historial.map { res ->
                    MensajeUI(
                        id = res.id,
                        usuarioId = res.usuarioId,
                        usuarioNombre = obtenerNombreUsuario(res.usuarioId),
                        contenido = res.contenido,
                        fechaEnvio = res.fechaEnvio,
                        isMe = res.usuarioId == currentUserId
                    )
                }
                
                _mensajesUi.update { actual ->
                    val idsExistentes = actual.map { it.id }.toSet()
                    val historialFiltrado = listaHistorialUi.filter { it.id !in idsExistentes }
                    (historialFiltrado + actual).sortedBy { it.id }
                }
                Log.d("ChatViewModel", "Historial sincronizado con éxito")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error al cargar historial: ${e.message}")
            }
        }
    }

    private suspend fun obtenerNombreUsuario(usuarioId: Long): String {
        return nombresCache[usuarioId] ?: withContext(Dispatchers.IO) {
            try {
                val usuario = usuarioApi.obtenerPorId(usuarioId)
                val nombreCompleto = "${usuario.nombre ?: ""} ${usuario.apellido ?: ""}".trim()
                val nombreAMostrar = if (nombreCompleto.isNotEmpty()) nombreCompleto else usuario.nombreUsuario ?: "Usuario $usuarioId"
                synchronized(nombresCache) {
                    nombresCache[usuarioId] = nombreAMostrar
                }
                nombreAMostrar
            } catch (e: Exception) {
                "Usuario $usuarioId"
            }
        }
    }

    fun enviarMensaje(viajeId: Long, usuarioId: Long, texto: String) {
        if (texto.isNotBlank()) {
            ws.enviarMensaje(viajeId, usuarioId, texto)
        }
    }

    override fun onCleared() {
        ws.desconectar()
        super.onCleared()
    }
}
