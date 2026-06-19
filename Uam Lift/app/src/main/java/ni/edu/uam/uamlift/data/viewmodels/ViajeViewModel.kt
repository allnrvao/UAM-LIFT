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
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.api.ViajeApiService
import ni.edu.uam.uamlift.data.models.*

class ViajeViewModel(
    private val apiService: ViajeApiService? = RetrofitClient.viajeApi
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
                Log.e("ViajeViewModel", "¡El apiService es NULO!")
                return@launch
            }

            _isLoading.value = true

            try {
                Log.d("ViajeViewModel", "Llamando a obtenerTodosLosViajes()...")
                val resultado = apiService.obtenerTodosLosViajes()
                Log.d("ViajeViewModel", "¡Petición exitosa! Cantidad de viajes recibidos: ${resultado.size}")

                resultado.forEach { v ->
                    Log.d("ViajeViewModel", "Viaje: origen=${v.origen?.nombre}, destino=${v.destino?.nombre}, conductor=${v.conductor?.nombre}")
                }
                _viajes.value = resultado
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Hubo un error en la petición:", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Publicar / Crear un viaje
     */
    fun publicarViaje(
        conductorCif: String,
        onExito: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Validar precio mínimo requerido por el backend
                val precioFinal = viaje.precioPorPersona
                if (precioFinal < 1.0) {
                    onError("El precio debe ser al menos de C$ 1")
                    return@launch
                }

                // 2. Mapear el conductor de forma que Spring Boot lo entienda
                val conductorSimulado = Usuario(cif = conductorCif)

                // 3. Modificamos el viaje para inyectarle el conductor y el estado correcto
                val viajeAEnviar = viaje.copy(
                    estadoViaje = EstadoViaje.PROPUESTO,
                    conductor = conductorSimulado
                )

                Log.d("ViajeViewModel", "Enviando viaje: $viajeAEnviar")

                // 4. Enviamos la petición al API
                val nuevoViaje = apiService?.crearViaje(conductorCif, viajeAEnviar)

                if (nuevoViaje != null) {
                    Log.d("ViajeViewModel", "Viaje creado correctamente: $nuevoViaje")
                    cargarViajesDesdeBackend()
                    viaje = Viaje() // Limpiar formulario
                    onExito()
                } else {
                    onError("No se pudo crear el viaje")
                }

            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al crear viaje", e)
                onError(e.message ?: "Error desconocido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Iniciar un viaje activo
     */
    fun iniciarViaje(viajeId: Long) {
        viewModelScope.launch {
            try {
                val respuesta = apiService?.iniciarViaje(viajeId)
                val exito = respuesta?.get("success") ?: respuesta?.get("status") ?: false
                if (exito) {
                    cargarViajesDesdeBackend()
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al iniciar viaje", e)
            }
        }
    }

    /**
     * Unirse a un viaje como pasajero
     */
    fun unirseAlViaje(viajeId: Long, usuarioCif: String) {
        viewModelScope.launch {
            try {
                val respuesta = apiService?.agregarPasajero(viajeId, usuarioCif)
                val esExitoso = respuesta?.get("success") ?: respuesta?.get("status") ?: false
                if (esExitoso) {
                    cargarViajesDesdeBackend()
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al unirse al viaje", e)
            }
        }
    }

    /**
     * Cancelar participación de un pasajero
     */
    fun cancelarParticipacion(viajeId: Long, usuarioCif: String) {
        viewModelScope.launch {
            try {
                val respuesta = apiService?.cancelarParticipacion(viajeId, usuarioCif)
                val exito = respuesta?.get("success") ?: respuesta?.get("status") ?: false
                if (exito) {
                    cargarViajesDesdeBackend()
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al cancelar participación", e)
            }
        }
    }

    /**
     * Finalizar viaje
     */
    fun finalizarViaje(viajeId: Long) {
        viewModelScope.launch {
            try {
                val respuesta = apiService?.finalizarViaje(viajeId)
                val exito = respuesta?.get("success") ?: respuesta?.get("status") ?: false
                if (exito) {
                    cargarViajesDesdeBackend()
                }
            } catch (e: Exception) {
                Log.e("ViajeViewModel", "Error al finalizar el viaje", e)
            }
        }
    }

    // ==========================================
    // Actualización del formulario y propiedades
    // ==========================================

    fun cargarViaje(viajeBD: Viaje) {
        viaje = viajeBD
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
    fun actualizarConductor(conductor: Usuario) { viaje = viaje.copy(conductor = conductor) }
    fun actualizarEstadoViaje(estado: EstadoViaje) { viaje = viaje.copy(estadoViaje = estado) }
    fun actualizarPasajeros(pasajeros: List<ViajeUsuario>) { viaje = viaje.copy(pasajeros = pasajeros) }
}