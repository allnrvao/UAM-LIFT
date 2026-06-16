package ni.edu.uam.uamlift.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.models.Usuario

class UsuarioViewModel : ViewModel() {

    // Estado que maneja los datos del usuario actual
    var usuario by mutableStateOf(
        Usuario(cif = "", nombreUsuario = "", nombre = "", apellido = "", correo = "", imagenUrl = "", contrasenia = "")
    )
        private set

    // Estado para controlar si hay una petición de red en progreso
    var cargando by mutableStateOf(false)
        private set

    // Estado para capturar y mostrar errores de red en la interfaz
    var mensajeError by mutableStateOf<String?>(null)
        private set


    fun obtenerUsuarioPorCif(cif: String) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                // Hacemos la petición HTTP a través de Retrofit
                val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCif(cif)
                usuario = usuarioServidor
            } catch (e: Exception) {
                mensajeError = "Error al obtener usuario: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun obtenerUsuarioPorCorreo(correo: String) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCorreo(correo)
                usuario = usuarioServidor
            } catch (e: Exception) {
                mensajeError = "Error al buscar por correo: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun registrarUsuarioActual(onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                // Enviamos el objeto 'usuario' que está actualmente en el estado
                val exito = RetrofitClient.usuarioApi.registrarUsuario(usuario)
                onResultado(exito)
            } catch (e: Exception) {
                mensajeError = "Error al registrar usuario: ${e.localizedMessage}"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }

    fun guardarCambiosUsuario(onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val exito = RetrofitClient.usuarioApi.actualizarUsuario(usuario.cif, usuario)
                onResultado(exito)
            } catch (e: Exception) {
                mensajeError = "Error al actualizar: ${e.localizedMessage}"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }

    fun eliminarUsuarioActual(onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                val exito = RetrofitClient.usuarioApi.eliminarUsuario(usuario.cif)
                if (exito) {
                    // Limpiamos el estado local si se eliminó de la BD
                    usuario = Usuario("", "", "", "", "", "", "")
                }
                onResultado(exito)
            } catch (e: Exception) {
                mensajeError = "Error al eliminar: ${e.localizedMessage}"
                onResultado(false)
            } finally {
                cargando = false
            }
        }
    }


    fun cargarUsuario(usuarioBD: Usuario) {
        usuario = usuarioBD
    }

    fun actualizarCif(cif: String) {
        usuario = usuario.copy(cif = cif)
    }

    fun actualizarNombreUsuario(nombreUsuario: String) {
        usuario = usuario.copy(nombreUsuario = nombreUsuario)
    }

    fun actualizarNombre(nombre: String) {
        usuario = usuario.copy(nombre = nombre)
    }

    fun actualizarApellido(apellido: String) {
        usuario = usuario.copy(apellido = apellido)
    }

    fun actualizarCorreo(correo: String) {
        usuario = usuario.copy(correo = correo)
    }

    fun actualizarImagenUrl(imagenUrl: String) {
        usuario = usuario.copy(imagenUrl = imagenUrl)
    }

    fun actualizarContrasenia(contrasenia: String) {
        usuario = usuario.copy(contrasenia = contrasenia)
    }
}