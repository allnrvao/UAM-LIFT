package ni.edu.uam.uamlift.data.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.api.ViajeApiService
import ni.edu.uam.uamlift.data.models.*
import java.text.SimpleDateFormat
import java.util.*

class ViajeViewModel(
    private val apiService: ViajeApiService? = RetrofitClient.viajeApi
) : ViewModel() {

    private val _viajes = MutableStateFlow<List<Viaje>>(emptyList())
    val viajes: StateFlow<List<Viaje>> = _viajes.asStateFlow()

    private val _misViajes = MutableStateFlow<List<Viaje>>(emptyList())
    val misViajes: StateFlow<List<Viaje>> = _misViajes.asStateFlow()

    private val _viajesOtros = MutableStateFlow<List<Viaje>>(emptyList())
    val viajesOtros: StateFlow<List<Viaje>> = _viajesOtros.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _pasajerosViaje = MutableStateFlow<List<Usuario>>(emptyList())
    val pasajerosViaje: StateFlow<List<Usuario>> = _pasajerosViaje.asStateFlow()

    var viaje by mutableStateOf(Viaje())
        private set

    init { }

    fun publicarViaje(
        usuarioId: Long,
        conductorCif: String,
        onExito: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (viaje.precioPorPersona < 1.0) {
                    onError("El aporte debe ser al menos de C$ 1")
                    return@launch
                }
                if (viaje.numeroAsientosDisponibles < 1) {
                    onError("Debe haber al menos 1 asiento disponible")
                    return@launch
                }
                if (viaje.carro == null) {
                    onError("Debes seleccionar un vehículo para el viaje")
                    return@launch
                }
                if (viaje.origen?.latitud == null || viaje.destino?.latitud == null) {
                    onError("Debes especificar la ubicación exacta en el mapa")
                    return@launch
                }

                if (!esFechaFutura(viaje.fechaHoraSalida)) {
                    onError("La fecha y hora de salida no puede ser anterior a la actual")
                    return@launch
                }

                val limiteValido = apiService?.validarNumViajes(usuarioId) ?: false
                if (!limiteValido) {
                    onError("Has alcanzado el límite de 2 viajes permitidos (entre creados y tomados).")
                    return@launch
                }

                val fechasValidas = apiService?.validarFechas(
                    usuarioId,
                    viaje.fechaHoraSalida ?: "",
                    viaje.fechaHoraLlegada ?: ""
                ) ?: false
                if (!fechasValidas) {
                    onError("Conflicto de horarios: Ya tienes un viaje en este rango de tiempo.")
                    return@launch
                }

                val conductorSimulado = Usuario(cif = conductorCif)
                val viajeAEnviar = viaje.copy(
                    estadoViaje = EstadoViaje.PROPUESTO,
                    conductor = conductorSimulado
                )

                val nuevoViaje = apiService?.crearViaje(conductorCif, viajeAEnviar)

                if (nuevoViaje != null) {
                    cargarViajesDesdeBackend(usuarioId)
                    viaje = Viaje()
                    onExito()
                } else {
                    onError("Error al procesar la solicitud.")
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error en publicación", e)
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun esFechaFutura(fechaStr: String?): Boolean {
        if (fechaStr.isNullOrBlank()) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val fecha = sdf.parse(fechaStr)
            val ahora = Calendar.getInstance().time
            fecha?.after(ahora) ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun cargarViajesDesdeBackend(usuarioId: Long? = null) {
        viewModelScope.launch {
            if (apiService == null) return@launch

            if (usuarioId == null && _viajesOtros.value.isNotEmpty()) return@launch

            val tieneDatos = _viajesOtros.value.isNotEmpty() || _misViajes.value.isNotEmpty()
            if (!tieneDatos) _isLoading.value = true

            try {
                if (usuarioId != null) {
                    val todosDef = async { apiService.obtenerTodosLosViajes() }
                    val condDef = async { apiService.obtenerViajesPorConductor(usuarioId) }
                    val userDef = async { apiService.obtenerViajesPorUsuario(usuarioId) }

                    val todos = todosDef.await()
                    val creados = condDef.await()
                    val unidos = userDef.await()

                    _viajes.value = todos

                    // Regla de visibilidad: 
                    // 1. Mis viajes creados se ven siempre (el conductor puede verlos aunque estén cancelados/terminados)
                    // 2. Viajes a los que me uní: Solo si no están CANCELADOS, FINALIZADOS, EN_CURSO o expirados
                    val misUnidosFiltrados = unidos.filter { v ->
                        v.estadoViaje != EstadoViaje.CANCELADO &&
                                v.estadoViaje != EstadoViaje.FINALIZADO &&
                                v.estadoViaje != EstadoViaje.EN_CURSO &&
                                esFechaFutura(v.fechaHoraLlegada)
                    }
                    _misViajes.value = (creados + misUnidosFiltrados).distinctBy { it.id }

                    // 3. Viajes de otros: Solo si hay asientos, no están cancelados/finalizados/en curso y no han expirado
                    _viajesOtros.value = todos.filter { v ->
                        val esConductor = v.conductor?.id == usuarioId
                        val esPasajero = v.pasajeros.any { p -> p.usuario?.id == usuarioId }

                        !esConductor && !esPasajero &&
                                v.numeroAsientosDisponibles > 0 &&
                                v.estadoViaje != EstadoViaje.CANCELADO &&
                                v.estadoViaje != EstadoViaje.FINALIZADO &&
                                v.estadoViaje != EstadoViaje.EN_CURSO &&
                                esFechaFutura(v.fechaHoraLlegada)
                    }
                } else {
                    val todos = apiService.obtenerTodosLosViajes()
                    _viajesOtros.value = todos.filter { v ->
                        v.estadoViaje != EstadoViaje.CANCELADO &&
                                v.estadoViaje != EstadoViaje.FINALIZADO &&
                                v.estadoViaje != EstadoViaje.EN_CURSO &&
                                esFechaFutura(v.fechaHoraLlegada) &&
                                v.numeroAsientosDisponibles > 0
                    }
                    _misViajes.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al cargar datos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelarViaje(viajeId: Long, usuarioId: Long, onExito: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val v = _misViajes.value.find { it.id == viajeId }
                // Regla: No se puede cancelar si ya inició
                if (v != null && (v.estadoViaje == EstadoViaje.EN_CURSO || v.estadoViaje == EstadoViaje.FINALIZADO)) {
                    onError("El viaje ya ha iniciado o finalizado y no se puede cancelar.")
                    return@launch
                }
                if (apiService?.cancelarViaje(viajeId) == true) {
                    cargarViajesDesdeBackend(usuarioId)
                    onExito()
                } else {
                    onError("No se pudo cancelar el viaje.")
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al cancelar", e)
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun iniciarViaje(viajeId: Long, usuarioId: Long, onExito: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val v = _misViajes.value.find { it.id == viajeId }
                // Regla: Solo se inicia a partir de la hora de inicio
                if (v != null && esFechaFutura(v.fechaHoraSalida)) {
                    onError("Solo se puede iniciar el viaje a partir de su hora de salida.")
                    return@launch
                }
                if (apiService?.iniciarViaje(viajeId) == true) {
                    cargarViajesDesdeBackend(usuarioId)
                    onExito()
                } else {
                    onError("No se pudo iniciar el viaje.")
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al iniciar", e)
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun finalizarViaje(viajeId: Long, usuarioId: Long, onExito: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (apiService?.finalizarViaje(viajeId) == true) {
                    cargarViajesDesdeBackend(usuarioId)
                    onExito()
                } else {
                    onError("No se pudo finalizar el viaje.")
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al finalizar", e)
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun obtenerPasajeros(viajeId: Long) {
        viewModelScope.launch {
            try {
                val pasajeros = apiService?.obtenerPasajerosPorViaje(viajeId) ?: emptyList()
                _pasajerosViaje.value = pasajeros
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al obtener pasajeros", e)
                _pasajerosViaje.value = emptyList()
            }
        }
    }

    fun validarNumViajes(usuarioId: Long, onExito: (Boolean) -> Unit = {}){
        viewModelScope.launch {
            val valido = apiService?.validarNumViajes(usuarioId) ?: false
            onExito(valido)
        }

    }
    fun unirseAlViaje(
        viajeId: Long,
        usuarioId: Long,
        usuarioCif: String,
        onExito: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exito = apiService?.agregarPasajero(viajeId, usuarioCif) ?: false
                if (exito) {
                    cargarViajesDesdeBackend(usuarioId)
                    onExito()
                } else {
                    onError("No se pudo unir al viaje.")
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al unirse", e)
                onError("Error de red.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelarParticipacion(viajeId: Long, usuarioId: Long, usuarioCif: String, onExito: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (apiService?.cancelarParticipacion(viajeId, usuarioCif) == true) {
                    cargarViajesDesdeBackend(usuarioId)
                    onExito()
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al cancelar participacion", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarOrigen(destino: Destino?) { viaje = viaje.copy(origen = destino) }
    fun actualizarDestino(destino: Destino?) { viaje = viaje.copy(destino = destino) }
    fun actualizarFechaHoraSalida(fecha: String) { viaje = viaje.copy(fechaHoraSalida = fecha) }
    fun actualizarFechaHoraLlegada(fecha: String) { viaje = viaje.copy(fechaHoraLlegada = fecha) }
    fun actualizarNumeroAsientos(numero: Int) { viaje = viaje.copy(numeroAsientosDisponibles = numero) }
    fun actualizarPrecio(precio: Double) { viaje = viaje.copy(precioPorPersona = precio) }
    fun actualizarCarro(carro: Carro?) { viaje = viaje.copy(carro = carro) }
}
