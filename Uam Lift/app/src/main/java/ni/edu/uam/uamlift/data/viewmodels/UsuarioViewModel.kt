package ni.edu.uam.uamlift.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.models.Usuario
import ni.edu.uam.uamlift.sesion.ControlSesion

class UsuarioViewModel : ViewModel() {

    // Estado que maneja los datos del usuario actual con los nuevos campos
    var usuario by mutableStateOf(Usuario())
        private set

    var cargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    var estaLogeado by mutableStateOf(false)
        private set

    fun verificarSesion(context: Context) {
        val controlSesion = ControlSesion(context)
        viewModelScope.launch {
            estaLogeado = controlSesion.estarLogeado.first()
            if (estaLogeado) {
                val correo = controlSesion.obtenerCorreoInstitucional.first()
                if (correo.isNotEmpty()) {
                    obtenerUsuarioPorCorreo(correo)
                }
            }
        }
    }

    fun obtenerUsuarioPorCif(cif: String, onResultado: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCif(cif)
                usuario = usuarioServidor
                onResultado(true)
            } catch (e: Exception) {
                mensajeError = "Usuario no encontrado"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }

    fun obtenerUsuarioPorCorreo(correo: String, onResultado: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCorreo(correo)
                usuario = usuarioServidor
                onResultado(true)
            } catch (e: Exception) {
                mensajeError = "Correo no registrado"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }

    fun registrarUsuarioActual(context: Context, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val exito = RetrofitClient.usuarioApi.registrarUsuario(usuario)
                if (exito) {
                    val controlSesion = ControlSesion(context)
                    controlSesion.guardarSesion(
                        true,
                        usuario.correo ?: "",
                        usuario.nombreUsuario ?: "",
                        usuario.cif ?: ""
                    )
                    estaLogeado = true
                }
                onResultado(exito)
            } catch (e: Exception) {
                mensajeError = "Error al registrar usuario: ${e.localizedMessage}"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }

    suspend fun guardarSesionLocal(context: Context) {
        val controlSesion = ControlSesion(context)
        controlSesion.guardarSesion(
            true,
            usuario.correo ?: "",
            usuario.nombreUsuario ?: "",
            usuario.cif ?: ""
        )
        estaLogeado = true
    }

    fun cerrarSesion(context: Context, onFin: () -> Unit) {
        viewModelScope.launch {
            val controlSesion = ControlSesion(context)
            controlSesion.cerrarSesion()
            estaLogeado = false
            usuario = Usuario() // Reset a estado inicial vacío
            onFin()
        }
    }

    fun actualizarCif(cif: String) { usuario = usuario.copy(cif = cif) }
    fun actualizarNombreUsuario(nombreUsuario: String) { usuario = usuario.copy(nombreUsuario = nombreUsuario) }
    fun actualizarNombre(nombre: String) { usuario = usuario.copy(nombre = nombre) }
    fun actualizarApellido(apellido: String) { usuario = usuario.copy(apellido = apellido) }
    fun actualizarCorreo(correo: String) { usuario = usuario.copy(correo = correo) }
    fun actualizarImagenUrl(imagenUrl: String) { usuario = usuario.copy(imagenUrl = imagenUrl) }
    fun actualizarContrasenia(contrasenia: String) { usuario = usuario.copy(contrasenia = contrasenia) }
}
