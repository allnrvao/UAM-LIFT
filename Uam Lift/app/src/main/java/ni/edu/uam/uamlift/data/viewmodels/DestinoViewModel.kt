package ni.edu.uam.uamlift.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ni.edu.uam.uamlift.data.models.Destino

class DestinoViewModel : ViewModel() {

    var destino by mutableStateOf(
        Destino(
            nombre = "",
            latitud = null,
            longitud = null,
            universidad = false,
            estado = false
        )
    )
        private set

    fun cargarDestino(destinoBD: Destino) {
        destino = destinoBD
    }

    fun actualizarNombre(nombre: String) {
        destino = destino.copy(nombre = nombre)
    }

    fun actualizarUniversidad(universidad: Boolean) {
        destino = destino.copy(universidad = universidad)
    }

    fun actualizarEstado(estado: Boolean) {
        destino = destino.copy(estado = estado)
    }
}