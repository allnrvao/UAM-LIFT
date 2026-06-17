package ni.edu.uam.uamlift.ui.screens.search

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
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.components.RideCard
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.viewmodel.ViajeViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(),
    usuarioViewModel: UsuarioViewModel = viewModel() // Agregado para obtener el CIF real
) {
    val chips = listOf("Todos", "Mañana", "Tarde", "Económicos")
    var activeChip by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }

    // Clonado exacto de los observadores del Backend de Spring Boot de tu HomeScreen
    val viajesBackend by viajeViewModel.viajes.collectAsState()
    val cargando by viajeViewModel.isLoading.collectAsState()

    // Lógica de filtrado reactiva y segura sobre la data real
    val viajesFiltrados = remember(viajesBackend, searchQuery, activeChip) {
        viajesBackend.filter { viaje ->
            // Filtro por texto (Buscador)
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                val nombreConductor = viaje.conductor?.nombre.orEmpty()
                val origen = viaje.origen?.nombre.orEmpty()
                val destino = viaje.destino?.nombre.orEmpty()

                nombreConductor.contains(searchQuery, ignoreCase = true) ||
                        origen.contains(searchQuery, ignoreCase = true) ||
                        destino.contains(searchQuery, ignoreCase = true)
            }

            // Filtro por franja horaria / precio (Chips)
            val fechaRaw = viaje.fechaHoraSalida.orEmpty()
            val horaSalida = if (fechaRaw.contains("T")) fechaRaw.substringAfter("T").take(5) else fechaRaw.take(5)

            val matchesChip = when (activeChip) {
                "Mañana" -> horaSalida in "00:00".."11:59"
                "Tarde" -> horaSalida in "12:00".."18:59"
                "Económicos" -> viaje.precioPorPersona <= 50.0
                else -> true
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
                    onValueChange = { searchQuery = it },
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

        // LISTADO DINÁMICO CORREGIDO SEGÚN TU HOME
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
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "No se encontraron viajes", fontSize = 16.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        // Replicamos el botón salvavidas de tu HomeScreen por si la lista está en caché vacía
                        Button(
                            onClick = { viajeViewModel.cargarViajesDesdeBackend() },
                            colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
                        ) {
                            Text("Recargar datos", color = Color.White)
                        }
                    }
                }
            } else {
                items(viajesFiltrados) { viaje ->
                    RideCard(
                        viaje = viaje,
                        onConfirmarClick = { idViaje ->
                            viajeViewModel.unirseAlViaje(
                                viajeId = idViaje,
                                usuarioCif = usuarioViewModel.usuario.cif ?: ""
                            )
                        }
                    )
                }
            }
        }
    }
}