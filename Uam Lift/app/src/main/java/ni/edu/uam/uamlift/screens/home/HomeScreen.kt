package ni.edu.uam.UAM_LIFT.screens.home


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.uamlift.components.RideCard

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "UAM",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "LIFT",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Movilidad colaborativa",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Search
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("¡Hola, Estudiante! 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Encuentra o comparte un viaje hoy", color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { /* abrir búsqueda */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscar viajes")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Viajes disponibles
        Text(
            text = "Viajes disponibles",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            RideCard(
                initials = "MR",
                name = "María Rodríguez",
                rating = "4.9",
                trips = 23,
                from = "Zona 10, Guatemala",
                to = "UAM Campus Central",
                time = "Hoy, 7:30 AM",
                price = "Q25",
                seats = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            RideCard(
                initials = "JL",
                name = "Juan López",
                rating = "5.0",
                trips = 45,
                from = "Carretera a El Salvador",
                to = "UAM Campus Central",
                time = "Hoy, 8:00 AM",
                price = "Q30",
                seats = 2
            )
        }
    }
}