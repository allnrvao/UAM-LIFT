package ni.edu.uam.uamlift.data.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.models.Carro
import ni.edu.uam.uamlift.data.models.Usuario

class CarroViewModel : ViewModel() {

    var cargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    var listaCarros by mutableStateOf<List<Carro>>(emptyList())
        private set

    fun crearCarro(
        placa: String,
        marca: String,
        modelo: String,
        color: String,
        propietario: Usuario,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val nuevoCarro = Carro(
                    placa = placa.uppercase().trim(),
                    marca = marca,
                    modelo = modelo,
                    color = color,
                    propietario = propietario
                )
                val response = RetrofitClient.carroApi.createCarro(nuevoCarro)
                if (response.isSuccessful) {
                    obtenerCarrosPorUsuario(propietario.id ?: 0)
                    onResultado(true)
                } else {
                    mensajeError = "No se pudo registrar el vehículo. Verifica los datos."
                    onResultado(false)
                }
            } catch (e: Exception) {
                mensajeError = "Error de conexión: ${e.localizedMessage}"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }

    fun verificarPlacaUnica(placa: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.carroApi.obtenerPorPlaca(placa.uppercase().trim())
                if (response.isSuccessful) {
                    // Si el carro existe, la placa no es única
                    onResultado(response.body() == null)
                } else if (response.code() == 404) {
                    // Si es 404, significa que no existe, por lo tanto es única
                    onResultado(true)
                } else {
                    onResultado(false)
                }
            } catch (e: Exception) {
                // En caso de error, por seguridad asumimos que no es única o hay error de red
                onResultado(false)
            }
        }
    }

    fun obtenerCarrosPorUsuario(usuarioId: Long) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val response = RetrofitClient.carroApi.getCarrosByUsuarioId(usuarioId)
                if (response.isSuccessful) {
                    listaCarros = response.body() ?: emptyList()
                } else {
                    mensajeError = "Error al obtener carros: ${response.code()}"
                }
            } catch (e: Exception) {
                mensajeError = "Error de conexión: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun eliminarCarro(id: Long, usuarioId: Long) {
        viewModelScope.launch {
            cargando = true
            try {
                val response = RetrofitClient.carroApi.deleteCarro(id)
                if (response.isSuccessful) {
                    obtenerCarrosPorUsuario(usuarioId)
                }
            } catch (e: Exception) {
                mensajeError = "Error al eliminar: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun actualizarCarro(carro: Carro, onResultado: (Boolean) -> Unit) {
        val id = carro.id ?: return
        viewModelScope.launch {
            cargando = true
            try {
                val response = RetrofitClient.carroApi.updateCarro(id, carro)
                if (response.isSuccessful) {
                    obtenerCarrosPorUsuario(carro.propietario.id ?: 0)
                    onResultado(true)
                } else {
                    onResultado(false)
                }
            } catch (e: Exception) {
                mensajeError = "Error al actualizar: ${e.localizedMessage}"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }
}
