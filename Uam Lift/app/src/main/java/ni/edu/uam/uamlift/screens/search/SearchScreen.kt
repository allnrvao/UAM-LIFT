package ni.edu.uam.UAM_LIFT.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ni.edu.uam.uamlift.components.RideCard

@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    val chips = listOf("Todos", "Mañana", "Tarde", "Económicos")
    var activeChip by remember { mutableStateOf("Todos") }

    Column(modifier = modifier.fillMaxSize()) {
        // Header + Search Bar
        Surface(
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Buscar viaje", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Origen, destino o conductor...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chips.forEach { chip ->
                        FilterChip(
                            selected = activeChip == chip,
                            onClick = { activeChip = chip },
                            label = { Text(chip) }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Disponibles hoy",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(listOf(1, 2, 3)) {
                RideCard(
                    initials = if (it == 1) "MR" else if (it == 2) "JL" else "AP",
                    name = if (it == 1) "María Rodríguez" else if (it == 2) "Juan López" else "Andrea Pérez",
                    rating = if (it == 1) "4.9" else if (it == 2) "5.0" else "4.8",
                    trips = if (it == 1) 23 else if (it == 2) 45 else 18,
                    from = if (it == 1) "Zona 10, Guatemala" else if (it == 2) "Carretera a El Salvador" else "Mixco, Guatemala",
                    to = "UAM Campus Central",
                    time = if (it == 1) "Hoy, 7:30 AM" else if (it == 2) "Hoy, 8:00 AM" else "Hoy, 7:00 AM",
                    price = if (it == 1) "Q25" else if (it == 2) "Q30" else "Q35",
                    seats = if (it == 1) 3 else if (it == 2) 2 else 1
                )
            }
        }
    }
}