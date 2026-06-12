package ni.edu.uam.uamlift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight // Corregido: Ubicación oficial en Material 3
import androidx.compose.material.icons.automirrored.filled.Send               // Corregido: Ubicación oficial en Material 3
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
import ni.edu.uam.uamlift.ui.components.WhyCard
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.ui.theme.UAMColorLight
import ni.edu.uam.uamlift.viewmodel.UsuarioViewModel
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
        // Tarjeta del Perfil Principal
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp) // Mantenemos tus 400.dp fijos
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            // Usamos un Box principal para poder encimar el avatar y el botón sobre los fondos
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. CAPA DE FONDOS: Dividida exactamente 50% y 50%
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mitad Superior: UAMColor (168.dp netos aproximados del espacio interno)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .background(UAMColor)
                    )
                    // Mitad Inferior: Blanco
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .background(Color.White)
                    )
                }

                // 2. CAPA DE CONTENIDO: Aquí distribuimos el Avatar y los Textos sin usar offsets problemáticos
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Espacio flexible superior para empujar el Avatar exactamente al centro de la tarjeta
                    Spacer(modifier = Modifier.weight(1f))

                    // El Avatar de 180.dp (quedará con 90.dp en la zona turquesa y 90.dp en la zona blanca)
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color.White) // Anillo de contraste exterior
                            .padding(6.dp) // Grosor del borde blanco
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = File(estudiante.imagenUrl),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Espacio controlado entre el Avatar y los textos
                    Spacer(modifier = Modifier.height(16.dp))

                    // Bloque de Textos perfectamente centrados en la sección inferior blanca
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "${estudiante.nombre} ${estudiante.apellido}",
                            color = Color(0xFF1E293B),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp // Un poco más grande para balancear la tarjeta de 400.dp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "@${estudiante.nombreUsuario}",
                            color = UAMColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Espacio flexible inferior para asegurar que los textos queden bien distribuidos y equilibrados
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 3. CAPA DEL BOTÓN DE EDICIÓN: Flota en la esquina superior sin interferir con la simetría central
                IconButton(
                    onClick = { navController.navigate("edit_profile") },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.25f), shape = CircleShape)
                        .size(48.dp) // Ajustado a 48.dp para mantener proporciones elegantes en M3
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

        // Sección de Insignias / Métricas
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem("Viajes", "34", Icons.AutoMirrored.Filled.Send) // Corregido
            StatItem("Ahorro", "C$ 850", Icons.Default.Star)
            StatItem("CO₂", "-12kg", Icons.Default.Eco) // Cambiado por un ícono representativo verde ecológico
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Estado Verificado (Estudiante UAM)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFECFDF5), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verificado",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Estudiante verificado",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Correo UAM confirmado",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = "Activo",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menú de opciones del perfil
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileMenuItem(
                    title = "Reseñas",
                    subtitle = "4.9 promedio",
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFFFB300),
                    onClick = { /* Acción */ }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )

                ProfileMenuItem(
                    title = "Rutas favoritas",
                    subtitle = "3 guardadas",
                    icon = Icons.Default.Place, // Cambiado por un ícono de ubicación coherente
                    iconTint = UAMColor,
                    onClick = { /* Acción */ }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )

                ProfileMenuItem(
                    title = "Notificaciones",
                    subtitle = "Activas",
                    icon = Icons.Default.Notifications,
                    iconTint = UAMColor,
                    onClick = { /* Acción */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¿Por qué UAM LIFT?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Bloque informativo del valor de la App
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WhyCard(
                    emoji = "💰",
                    title = "Ahorra dinero",
                    subtitle = "Comparte combustible",
                    modifier = Modifier.weight(1f)
                )
                WhyCard(
                    emoji = "🌱",
                    title = "Eco-friendly",
                    subtitle = "Menos huella de carbono",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WhyCard(
                    emoji = "🤝",
                    title = "Comunidad",
                    subtitle = "Conoce compañeros",
                    modifier = Modifier.weight(1f)
                )
                WhyCard(
                    emoji = "🔒",
                    title = "Seguro",
                    subtitle = "Solo personas de UAM",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    icon: ImageVector = Icons.Default.Face,
    iconTint: Color = UAMColor,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, // Corregido el paquete Material 3
            contentDescription = null,
            tint = Color(0xFF888888)
        )
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = UAMColor)
        Spacer(modifier = Modifier.height(4.dp))
      //  Text(value, java.lang.String.valueOf(value), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}