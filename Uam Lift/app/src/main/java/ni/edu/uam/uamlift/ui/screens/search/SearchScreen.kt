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

    // Estado para capturar lo que el estudiante escribe
    var searchQuery by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            viajeViewModel.cargarViajesDesdeBackend()
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

            if (cargando && viajesFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UAMColor)
                }
            } else if (viajesFiltrados.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron viajes", color = Color.Gray, fontSize = 16.sp)
                    }
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
