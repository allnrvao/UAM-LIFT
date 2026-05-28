package ni.edu.uam.UAM_LIFT.screens.search

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.annotations.concurrent.Background
import ni.edu.uam.uamlift.components.RideCard
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor

@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    val chips = listOf("Todos", "Mañana", "Tarde", "Económicos")
    var activeChip by remember { mutableStateOf("Todos") }

    Column(modifier = modifier.fillMaxSize().background(Gray)) {
        // Header + Search Bar
        Surface(
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Buscar viaje", fontSize = 30.sp,
                    fontWeight = FontWeight.Black, color = UAMColor,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Origen, destino o conductor...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEach { chip ->
                        val isSelected = activeChip == chip

                        FilterChip(
                            selected = isSelected,
                            onClick = { activeChip = chip },
                            label = {
                                Text(
                                    text = chip,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                // 1. Estados cuando NO está seleccionado
                                containerColor = Color(0x1900BCD4),
                                labelColor = UAMColor,

                                // 2. Estados cuando SÍ está seleccionado
                                selectedContainerColor = UAMColor,
                                selectedLabelColor = Color.White
                            ),
                            border = null
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
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color.Black
                )
            }
            //se agregan elementos a las cartas y se van agregando a la pantalla
            items(listOf(1)) {
                RideCard(
                    initials = "LC",
                    name = "Luis Casco",
                    rating = "4.8",
                    trips = 15,
                    from = "Metrocentro, Managua",
                    to = "UAM Campus Central",
                    time = "Hoy, 9:00 AM",
                    price = "C$60",
                    seats = 3
                )

                RideCard(
                    initials = "Fg",
                    name = "Fernando Gomez",
                    rating = "4.8",
                    trips = 15,
                    from = "Granada, Granada",
                    to = "UAM Campus Central",
                    time = "Hoy, 1:00 PM",
                    price = "C$40",
                    seats = 4
                )
            }
        }
    }
}