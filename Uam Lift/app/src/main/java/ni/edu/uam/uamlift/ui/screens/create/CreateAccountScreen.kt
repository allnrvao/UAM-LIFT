package ni.edu.uam.uamlift.ui.screens.create

import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.validator.SesionGoogle


private val PrimaryColor = Color(0xFF019AA8)
private val SuccessGreen = Color(0xFF4CAF50)

@Composable
fun CreateAccountScreen(
    modifier: Modifier = Modifier,
    usuarioViewModel: UsuarioViewModel,
    onAccountCreated: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val sesionGoogle: SesionGoogle = viewModel()

    // Usamos rememberSaveable para persistir datos ante rotaciones
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    // Estados para alertas de correo duplicado
    var mostrarAlertaExistente by rememberSaveable { mutableStateOf(false) }
    var mensajeAlerta by rememberSaveable { mutableStateOf("") }

    // Estados para manejar errores de validación local
    var errorNombre by rememberSaveable { mutableStateOf<String?>(null) }
    var errorApellido by rememberSaveable { mutableStateOf<String?>(null) }
    var errorUsuario by rememberSaveable { mutableStateOf<String?>(null) }
    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorCif by rememberSaveable { mutableStateOf<String?>(null) }
    var errorContrasenia by rememberSaveable { mutableStateOf<String?>(null) }
    var errorConfirmacion by rememberSaveable { mutableStateOf<String?>(null) }

    val passwordActual = usuarioViewModel.usuario.contrasenia ?: ""
    val tieneLongitud = passwordActual.length >= 8
    val tieneMayuscula = passwordActual.any { it.isUpperCase() }
    val tieneMinuscula = passwordActual.any { it.isLowerCase() }
    val tieneNumero = passwordActual.any { it.isDigit() }
    val tieneEspecial = passwordActual.any { !it.isLetterOrDigit() }

    val contraseniaEsSegura = tieneLongitud && tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize().background(PrimaryColor)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 24.dp, vertical = 40.dp),
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

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de Error Global del Backend
            usuarioViewModel.mensajeError?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFFDC2626),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // --- SECCIÓN DE CORREO ---
                    if (usuarioViewModel.usuario.correo.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                sesionGoogle.iniciarSesion(context) { correo ->
                                    if (correo != null) {
                                        usuarioViewModel.comprobarCorreo(correo) { existe ->
                                            if (existe) {
                                                mensajeAlerta = "Este correo ($correo) ya está registrado en UAM LIFT. Por favor, inicia sesión."
                                                mostrarAlertaExistente = true
                                            } else {
                                                usuarioViewModel.actualizarCorreo(correo)
                                                errorCorreo = null
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = !usuarioViewModel.cargando,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                        ) {
                            if (usuarioViewModel.cargando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryColor)
                            } else {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = ni.edu.uam.uamlift.R.drawable.ic_google),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Vincular con Google", fontWeight = FontWeight.Bold)
                            }
                        }
                        if (errorCorreo != null) {
                            Text(errorCorreo!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp))
                        }
                    } else {

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Correo institucional verificado", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    Text(usuarioViewModel.usuario.correo!!, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    // --- RESTO DE CAMPOS ---
                    val textFieldColors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.nombre ?: "",
                        onValueChange = { errorNombre = null; usuarioViewModel.actualizarNombre(it) },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorNombre != null,
                        supportingText = { if (errorNombre != null) Text(errorNombre!!, color = MaterialTheme.colorScheme.error) },
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.apellido ?: "",
                        onValueChange = { errorApellido = null; usuarioViewModel.actualizarApellido(it) },
                        label = { Text("Apellido") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorApellido != null,
                        supportingText = { if (errorApellido != null) Text(errorApellido!!, color = MaterialTheme.colorScheme.error) },
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.nombreUsuario ?: "",
                        onValueChange = { errorUsuario = null; usuarioViewModel.actualizarNombreUsuario(it) },
                        label = { Text("Nombre de Usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorUsuario != null,
                        supportingText = { if (errorUsuario != null) Text(errorUsuario!!, color = MaterialTheme.colorScheme.error) },
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = usuarioViewModel.usuario.cif ?: "",
                        onValueChange = { errorCif = null; usuarioViewModel.actualizarCif(it) },
                        label = { Text("CIF") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorCif != null,
                        supportingText = { if (errorCif != null) Text(errorCif!!, color = MaterialTheme.colorScheme.error) },
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = passwordActual,
                        onValueChange = { errorContrasenia = null; usuarioViewModel.actualizarContrasenia(it) },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor) },
                        trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorContrasenia != null || (passwordActual.isNotEmpty() && !contraseniaEsSegura),
                        supportingText = { if (errorContrasenia != null) Text(errorContrasenia!!, color = MaterialTheme.colorScheme.error) },
                        colors = textFieldColors
                    )

                    Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Requisitos:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        RequisitoContrasenia("8+ caracteres", tieneLongitud)
                        RequisitoContrasenia("Mayúscula", tieneMayuscula)
                        RequisitoContrasenia("Número", tieneNumero)
                        RequisitoContrasenia("Símbolo", tieneEspecial)
                    }

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { errorConfirmacion = null; confirmPassword = it },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryColor) },
                        trailingIcon = { IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) { Icon(if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = errorConfirmacion != null,
                        supportingText = { if (errorConfirmacion != null) Text(errorConfirmacion!!, color = MaterialTheme.colorScheme.error) },
                        colors = textFieldColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val u = usuarioViewModel.usuario
                    var esValido = true
                    if (u.correo.isNullOrBlank()) { errorCorreo = "Debes vincular tu correo primero"; esValido = false }
                    if (u.nombre.isNullOrBlank()) { errorNombre = "Requerido"; esValido = false }
                    if (u.apellido.isNullOrBlank()) { errorApellido = "Requerido"; esValido = false }
                    if (u.nombreUsuario.isNullOrBlank()) { errorUsuario = "Requerido"; esValido = false }
                    if (u.cif.isNullOrBlank()) { errorCif = "Requerido"; esValido = false }
                    if (u.contrasenia.isNullOrBlank() || !contraseniaEsSegura) { errorContrasenia = "Contraseña no válida"; esValido = false }
                    if (confirmPassword != u.contrasenia) { errorConfirmacion = "No coinciden"; esValido = false }

                    if (esValido) mostrarDialogo = true
                },
                enabled = !usuarioViewModel.cargando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryColor)
            ) {
                Text(text = "Crear Cuenta", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onBackToLogin) {
                Text(text = "¿Ya tienes cuenta? Inicia sesión", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }
    }
    
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { if (!usuarioViewModel.cargando) mostrarDialogo = false },
            title = { Text("Confirmar registro") },
            text = { Text("¿Deseas registrarte con el correo ${usuarioViewModel.usuario.correo}?") },
            confirmButton = {
                Button(onClick = {
                    usuarioViewModel.registrarUsuarioActual(context) { exito ->
                        mostrarDialogo = false
                        if (exito){
                           Log.d("REGISTRO_FLOW", "Registro exitoso")
                           onBackToLogin()
                        } else {
                           
                            if (usuarioViewModel.mensajeError?.contains("registrado", ignoreCase = true) == true) {
                                mensajeAlerta = usuarioViewModel.mensajeError!!
                                mostrarAlertaExistente = true
                            }
                        }
                    }
                }, enabled = !usuarioViewModel.cargando) {
                    if (usuarioViewModel.cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White) else Text("Confirmar")
                }
            },
            dismissButton = { OutlinedButton(onClick = { mostrarDialogo = false }, enabled = !usuarioViewModel.cargando) { Text("Cancelar") } }
        )
    }

    // Alerta de Usuario/Correo Existente
    if (mostrarAlertaExistente) {
        AlertDialog(
            onDismissRequest = { mostrarAlertaExistente = false },
            title = { Text("Cuenta existente") },
            text = { Text(mensajeAlerta) },
            confirmButton = {
                Button(onClick = { mostrarAlertaExistente = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun RequisitoContrasenia(texto: String, cumplido: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (cumplido) Icons.Default.Check else Icons.Default.Clear,
            contentDescription = null,
            tint = if (cumplido) SuccessGreen else Color.LightGray,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = texto, color = if (cumplido) SuccessGreen else Color.Gray, fontSize = 11.sp)
    }
}
