package ni.edu.uam.uamlift.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.dto.Ubicacion
import ni.edu.uam.uamlift.repository.ViajeRepository

class UbicacionViewModel(private val repository: ViajeRepository) : ViewModel() {

    private val _ubicacion = MutableStateFlow<Ubicacion?>(null)
    val ubicacion = _ubicacion.asStateFlow()

    private var job: Job? = null

    fun conectar(idViaje: Long) {
        repository.conectar(idViaje)

        // Cancelamos cualquier suscripción previa para evitar duplicidad
        job?.cancel()
        job = viewModelScope.launch {
            repository.ubicaciones.collect {
                _ubicacion.value = it
            }
        }
    }

    fun enviar(
        idViaje: Long,
        lat: Double,
        lng: Double
    ) {
        repository.enviar(idViaje, lat, lng)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
