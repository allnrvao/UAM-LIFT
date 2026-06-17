package ni.edu.uam.uamlift.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
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
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel


private val PrimaryColor = Color(0xFF019AA8)
private val SuccessGreen = Color(0xFF4CAF50) // Verde para los checks

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

    // Estados para manejar errores
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorApellido by remember { mutableStateOf<String?>(null) }
    var errorUsuario by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorCif by remember { mutableStateOf<String?>(null) }
    var errorContrasenia by remember { mutableStateOf<String?>(null) }
    var errorConfirmacion by remember { mutableStateOf<String?>(null) }

    // Validaciones en tiempo real para la contraseña
    val passwordActual = usuarioViewModel.usuario.contrasenia ?: ""
    val tieneLongitud = passwordActual.length >= 8
    val tieneMayuscula = passwordActual.any { it.isUpperCase() }
    val tieneMinuscula = passwordActual.any { it.isLowerCase() }
    val tieneNumero = passwordActual.any { it.isDigit() }
    val tieneEspecial = passwordActual.any { !it.isLetterOrDigit() }

    val contraseniaEsSegura = tieneLongitud && tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial

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
                Text(text = usuarioViewModel.mensajeError!!, color = Color(0xFFFFCDD2), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
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
                        onValueChange = {
                            errorNombre = null
                            usuarioViewModel.actualizarNombre(it)
                        },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorNombre != null,
                        supportingText = { if (errorNombre != null) Text(errorNombre!!, color = MaterialTheme.colorScheme.error) }
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.apellido ?: "",
                        onValueChange = {
                            errorApellido = null
                            usuarioViewModel.actualizarApellido(it)
                        },
                        label = { Text("Apellido") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorApellido != null,
                        supportingText = { if (errorApellido != null) Text(errorApellido!!, color = MaterialTheme.colorScheme.error) }
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.nombreUsuario ?: "",
                        onValueChange = {
                            errorUsuario = null
                            usuarioViewModel.actualizarNombreUsuario(it)
                        },
                        label = { Text("Nombre de Usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorUsuario != null,
                        supportingText = { if (errorUsuario != null) Text(errorUsuario!!, color = MaterialTheme.colorScheme.error) }
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.correo ?: "",
                        onValueChange = {
                            errorCorreo = null
                            usuarioViewModel.actualizarCorreo(it)
                        },
                        label = { Text("Correo UAM") },
                        leadingIcon = { Icon(Icons.Default.Mail, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        // ¡AQUÍ ESTABA EL BUG! Se eliminó el "enabled" que bloqueaba el campo
                        isError = errorCorreo != null,
                        supportingText = { if (errorCorreo != null) Text(errorCorreo!!, color = MaterialTheme.colorScheme.error) }
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.cif ?: "",
                        onValueChange = {
                            errorCif = null
                            usuarioViewModel.actualizarCif(it)
                        },
                        label = { Text("CIF") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorCif != null,
                        supportingText = { if (errorCif != null) Text(errorCif!!, color = MaterialTheme.colorScheme.error) }
                    )

                    OutlinedTextField(
                        value = passwordActual,
                        onValueChange = {
                            errorContrasenia = null
                            usuarioViewModel.actualizarContrasenia(it)
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        // Pinta rojo si hay error (vacío) o si empezó a escribir y aún no es segura
                        isError = errorContrasenia != null || (passwordActual.isNotEmpty() && !contraseniaEsSegura),
                        supportingText = { if (errorContrasenia != null) Text(errorContrasenia!!, color = MaterialTheme.colorScheme.error) }
                    )

                    // LISTA DINÁMICA DE REQUISITOS (¡Ahora siempre visible!)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 2.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Tu contraseña debe tener:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        RequisitoContrasenia("Mínimo 8 caracteres", tieneLongitud)
                        RequisitoContrasenia("Una letra mayúscula", tieneMayuscula)
                        RequisitoContrasenia("Una letra minúscula", tieneMinuscula)
                        RequisitoContrasenia("Un número", tieneNumero)
                        RequisitoContrasenia("Un símbolo especial (@#\$%^&+=!_...)", tieneEspecial)
                    }

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            errorConfirmacion = null
                            confirmPassword = it
                        },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorConfirmacion != null,
                        supportingText = { if (errorConfirmacion != null) Text(errorConfirmacion!!, color = MaterialTheme.colorScheme.error) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val u = usuarioViewModel.usuario
                    val dominio = "@uamv.edu.ni"
                    var esValido = true

                    // VALIDACIONES
                    if (u.nombre.isNullOrBlank()) {
                        errorNombre = "El nombre no puede estar vacío"
                        esValido = false
                    }
                    if (u.apellido.isNullOrBlank()) {
                        errorApellido = "El apellido no puede estar vacío"
                        esValido = false
                    }
                    if (u.nombreUsuario.isNullOrBlank()) {
                        errorUsuario = "Ingresa un nombre de usuario"
                        esValido = false
                    }
                    if (u.correo.isNullOrBlank()) {
                        errorCorreo = "El correo es obligatorio"
                        esValido = false
                    } else if (!u.correo!!.lowercase().endsWith(dominio)) {
                        errorCorreo = "Debes usar tu correo $dominio"
                        esValido = false
                    }
                    if (u.cif.isNullOrBlank()) {
                        errorCif = "El CIF es obligatorio"
                        esValido = false
                    }

                    // Validación si la contraseña está vacía o si no cumple todos los checks
                    if (u.contrasenia.isNullOrBlank()) {
                        errorContrasenia = "La contraseña no puede estar vacía"
                        esValido = false
                    } else if (!contraseniaEsSegura) {
                        errorContrasenia = "Aún faltan requisitos en tu contraseña"
                        esValido = false
                    }

                    if (confirmPassword.isBlank()) {
                        errorConfirmacion = "Confirma tu contraseña"
                        esValido = false
                    } else if (u.contrasenia != confirmPassword) {
                        errorConfirmacion = "Las contraseñas no coinciden"
                        esValido = false
                    }

                    // Verifica que todo el formulario esté perfecto
                    if (esValido && contraseniaEsSegura) {
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
            onDismissRequest = { if (!usuarioViewModel.cargando) mostrarDialogo = false },
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
                    },
                    enabled = !usuarioViewModel.cargando
                ) {
                    if (usuarioViewModel.cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Confirmar")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarDialogo = false },
                    enabled = !usuarioViewModel.cargando
                ) { Text("Cancelar") }
            }
        )
    }
}

// COMPONENTE EXTRA PARA PINTAR LOS CHECKS VERDES O GRISES
@Composable
fun RequisitoContrasenia(texto: String, cumplido: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (cumplido) Icons.Default.Check else Icons.Default.Clear,
            contentDescription = null,
            tint = if (cumplido) SuccessGreen else Color.LightGray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            color = if (cumplido) SuccessGreen else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (cumplido) FontWeight.Medium else FontWeight.Normal
        )
    }
}