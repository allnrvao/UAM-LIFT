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
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
    import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
    import ni.edu.uam.uamlift.sesion.ControlSesion
    import ni.edu.uam.uamlift.ui.components.RideCard
    import ni.edu.uam.uamlift.ui.theme.Gray
    import ni.edu.uam.uamlift.ui.theme.UAMColor

    @Composable
    fun SearchScreen(
        viajeViewModel: ViajeViewModel,
        usuarioViewModel: UsuarioViewModel
    ) {
        val context = LocalContext.current
        val session = remember { ControlSesion(context) }
        val userCif by session.obtenerCif.collectAsState(initial = "")

        val chips = listOf("Todos", "Mañana", "Tarde", "Económicos")
        var activeChip by remember { mutableStateOf("Todos") }
        var searchQuery by remember { mutableStateOf("") }

        val viajesBackend by viajeViewModel.viajes.collectAsState()
        val cargando by viajeViewModel.isLoading.collectAsState()

        LaunchedEffect(Unit) {
            viajeViewModel.cargarViajesDesdeBackend()
        }

        val viajesFiltrados = remember(viajesBackend, searchQuery, activeChip) {
            viajesBackend.filter { viaje ->
                val nombreCompleto = "${viaje.conductor?.nombre.orEmpty()} ${viaje.conductor?.apellido.orEmpty()}"
                val origen = viaje.origen?.nombre.orEmpty()
                val destino = viaje.destino?.nombre.orEmpty()

                val matchesQuery = nombreCompleto.contains(searchQuery, ignoreCase = true) ||
                        origen.contains(searchQuery, ignoreCase = true) ||
                        destino.contains(searchQuery, ignoreCase = true)

                val horaSalida = viaje.fechaHoraSalida?.substringAfter("T")?.take(5).orEmpty()
                val matchesChip = when (activeChip) {
                    "Mañana" -> horaSalida in "00:00".."11:59"
                    "Tarde" -> horaSalida in "12:00".."23:59"
                    "Económicos" -> viaje.precioPorPersona <= 100.0
                    else -> true
                }
                matchesQuery && matchesChip
            }
        }

        Column(modifier = Modifier.fillMaxSize().background(Gray)) {
            Surface(color = Color.White, shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Viajes Disponibles", fontSize = 28.sp, fontWeight = FontWeight.Black, color = UAMColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("¿A dónde vas?") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        chips.forEach { chip ->
                            FilterChip(
                                selected = activeChip == chip,
                                onClick = { activeChip = chip },
                                label = { Text(chip) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = UAMColor,
                                    selectedLabelColor = Color.White
                                )
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay viajes disponibles por ahora", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    items(viajesFiltrados) { miViaje ->
                        RideCard(
                            viaje = miViaje,
                            esConductor = miViaje.conductor?.cif == usuarioViewModel.usuario.cif,
                            onConfirmarClick = { idViaje -> viajeViewModel.unirseAlViaje(idViaje, userCif) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
