package ni.edu.uam.uamlift.data.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.api.ViajeApiService
import ni.edu.uam.uamlift.data.models.*
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.enums.EstadoViaje

class ViajeViewModel(
    private val apiService: ViajeApiService? = RetrofitClient.viajeApi
) : ViewModel() {

    private val _viajes = MutableStateFlow<List<Viaje>>(emptyList())
    val viajes: StateFlow<List<Viaje>> = _viajes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var viaje by mutableStateOf(Viaje())
        private set

    init {
        cargarViajesDesdeBackend()
    }

    fun cargarViajesDesdeBackend() {
        viewModelScope.launch {
            if (apiService == null) return@launch
            _isLoading.value = true
            try {
                val resultado = apiService.obtenerTodosLosViajes()
                _viajes.value = resultado
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun publicarViaje(conductorCif: String, onExito: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                viaje = viaje.copy(estadoViaje = EstadoViaje.PROPUESTO)
                val exito = apiService?.crearViaje(conductorCif, viaje) ?: false
                if (exito) {
                    cargarViajesDesdeBackend()
                    onExito()
                    viaje = Viaje() 
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun iniciarViaje(viajeId: Long) {
        viewModelScope.launch {
            try {
                val exito = apiService?.iniciarViaje(viajeId) ?: false
                if (exito) {
                    cargarViajesDesdeBackend()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unirseAlViaje(viajeId: Long, usuarioCif: String) {
        viewModelScope.launch {
            try {
                val esExitoso = apiService?.agregarPasajero(viajeId, usuarioCif) ?: false
                if (esExitoso) {
                    cargarViajesDesdeBackend()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarOrigen(nombre: String, lat: Double?, lng: Double?, esUam: Boolean = false) {
        viaje = viaje.copy(origen = Destino(nombre = nombre, latitud = lat, longitud = lng, universidad = esUam))
    }

    fun actualizarDestino(nombre: String, lat: Double?, lng: Double?, esUam: Boolean = false) {
        viaje = viaje.copy(destino = Destino(nombre = nombre, latitud = lat, longitud = lng, universidad = esUam))
    }

    fun actualizarFechaHoraSalida(fecha: String) { viaje = viaje.copy(fechaHoraSalida = fecha) }
    fun actualizarFechaHoraLlegada(fecha: String) { viaje = viaje.copy(fechaHoraLlegada = fecha) }
    fun actualizarNumeroAsientos(numero: Int) { viaje = viaje.copy(numeroAsientosDisponibles = numero) }
    fun actualizarPrecio(precio: Double) { viaje = viaje.copy(precioPorPersona = precio) }
}
