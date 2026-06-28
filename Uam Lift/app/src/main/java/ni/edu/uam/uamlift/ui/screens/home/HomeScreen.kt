package ni.edu.uam.uamlift.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.UbicacionViewModel
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
    navController: NavController? = null,
    viajeViewModel: ViajeViewModel = viewModel(factory = AppViewModelFactory()),
    usuarioViewModel: UsuarioViewModel,
    ubicacionViewModel: UbicacionViewModel = viewModel(factory = AppViewModelFactory())
) {
    val backgroundColor = Gray
    val usuario = usuarioViewModel.usuario
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(usuario?.id) {
        val idUsuario = usuario?.id
        if (idUsuario != null && idUsuario != 0L) {
            viajeViewModel.cargarViajesDesdeBackend(idUsuario)
        }
    }

    val misViajes by viajeViewModel.misViajes.collectAsState()
    val viajesOtros by viajeViewModel.viajesOtros.collectAsState()
    val cargando by viajeViewModel.isLoading.collectAsState()
    val pasajerosViaje by viajeViewModel.pasajerosViaje.collectAsState()

    // Solo viajes EN_CURSO activos (no finalizados ni cancelados)
    val viajeActivo = misViajes.find {
        it.estadoViaje == EstadoViaje.EN_CURSO && it.conductor?.id == usuario?.id
    }

    LaunchedEffect(viajeActivo) {
        val idViaje = viajeActivo?.id
        if (viajeActivo != null && idViaje != null) {
            ubicacionViewModel.conectar(idViaje)
            while (true) {
                val latSimulada = 12.1276
                val lngSimulada = -86.2713
                ubicacionViewModel.enviar(idViaje, latSimulada, lngSimulada)
                delay(5000)
            }
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Explorar", "Mis Viajes")

    var selectedViaje by remember { mutableStateOf<Viaje?>(null) }

    if (selectedViaje != null) {
        PassengersDialog(
            conductor = selectedViaje?.conductor,
            pasajeros = pasajerosViaje,
            onDismissRequest = { selectedViaje = null }
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
                        // Cabecera
                        Surface(
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "UAM ",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "LIFT",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF019AA8)
                                    )
                                }
                                Text(
                                    text = "Movilidad colaborativa",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Saludo + búsqueda
                        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(UAMColor)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                ) {
                                    val nombreEstudiante = usuario?.nombre ?: "Estudiante"
                                    Text(
                                        text = "¡Hola, $nombreEstudiante! 👋",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Encuentra o comparte un viaje hoy",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        OutlinedTextField(
                                            value = "",
                                            onValueChange = {},
                                            placeholder = { Text("¿Desde dónde sales?", fontSize = 14.sp) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            contentPadding = PaddingValues(vertical = 4.dp),
                                            onClick = { },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(brush = Degradado2, shape = RoundedCornerShape(14.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Search,
                                                        null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Buscar viajes",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tabs
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
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Lista de viajes
                if (cargando) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = UAMColor)
                        }
                    }
                } else {
                    // LIFO: el más reciente primero (reversed)
                    val listaAMostrar = if (selectedTabIndex == 0)
                        viajesOtros.reversed()
                    else
                        misViajes.reversed()

                    if (listaAMostrar.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedTabIndex == 0)
                                        "No hay viajes disponibles de otros usuarios"
                                    else
                                        "No has creado ningún viaje aún",
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(listaAMostrar) { viaje ->
                            val idUsuarioActual = usuario?.id ?: 0L
                            val cifUsuarioActual = usuario?.cif ?: ""

                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                RideCard(
                                    viaje = viaje,
                                    usuarioIdActual = idUsuarioActual,
                                    esConductor = viaje.conductor?.id == idUsuarioActual,
                                    onCardEnCursoClick = { viajeEnCurso ->
                                        // Navegar a pantalla de mapa del viaje en curso
                                        navController?.navigate("active_ride/${viajeEnCurso.id}")
                                    },
                                    onConfirmarClick = { idViaje ->
                                        viajeViewModel.unirseAlViaje(
                                            viajeId = idViaje,
                                            usuarioId = idUsuarioActual,
                                            usuarioCif = cifUsuarioActual,
                                            onExito = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("¡Te has unido al viaje!")
                                                }
                                            },
                                            onError = {
                                                scope.launch { snackbarHostState.showSnackbar(it) }
                                            }
                                        )
                                    },
                                    onCancelarParticipacion = { idViaje ->
                                        viajeViewModel.cancelarParticipacion(
                                            viajeId = idViaje,
                                            usuarioId = idUsuarioActual,
                                            usuarioCif = cifUsuarioActual,
                                            onExito = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Has cancelado tu participación")
                                                }
                                            }
                                        )
                                    },
                                    onIniciarViaje = { idViaje ->
                                        viajeViewModel.iniciarViaje(
                                            viajeId = idViaje,
                                            conductorId = idUsuarioActual,
                                            onExito = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("¡Viaje iniciado!")
                                                }
                                                ubicacionViewModel.conectar(idViaje)
                                            },
                                            onError = {
                                                scope.launch { snackbarHostState.showSnackbar(it) }
                                            }
                                        )
                                    },
                                    onFinalizarViaje = { idViaje ->
                                        viajeViewModel.finalizarViaje(
                                            viajeId = idViaje,
                                            usuarioId = idUsuarioActual,
                                            onExito = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Viaje finalizado con éxito")
                                                }
                                            },
                                            onError = {
                                                scope.launch { snackbarHostState.showSnackbar(it) }
                                            }
                                        )
                                    },
                                    onCancelarViaje = { idViaje ->
                                        viajeViewModel.cancelarViaje(
                                            viajeId = idViaje,
                                            usuarioId = idUsuarioActual,
                                            onExito = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Viaje cancelado")
                                                }
                                            },
                                            onError = {
                                                scope.launch { snackbarHostState.showSnackbar(it) }
                                            }
                                        )
                                    },
                                    onVerPasajeros = { idViaje ->
                                        selectedViaje = viaje
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