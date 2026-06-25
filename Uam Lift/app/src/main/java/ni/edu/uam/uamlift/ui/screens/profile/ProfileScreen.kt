package ni.edu.uam.uamlift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.components.WhyCard
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import java.io.File

@Composable
fun ProfileScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    modifier: Modifier = Modifier
) {
    val estudiante = usuarioViewModel.usuario
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Estados para diálogos
    var mostrarWhyDialog by remember { mutableStateOf(false) }
    var mostrarLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray)
            .verticalScroll(scrollState)
    ) {
        // --- TARJETA PRINCIPAL DEL PERFIL ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            modifier = Modifier.fillMaxWidth().height(320.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fondo decorativo
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(0.5f).background(UAMColor))
                    Box(modifier = Modifier.fillMaxWidth().weight(0.5f).background(Color.White))
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Avatar con lógica de carga mejorada
                    Surface(
                        modifier = Modifier.size(130.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(4.dp).clip(CircleShape).background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!estudiante.imagenUrl.isNullOrEmpty()) {
                                // Soporta tanto archivos locales como URLs remotas
                                val model = if (estudiante.imagenUrl!!.startsWith("/")) {
                                    File(estudiante.imagenUrl!!)
                                } else {
                                    estudiante.imagenUrl
                                }

                                AsyncImage(
                                    model = model,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Sin foto",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Información del Usuario
                    Text(
                        text = "${estudiante.nombre ?: "Usuario"} ${estudiante.apellido ?: ""}".trim(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "@${estudiante.nombreUsuario ?: "sin_usuario"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = UAMColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botón Editar
                IconButton(
                    onClick = { navController.navigate("edit_profile") },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, "Editar", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MÉTRICAS ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Viajes", "${estudiante.numeroViajes}", Icons.AutoMirrored.Filled.Send)
            StatItem("Km", String.format("%.1f", estudiante.kilometrosTotales), Icons.Default.Route)
            StatItem("CO₂ kg", String.format("%.1f", estudiante.co2Ahorrado), Icons.Default.Eco)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ESTADO VERIFICADO ---
        val esDominioUAM = estudiante.correo?.run { endsWith("@uamv.edu.ni") || endsWith("@uam.edu.ni") } ?: false
        if (estudiante.correoVerificado || esDominioUAM) {
            VerificationBadge()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- MENÚ DE OPCIONES ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                ProfileMenuItem("Mi Vehículo", "Gestionar mis autos", Icons.Default.DirectionsCar) {
                    navController.navigate("my_cars")
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray)

                ProfileMenuItem("Historial", "Ver viajes pasados", Icons.Default.History) {
                    // Implementar navegación a historial
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray)

                ProfileMenuItem(
                    title = "Cerrar Sesión",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconTint = Color.Red,
                    showArrow = false
                ) {
                    mostrarLogoutConfirm = true
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN INFORMATIVA ---
        Text(
            text = "¿Por qué UAM LIFT?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            // SOLUCIÓN AL ERROR: Usamos start, end y bottom en lugar de horizontal/bottom
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WhyCard("💰", "Ahorra", "Comparte gastos", Modifier.weight(1f)) { mostrarWhyDialog = true }
                WhyCard("🌱", "Eco", "Reduce emisiones", Modifier.weight(1f)) { mostrarWhyDialog = true }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WhyCard("🤝", "Social", "Nuevos amigos", Modifier.weight(1f)) { mostrarWhyDialog = true }
                WhyCard("🔒", "Seguro", "Solo comunidad UAM", Modifier.weight(1f)) { mostrarWhyDialog = true }
            }
        }
    }

    // --- DIÁLOGOS ---

    if (mostrarWhyDialog) {
        AlertDialog(
            onDismissRequest = { mostrarWhyDialog = false },
            title = { Text("Impacto UAM LIFT") },
            text = { Text("Al compartir tu viaje, reduces el tráfico en el campus, ahorras dinero en combustible y contribuyes a un campus más sostenible.") },
            confirmButton = {
                TextButton(onClick = { mostrarWhyDialog = false }) { Text("Entendido") }
            }
        )
    }

    if (mostrarLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { mostrarLogoutConfirm = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas salir de tu cuenta?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarLogoutConfirm = false
                        usuarioViewModel.cerrarSesion(context) {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Salir", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarLogoutConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun VerificationBadge() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = Color(0xFFECFDF5),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, "Verificado", tint = Color(0xFF10B981))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Perfil Verificado", fontWeight = FontWeight.Bold, color = Color(0xFF065F46), fontSize = 14.sp)
                Text("Miembro oficial de la comunidad UAM", color = Color(0xFF047857), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    subtitle: String = "",
    icon: ImageVector,
    iconTint: Color = UAMColor,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(iconTint.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Color.Black)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        if (showArrow) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Icon(icon, null, tint = UAMColor, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}