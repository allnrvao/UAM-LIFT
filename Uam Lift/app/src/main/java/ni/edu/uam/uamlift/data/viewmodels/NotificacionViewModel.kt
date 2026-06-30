package ni.edu.uam.uamlift.data.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ni.edu.uam.uamlift.data.api.NotificacionApiService
import ni.edu.uam.uamlift.data.models.Notificacion

/**
 * Maneja el estado de las notificaciones del usuario actual: la lista
 * (ordenada de la más reciente a la más antigua) y el conteo de no leídas,
 * usado para mostrar el círculo rojo sobre el botón de notificaciones.
 *
 * También detecta notificaciones nuevas que llegan en cada refresco para
 * poder disparar un aviso del sistema (ver [NotificationHelper]).
 */
class NotificacionViewModel(
    private val apiService: NotificacionApiService
) : ViewModel() {

    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    private val _noLeidas = MutableStateFlow(0L)
    val noLeidas: StateFlow<Long> = _noLeidas.asStateFlow()

    // IDs de notificaciones que ya conocemos, para detectar cuáles son nuevas en cada refresco.
    private var idsConocidos: Set<Long> = emptySet()
    private var primeraCarga = true

    // Callback opcional usado por la UI para mostrar la notificación del sistema cuando llega una nueva.
    var onNuevaNotificacion: ((Notificacion) -> Unit)? = null

    fun cargarNotificaciones(usuarioId: Long) {
        if (usuarioId <= 0L) return
        viewModelScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) { apiService.obtenerNotificaciones(usuarioId) }
                    .sortedByDescending { it.fechaCreacion ?: "" } // Más reciente arriba, más vieja abajo

                detectarNotificacionesNuevas(lista)
                _notificaciones.value = lista

                _noLeidas.value = withContext(Dispatchers.IO) { apiService.contarNoLeidas(usuarioId) }
            } catch (e: Exception) {
                Log.e("NotificacionViewModel", "Error al cargar notificaciones", e)
            }
        }
    }

    private fun detectarNotificacionesNuevas(listaActual: List<Notificacion>) {
        val idsActuales = listaActual.mapNotNull { it.id }.toSet()

        if (primeraCarga) {
            // En la primera carga no disparamos avisos del sistema por notificaciones ya existentes.
            idsConocidos = idsActuales
            primeraCarga = false
            return
        }

        val nuevas = listaActual.filter { it.id != null && it.id !in idsConocidos }
        idsConocidos = idsActuales

        // Disparamos en orden cronológico (de la más vieja a la más nueva de este lote).
        nuevas.sortedBy { it.id }.forEach { onNuevaNotificacion?.invoke(it) }
    }

    fun marcarComoLeida(id: Long, usuarioId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { apiService.marcarComoLeida(id) }
                _notificaciones.value = _notificaciones.value.map {
                    if (it.id == id) it.copy(leida = true) else it
                }
                _noLeidas.value = withContext(Dispatchers.IO) { apiService.contarNoLeidas(usuarioId) }
            } catch (e: Exception) {
                Log.e("NotificacionViewModel", "Error al marcar notificación como leída", e)
            }
        }
    }

    fun marcarTodasComoLeidas(usuarioId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { apiService.marcarTodasComoLeidas(usuarioId) }
                _notificaciones.value = _notificaciones.value.map { it.copy(leida = true) }
                _noLeidas.value = 0L
            } catch (e: Exception) {
                Log.e("NotificacionViewModel", "Error al marcar notificaciones como leídas", e)
            }
        }
    }
}
