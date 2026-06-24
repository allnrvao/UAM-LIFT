package ni.edu.uam.uamlift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray)
            .verticalScroll(scrollState)
    ) {
        // --- TARJETA PRINCIPAL DEL PERFIL ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. Capa de Fondos (Mitad y Mitad)
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(0.5f).background(UAMColor))
                    Box(modifier = Modifier.fillMaxWidth().weight(0.5f).background(Color.White))
                }

                // 2. Capa de Contenido (Avatar y Textos)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(140.dp) // Ajustado ligeramente para proporción en 310dp
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    ) {
                        if (!estudiante.imagenUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = File(estudiante.imagenUrl!!), // Carga la imagen local sin fallos
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Sin foto de perfil",
                                tint = Color.Gray,
                                modifier = Modifier.fillMaxSize().padding(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Textos Seguros (Evitan mostrar "null null" si los datos aún están cargando)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "${estudiante.nombre ?: "Estudiante"} ${estudiante.apellido ?: ""}".trim(),
                            color = Color(0xFF1E293B),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "@${estudiante.nombreUsuario ?: "usuario"}",
                            color = UAMColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                // 3. Botón de Editar (Conecta directo a tu nueva pantalla)
                IconButton(
                    onClick = { navController.navigate("edit_profile") },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.25f), shape = CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Perfil",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- MÉTRICAS ---
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem("Viajes", "34", Icons.AutoMirrored.Filled.Send)
            StatItem("Ahorro", "C$ 850", Icons.Default.Star)
            StatItem("CO₂", "-12kg", Icons.Default.Eco)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ESTADO VERIFICADO ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp).background(Color(0xFFECFDF5), shape = CircleShape)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Estudiante verificado", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Text("Correo UAM confirmado", fontSize = 12.sp, color = Color.Gray)
                }

                Surface(color = Color(0xFFECFDF5), shape = RoundedCornerShape(50.dp)) {
                    Text("Activo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MENÚ DEL PERFIL ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileMenuItem("Reseñas", "4.9 promedio", Icons.Default.Star, Color(0xFFFFB300)) { /* Acción */ }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color(0xFFE0E0E0))
                ProfileMenuItem("Rutas favoritas", "3 guardadas", Icons.Default.Place, UAMColor) { /* Acción */ }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color(0xFFE0E0E0))
                ProfileMenuItem("Mi Vehículo", "Gestionar carros", Icons.Default.DirectionsCar, UAMColor) {
                    navController.navigate("my_cars")
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color(0xFFE0E0E0))
                ProfileMenuItem("Notificaciones", "Activas", Icons.Default.Notifications, UAMColor) { /* Acción */ }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN INFORMATIVA ---
        Text(
            text = "¿Por qué UAM LIFT?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WhyCard("💰", "Ahorra dinero", "Comparte combustible", Modifier.weight(1f))
                WhyCard("🌱", "Eco-friendly", "Menos huella de carbono", Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WhyCard("🤝", "Comunidad", "Conoce compañeros", Modifier.weight(1f))
                WhyCard("🔒", "Seguro", "Solo personas de UAM", Modifier.weight(1f))
            }
        }
    }
}

// --- SUBCOMPONENTES ---

@Composable
fun ProfileMenuItem(
    title: String,
    subtitle: String = "",
    icon: ImageVector = Icons.Default.Face,
    iconTint: Color = UAMColor,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.Black)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF888888))
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = UAMColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}