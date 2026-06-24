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

                // Validación modificada para la fecha y hora actual
                if (!esFechaHoraValida(viaje.fechaHoraSalida)) {
                    onError("El viaje debe ser para hoy o un día futuro, y la hora debe ser posterior a la actual.")
                    return@launch
                }

                val limiteValido = withContext(Dispatchers.IO) {
                    api.validarNumViajes(usuarioId)
                }
                if (!limiteValido) {
                    onError("Has alcanzado el límite de 2 viajes permitidos.")
                    return@launch
                }

                val fechasValidas = withContext(Dispatchers.IO) {
                    api.validarFechas(
                        usuarioId,
                        viaje.fechaHoraSalida ?: "",
                        viaje.fechaHoraLlegada ?: ""
                    )
                }
                if (!fechasValidas) {
                    onError("Conflicto de horarios: Ya tienes un viaje en este rango de tiempo.")
                    return@launch
                }

                val conductorSimulado = Usuario(cif = conductorCif)
                val viajeAEnviar = viaje.copy(
                    estadoViaje = EstadoViaje.PROPUESTO,
                    conductor = conductorSimulado
                )

                val nuevoViaje = withContext(Dispatchers.IO) {
                    api.crearViaje(conductorCif, viajeAEnviar)
                }

                if (nuevoViaje != null) {
                    fetchViajesInternal(api, usuarioId)
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

    /**
     * Valida que la fecha provista sea de hoy o posterior,
     * y si es hoy, que la hora sea estrictamente posterior a la actual.
     */
    private fun esFechaHoraValida(fechaStr: String?): Boolean {
        if (fechaStr.isNullOrBlank()) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val fechaViaje = sdf.parse(fechaStr) ?: return false
            val ahora = Calendar.getInstance().time

            // Al usar .after(ahora) valida milisegundo a milisegundo.
            // Si el viaje es hoy pero en el futuro, o si es un día de mañana en adelante, retornará true.
            fechaViaje.after(ahora)
        } catch (e: Exception) {
            false
        }
    }

    fun cargarViajesDesdeBackend(usuarioId: Long? = null) {
        val api = apiService ?: return
        viewModelScope.launch {
            if (usuarioId == null && _viajesOtros.value.isNotEmpty()) return@launch

            val tieneDatos = _viajesOtros.value.isNotEmpty() || _misViajes.value.isNotEmpty()
            if (!tieneDatos) _isLoading.value = true

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

                val misUnidosFiltrados = unidos.filter { v ->
                    v.estadoViaje != EstadoViaje.CANCELADO
                }
                val misViajesResult = (creados + misUnidosFiltrados).distinctBy { it.id }

                val otrosResult = todos.filter { v ->
                    val esConductor = v.conductor?.id == usuarioId
                    val esPasajero = v.pasajeros.any { p -> p.usuario?.id == usuarioId }

                    !esConductor && !esPasajero &&
                            v.estadoViaje != EstadoViaje.CANCELADO
                }

                withContext(Dispatchers.Main) {
                    _viajes.value = todos
                    _misViajes.value = misViajesResult
                    _viajesOtros.value = otrosResult
                }
            } else {
                val todos = api.obtenerTodosLosViajes()
                val otrosResult = todos.filter { v ->
                    v.estadoViaje != EstadoViaje.CANCELADO
                }
                withContext(Dispatchers.Main) {
                    _viajesOtros.value = otrosResult
                    _misViajes.value = emptyList()
                }
            }
        }
    }

    fun cancelarViaje(viajeId: Long, usuarioId: Long, onExito: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val v = _misViajes.value.find { it.id == viajeId }
                if (v != null && (v.estadoViaje == EstadoViaje.EN_CURSO || v.estadoViaje == EstadoViaje.FINALIZADO)) {
                    onError("El viaje ya ha iniciado o finalizado y no se puede cancelar.")
                    return@launch
                }
                val exito = withContext(Dispatchers.IO) {
                    api.cancelarViaje(viajeId)
                }
                if (exito) {
                    fetchViajesInternal(api, usuarioId)
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
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resultado = withContext(Dispatchers.IO) {
                    api.iniciarViaje(viajeId)
                }
                if (resultado) {
                    fetchViajesInternal(api, usuarioId)
                    onExito()
                } else {
                    onError("No se pudo iniciar el viaje en el servidor.")
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al iniciar viaje", e)
                onError("Error de conexión con el servidor.")
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
                val exito = withContext(Dispatchers.IO) {
                    api.finalizarViaje(viajeId)
                }
                if (exito) {
                    fetchViajesInternal(api, usuarioId)
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
        val api = apiService ?: return
        viewModelScope.launch {
            try {
                val pasajeros = withContext(Dispatchers.IO) {
                    api.obtenerPasajerosPorViaje(viajeId)
                }
                _pasajerosViaje.value = pasajeros
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al obtener pasajeros", e)
                _pasajerosViaje.value = emptyList()
            }
        }
    }

    fun validarNumViajes(usuarioId: Long, onExito: (Boolean) -> Unit = {}) {
        val api = apiService ?: return
        viewModelScope.launch {
            val valido = withContext(Dispatchers.IO) {
                api.validarNumViajes(usuarioId)
            }
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
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exito = withContext(Dispatchers.IO) {
                    api.agregarPasajero(viajeId, usuarioCif)
                }
                if (exito) {
                    fetchViajesInternal(api, usuarioId)
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
        val api = apiService ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val exito = withContext(Dispatchers.IO) {
                    api.cancelarParticipacion(viajeId, usuarioCif)
                }
                if (exito) {
                    fetchViajesInternal(api, usuarioId)
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
    fun actualizarCarro(carro: Carro?) { viaje = viaje.copy(carro = carro) }
    fun actualizarNumeroAsientos(asientos: Int) { viaje = viaje.copy(numeroAsientosDisponibles = asientos) }
    fun actualizarPrecio(precio: Double) { viaje = viaje.copy(precioPorPersona = precio) }
}