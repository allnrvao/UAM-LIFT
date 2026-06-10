package ni.edu.uam.uamlift.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ni.edu.uam.uamlift.data.models.Usuario

class UsuarioViewModel : ViewModel() {

    var usuario by mutableStateOf(Usuario())
        private set

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
}