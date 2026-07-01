package ni.edu.uam.uamlift.data.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ni.edu.uam.uamlift.data.ChatLocalCache
import ni.edu.uam.uamlift.data.ChatWebSocketManager
import ni.edu.uam.uamlift.data.api.ChatApi
import ni.edu.uam.uamlift.data.api.UsuarioApiService
import ni.edu.uam.uamlift.data.models.Usuario

data class MensajeUI(
    val id: Long,
    val usuarioId: Long,
    val usuarioNombre: String,
    val contenido: String,
    val fechaEnvio: String,
    val isMe: Boolean,
    val timestamp: Long = System.currentTimeMillis() // Para desempatar mensajes sin ID real
)

class ChatViewModel(
    private val api: ChatApi,
    private val usuarioApi: UsuarioApiService
) : ViewModel() {

    private val ws = ChatWebSocketManager()
    private val nombresCache = mutableMapOf<Long, String>()
    private val fetchingIds = mutableSetOf<Long>()

    private val _mensajesUi = MutableStateFlow<List<MensajeUI>>(emptyList())
    val mensajesUi = _mensajesUi.asStateFlow()

    private var currentViajeId: Long? = null
    private var connectionJob: Job? = null

    fun iniciarChat(viajeId: Long, currentUserId: Long, context: Context? = null) {
        if (currentViajeId == viajeId) return

        connectionJob?.cancel()
        currentViajeId = viajeId

        // Mostramos de inmediato los últimos mensajes guardados localmente (si los hay)
        // para que el chat nunca se vea vacío mientras llega la respuesta del backend.
        val appContext = context?.applicationContext
        _mensajesUi.value = if (appContext != null) {
            ChatLocalCache.obtenerUltimos(appContext, viajeId)
                .map { it.copy(isMe = it.usuarioId == currentUserId) }
        } else {
            emptyList()
        }

        connectionJob = viewModelScope.launch {
            try {
                ws.conectar(viajeId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error al conectar WS: ${e.message}")
            }

            // Escucha de mensajes en tiempo real
            launch {
                ws.mensajes.collect { nuevo ->
                    // Si el viajeId del mensaje es 0 o coincide con el actual, lo aceptamos
                    if (nuevo.viajeId == 0L || nuevo.viajeId == viajeId) {
                        val nombre = resolveName(nuevo.usuarioId)
                        val nuevoUi = MensajeUI(
                            id = nuevo.id,
                            usuarioId = nuevo.usuarioId,
                            usuarioNombre = nombre,
                            contenido = nuevo.contenido,
                            fechaEnvio = nuevo.fechaEnvio.ifBlank { System.currentTimeMillis().toString() },
                            isMe = nuevo.usuarioId == currentUserId
                        )

                        _mensajesUi.update { actual ->
                            // Solo filtramos duplicados si el ID es válido (distinto de 0)
                            if (nuevoUi.id != 0L && actual.any { it.id == nuevoUi.id }) {
                                actual
                            } else {
                                (actual + nuevoUi).sortedWith(compareBy({ it.id }, { it.timestamp }))
                            }
                        }
                        appContext?.let { ctx ->
                            ChatLocalCache.guardarUltimos(ctx, viajeId, _mensajesUi.value)
                        }
                    }
                }
            }

            // Carga de historial
            try {
                val historial = api.obtenerHistorial(viajeId)
                val listaHistorialUi = historial.map { res ->
                    MensajeUI(
                        id = res.id,
                        usuarioId = res.usuarioId,
                        usuarioNombre = resolveName(res.usuarioId),
                        contenido = res.contenido,
                        fechaEnvio = res.fechaEnvio,
                        isMe = res.usuarioId == currentUserId
                    )
                }

                _mensajesUi.update { actual ->
                    val idsExistentes = actual.filter { it.id != 0L }.map { it.id }.toSet()
                    val historialFiltrado = listaHistorialUi.filter { it.id !in idsExistentes }
                    (historialFiltrado + actual).sortedWith(compareBy({ it.id }, { it.timestamp }))
                }

                appContext?.let { ctx ->
                    ChatLocalCache.guardarUltimos(ctx, viajeId, _mensajesUi.value)
                }

                resolveMissingNames()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error al cargar historial: ${e.message}")
            }
        }
    }

    private fun resolveName(id: Long): String {
        val cached = synchronized(nombresCache) { nombresCache[id] }
        return if (cached != null) {
            cached
        } else {
            fetchNameAsync(id)
            "Usuario #$id"
        }
    }

    private fun fetchNameAsync(id: Long) {
        synchronized(fetchingIds) {
            if (fetchingIds.contains(id)) return
            fetchingIds.add(id)
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val u = usuarioApi.obtenerPorId(id)
                val name = u.nombreUsuario?.takeIf { it.isNotBlank() }
                    ?: "${u.nombre ?: ""} ${u.apellido ?: ""}".trim().ifEmpty { "Usuario #$id" }

                synchronized(nombresCache) { nombresCache[id] = name }

                withContext(Dispatchers.Main) {
                    _mensajesUi.update { actual ->
                        actual.map { if (it.usuarioId == id) it.copy(usuarioNombre = name) else it }
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching name: ${e.message}")
            } finally {
                synchronized(fetchingIds) { fetchingIds.remove(id) }
            }
        }
    }

    private fun resolveMissingNames() {
        val missingIds = _mensajesUi.value
            .filter { it.usuarioNombre.startsWith("Usuario #") }
            .map { it.usuarioId }
            .distinct()
        missingIds.forEach { fetchNameAsync(it) }
    }

    fun precargarNombres(participantes: List<Usuario>) {
        participantes.forEach { u ->
            val id = u.id ?: return@forEach
            val name = u.nombreUsuario?.takeIf { it.isNotBlank() }
                ?: "${u.nombre ?: ""} ${u.apellido ?: ""}".trim().ifEmpty { "Usuario #$id" }

            synchronized(nombresCache) { nombresCache[id] = name }
            _mensajesUi.update { actual ->
                actual.map { if (it.usuarioId == id) it.copy(usuarioNombre = name) else it }
            }
        }
    }

    fun enviarMensaje(viajeId: Long, usuarioId: Long, contenido: String) {
        if (contenido.isNotBlank()) {
            ws.enviarMensaje(viajeId, usuarioId, contenido)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ws.desconectar()
    }
}