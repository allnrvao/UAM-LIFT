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
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    usuarioViewModel: UsuarioViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val usuarioActual = usuarioViewModel.usuario

    // Estados para los campos
    var nombreActual by remember { mutableStateOf(usuarioActual.nombre ?: "") }
    var apellidoActual by remember { mutableStateOf(usuarioActual.apellido ?: "") }
    var nombreUsuarioActual by remember { mutableStateOf(usuarioActual.nombreUsuario ?: "") }
    var correoActual by remember { mutableStateOf(usuarioActual.correo ?: "") }

    // Estados de validación asíncrona
    var nombreUsuarioUnico by remember { mutableStateOf(true) }
    var correoUnico by remember { mutableStateOf(true) }
    var verificandoUnicidad by remember { mutableStateOf(false) }

    // Estados de UI (Diálogos)
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var mostrarDialogoExito by remember { mutableStateOf(false) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    // Lógica interactiva para el Nombre de Usuario (Te dice exactamente qué falla)
    val errorUsuarioTexto = when {
        nombreUsuarioActual.length < 4 -> "Mínimo 4 caracteres"
        nombreUsuarioActual.contains(" ") -> "No puede contener espacios"
        verificandoUnicidad -> "Verificando disponibilidad..."
        !nombreUsuarioUnico -> "Este usuario ya está ocupado"
        else -> "" // Si está vacío, todo está perfecto
    }

    // Lógica interactiva para el Correo
    val errorCorreoTexto = when {
        !android.util.Patterns.EMAIL_ADDRESS.matcher(correoActual).matches() -> "Formato inválido"
        !correoActual.endsWith("@uamv.edu.ni") -> "Debe ser correo institucional @uamv.edu.ni"
        !correoUnico -> "El correo ya está registrado"
        else -> ""
    }

    // Validaciones booleanas simples
    val nombreUsuarioValido = errorUsuarioTexto.isEmpty()
    val correoValido = errorCorreoTexto.isEmpty()
    val nombreValido = nombreActual.trim().length >= 3
    val apellidoValido = apellidoActual.trim().length >= 3

    // Efecto para verificar unicidad con debounce (No saturar el servidor)
    LaunchedEffect(nombreUsuarioActual, correoActual) {
        if (nombreUsuarioActual != usuarioActual.nombreUsuario || correoActual != usuarioActual.correo) {
            verificandoUnicidad = true
            delay(500) // Debounce de medio segundo

            if (nombreUsuarioActual != usuarioActual.nombreUsuario && nombreUsuarioActual.length >= 4 && !nombreUsuarioActual.contains(" ")) {
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

    // Botón habilitado solo si TODO está perfecto
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
            .padding(24.dp)
            .verticalScroll(rememberScrollState()) // Añadido scroll para pantallas pequeñas
    ) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.Black)
            }
            Text(
                text = "Editar perfil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- FOTO DE PERFIL ---
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = UAMColor.copy(alpha = 0.1f),
                    onClick = { launcher.launch("image/*") }
                ) {
                    if (imagenUri != null) {
                        AsyncImage(model = imagenUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (!usuarioActual.imagenUrl.isNullOrEmpty()) {
                        AsyncImage(model = File(usuarioActual.imagenUrl!!), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = UAMColor, modifier = Modifier.size(36.dp))
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { launcher.launch("image/*") },
                    containerColor = UAMColor,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp).align(Alignment.BottomEnd)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMPOS DE FORMULARIO ---

        // 1. Nombre (BLOQUEADO)
        ValidatableField(
            value = nombreActual,
            onValueChange = {},
            label = "Nombre",
            isValid = true,
            errorText = "",
            readOnly = true // Ya no se puede escribir aquí
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Apellido (BLOQUEADO)
        ValidatableField(
            value = apellidoActual,
            onValueChange = {},
            label = "Apellido",
            isValid = true,
            errorText = "",
            readOnly = true // Ya no se puede escribir aquí
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Nombre de Usuario (INTERACTIVO)
        ValidatableField(
            value = nombreUsuarioActual,
            onValueChange = { nombreUsuarioActual = it },
            label = "Nombre de Usuario",
            isValid = nombreUsuarioValido,
            errorText = errorUsuarioTexto
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Correo (INTERACTIVO)
        ValidatableField(
            value = correoActual,
            onValueChange = { correoActual = it },
            label = "Correo UAM",
            isValid = correoValido,
            errorText = errorCorreoTexto
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN GUARDAR ---
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = if (formularioValido) UAMColor else Color.Gray
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp),
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

    // --- DIÁLOGO 1: CONFIRMAR CAMBIOS ---
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("Confirmar cambios") },
            text = { Text("¿Estás seguro de que deseas actualizar tu perfil con estos datos?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                    onClick = {
                        val pathImagen = imagenUri?.let { guardarUriEnAlmacenamientoInterno(context, it) }

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
                                mostrarDialogoExito = true // Lanza el diálogo bonito de éxito
                            } else {
                                Toast.makeText(context, "Error al actualizar en el servidor", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }

    // --- DIÁLOGO 2: MENSAJE DE ÉXITO (El que te regresa al perfil) ---
    if (mostrarDialogoExito) {
        AlertDialog(
            onDismissRequest = { }, // No se cierra tocando afuera, obliga a darle OK
            icon = {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
            },
            title = { Text("¡Perfil Actualizado!") },
            text = { Text("Tus cambios se han guardado exitosamente.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        mostrarDialogoExito = false
                        onBack() // AQUÍ TE MANDA DE UNA AL PERFIL
                    }
                ) {
                    Text("Continuar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// --- COMPONENTE VALIDATABLE FIELD (CORREGIDO PARA ACEPTAR READONLY) ---
@Composable
fun ValidatableField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isValid: Boolean,
    errorText: String,
    readOnly: Boolean = false // <--- SE AÑADIÓ ESTO PARA QUE NO DE ERROR AL BLOQUEAR
) {
    // Si es readOnly, lo ponemos gris para que el usuario sepa que no se toca
    val colorEstado = if (readOnly) Color.Gray else if (isValid) Color(0xFF4CAF50) else Color.Red

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = !isValid && !readOnly,
        readOnly = readOnly, // <--- SE APLICA AQUÍ
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
        val carpetaFotos = File(context.filesDir, "uam_photos").apply { if (!exists()) mkdirs() }
        val archivoSalida = File(carpetaFotos, "perfil_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivoSalida).use { output ->
            streamEntrada.use { input -> input.copyTo(output) }
        }
        archivoSalida.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}