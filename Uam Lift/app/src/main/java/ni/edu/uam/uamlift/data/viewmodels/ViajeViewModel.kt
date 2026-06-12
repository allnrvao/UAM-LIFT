package ni.edu.uam.uamlift.viewmodel

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

class ViajeViewModel(
    // Si usas inyección de dependencias o lo instancias, pasamos el servicio aquí
    private val apiService: ViajeApiService? = null
) : ViewModel() {

    // --- NUEVO: Estados para el listado del Home ---
    private val _viajes = MutableStateFlow<List<Viaje>>(emptyList())
    val viajes: StateFlow<List<Viaje>> = _viajes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- Tu lógica original para crear/editar un viaje ---
    var viaje by mutableStateOf(Viaje())
        private set
    //Este init es es unicamente para probar la funcionalidad de los ride card
    init {
        _viajes.value = listOf(
            Viaje(
                id = 1L,
                conductor = Usuario(nombre = "Luis Casco"),
                origen = Destino(nombre = "Metrocentro, Managua"),
                destino = Destino(nombre = "UAM Campus Central"),
                fechaHoraSalida = "2026-06-15T09:00:00",
                numeroAsientosDisponibles = 3,
                precioPorPersona = 60.0
            ),
            Viaje(
                id = 2L,
                conductor = Usuario(nombre = "Fernando Gomez"),
                origen = Destino(nombre = "Granada, Granada"),
                destino = Destino(nombre = "UAM Campus Central"),
                fechaHoraSalida = "2026-06-15T13:00:00",
                numeroAsientosDisponibles = 4,
                precioPorPersona = 40.0
            )
        )
    }
    //
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

    /**
     * Método para reservar un asiento. Resuelve el error "Unresolved reference 'unirseAlViaje'"
     */
    fun unirseAlViaje(viajeId: Long, usuarioCif: String) {
        viewModelScope.launch {
            try {
                val esExitoso = apiService?.agregarPasajero(viajeId, usuarioCif) ?: false
                if (esExitoso) {
                    cargarViajesDesdeBackend() // Refresca la lista automáticamente
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Tus funciones originales de actualización de formulario ---
    fun cargarViaje(viajeBD: Viaje) { viaje = viajeBD }
    fun actualizarOrigen(origen: Destino) { viaje = viaje.copy(origen = origen) }
    fun actualizarDestino(destino: Destino) { viaje = viaje.copy(destino = destino) }
    fun actualizarFechaHoraSalida(fecha: String) { viaje = viaje.copy(fechaHoraSalida = fecha) }
    fun actualizarFechaHoraLlegada(fecha: String) { viaje = viaje.copy(fechaHoraLlegada = fecha) }
    fun actualizarNumeroAsientos(numero: Int) { viaje = viaje.copy(numeroAsientosDisponibles = numero) }
    fun actualizarPrecio(precio: Double) { viaje = viaje.copy(precioPorPersona = precio) }
    fun actualizarConductor(conductor: Usuario) { viaje = viaje.copy(conductor = conductor) }
    fun actualizarEstadoViaje(estado: EstadoViaje) { viaje = viaje.copy(estadoViaje = estado) }
    fun actualizarPasajeros(pasajeros: List<ViajeUsuario>) { viaje = viaje.copy(pasajeros = pasajeros) }
}