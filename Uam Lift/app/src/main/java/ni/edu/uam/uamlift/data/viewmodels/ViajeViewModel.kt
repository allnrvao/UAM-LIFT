package ni.edu.uam.uamlift.data.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.api.ViajeApiService
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.models.*
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.enums.EstadoViaje

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

    fun publicarViaje(
        usuarioId: Long,
        conductorCif: String,
        onExito: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // 1. Validación: Ya tiene un viaje activo
                val tieneViajeActivo = _misViajes.value.any { 
                    it.estadoViaje == EstadoViaje.PROPUESTO ||
                    it.estadoViaje == EstadoViaje.PROGRAMADO || 
                    it.estadoViaje == EstadoViaje.EN_CURSO 
                }
                if (tieneViajeActivo) {
                    onError("Ya tienes un viaje activo. Debes finalizarlo antes de crear uno nuevo.")
                    return@launch
                }

                if (viaje.precioPorPersona < 1.0) {
                    onError("El aporte debe ser al menos de C$ 1")
                    return@launch
                }
                
                // 2. Validación: Conflicto de horario (al mismo tiempo)
                val fechasValidas = withContext(Dispatchers.IO) {
                    api.validarFechas(usuarioId, viaje.fechaHoraSalida ?: "", viaje.fechaHoraLlegada ?: "")
                }
                if (!fechasValidas) {
                    onError("Conflicto de horario: Ya tienes otro viaje programado en ese mismo horario.")
                    return@launch
                }

                val conductorSimulado = Usuario(cif = conductorCif)
                val viajeAEnviar = viaje.copy(estadoViaje = EstadoViaje.PROPUESTO, conductor = conductorSimulado)
                val nuevoViaje = withContext(Dispatchers.IO) { api.crearViaje(conductorCif, viajeAEnviar) }

                if (nuevoViaje != null) {
                    fetchViajesInternal(api, usuarioId)
                    viaje = Viaje()
                    onExito()
                } else {
                    onError("Error al procesar la solicitud.")
                }
            } catch (e: Exception) {
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unirseAlViaje(
        viajeId: Long,
        usuarioId: Long,
        usuarioCif: String,
        onExito: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Validación: Ya participa en un viaje activo
                val tieneViajeActivo = _misViajes.value.any { 
                    it.estadoViaje == EstadoViaje.PROPUESTO || 
                    it.estadoViaje == EstadoViaje.PROGRAMADO || 
                    it.estadoViaje == EstadoViaje.EN_CURSO 
                }
                if (tieneViajeActivo) {
                    onError("Ya estás participando en un viaje activo. Finalízalo antes de unirte a otro.")
                    return@launch
                }

                // 2. Validación: Conflicto de horario (mismo tiempo)
                val viajeADeterminar = _viajesOtros.value.find { it.id == viajeId } ?: _viajes.value.find { it.id == viajeId }
                if (viajeADeterminar != null) {
                    val fechasValidas = withContext(Dispatchers.IO) {
                        api.validarFechas(usuarioId, viajeADeterminar.fechaHoraSalida ?: "", viajeADeterminar.fechaHoraLlegada ?: "")
                    }
                    if (!fechasValidas) {
                        onError("Conflicto de horario: Tienes otro viaje en este mismo horario.")
                        return@launch
                    }
                }

                val exito = withContext(Dispatchers.IO) { api.agregarPasajero(viajeId, usuarioCif) }
                if (exito) {
                    fetchViajesInternal(api, usuarioId)
                    onExito()
                } else {
                    onError("No se pudo unir al viaje.")
                }
            } catch (e: Exception) {
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarViajesDesdeBackend(usuarioId: Long? = null) {
        val api = apiService ?: return
        viewModelScope.launch {
            if (usuarioId == null && _viajesOtros.value.isNotEmpty()) return@launch
            try {
                fetchViajesInternal(api, usuarioId)
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al cargar datos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchViajesInternal(api: ViajeApiService, usuarioId: Long?) {
        withContext(Dispatchers.IO) {
            if (usuarioId != null) {
                val todosDef = async { api.obtenerTodosLosViajes() }
                val condDef = async { api.obtenerViajesPorConductor(usuarioId) }
                val userDef = async { api.obtenerViajesPorUsuario(usuarioId) }

                val todos = todosDef.await()
                val creados = condDef.await()
                val unidos = userDef.await()

                val misUnidosFiltrados = unidos.filter { it.estadoViaje != EstadoViaje.CANCELADO }
                val misViajesResult = (creados + misUnidosFiltrados).distinctBy { it.id }
                val otrosResult = todos.filter { v ->
                    val esConductor = v.conductor?.id == usuarioId
                    val esPasajero = v.pasajeros.any { it.usuario?.id == usuarioId }
                    !esConductor && !esPasajero && v.estadoViaje != EstadoViaje.CANCELADO && v.estadoViaje != EstadoViaje.FINALIZADO
                }

                withContext(Dispatchers.Main) {
                    _viajes.value = todos
                    _misViajes.value = misViajesResult
                    _viajesOtros.value = otrosResult
                }
            }
        }
    }

    fun cancelarViaje(viajeId: Long, usuarioId: Long, onExito: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exito = withContext(Dispatchers.IO) { api.cancelarViaje(viajeId) }
                if (exito) {
                    fetchViajesInternal(api, usuarioId)
                    onExito()
                } else {
                    onError("No se pudo cancelar el viaje.")
                }
            } catch (e: Exception) {
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun iniciarViaje(viajeId: Long, conductorId: Long, onExito: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Validación local para no iniciar un viaje que ya está en curso
                val viajeActual = _misViajes.value.find { it.id == viajeId }
                if (viajeActual?.estadoViaje == EstadoViaje.EN_CURSO) {
                    onExito()
                    return@launch
                }

                val resultado = withContext(Dispatchers.IO) { api.iniciarViaje(viajeId, conductorId) }
                if (resultado) {
                    fetchViajesInternal(api, conductorId)
                    onExito()
                } else {
                    onError("No se pudo iniciar el viaje.")
                }
            } catch (e: Exception) {
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun finalizarViaje(viajeId: Long, usuarioId: Long, onExito: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exito = withContext(Dispatchers.IO) { api.finalizarViaje(viajeId) }
                if (exito) {
                    fetchViajesInternal(api, usuarioId)
                    onExito()
                } else {
                    onError("No se pudo finalizar el viaje.")
                }
            } catch (e: Exception) {
                onError("Error de conexión.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun obtenerPasajeros(viajeId: Long) {
        val api = apiService ?: return
        viewModelScope.launch {
            try {
                val pasajeros = withContext(Dispatchers.IO) { api.obtenerPasajerosPorViaje(viajeId) }
                _pasajerosViaje.value = pasajeros
            } catch (e: Exception) {
                _pasajerosViaje.value = emptyList()
            }
        }
    }

    fun cancelarParticipacion(viajeId: Long, usuarioId: Long, usuarioCif: String, onExito: () -> Unit = {}) {
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exito = withContext(Dispatchers.IO) { api.cancelarParticipacion(viajeId, usuarioCif) }
                if (exito) fetchViajesInternal(api, usuarioId)
                onExito()
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al cancelar participacion", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun validarNumViajes(usuarioId: Long, onResultado: (Boolean) -> Unit) {
        val api = apiService ?: return
        viewModelScope.launch {
            try {
                val esValido = withContext(Dispatchers.IO) { api.validarNumViajes(usuarioId) }
                onResultado(esValido)
            } catch (e: Exception) {
                onResultado(false)
            }
        }
    }

    fun actualizarOrigen(destino: Destino?) { viaje = viaje.copy(origen = destino) }
    fun actualizarDestino(destino: Destino?) { viaje = viaje.copy(destino = destino) }
    fun actualizarFechaHoraSalida(fecha: String) { viaje = viaje.copy(fechaHoraSalida = fecha) }
    fun actualizarFechaHoraLlegada(fecha: String) { viaje = viaje.copy(fechaHoraLlegada = fecha) }
    fun actualizarNumeroAsientos(numero: Int) { viaje = viaje.copy(numeroAsientosDisponibles = numero) }
    fun actualizarPrecio(precio: Double) { viaje = viaje.copy(precioPorPersona = precio) }
}
