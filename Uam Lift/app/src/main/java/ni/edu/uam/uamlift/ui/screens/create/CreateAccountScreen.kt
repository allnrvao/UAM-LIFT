package ni.edu.uam.uamlift.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.uamlift.viewmodel.UsuarioViewModel

private val PrimaryColor = Color(0xFF019AA8)

@Composable
fun CreateAccountScreen(
    modifier: Modifier = Modifier,
    usuarioViewModel: UsuarioViewModel,
    onAccountCreated: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "UL", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Crear Cuenta", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text(text = "Únete a la comunidad UAM LIFT", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(40.dp))

            if (usuarioViewModel.mensajeError != null) {
                Text(text = usuarioViewModel.mensajeError!!, color = Color.Red, modifier = Modifier.padding(8.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = usuarioViewModel.usuario.nombre ?: "",
                        onValueChange = { usuarioViewModel.actualizarNombre(it) },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.apellido ?: "",
                        onValueChange = { usuarioViewModel.actualizarApellido(it) },
                        label = { Text("Apellido") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.nombreUsuario ?: "",
                        onValueChange = { usuarioViewModel.actualizarNombreUsuario(it) },
                        label = { Text("Nombre de Usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.correo ?: "",
                        onValueChange = { usuarioViewModel.actualizarCorreo(it) },
                        label = { Text("Correo UAM") },
                        leadingIcon = { Icon(Icons.Default.Mail, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = usuarioViewModel.usuario.correo.isNullOrEmpty() // Bloquear si viene de Google
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.cif ?: "",
                        onValueChange = { usuarioViewModel.actualizarCif(it) },
                        label = { Text("CIF") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.contrasenia ?: "",
                        onValueChange = { usuarioViewModel.actualizarContrasenia(it) },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    if (usuarioViewModel.usuario.contrasenia == confirmPassword) {
                        mostrarDialogo = true 
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryColor)
            ) {
                Text(text = "Crear Cuenta", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBackToLogin) {
                Text(text = "¿Ya tienes cuenta? Inicia sesión", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Confirmar registro") },
            text = { Text("¿Deseas registrarte con estos datos?") },
            confirmButton = {
                Button(
                    onClick = {
                        usuarioViewModel.registrarUsuarioActual(context) { exito ->
                            if (exito) {
                                mostrarDialogo = false
                                onAccountCreated()
                            }
                        }
                    }
                ) {
                    if (usuarioViewModel.cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}
