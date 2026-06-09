package ni.edu.uam.uamlift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.uamlift.data.models.Usuario
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import androidx.navigation.NavController // Importamos NavController

@Composable
fun ProfileScreen(
    navController: NavController, // 1. PASAMOS EL NAVCONTROLLER COMO PARÁMETRO AQUÍ
    modifier: Modifier = Modifier
) {
    //variables para el uso de los campos
    var cif by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }
    var numeroViajes by remember { mutableStateOf(0) }

    //Se crea el objeto
    val usuario = Usuario()
    usuario.cif = cif
    usuario.nombre = nombre
    usuario.apellido = apellido
    usuario.correo = correo
    usuario.nombreUsuario = nombreUsuario
    usuario.imagenUrl = imagenUrl

    //Interfaz
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray).scrollable(rememberScrollState(), Orientation.Vertical),
    ) {
        // Hero Card
        Card(
            colors = CardDefaults.cardColors(UAMColor),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.Red.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                    //box que contiene la informacion del perfil
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = usuario.imagenUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("$nombreUsuario", color = Color.White, fontSize = 16.sp)
                Text("$nombre $apellido", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("$correo", color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem("Viajes", "34", Icons.Default.Send)
            StatItem("Ahorro", "Q850", Icons.Default.Star)
            StatItem("CO₂", "-12kg", Icons.Default.Star)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileMenuItem(
                    title = "Editar perfil",
                    icon = Icons.Default.Edit,
                    onClick = {
                        // 2. CORREGIDO: Solo llamamos al método navigate con una ruta de texto
                        navController.navigate("edit_profile")
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    title = "Reseñas",
                    subtitle = "4.9 promedio",
                    icon = Icons.Default.Star,
                    onClick = { /* Acción */ }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    title = "Rutas favoritas",
                    subtitle = "3 guardadas",
                    icon = Icons.Default.Check,
                    onClick = { /* Acción */ }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(
                    title = "Notificaciones",
                    subtitle = "Activas",
                    icon = Icons.Default.Notifications,
                    onClick = { /* Acción */ }
                )
            }
        }
    }
}

// ======================== COMPONENTE: ProfileMenuItem ========================
@Composable
fun ProfileMenuItem(
    title: String = "",
    subtitle: String = "",
    icon: ImageVector = Icons.Default.Face,
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
            imageVector = Icons.Default.KeyboardArrowRight, // Cambié Face por una flecha para que se vea mejor como menú
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
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