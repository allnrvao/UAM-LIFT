package ni.edu.uam.uamlift.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
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
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.viewmodel.UsuarioViewModel
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

    // Inicialización de Estados con los datos del ViewModel
    var nombreActual by remember { mutableStateOf(usuarioActual.nombre ?: "") }
    var nombreUsuarioActual by remember { mutableStateOf(usuarioActual.nombreUsuario ?: "") }
    var apellidoActual by remember { mutableStateOf(usuarioActual.apellido ?: "") }
    var correoActual by remember { mutableStateOf(usuarioActual.correo ?: "") }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher oficial para abrir la galería del teléfono
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
    ) {
        // Encabezado / TopBar Personalizado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color.Black
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

        Spacer(modifier = Modifier.height(24.dp))

        // Contenedor de Foto de Perfil Rediseñado (Simétrico y sin distorsión)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = UAMColor.copy(alpha = 0.1f),
                    onClick = { launcher.launch("image/*") }
                ) {
                    if (imagenUri != null) {
                        AsyncImage(
                            model = imagenUri,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop // Evita el estiramiento de la imagen
                        )
                    } else if (!usuarioActual.imagenUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = File(usuarioActual.imagenUrl!!),
                            contentDescription = "Foto de perfil",
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

                // Botón de Cámara Flotante perfectamente alineado abajo a la derecha
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
                        contentDescription = "Cambiar foto",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Formulario de Datos con Estilo Unificado
        OutlinedTextField(
            value = nombreActual,
            onValueChange = { nombreActual = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UAMColor,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = UAMColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apellidoActual,
            onValueChange = { apellidoActual = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UAMColor,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = UAMColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombreUsuarioActual,
            onValueChange = { nombreUsuarioActual = it },
            label = { Text("Nombre de Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UAMColor,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = UAMColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correoActual,
            onValueChange = { correoActual = it },
            label = { Text("Correo UAM") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UAMColor,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = UAMColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Acción Principal
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = { mostrarDialogo = true }
        ) {
            Text("Guardar cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    // Diálogo de confirmación con Extractor de Ruta Interna
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Guardar cambios") },
            text = { Text("¿Desea guardar los cambios realizados?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                    onClick = {
                        // Si hay una URI de la galería, extraemos su ruta de archivo real
                        imagenUri?.let { uri ->
                            val rutaFisicaGenerada = guardarUriEnAlmacenamientoInterno(context, uri)
                            if (rutaFisicaGenerada != null) {
                                usuarioViewModel.actualizarImagenUrl(rutaFisicaGenerada)
                            }
                        }

                        // Sincronización del resto del estado global
                        usuarioViewModel.actualizarNombre(nombreActual)
                        usuarioViewModel.actualizarApellido(apellidoActual)
                        usuarioViewModel.actualizarCorreo(correoActual)
                        usuarioViewModel.actualizarNombreUsuario(nombreUsuarioActual)

                        mostrarDialogo = false
                        onBack()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogo = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}

/**
 * Procesa la URI virtual devuelta por el GetContent de Android,
 * lee sus bytes en memoria y los clona en un archivo físico reproducible.
 */
fun guardarUriEnAlmacenamientoInterno(context: Context, uri: Uri): String? {
    return try {
        val resolver = context.contentResolver
        val streamEntrada = resolver.openInputStream(uri) ?: return null

        val carpetaFotos = File(context.filesDir, "uam_photos")
        if (!carpetaFotos.exists()) {
            carpetaFotos.mkdirs()
        }

        val archivoSalida = File(carpetaFotos, "perfil_usuario.jpg")
        val streamSalida = FileOutputStream(archivoSalida)

        val buffer = ByteArray(1024)
        var longitud: Int
        while (streamEntrada.read(buffer).also { longitud = it } != -1) {
            streamSalida.write(buffer, 0, longitud)
        }

        streamSalida.flush()
        streamSalida.close()
        streamEntrada.close()

        archivoSalida.absolutePath // Retorna la ruta nativa limpia string
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}