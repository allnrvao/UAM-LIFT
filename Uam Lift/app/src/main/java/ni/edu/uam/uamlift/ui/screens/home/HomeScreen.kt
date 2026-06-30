package ni.edu.uam.uamlift.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.NotificacionViewModel
import ni.edu.uam.uamlift.data.viewmodels.UbicacionViewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.ui.components.RideCard
import ni.edu.uam.uamlift.ui.components.NotificationBellButton
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.ui.components.PassengersDialog

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viajeViewModel: ViajeViewModel = viewModel(factory = AppViewModelFactory()),
    usuarioViewModel: UsuarioViewModel,
    ubicacionViewModel: UbicacionViewModel = viewModel(factory = AppViewModelFactory()),
    notificacionViewModel: NotificacionViewModel = viewModel(factory = AppViewModelFactory())
) {
    val backgroundColor = Gray
    val usuario = usuarioViewModel.usuario
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
    val notificacionesNoLeidas by notificacionViewModel.noLeidas.collectAsState()

    val viajeActivo = misViajes.find {
        it.estadoViaje == EstadoViaje.EN_CURSO && it.conductor?.id == usuario?.id
    }

    // GESTIÓN DE PERMISOS: Solo pedir una vez
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Bandera para no repetir la solicitud si ya se intentó
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(viajeActivo, hasLocationPermission) {
        val idViaje = viajeActivo?.id
        if (viajeActivo != null && idViaje != null) {
            if (!hasLocationPermission) {
                if (!permissionRequested) {
                    permissionRequested = true
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            } else {
                ubicacionViewModel.conectar(idViaje)
                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setMinUpdateIntervalMillis(3000)
                    .build()
                val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {}
                }
                try {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper())
                    while (true) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                ubicacionViewModel.enviar(idViaje, location.latitude, location.longitude)
                            }
                        }
                        delay(5000)
                    }
                } catch (e: SecurityException) {
                } finally {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        } else {
            permissionRequested = false
        }
    }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Explorar", "Mis Viajes")
    var selectedViaje by remember { mutableStateOf<Viaje?>(null) }

    if (selectedViaje != null) {
        PassengersDialog(
            conductor = selectedViaje?.conductor,
            pasajeros = pasajerosViaje,
            esConductorActual = selectedViaje?.conductor?.id == usuario?.id,
            onEliminarPasajero = { usuarioCif ->
                viajeViewModel.eliminarPasajero(
                    viajeId = selectedViaje?.id ?: 0L,
                    usuarioId = usuario?.id ?: 0L,
                    usuarioCif = usuarioCif,
                    onExito = { scope.launch { snackbarHostState.showSnackbar("Asiento liberado") } },
                    onError = { error -> scope.launch { snackbarHostState.showSnackbar(error) } }
                )
            },
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
                        Surface(
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp), // Ajustado para mejor espaciado
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Textos (Logo + Subtítulo) alineados a la izquierda
                                Column(
                                    modifier = Modifier.weight(1f) // Ocupa todo el espacio disponible y empuja la campana a la derecha
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "UAM ", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                        Text(text = "LIFT", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF019AA8))
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Movilidad colaborativa",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }

                                // 2. Botón de notificación en el extremo derecho
                                NotificationBellButton(
                                    tieneNoLeidas = notificacionesNoLeidas > 0,
                                    tint = UAMColor,
                                    onClick = { navController?.navigate("notifications") }
                                )
                            }
                        }

                        // Banner de Bienvenida Centrado Verticalmente
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(UAMColor)
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.Center
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

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                )
                            }
                        }
                    }
                }

                if (cargando) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = UAMColor)
                        }
                    }
                } else {
                    val listaAMostrar = if (selectedTabIndex == 0) viajesOtros.reversed() else misViajes.reversed()
                    if (listaAMostrar.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (selectedTabIndex == 0) "No hay viajes disponibles" else "No has creado viajes aún",
                                    color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(listaAMostrar) { viaje ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                RideCard(
                                    viaje = viaje,
                                    usuarioIdActual = usuario?.id ?: 0L,
                                    esConductor = viaje.conductor?.id == usuario?.id,
                                    onCardEnCursoClick = { navController?.navigate("active_ride/${it.id}") },
                                    onConfirmarClick = { id ->
                                        viajeViewModel.unirseAlViaje(id, usuario?.id ?: 0L, usuario?.cif ?: "",
                                            onExito = { scope.launch { snackbarHostState.showSnackbar("¡Te has unido!") } },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    },
                                    onCancelarParticipacion = { id ->
                                        viajeViewModel.cancelarParticipacion(id, usuario?.id ?: 0L, usuario?.cif ?: "",
                                            onExito = { scope.launch { snackbarHostState.showSnackbar("Cancelado") } }
                                        )
                                    },
                                    onIniciarViaje = { id ->
                                        viajeViewModel.iniciarViaje(id, usuario?.id ?: 0L,
                                            onExito = {
                                                scope.launch { snackbarHostState.showSnackbar("¡Viaje iniciado!") }
                                                ubicacionViewModel.conectar(id)
                                            },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    },
                                    onFinalizarViaje = { id ->
                                        viajeViewModel.finalizarViaje(id, usuario?.id ?: 0L,
                                            onExito = { scope.launch { snackbarHostState.showSnackbar("Finalizado") } },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    },
                                    onCancelarViaje = { id, motivo ->
                                        viajeViewModel.cancelarViaje(id, usuario?.id ?: 0L, motivo,
                                            onExito = { scope.launch { snackbarHostState.showSnackbar("Viaje cancelado") } },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    },
                                    onVerPasajeros = { id ->
                                        selectedViaje = viaje
                                        viajeViewModel.obtenerPasajeros(id)
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