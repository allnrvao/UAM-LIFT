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
import ni.edu.uam.uamlift.data.dto.EstadisticasUsuario
import ni.edu.uam.uamlift.data.models.Usuario
import ni.edu.uam.uamlift.sesion.ControlSesion
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UsuarioViewModel : ViewModel() {

    var usuario by mutableStateOf(Usuario())
        private set

    var cargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    var estaLogeado by mutableStateOf(false)
        private set

    // Flag que indica que verificarSesion() terminó (con o sin sesión activa).
    var sesionVerificada by mutableStateOf(false)
        private set

    // ── ESTADÍSTICAS ──────────────────────────────────────────────────────────
    var estadisticas by mutableStateOf(EstadisticasUsuario())
        private set

    var cargandoEstadisticas by mutableStateOf(false)
        private set

    // ─────────────────────────────────────────────────────────────────────────
    // Sesión
    // ─────────────────────────────────────────────────────────────────────────

    fun verificarSesion(context: Context) {
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val controlSesion = ControlSesion(appContext)
                val logeado = controlSesion.estarLogeado.first()

                if (logeado) {
                    val correo = controlSesion.obtenerCorreoInstitucional.first()
                    if (correo.isNotEmpty()) {
                        val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCorreo(correo)
                        withContext(Dispatchers.Main) {
                            usuario = usuarioServidor
                            estaLogeado = true
                        }
                        // Cargamos estadísticas justo tras restaurar sesión
                        usuarioServidor.id?.let { cargarEstadisticasInterno(it) }
                    } else {
                        withContext(Dispatchers.Main) { estaLogeado = false }
                    }
                } else {
                    withContext(Dispatchers.Main) { estaLogeado = false }
                }
            } catch (e: Exception) {
                Log.e("SESION_VERIF", "Error al verificar sesión: ${e.localizedMessage}")
                withContext(Dispatchers.Main) { estaLogeado = false }
            } finally {
                withContext(Dispatchers.Main) { sesionVerificada = true }
            }
        }
    }

    fun obtenerUsuarioPorCorreo(correo: String, onResultado: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { cargando = true; mensajeError = null }
            try {
                val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCorreo(correo)
                withContext(Dispatchers.Main) {
                    usuario = usuarioServidor
                    onResultado(true)
                }
                usuarioServidor.id?.let { cargarEstadisticasInterno(it) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mensajeError = "Correo no registrado"
                    onResultado(false)
                }
            } finally {
                withContext(Dispatchers.Main) { cargando = false }
            }
        }
    }

    fun obtenerUsuarioPorCif(cif: String, onResultado: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { cargando = true; mensajeError = null }
            try {
                val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCif(cif)
                withContext(Dispatchers.Main) {
                    usuario = usuarioServidor
                    onResultado(true)
                }
                usuarioServidor.id?.let { cargarEstadisticasInterno(it) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mensajeError = "Usuario no encontrado"
                    onResultado(false)
                }
            } finally {
                withContext(Dispatchers.Main) { cargando = false }
            }
        }
    }

    fun comprobarCorreo(correo: String?, onResultado: (Boolean) -> Unit) {
        if (correo == null) { onResultado(false); return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val respuestaCuerpo = RetrofitClient.usuarioApi.verificarCorreo(correo)
                val correoExiste = respuestaCuerpo.string().trim().toBoolean()
                withContext(Dispatchers.Main) { onResultado(correoExiste) }
            } catch (e: Exception) {
                Log.e("RETROFIT_GET", "Error al verificar correo: ${e.localizedMessage}")
                withContext(Dispatchers.Main) { onResultado(false) }
            }
        }
    }

    fun registrarUsuarioActual(context: Context, onResultado: (Boolean) -> Unit) {
        Log.d("REGISTRO_FLOW", "Registrando usuario actual...")
        if (cargando) return
        cargando = true
        mensajeError = null
        val appContext = context.applicationContext

        comprobarCorreo(usuario.correo) { correoExiste ->
            if (correoExiste) {
                mensajeError = "El correo ya está registrado."
                cargando = false
                onResultado(false)
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val respuestaRegistro = RetrofitClient.usuarioApi.registrarUsuario(usuario)
                        val registroExitoso = respuestaRegistro.string().trim().toBoolean()
                        if (registroExitoso) {
                            guardarSesionLocal(appContext)
                            withContext(Dispatchers.Main) { onResultado(true) }
                        } else {
                            withContext(Dispatchers.Main) {
                                mensajeError = "No se pudo completar el registro. Inténtalo de nuevo."
                                onResultado(false)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("REGISTRO_FLOW", "Error al registrar: ${e.localizedMessage}")
                        withContext(Dispatchers.Main) {
                            mensajeError = "Error de conexión: ${e.localizedMessage}"
                            onResultado(false)
                        }
                    } finally {
                        withContext(Dispatchers.Main) { cargando = false }
                    }
                }
            }
        }
    }

    suspend fun guardarSesionLocal(context: Context) {
        val appContext = context.applicationContext
        withContext(Dispatchers.IO) {
            val controlSesion = ControlSesion(appContext)
            controlSesion.guardarSesion(
                estaLogeado = true,
                correo      = usuario.correo ?: "",
                nombre      = usuario.nombre ?: usuario.nombreUsuario ?: "Estudiante",
                cifUsuario  = usuario.cif ?: ""
            )
        }
        withContext(Dispatchers.Main) { estaLogeado = true }
    }

    fun cerrarSesion(context: Context, onFin: () -> Unit) {
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val controlSesion = ControlSesion(appContext)
                controlSesion.cerrarSesion()
                withContext(Dispatchers.Main) {
                    estaLogeado      = false
                    sesionVerificada = false
                    usuario          = Usuario()
                    estadisticas     = EstadisticasUsuario()
                    onFin()
                }
            } catch (e: Exception) {
                Log.e("CERRAR_SESION", "Error al cerrar sesión: ${e.localizedMessage}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Estadísticas
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Llama al endpoint /api/usuarios/{id}/estadisticas y actualiza [estadisticas].
     * Llamar desde ProfileScreen con LaunchedEffect para refrescar al entrar.
     */
    fun cargarEstadisticas() {
        val id = usuario.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            cargarEstadisticasInterno(id)
        }
    }

    private suspend fun cargarEstadisticasInterno(usuarioId: Long) {
        withContext(Dispatchers.Main) { cargandoEstadisticas = true }
        try {
            val stats = RetrofitClient.usuarioApi.obtenerEstadisticas(usuarioId)
            withContext(Dispatchers.Main) { estadisticas = stats }
        } catch (e: Exception) {
            Log.e("ESTADISTICAS", "Error al cargar estadísticas: ${e.localizedMessage}")
        } finally {
            withContext(Dispatchers.Main) { cargandoEstadisticas = false }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de edición local
    // ─────────────────────────────────────────────────────────────────────────

    fun verificarNombreUsuarioUnico(nombreUsuario: String, onResultado: (Boolean) -> Unit) {
        if (nombreUsuario == usuario.nombreUsuario) { onResultado(true); return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val u = RetrofitClient.usuarioApi.obtenerPorNombreUsuario(nombreUsuario)
                withContext(Dispatchers.Main) { onResultado(u == null || u.nombreUsuario == null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResultado(true) }
            }
        }
    }

    fun verificarCorreoUnico(correo: String, onResultado: (Boolean) -> Unit) {
        if (correo == usuario.correo) { onResultado(true); return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val u = RetrofitClient.usuarioApi.obtenerPorCorreo(correo)
                withContext(Dispatchers.Main) { onResultado(u == null || u.correo == null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResultado(true) }
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
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { cargando = true; mensajeError = null }
            try {
                val usuarioActualizado = usuario.copy(
                    nombre        = nuevoNombre,
                    apellido      = nuevoApellido,
                    nombreUsuario = nuevoNombreUsuario,
                    correo        = nuevoCorreo,
                    // No actualizamos imagenUrl localmente aquí, el servidor lo hará.
                )

                // Preparar imagen para Multipart si existe una ruta nueva
                val imagenPart = nuevaImagenUrl?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("imagen", file.name, requestFile)
                    } else null
                }

                val exito = RetrofitClient.usuarioApi.actualizarUsuario(
                    usuario.cif,
                    usuarioActualizado,
                    imagenPart
                )

                if (exito) {
                    // Refrescamos los datos del servidor para obtener la nueva URL de imagen (Cloudinary/S3/Local)
                    val usuarioServidor = RetrofitClient.usuarioApi.obtenerPorCif(usuario.cif!!)
                    withContext(Dispatchers.Main) { usuario = usuarioServidor }
                    guardarSesionLocal(appContext)
                }
                withContext(Dispatchers.Main) { onResultado(exito) }
            } catch (e: Exception) {
                Log.e("UPDATE_USER", "Error al actualizar perfil: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    mensajeError = "Error al actualizar: ${e.localizedMessage}"
                    onResultado(false)
                }
            } finally {
                withContext(Dispatchers.Main) { cargando = false }
            }
        }
    }

    fun actualizarNombre(nombre: String)               { usuario = usuario.copy(nombre = nombre) }
    fun actualizarApellido(apellido: String)           { usuario = usuario.copy(apellido = apellido) }
    fun actualizarNombreUsuario(nombreUsuario: String) { usuario = usuario.copy(nombreUsuario = nombreUsuario) }
    fun actualizarCorreo(correo: String)               { usuario = usuario.copy(correo = correo) }
    fun actualizarCif(cif: String)                     { usuario = usuario.copy(cif = cif) }
    fun actualizarContrasenia(contrasenia: String)     { usuario = usuario.copy(contrasenia = contrasenia) }
    fun actualizarImagenUrl(url: String)               { usuario = usuario.copy(imagenUrl = url) }
}