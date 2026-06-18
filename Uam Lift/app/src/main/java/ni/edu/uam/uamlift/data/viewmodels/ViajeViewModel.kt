<<<<<<< Updated upstream
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
=======
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
>>>>>>> Stashed changes
        }

<<<<<<< Updated upstream
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
=======
        fun cargarViajesDesdeBackend() {
            viewModelScope.launch {
                if (apiService == null) {
                    android.util.Log.d("API_DEBUG", "¡El apiService es NULO!")
                    return@launch
                }
                _isLoading.value = true
                try {
                    android.util.Log.d("API_DEBUG", "Llamando a obtenerTodosLosViajes()...")
                    val resultado = apiService.obtenerTodosLosViajes()

                    // Verificamos el tamaño de la lista que viene del backend
                    android.util.Log.d("API_DEBUG", "¡Petición exitosa! Cantidad de viajes recibidos: ${resultado.size}")

                    _viajes.value = resultado
                } catch (e: Exception) {
                    android.util.Log.e("API_DEBUG", "Hubo un error en la petición:", e)
                } finally {
                    _isLoading.value = false
                }
>>>>>>> Stashed changes
            }
        }

<<<<<<< Updated upstream
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
=======
        fun publicarViaje(
            conductorCif: String,
            onExito: () -> Unit,
            onError: (String) -> Unit = {}
        ) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    // 1. Asegurarnos de que el precio cumpla con el @Min(1) del backend
                    val precioFinal = viaje.precioPorPersona
                    if (precioFinal < 1.0) {
                        onError("El precio debe ser al menos de C$ 1")
                        return@launch
                    }

                    // 2. Mapear el conductor de forma que Spring Boot lo entienda.
                    // Creamos un objeto Usuario simulado que solo lleve el CIF (o ID, según use tu backend)
                    // Asegúrate de que tu clase 'Usuario' en Kotlin tenga una propiedad para el CIF.
                    val conductorSimulado = Usuario(cif = conductorCif)

                    // 3. Modificamos el viaje para inyectarle el conductor y el estado correcto
                    val viajeAEnviar = viaje.copy(
                        estadoViaje = EstadoViaje.PROPUESTO,
                        conductor = conductorSimulado // <-- ¡ESTO EVITARÁ EL NULLABLE = FALSE EN LA BD!
                    )

                    android.util.Log.d("VIAJE_API", "Enviando viaje: $viajeAEnviar")

                    // 4. Enviamos la petición al API
                    val nuevoViaje = apiService?.crearViaje(conductorCif, viajeAEnviar)

                    if (nuevoViaje != null) {
                        android.util.Log.d("VIAJE_API", "Viaje creado correctamente: $nuevoViaje")
                        cargarViajesDesdeBackend()
                        viaje = Viaje() // Limpiar formulario
                        onExito()
                    } else {
                        onError("No se pudo crear el viaje")
                    }

                } catch (e: Exception) {
                    android.util.Log.e("VIAJE_API", "Error al crear viaje", e)
                    onError(e.message ?: "Error desconocido")
                } finally {
                    _isLoading.value = false
                }
>>>>>>> Stashed changes
            }
        }

<<<<<<< Updated upstream
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
=======
        fun iniciarViaje(viajeId: Long) {
            viewModelScope.launch {
                try {
                    val respuesta = apiService?.finalizarViaje(viajeId)
                    val exito = respuesta?.get("success") ?: false
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
                    val respuesta = apiService?.agregarPasajero(viajeId, usuarioCif)
                    val esExitoso = respuesta?.get("success") ?: false
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
>>>>>>> Stashed changes
