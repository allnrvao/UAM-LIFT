package ni.edu.uam.uamlift.ui.screens.profile

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
import androidx.compose.ui.unit.dp
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.viewmodel.UsuarioViewModel
import android.net.Uri
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    usuarioViewModel: UsuarioViewModel,
    onBack: () -> Unit = {}
) {
    var usuarioActual = usuarioViewModel.usuario

    var nombreActual by remember {
        mutableStateOf(usuarioActual.nombre ?: "")
    }

    var nombreUsuarioActual by remember {mutableStateOf(usuarioActual.nombreUsuario ?: "")
    }

    var apellidoActual by remember {
        mutableStateOf(usuarioActual.apellido ?: "")
    }

    var correoActual by remember {
        mutableStateOf(usuarioActual.correo ?: "")
    }

    var mostrarDialogo by remember {
        mutableStateOf(false)
    }
    var imagenUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray)
            .padding(30.dp)
    ) {

        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Regresar"
                )
            }

            Text(
                text = "Editar perfil",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Foto de perfil
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                Surface(
                    modifier = Modifier
                        .size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = {
                        launcher.launch("image/*")
                    }
                ) {

                    if (imagenUri != null) {

                        AsyncImage(
                            model = imagenUri,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize()
                        )

                    } else {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                    }
                }

                FloatingActionButton(
                    onClick = {
                        launcher.launch("image/*")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 30.dp, y = 14.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Nombre
        OutlinedTextField(
            value = nombreActual,
            onValueChange = {
                nombreActual = it
            },
            label = {
                Text("Nombre")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Apellido
        OutlinedTextField(
            value = apellidoActual,
            onValueChange = {
                apellidoActual = it
            },
            label = {
                Text("Apellido")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = nombreUsuarioActual,
            onValueChange = {
                nombreUsuarioActual = it
            },
            label = {
                Text("Nombre de Usuario")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Correo
        OutlinedTextField(
            value = correoActual,
            onValueChange = {
                correoActual = it
            },
            label = {
                Text("Correo UAM")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = UAMColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = {
                mostrarDialogo = true
            }
        ) {
            Text("Guardar cambios")
        }
    }

    // Dialogo de confirmación de cambio de datos
    if (mostrarDialogo) {

        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
            },

            title = {
                Text("Guardar cambios")
            },

            text = {
                Text("¿Desea guardar los cambios realizados?")
            },

            confirmButton = {

                Button(
                    onClick = {

                        usuarioViewModel.actualizarNombre(nombreActual)
                        usuarioViewModel.actualizarApellido(apellidoActual)
                        usuarioViewModel.actualizarCorreo(correoActual)
                        usuarioViewModel.actualizarNombreUsuario(nombreUsuarioActual)


                        mostrarDialogo = false

                        // Volver a la pantalla anterior
                        onBack()
                    }
                ) {
                    Text("Sí")
                }

            },

            dismissButton = {

                OutlinedButton(
                    onClick = {
                        mostrarDialogo = false
                    }
                ) {
                    Text("No")
                }

            }
        )
    }
}
