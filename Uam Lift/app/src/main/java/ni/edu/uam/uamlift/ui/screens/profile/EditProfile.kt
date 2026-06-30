package ni.edu.uam.uamlift.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    usuarioViewModel: UsuarioViewModel,
    // onBack es llamado tanto por el botón "<-- Editar perfil" como por el diálogo de éxito
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val usuarioActual = usuarioViewModel.usuario

    // Usamos rememberSaveable para que los cambios no se pierdan al rotar el teléfono
    var nombreActual by rememberSaveable { mutableStateOf(usuarioActual.nombre ?: "") }
    var apellidoActual by rememberSaveable { mutableStateOf(usuarioActual.apellido ?: "") }
    var nombreUsuarioActual by rememberSaveable { mutableStateOf(usuarioActual.nombreUsuario ?: "") }
    var correoActual by rememberSaveable { mutableStateOf(usuarioActual.correo ?: "") }

    var nombreUsuarioUnico by rememberSaveable { mutableStateOf(true) }
    var correoUnico by rememberSaveable { mutableStateOf(true) }
    var verificandoUnicidad by rememberSaveable { mutableStateOf(false) }

    var mostrarDialogoConfirmacion by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoExito by rememberSaveable { mutableStateOf(false) }
    var imagenUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val errorUsuarioTexto = when {
        nombreUsuarioActual.length < 4 -> "Mínimo 4 caracteres"
        nombreUsuarioActual.contains(" ") -> "No puede contener espacios"
        verificandoUnicidad -> "Verificando disponibilidad..."
        !nombreUsuarioUnico -> "Este usuario ya está ocupado"
        else -> ""
    }

    val errorCorreoTexto = when {
        !android.util.Patterns.EMAIL_ADDRESS.matcher(correoActual).matches() -> "Formato inválido"
        !correoActual.endsWith("@uamv.edu.ni") -> "Debe ser correo institucional @uamv.edu.ni"
        !correoUnico -> "El correo ya está registrado"
        else -> ""
    }

    val nombreUsuarioValido = errorUsuarioTexto.isEmpty()
    val correoValido = errorCorreoTexto.isEmpty()
    val nombreValido = nombreActual.trim().length >= 3
    val apellidoValido = apellidoActual.trim().length >= 3

    LaunchedEffect(nombreUsuarioActual, correoActual) {
        if (nombreUsuarioActual != usuarioActual.nombreUsuario || correoActual != usuarioActual.correo) {
            verificandoUnicidad = true
            delay(500)

            if (nombreUsuarioActual != usuarioActual.nombreUsuario &&
                nombreUsuarioActual.length >= 4 && !nombreUsuarioActual.contains(" ")
            ) {
                usuarioViewModel.verificarNombreUsuarioUnico(nombreUsuarioActual) { unico ->
                    nombreUsuarioUnico = unico
                }
            } else {
                nombreUsuarioUnico = true
            }

            if (correoActual != usuarioActual.correo && correoActual.endsWith("@uamv.edu.ni")) {
                usuarioViewModel.verificarCorreoUnico(correoActual) { unico ->
                    correoUnico = unico
                }
            } else {
                correoUnico = true
            }
            verificandoUnicidad = false
        } else {
            nombreUsuarioUnico = true
            correoUnico = true
        }
    }

    val formularioValido = nombreValido && apellidoValido && correoValido &&
            nombreUsuarioValido && !verificandoUnicidad

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray)
            .verticalScroll(rememberScrollState())
    ) {
        // ── TOP BAR (botón funcional "<- Editar perfil") ──────────────────────
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón que regresa a la pantalla principal de perfil
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar al perfil",
                        tint = UAMColor
                    )
                }
                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── FOTO DE PERFIL ───────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = UAMColor.copy(alpha = 0.1f),
                        onClick = { launcher.launch("image/*") }
                    ) {
                        val model = remember(imagenUri, usuarioActual.imagenUrl) {
                            if (imagenUri != null) {
                                imagenUri
                            } else if (!usuarioActual.imagenUrl.isNullOrEmpty()) {
                                val url = usuarioActual.imagenUrl!!
                                when {
                                    url.startsWith("http") -> url
                                    url.startsWith("C:") || url.contains("uam_photos") -> File(url)
                                    else -> {
                                        val base = RetrofitClient.BASE_URL.trimEnd('/')
                                        val relative = url.trimStart('/')
                                        "$base/$relative"
                                    }
                                }
                            } else null
                        }

                        if (model != null) {
                            AsyncImage(
                                model = model,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = UAMColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = { launcher.launch("image/*") },
                        containerColor = UAMColor,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── CAMPOS ───────────────────────────────────────────────────────

            ValidatableField(
                value = nombreActual,
                onValueChange = {},
                label = "Nombre",
                isValid = true,
                errorText = "",
                readOnly = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            ValidatableField(
                value = apellidoActual,
                onValueChange = {},
                label = "Apellido",
                isValid = true,
                errorText = "",
                readOnly = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            ValidatableField(
                value = nombreUsuarioActual,
                onValueChange = { nombreUsuarioActual = it },
                label = "Nombre de Usuario",
                isValid = nombreUsuarioValido,
                errorText = errorUsuarioTexto
            )
            Spacer(modifier = Modifier.height(16.dp))

            ValidatableField(
                value = correoActual,
                onValueChange = {},
                label = "Correo UAM",
                isValid = true,
                errorText = "",
                readOnly = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            // ── BOTÓN GUARDAR ────────────────────────────────────────────────
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (formularioValido) UAMColor else Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = formularioValido && !usuarioViewModel.cargando,
                onClick = { mostrarDialogoConfirmacion = true }
            ) {
                if (usuarioViewModel.cargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── DIÁLOGO 1: CONFIRMAR ─────────────────────────────────────────────────
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            containerColor = Color.White,
            title = { Text("Confirmar cambios") },
            text = { Text("¿Estás seguro de que deseas actualizar tu perfil con estos datos?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                    onClick = {
                        val pathImagen =
                            imagenUri?.let { guardarUriEnAlmacenamientoInterno(context, it) }
                        usuarioViewModel.actualizarPerfil(
                            nuevoNombre = nombreActual,
                            nuevoApellido = apellidoActual,
                            nuevoNombreUsuario = nombreUsuarioActual,
                            nuevoCorreo = correoActual,
                            nuevaImagenUrl = pathImagen,
                            context = context
                        ) { exito ->
                            mostrarDialogoConfirmacion = false
                            if (exito) {
                                mostrarDialogoExito = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al actualizar en el servidor",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── DIÁLOGO 2: ÉXITO → navega a ProfileScreen ────────────────────────────
    if (mostrarDialogoExito) {
        AlertDialog(
            onDismissRequest = { /* no cerrar tocando afuera */ },
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("¡Perfil Actualizado!") },
            text = { Text("Tus cambios se han guardado exitosamente.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        mostrarDialogoExito = false
                        // Regresa a ProfileScreen (la pantalla principal de perfil)
                        onBack()
                    }
                ) {
                    Text("Continuar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ── Componente ValidatableField (sin cambios) ─────────────────────────────────
@Composable
fun ValidatableField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isValid: Boolean,
    errorText: String,
    readOnly: Boolean = false
) {
    val colorEstado = if (readOnly) Color.Gray
    else if (isValid) Color(0xFF4CAF50)
    else Color.Red

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = !isValid && !readOnly,
        readOnly = readOnly,
        trailingIcon = {
            if (!readOnly) {
                if (isValid) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colorEstado)
                } else {
                    Icon(Icons.Default.Error, contentDescription = null, tint = colorEstado)
                }
            }
        },
        supportingText = {
            if (!isValid && !readOnly && errorText.isNotEmpty()) {
                Text(text = errorText, color = colorEstado)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = if (readOnly) Color.Gray else colorEstado,
            unfocusedBorderColor = if (isValid || readOnly) Color.Gray else Color.Red,
            focusedLabelColor = if (readOnly) Color.Gray else colorEstado,
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red,
            errorSupportingTextColor = Color.Red
        )
    )
}

fun guardarUriEnAlmacenamientoInterno(context: Context, uri: Uri): String? {
    return try {
        val streamEntrada = context.contentResolver.openInputStream(uri) ?: return null
        val carpetaFotos =
            File(context.filesDir, "uam_photos").apply { if (!exists()) mkdirs() }
        val archivoSalida =
            File(carpetaFotos, "perfil_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivoSalida).use { output ->
            streamEntrada.use { input -> input.copyTo(output) }
        }
        archivoSalida.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}