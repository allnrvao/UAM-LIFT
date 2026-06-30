package ni.edu.uam.uamlift.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.components.WhyCard
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import java.io.File

// Modelo de datos para las WhyCards
data class WhyCardData(
    val emoji: String,
    val titulo: String,
    val subtitulo: String,
    val descripcionDetallada: String
)

@Composable
fun ProfileScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    modifier: Modifier = Modifier
) {
    val estudiante       = usuarioViewModel.usuario
    val estadisticas     = usuarioViewModel.estadisticas
    val cargandoStats    = usuarioViewModel.cargandoEstadisticas
    val scrollState      = rememberScrollState()
    val context          = LocalContext.current

    val listaWhyCards = remember {
        listOf(
            WhyCardData("💰", "Ahorra", "Comparte gastos", "Reduce tus gastos mensuales compartiendo los costos de combustible y parqueo con otros estudiantes que llevan tu misma ruta."),
            WhyCardData("🌱", "Eco", "Reduce emisiones", "Al viajar juntos disminuyes la cantidad de vehículos en circulación hacia la UAM, mitigando directamente la huella de carbono del campus."),
            WhyCardData("🤝", "Social", "Nuevos amigos", "Conecta con compañeros de otras facultades o carreras, amplía tu red de contactos y haz los viajes cotidianos mucho más entretenidos."),
            WhyCardData("🔒", "Seguro", "Solo comunidad UAM", "Viaja con total tranquilidad. Todos los usuarios de la app son estudiantes, docentes o colaboradores activos y verificados por la universidad.")
        )
    }

    // Usamos el índice para recordar qué tarjeta estaba seleccionada
    var tarjetaIndexSeleccionada by rememberSaveable { mutableIntStateOf(-1) }
    var mostrarLogoutConfirm by rememberSaveable { mutableStateOf(false) }

    val tarjetaSeleccionada = if (tarjetaIndexSeleccionada != -1) listaWhyCards[tarjetaIndexSeleccionada] else null

    LaunchedEffect(estudiante.id) {
        if (estudiante.id != null) {
            usuarioViewModel.cargarEstadisticas()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray)
            .verticalScroll(scrollState)
    ) {
        Card(
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            shape     = RoundedCornerShape(24.dp),
            modifier  = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(0.5f).background(UAMColor))
                    Box(modifier = Modifier.fillMaxWidth().weight(0.5f).background(Color.White))
                }

                Column(
                    modifier            = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Surface(
                        modifier        = Modifier.size(130.dp),
                        shape           = CircleShape,
                        color           = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier          = Modifier.padding(4.dp).clip(CircleShape).background(Color(0xFFF1F5F9)),
                            contentAlignment  = Alignment.Center
                        ) {
                            val model = remember(estudiante.imagenUrl) {
                                val path = estudiante.imagenUrl
                                if (path.isNullOrBlank()) {
                                    Log.d("ProfileImage", "URL vacía")
                                    null
                                } else {
                                    val finalUrl: Any = when {
                                        path.startsWith("http") -> path
                                        path.startsWith("C:") || path.contains("uam_photos") -> File(path)
                                        else -> {
                                            val base = RetrofitClient.BASE_URL.trimEnd('/')
                                            val relative = path.trimStart('/')
                                            "$base/$relative"
                                        }
                                    }
                                    Log.d("ProfileImage", "URL generada: $finalUrl")
                                    finalUrl
                                }
                            }

                            if (model != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(model)
                                        .crossfade(true)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .listener(
                                            onError = { _, result ->
                                                Log.e("ProfileImage", "Error Coil: ${result.throwable.message}")
                                            },
                                            onSuccess = { _, _ ->
                                                Log.d("ProfileImage", "Imagen cargada con éxito")
                                            }
                                        )
                                        .build(),
                                    contentDescription = "Foto de perfil",
                                    modifier         = Modifier.fillMaxSize(),
                                    contentScale     = ContentScale.Crop,
                                    error = rememberVectorPainter(Icons.Default.Person),
                                    placeholder = rememberVectorPainter(Icons.Default.Person)
                                )
                            } else {
                                Icon(
                                    imageVector     = Icons.Default.Person,
                                    contentDescription = "Sin foto",
                                    tint            = Color.LightGray,
                                    modifier        = Modifier.size(64.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text       = "${estudiante.nombre ?: "Usuario"} ${estudiante.apellido ?: ""}".trim(),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1E293B)
                    )
                    Text(
                        text       = "@${estudiante.nombreUsuario ?: "sin_usuario"}",
                        style      = MaterialTheme.typography.bodyLarge,
                        color      = UAMColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick  = { navController.navigate("edit_profile") },
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

        if (cargandoStats) {
            Box(
                modifier         = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = UAMColor, modifier = Modifier.size(28.dp))
            }
        } else {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Viajes", value = "${estadisticas.totalViajes}", icon  = Icons.AutoMirrored.Filled.Send)
                StatItem(label = "Km", value = String.format("%.1f", estadisticas.kilometrosTotales), icon  = Icons.Default.Route)
                StatItem(label = "CO₂ kg", value = String.format("%.1f", estadisticas.co2Ahorrado), icon  = Icons.Default.Eco)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val esDominioUAM = estudiante.correo?.run { endsWith("@uamv.edu.ni") || endsWith("@uam.edu.ni") } ?: false
        if (estudiante.correoVerificado || esDominioUAM) {
            VerificationBadge()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors   = CardDefaults.cardColors(containerColor = Color.White),
            shape    = RoundedCornerShape(16.dp)
        ) {
            Column {
                ProfileMenuItem("Mi Vehículo", "Gestionar mis autos", Icons.Default.DirectionsCar) { navController.navigate("my_cars") }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray)
                ProfileMenuItem("Historial de Viajes", "Ver viajes pasados", Icons.Default.History) { navController.navigate("my_rides") }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Gray)
                ProfileMenuItem("Cerrar Sesión", icon = Icons.AutoMirrored.Filled.ExitToApp, iconTint = Color.Red, showArrow = false) { mostrarLogoutConfirm = true }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "¿Por qué UAM LIFT?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp), color = Color.Black, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WhyCard("💰", "Ahorra", "Comparte gastos", Modifier.weight(1f)) { tarjetaIndexSeleccionada = 0 }
                WhyCard("🌱", "Eco", "Reduce emisiones", Modifier.weight(1f)) { tarjetaIndexSeleccionada = 1 }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WhyCard("🤝", "Social", "Nuevos amigos", Modifier.weight(1f)) { tarjetaIndexSeleccionada = 2 }
                WhyCard("🔒", "Seguro", "Solo comunidad UAM", Modifier.weight(1f)) { tarjetaIndexSeleccionada = 3 }
            }
        }
    }

    tarjetaSeleccionada?.let { tarjeta ->
        AlertDialog(
            onDismissRequest = { tarjetaIndexSeleccionada = -1 },
            containerColor = Color.White,
            title   = { Text(text = "${tarjeta.emoji} ${tarjeta.titulo}", color = UAMColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
            text    = { Text(text = tarjeta.descripcionDetallada, color = Color(0xFF334155), style = MaterialTheme.typography.bodyLarge, lineHeight = 22.sp, fontWeight = FontWeight.Normal) },
            confirmButton = { TextButton(onClick = { tarjetaIndexSeleccionada = -1 }, colors = ButtonDefaults.textButtonColors(contentColor = UAMColor)) { Text(text = "Entendido", fontWeight = FontWeight.SemiBold) } }
        )
    }

    if (mostrarLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { mostrarLogoutConfirm = false },
            containerColor = Color.White,
            title   = { Text("Cerrar Sesión") },
            text    = { Text("¿Estás seguro de que deseas salir de tu cuenta?") },
            confirmButton = {
                Button(onClick = {
                        mostrarLogoutConfirm = false
                        usuarioViewModel.cerrarSesion(context) { navController.navigate("login") { popUpTo(0) { inclusive = true } } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Salir", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { mostrarLogoutConfirm = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) { Text("Cancelar") } }
        )
    }
}

@Composable
fun VerificationBadge() {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), color = Color(0xFFECFDF5), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
fun ProfileMenuItem(title: String, subtitle: String = "", icon: ImageVector, iconTint: Color = UAMColor, showArrow: Boolean = true, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(iconTint.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Color.Black)
            if (subtitle.isNotEmpty()) { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
        }
        if (showArrow) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray) }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Icon(icon, null, tint = UAMColor, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}
