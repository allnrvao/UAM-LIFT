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
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.ui.components.RideCard
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(factory = AppViewModelFactory()),
    usuarioViewModel: UsuarioViewModel
) {
    val chips = listOf("Todos", "Mañana", "Tarde", "Económicos")
    var activeChip by remember { mutableStateOf("Todos") }
    val usuario = usuarioViewModel.usuario

    var searchQuery by remember { mutableStateOf("") }

    val viajesOtros by viajeViewModel.viajesOtros.collectAsState()
    val cargando by viajeViewModel.isLoading.collectAsState()

    val viajesFiltrados = remember(viajesOtros, searchQuery, activeChip) {
        viajesOtros.filter { viaje ->
            val matchesSearch = (viaje.origen?.nombre?.contains(searchQuery, ignoreCase = true) == true) ||
                    (viaje.destino?.nombre?.contains(searchQuery, ignoreCase = true) == true) ||
                    (viaje.conductor?.nombre?.contains(searchQuery, ignoreCase = true) == true) ||
                    (viaje.conductor?.apellido?.contains(searchQuery, ignoreCase = true) == true) ||
                    (viaje.conductor?.nombreUsuario?.contains(searchQuery, ignoreCase = true) == true)

            val matchesChip = when (activeChip) {
                "Mañana" -> {
                    val hora = viaje.fechaHoraSalida?.substringAfter("T")?.take(2)?.toIntOrNull() ?: 0
                    hora in 5..11
                }
                "Tarde" -> {
                    val hora = viaje.fechaHoraSalida?.substringAfter("T")?.take(2)?.toIntOrNull() ?: 0
                    hora in 12..18
                }
                "Económicos" -> viaje.precioPorPersona <= 30.0
                else -> true
            }
            (searchQuery.isEmpty() || matchesSearch) && matchesChip
        }
    }

    LaunchedEffect(usuario.id) {
        usuario.id?.let {
            viajeViewModel.cargarViajesDesdeBackend(it)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Gray)) {
        Surface(
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Buscar viaje",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = UAMColor
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

        if (cargando && viajesFiltrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UAMColor)
            }
        } else if (viajesFiltrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No se encontraron viajes", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                items(viajesFiltrados) { miViaje ->
                    RideCard(
                        viaje = miViaje,
                        usuarioIdActual = usuario.id ?: 0L,
                        esConductor = miViaje.conductor?.id == usuario.id,
                        onConfirmarClick = { idViaje ->
                            viajeViewModel.unirseAlViaje(
                                viajeId = idViaje,
                                usuarioId = usuario.id ?: 0L,
                                usuarioCif = usuario.cif ?: ""
                            )
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
