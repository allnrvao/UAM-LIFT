package ni.edu.uam.uamlift.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.ui.components.RideCard
import ni.edu.uam.uamlift.ui.theme.Degradado2
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.ui.components.PassengersDialog

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(),
    usuarioViewModel: UsuarioViewModel
) {
    val backgroundColor = Gray
    val usuario = usuarioViewModel.usuario
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargamos los viajes filtrados al iniciar
    LaunchedEffect(usuario.id) {
        viajeViewModel.cargarViajesDesdeBackend(usuario.id)
    }

    val misViajes by viajeViewModel.misViajes.collectAsState()
    val viajesOtros by viajeViewModel.viajesOtros.collectAsState()
    val cargando by viajeViewModel.isLoading.collectAsState()
    val pasajerosViaje by viajeViewModel.pasajerosViaje.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Explorar", "Mis Viajes")

    var viajeParaPasajeros by remember { mutableStateOf<Long?>(null) }

    if (viajeParaPasajeros != null) {
        PassengersDialog(
            pasajeros = pasajerosViaje,
            onDismissRequest = { viajeParaPasajeros = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column {
                        // Cabeza de página
                        Surface(
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(20.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(text = "UAM ", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    Text(text = "LIFT", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF019AA8))
                                }
                                Text(text = "Movilidad colaborativa", color = Color.Gray, fontSize = 18.sp)
                            }
                        }

                        // Saludo al estudiante con fondo Verde Azulado y caja flotante
                        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                            Box(modifier = Modifier.fillMaxWidth().height(255.dp).background(UAMColor))

                            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                                    Text(text = "¡Hola, ${usuario.nombre}! \uD83D\uDC4B", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Text(text = "Encuentra o comparte un viaje hoy", fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Caja blanca flotante de búsqueda
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                                ) {
                                    Column(modifier = Modifier.padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        OutlinedTextField(
                                            value = "",
                                            onValueChange = {},
                                            placeholder = { Text("¿Desde dónde sales?") },
                                            leadingIcon = { Icon(Icons.Default.Search, null) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            contentPadding = PaddingValues(vertical = 6.dp),
                                            onClick = { /* abrir búsqueda */ },
                                            modifier = Modifier.fillMaxWidth().height(54.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize().background(brush = Degradado2, shape = RoundedCornerShape(16.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                    Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(text = "Buscar viajes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tab Selector
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.White,
                            contentColor = UAMColor,
                            indicator = { tabPositions ->
                                if (selectedTabIndex < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                        color = UAMColor
                                    )
                                }
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(text = title, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }
                }

                // LISTADO DINÁMICO SEGÚN TAB
                if (cargando) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = UAMColor)
                        }
                    }
                } else {
                    val listaAMostrar = if (selectedTabIndex == 0) viajesOtros else misViajes
                    
                    if (listaAMostrar.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (selectedTabIndex == 0) "No hay viajes disponibles de otros usuarios" else "No has creado ningún viaje aún",
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(listaAMostrar) { viaje ->
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                RideCard(
                                    viaje = viaje,
                                    esConductor = selectedTabIndex == 1,
                                    onConfirmarClick = { idViaje ->
                                        viajeViewModel.unirseAlViaje(
                                            viajeId = idViaje,
                                            usuarioId = usuario.id ?: 0L,
                                            usuarioCif = usuario.cif ?: "",
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    },
                                    onIniciarViaje = { idViaje ->
                                        viajeViewModel.iniciarViaje(
                                            viajeId = idViaje,
                                            usuarioId = usuario.id ?: 0L,
                                            onExito = { scope.launch { snackbarHostState.showSnackbar("¡Viaje iniciado!") } },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    },
                                    onFinalizarViaje = { idViaje ->
                                        viajeViewModel.finalizarViaje(
                                            viajeId = idViaje,
                                            usuarioId = usuario.id ?: 0L,
                                            onExito = { scope.launch { snackbarHostState.showSnackbar("Viaje finalizado con éxito") } },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    },
                                    onCancelarViaje = { idViaje ->
                                        viajeViewModel.cancelarViaje(
                                            viajeId = idViaje,
                                            usuarioId = usuario.id ?: 0L,
                                            onExito = { scope.launch { snackbarHostState.showSnackbar("Viaje cancelado") } },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    },
                                    onVerPasajeros = { idViaje ->
                                        viajeParaPasajeros = idViaje
                                        viajeViewModel.obtenerPasajeros(idViaje)
                                    }
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
