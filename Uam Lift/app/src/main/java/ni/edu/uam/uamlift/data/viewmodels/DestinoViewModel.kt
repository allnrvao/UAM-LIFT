package ni.edu.uam.uamlift.data.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.RetrofitClient
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

    var destinoDefecto by mutableStateOf<Destino?>(null)
        private set

    fun obtenerDestinoDefecto() {
        viewModelScope.launch {
            try {
                destinoDefecto = RetrofitClient.destinoApi.obtenerDestinoDefecto()
            } catch (e: Exception) {
                Log.e("DestinoViewModel", "Error al obtener destino por defecto", e)
            }
        }
    }

    suspend fun agregarDestino(destinoParaAgregar: Destino): Destino? {
        return try {
            RetrofitClient.destinoApi.agregarDestino(destinoParaAgregar)
        } catch (e: Exception) {
            Log.e("DestinoViewModel", "Error al agregar destino", e)
            null
        }
    }

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
