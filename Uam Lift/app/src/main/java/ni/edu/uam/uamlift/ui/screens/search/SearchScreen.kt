package ni.edu.uam.UAM_LIFT.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Agregado para usar tu ViewModel
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.ui.components.RideCard
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.viewmodel.ViajeViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    // 1. Inyectamos tu ViewModel real para buscar en la base de datos
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val chips = listOf("Todos", "Mañana", "Tarde", "Económicos")
    var activeChip by remember { mutableStateOf("Todos") }

    // Estado para capturar lo que el estudiante escribe
    var searchQuery by remember { mutableStateOf("") }

    // Observamos los viajes reales y el estado de carga que vienen de Spring Boot
    val viajesBackend by viajeViewModel.viajes.collectAsState()
    val cargando by viajeViewModel.isLoading.collectAsState()

    // Estado para abrir detalles del viaje al seleccionarlo
    var selectedViaje by remember { mutableStateOf<Viaje?>(null) }

    // 2. Lógica de filtrado en tiempo real (Filtra por query escrito y por los Chips)
    val viajesFiltrados = remember(viajesBackend, searchQuery, activeChip) {
        viajesBackend.filter { viaje ->
            val nombreConductor = viaje.conductor?.nombre.orEmpty()
            val origen = viaje.origen?.nombre.orEmpty()
            val destino = viaje.destino?.nombre.orEmpty()

            // Coincidencia de texto básico
            val matchesQuery = nombreConductor.contains(searchQuery, ignoreCase = true) ||
                    origen.contains(searchQuery, ignoreCase = true) ||
                    destino.contains(searchQuery, ignoreCase = true)

            // Lógica para los chips de filtro
            val horaSalida = viaje.fechaHoraSalida?.substringAfter("T")?.take(5).orEmpty() // "HH:mm"
            val matchesChip = when (activeChip) {
                "Mañana" -> horaSalida in "00:00".."11:59"
                "Tarde" -> horaSalida in "12:00".."18:59"
                "Económicos" -> viaje.precioPorPersona <= 50.0
                else -> true // "Todos"
            }

            matchesQuery && matchesChip
        }
    }

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
                    value = searchQuery,
                    onValueChange = { searchQuery = it }, // Actualiza la lista automáticamente al escribir
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
                                containerColor = Color(0x1900BCD4),
                                labelColor = UAMColor,
                                selectedContainerColor = UAMColor,
                                selectedLabelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }
            }
        }

        // Listado Dinámico Conectado a la Base de Datos
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (searchQuery.isEmpty() && activeChip == "Todos") "Disponibles hoy" else "Resultados de búsqueda",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }

            if (cargando) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UAMColor)
                    }
                }
            } else if (viajesFiltrados.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron viajes", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                // CORREGIDO: Ahora itera sobre los objetos Viaje reales y los inyecta correctamente en tu RideCard
                items(viajesFiltrados) { miViaje ->
                    RideCard(
                        viaje = miViaje,
                        onClick = { selectedViaje = miViaje } // Abre el modal de confirmación
                    )
                }
            }
        }
    }

    // Modal de Confirmación para unirse al viaje seleccionado
    selectedViaje?.let { viaje ->
        AlertDialog(
            onDismissRequest = { selectedViaje = null },
            confirmButton = {
                TextButton(onClick = {
                    viaje.id?.let { id ->
                        viajeViewModel.unirseAlViaje(id, "CIF_ESTUDIANTE")
                    }
                    selectedViaje = null
                }) {
                    Text("Reservar Asiento")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedViaje = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text(text = "Detalles del viaje de ${viaje.conductor?.nombre ?: "Conductor UAM"}") },
            text = {
                Text(text = "Ruta: ${viaje.origen?.nombre ?: "Origen"} -> ${viaje.destino?.nombre ?: "Destino"}\n" +
                        "Precio: C$ ${viaje.precioPorPersona.toInt()}\n" +
                        "Asientos disponibles: ${viaje.numeroAsientosDisponibles}")
            }
        )
    }
}