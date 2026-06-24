package ni.edu.uam.uamlift.data.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                cargando = true
                mensajeError = null
            }
            try {
                val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCorreo(correo)
                withContext(Dispatchers.Main) {
                    usuario = usuarioServidor
                    onResultado(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mensajeError = "Correo no registrado"
                    onResultado(false)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    cargando = false
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
                withContext(Dispatchers.Main) {
                    cargando = false
                }
            }
        }
    }

    /**
     * 🌟 CORREGIDO: En lugar de usar un endpoint inexistente, usamos obtenerPorCorreo.
     * Si el servidor devuelve un usuario válido, significa que el correo ya existe.
     */
    fun comprobarCorreo(correo: String?, onResultado: (Boolean) -> Unit) {
        if (correo.isNullOrEmpty()) {
            onResultado(false)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val usuarioExistente = RetrofitClient.usuarioApi.obtenerPorCorreo(correo)
                withContext(Dispatchers.Main) {
                    // Si el objeto no es nulo y tiene datos reales, el correo ya está registrado
                    onResultado(usuarioExistente.correo != null)
                }
            } catch (e: Exception) {
                Log.d("RETROFIT_GET", "El correo no existe en el sistema (comportamiento esperado)")
                withContext(Dispatchers.Main) {
                    onResultado(false)
                }
            }
        }
    }

    /**
     * 🌟 CORREGIDO: Adaptado para leer el Boolean directo que devuelve registrarUsuario()
     */
    fun registrarUsuarioActual(context: Context, onResultado: (Boolean) -> Unit) {
        Log.d("REGISTRO_FLOW", "Registrando usuario actual...")
        if (cargando) return

        cargando = true
        mensajeError = null

        comprobarCorreo(usuario.correo) { correoExiste ->
            if (correoExiste) {
                Log.d("REGISTRO_FLOW", "Correo ya registrado.")
                mensajeError = "El correo ya está registrado."
                cargando = false
                onResultado(false)
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        // 🌟 Tu interfaz devuelve directamente un Boolean aquí:
                        val registroExitoso = RetrofitClient.usuarioApi.registrarUsuario(usuario)
                        Log.d("REGISTRO_FLOW", "Registro exitoso según servidor: $registroExitoso")

                        if (registroExitoso) {
                            Log.d("REGISTRO_FLOW", "Guardando sesión local...")
                            guardarSesionLocal(context)
                            withContext(Dispatchers.Main) {
                                onResultado(true)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                mensajeError = "No se pudo completar el registro. Inténtalo de nuevo."
                                onResultado(false)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("REGISTRO_FLOW", "Error al registrar", e)
                        withContext(Dispatchers.Main) {
                            mensajeError = "Error de conexión: ${e.localizedMessage}"
                            onResultado(false)
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            cargando = false
                        }
                    }
                }
            }
        }
    }

    fun comprobarCorreo(correo: String?, onResultado: (Boolean) -> Unit) {
        if (correo == null) {
            onResultado(false)
            return
        }

        viewModelScope.launch {
            try {
                val respuestaCuerpo = RetrofitClient.usuarioApi.verificarCorreo(correo)

                val textoPlano = respuestaCuerpo.string().trim()

                val correoExiste = textoPlano.toBoolean()

                withContext(Dispatchers.Main) {
                    onResultado(correoExiste)
                }

            } catch (e: Exception) {
                Log.e("RETROFIT_GET", "Error al verificar correo: ${e.localizedMessage}")

                withContext(Dispatchers.Main) {
                    onResultado(false)
                }
            }
        }
    }

    fun registrarUsuarioActual(context: Context, onResultado: (Boolean) -> Unit) {
        Log.d("REGISTRO_FLOW", "Registrando usuario actual...")
        if (cargando) return

        cargando = true
        mensajeError = null

        comprobarCorreo(usuario.correo) { correoExiste ->

            if (correoExiste) {
                Log.d("REGISTRO_FLOW", "Correo ya registrado.")
                mensajeError = "El correo ya está registrado."
                cargando = false
                onResultado(false)
            } else {
                viewModelScope.launch {
                    try {
                        val respuestaRegistro = RetrofitClient.usuarioApi.registrarUsuario(usuario)
                        val texto = respuestaRegistro.string().trim()
                        Log.d("REGISTRO_FLOW", "Respuesta del servidor: $texto")
                        val registroExitoso = texto.toBoolean()
                        Log.d("REGISTRO_FLOW", "Registro exitoso: $registroExitoso")

                        if (registroExitoso) {
                            Log.d("REGISTRO_FLOW", "Registro exitoso. Guardando sesión local...")
                            guardarSesionLocal(context)
                            withContext(Dispatchers.Main) {
                                onResultado(true)
                            }
                        } else {
                            mensajeError = "No se pudo completar el registro. Inténtalo de nuevo."
                            withContext(Dispatchers.Main) {
                                onResultado(false)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("REGISTRO_FLOW", "Error al registrar: ${e.localizedMessage}")
                        mensajeError = "Error de conexión: ${e.localizedMessage}"
                        withContext(Dispatchers.Main) {
                            onResultado(false)
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            cargando = false
                        }
                    }
                }
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
