package ni.edu.uam.uamlift.data.viewmodels

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

    fun registrarUsuarioActual(context: Context, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val exito = RetrofitClient.usuarioApi.registrarUsuario(usuario)
                if (exito) {
                    guardarSesionLocal(context)
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
            usuario = Usuario()
            onFin()
        }
    }

    fun verificarNombreUsuarioUnico(nombreUsuario: String, onResultado: (Boolean) -> Unit) {
        if (nombreUsuario == usuario.nombreUsuario) {
            onResultado(true)
            return
        }
        viewModelScope.launch {
            try {
                val u = RetrofitClient.usuarioApi.obtenerPorNombreUsuario(nombreUsuario)
                onResultado(u == null || u.nombreUsuario == null)
            } catch (e: Exception) {
                onResultado(true)
            }
        }
    }

    fun verificarCorreoUnico(correo: String, onResultado: (Boolean) -> Unit) {
        if (correo == usuario.correo) {
            onResultado(true)
            return
        }
        viewModelScope.launch {
            try {
                val u = RetrofitClient.usuarioApi.obtenerPorCorreo(correo)
                onResultado(u == null || u.correo == null)
            } catch (e: Exception) {
                onResultado(true)
            }
        }
    }

    fun actualizarPerfil(
        nuevoNombre: String,
        nuevoApellido: String,
        nuevoNombreUsuario: String,
        nuevoCorreo: String,
        nuevaImagenUrl: String?,
        context: Context,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val usuarioActualizado = usuario.copy(
                    nombre = nuevoNombre,
                    apellido = nuevoApellido,
                    nombreUsuario = nuevoNombreUsuario,
                    correo = nuevoCorreo,
                    imagenUrl = nuevaImagenUrl ?: usuario.imagenUrl
                )
                
                val exito = RetrofitClient.usuarioApi.actualizarUsuario(usuario.cif, usuarioActualizado)
                if (exito) {
                    usuario = usuarioActualizado
                    guardarSesionLocal(context)
                }
                onResultado(exito)
            } catch (e: Exception) {
                mensajeError = "Error al actualizar: ${e.localizedMessage}"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }

    fun actualizarNombre(nombre: String) { usuario = usuario.copy(nombre = nombre) }
    fun actualizarApellido(apellido: String) { usuario = usuario.copy(apellido = apellido) }
    fun actualizarNombreUsuario(nombreUsuario: String) { usuario = usuario.copy(nombreUsuario = nombreUsuario) }
    fun actualizarCorreo(correo: String) { usuario = usuario.copy(correo = correo) }
    fun actualizarCif(cif: String) { usuario = usuario.copy(cif = cif) }
    fun actualizarContrasenia(contrasenia: String) { usuario = usuario.copy(contrasenia = contrasenia) }
    fun actualizarImagenUrl(url: String) { usuario = usuario.copy(imagenUrl = url) }
}
