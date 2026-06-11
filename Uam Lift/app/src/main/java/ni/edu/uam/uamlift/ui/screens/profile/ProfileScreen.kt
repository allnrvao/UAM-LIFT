package ni.edu.uam.uamlift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
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
import coil.compose.AsyncImage
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import androidx.navigation.NavController
import ni.edu.uam.uamlift.ui.components.WhyCard
import ni.edu.uam.uamlift.ui.theme.UAMColorLight
import ni.edu.uam.uamlift.viewmodel.UsuarioViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Interfaz
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray)
            .verticalScroll(scrollState)
    ) {
        // Perfil card
        Card(
            colors = CardDefaults.cardColors(UAMColor),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(15.dp)
            ) {
                // Boton editar perfil
                Button(
                    onClick = {
                        navController.navigate("edit_profile")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UAMColorLight
                    ),
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .align(Alignment.End),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Perfil",
                        tint = Color.White
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.Red.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = usuarioViewModel.usuario.imagenUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(200.dp, 200.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "${usuarioViewModel.usuario.nombreUsuario}",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = usuarioViewModel.usuario.nombre + " " + usuarioViewModel.usuario.apellido,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Text("${usuarioViewModel.usuario.correo}", color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Insignias
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem("Viajes", "34", Icons.Default.Send)
            StatItem("Ahorro", "C$ 850", Icons.Default.Star)
            StatItem("CO₂", "-12kg", Icons.Default.Star)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card de estado verificado
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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

                Column(
                    modifier = Modifier.weight(1f)
                ) {
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

        // Card del menú de opciones
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {

                ProfileMenuItem(
                    title = "Reseñas",
                    subtitle = "4.9 promedio",
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFFFB300), // Estrella Dorada
                    titleColor = Color.Black,     // Texto oscuro para el fondo blanco
                    arrowTint = Color(0xFF888888),
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
                    icon = Icons.Default.Check,
                    iconTint = MaterialTheme.colorScheme.primary,
                    titleColor = Color.Black,
                    arrowTint = Color(0xFF888888),
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
                    iconTint = MaterialTheme.colorScheme.primary,
                    titleColor = Color.Black,
                    arrowTint = Color(0xFF888888),
                    onClick = { /* Acción */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text(
                text = "¿Por qué UAM LIFT?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Primera Fila
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

            // Segunda Fila
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

// ======================== COMPONENTE COMPLEMENTARIO: ProfileMenuItem ========================
@Composable
fun ProfileMenuItem(
    title: String = "",
    subtitle: String = "",
    icon: ImageVector = Icons.Default.Face,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    arrowTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
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
                color = titleColor
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = arrowTint
        )
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}