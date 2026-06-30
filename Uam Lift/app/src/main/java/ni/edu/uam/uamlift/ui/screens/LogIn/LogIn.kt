package ni.edu.uam.uamlift.ui.screens.LogIn

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.R
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.validator.SesionGoogle

private val PrimaryColor = Color(0xFF019AA8)

@Composable
fun LogIn(
    navController: NavController,
    modifier: Modifier = Modifier,
    usuarioViewModel: UsuarioViewModel,
    onLogin: () -> Unit
){
    // Usamos rememberSaveable para que los datos no se borren al girar el teléfono
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    var errorMessage by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sesionGoogle: SesionGoogle = viewModel()

    var googleVerificado by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    fun manejarLoginGoogle() {
        sesionGoogle.iniciarSesion(context) { correo ->
            if (correo != null) {
                email = correo
                usuarioViewModel.obtenerUsuarioPorCorreo(correo) { existe ->
                    if (existe) {
                        googleVerificado = true
                        showError = false
                    } else {
                        usuarioViewModel.actualizarCorreo(correo)
                        navController.navigate("createAccount")
                    }
                }
            } else {
                errorMessage = "Error en la autenticación con Google o dominio no permitido."
                showError = true
            }
        }
    }

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

            Text(text = "UAM LIFT", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text(text = "Movilidad colaborativa estudiantil", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(32.dp))

            if (showError || usuarioViewModel.mensajeError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = usuarioViewModel.mensajeError ?: errorMessage,
                        color = Color(0xFFDC2626),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!googleVerificado) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (showError) showError = false
                            },
                            label = { Text("Correo o CIF") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = PrimaryColor) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color(0xFFF6F8FA), unfocusedContainerColor = Color(0xFFF6F8FA)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )
                    } else {
                        val nombreGoogle = usuarioViewModel.usuario?.nombre ?: "Estudiante"
                        Text(text = "Hola, $nombreGoogle", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryColor)
                        Text(text = email, fontSize = 14.sp, color = Color.Gray)
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (showError) showError = false
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color(0xFFF6F8FA), unfocusedContainerColor = Color(0xFFF6F8FA)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                    )

                    if (!googleVerificado) {
                        TextButton(onClick = { /* Recuperar */ }, modifier = Modifier.align(Alignment.End)) {
                            Text(text = "¿Olvidaste tu contraseña?", color = PrimaryColor, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Completa todos los campos."
                        showError = true
                    } else {
                        if (googleVerificado || email.contains("@")) {
                            usuarioViewModel.obtenerUsuarioPorCorreo(email.trim()) { existe ->
                                val userActual = usuarioViewModel.usuario
                                if (existe && userActual != null && userActual.contrasenia == password) {
                                    scope.launch {
                                        usuarioViewModel.guardarSesionLocal(context.applicationContext)
                                        onLogin()
                                    }
                                } else {
                                    errorMessage = "Contraseña incorrecta."
                                    showError = true
                                }
                            }
                        } else {
                            usuarioViewModel.obtenerUsuarioPorCif(email.trim()) { existe ->
                                val userActual = usuarioViewModel.usuario
                                if (existe && userActual != null && userActual.contrasenia == password) {
                                    scope.launch {
                                        usuarioViewModel.guardarSesionLocal(context.applicationContext)
                                        onLogin()
                                    }
                                } else {
                                    errorMessage = "Datos incorrectos o contraseña inválida."
                                    showError = true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !usuarioViewModel.cargando,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryColor)
            ) {
                if (usuarioViewModel.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = PrimaryColor)
                } else {
                    Text(text = "Iniciar sesión", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!googleVerificado) {
                OutlinedButton(
                    onClick = { manejarLoginGoogle() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Google Logo", modifier = Modifier.size(24.dp), tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Continuar con Google",fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "¿No tienes una cuenta?", color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        if (email.contains("@")) {
                            usuarioViewModel.actualizarCorreo(email)
                        } else if (email.isNotEmpty()) {
                            usuarioViewModel.actualizarCif(email)
                        }
                        navController.navigate("createAccount")
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor, contentColor = Color.White)
                ) {
                    Text(text = "Regístrate", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
            Text(text = "Solo para estudiantes UAM verificados", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}