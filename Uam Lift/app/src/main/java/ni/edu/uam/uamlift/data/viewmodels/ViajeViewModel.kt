package ni.edu.uam.uamlift.viewmodel

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
import ni.edu.uam.uamlift.data.api.ViajeApiService
import ni.edu.uam.uamlift.data.models.*

class ViajeViewModel(
    private val apiService: ViajeApiService? = null
) : ViewModel() {

    // Lista de viajes
    private val _viajes = MutableStateFlow<List<Viaje>>(emptyList())
    val viajes: StateFlow<List<Viaje>> = _viajes.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Viaje actual (formulario)
    var viaje by mutableStateOf(Viaje())
        private set

    // Al iniciar el ViewModel se cargan los viajes desde la BD
    init {
        cargarViajesDesdeBackend()
    }

    /**
     * Obtiene todos los viajes del backend
     */
    fun cargarViajesDesdeBackend() {
        viewModelScope.launch {
            Log.d("ViajeViewModel", "cargarViajesDesdeBackend() llamado. apiService = $apiService")

            if (apiService == null) {
                Log.e("ViajeViewModel", "apiService es null, no se pueden cargar los viajes")
                return@launch
            }

            _isLoading.value = true

            try {
                Log.d("ViajeViewModel", "Llamando a apiService.obtenerTodosLosViajes()")
                val resultado = apiService.obtenerTodosLosViajes()
                Log.d("ViajeViewModel", "Viajes obtenidos: ${resultado.size} viajes")
                resultado.forEach { viaje ->
                    Log.d("ViajeViewModel", "Viaje: origen=${viaje.origen?.nombre}, destino=${viaje.destino?.nombre}, conductor=${viaje.conductor?.nombre}")
                }
                _viajes.value = resultado
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al cargar viajes: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crear un viaje
     */
    fun crearViaje(conductorCif: String) {
        viewModelScope.launch {
            try {

                apiService?.crearViaje(conductorCif, viaje)
                    ?: return@launch

                // Recargar todos los viajes desde la base de datos
                cargarViajesDesdeBackend()

                // Limpiar el formulario
                viaje = Viaje()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Unirse a un viaje
     */
    fun unirseAlViaje(
        viajeId: Long,
        usuarioCif: String
    ) {
        viewModelScope.launch {
            try {
                val esExitoso =
                    apiService?.agregarPasajero(viajeId, usuarioCif) ?: false

                if (esExitoso) {
                    cargarViajesDesdeBackend()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Cancelar participación
     */
    fun cancelarParticipacion(
        viajeId: Long,
        usuarioCif: String
    ) {
        viewModelScope.launch {
            try {
                val exito =
                    apiService?.cancelarParticipacion(viajeId, usuarioCif) ?: false

                if (exito) {
                    cargarViajesDesdeBackend()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Finalizar viaje
     */
    fun finalizarViaje(viajeId: Long) {
        viewModelScope.launch {
            try {
                val exito =
                    apiService?.finalizarViaje(viajeId) ?: false

                if (exito) {
                    cargarViajesDesdeBackend()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================
    // Actualización del formulario
    // ==========================

    fun cargarViaje(viajeBD: Viaje) {
        viaje = viajeBD
    }

    fun actualizarOrigen(origen: Destino) {
        viaje = viaje.copy(origen = origen)
    }

    fun actualizarDestino(destino: Destino) {
        viaje = viaje.copy(destino = destino)
    }

    fun actualizarFechaHoraSalida(fecha: String) {
        viaje = viaje.copy(fechaHoraSalida = fecha)
    }

    fun actualizarFechaHoraLlegada(fecha: String) {
        viaje = viaje.copy(fechaHoraLlegada = fecha)
    }

    fun actualizarNumeroAsientos(numero: Int) {
        viaje = viaje.copy(numeroAsientosDisponibles = numero)
    }

    fun actualizarPrecio(precio: Double) {
        viaje = viaje.copy(precioPorPersona = precio)
    }

    fun actualizarConductor(conductor: Usuario) {
        viaje = viaje.copy(conductor = conductor)
    }

    fun actualizarEstadoViaje(estado: EstadoViaje) {
        viaje = viaje.copy(estadoViaje = estado)
    }

    fun actualizarPasajeros(pasajeros: List<ViajeUsuario>) {
        viaje = viaje.copy(pasajeros = pasajeros)
    }
}