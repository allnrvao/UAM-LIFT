package ni.edu.uam.uamlift.data.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.api.ViajeApiService
import ni.edu.uam.uamlift.data.models.*

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
                Log.e("ViajeViewModel", "Error al cargar viajes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun publicarViaje(
        conductorCif: String,
        onExito: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (viaje.precioPorPersona < 1.0) {
                    onError("El precio debe ser al menos de C$ 1")
                    return@launch
                }
                if (viaje.numeroAsientosDisponibles < 1) {
                    onError("Debe haber al menos 1 asiento disponible")
                    return@launch
                }

                val conductorSimulado = Usuario(cif = conductorCif)
                val viajeAEnviar = viaje.copy(
                    estadoViaje = EstadoViaje.PROPUESTO,
                    conductor = conductorSimulado
                )

                Log.d("ViajeViewModel", "Publicando viaje: $viajeAEnviar")
                val nuevoViaje = apiService?.crearViaje(conductorCif, viajeAEnviar)

                if (nuevoViaje != null) {
                    cargarViajesDesdeBackend()
                    viaje = Viaje() 
                    onExito()
                } else {
                    onError("No se pudo completar la publicación")
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error en publicación", e)
                onError(e.localizedMessage ?: "Error desconocido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unirseAlViaje(
        viajeId: Long,
        usuarioCif: String,
        onExito: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService?.agregarPasajero(viajeId, usuarioCif)
                if (response?.get("success") == true) {
                    cargarViajesDesdeBackend()
                    onExito()
                } else {
                    onError("No se pudo unir al viaje")
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al unirse al viaje", e)
                onError(e.localizedMessage ?: "Error desconocido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarOrigen(destino: Destino?) {
        viaje = viaje.copy(origen = destino)
    }

    fun actualizarDestino(destino: Destino?) {
        viaje = viaje.copy(destino = destino)
    }

    fun actualizarFechaHoraSalida(fecha: String) { viaje = viaje.copy(fechaHoraSalida = fecha) }
    fun actualizarFechaHoraLlegada(fecha: String) { viaje = viaje.copy(fechaHoraLlegada = fecha) }
    fun actualizarNumeroAsientos(numero: Int) { viaje = viaje.copy(numeroAsientosDisponibles = numero) }
    fun actualizarPrecio(precio: Double) { viaje = viaje.copy(precioPorPersona = precio) }
}
