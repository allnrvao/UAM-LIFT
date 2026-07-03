package ni.edu.uam.uamlift.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.enums.TipoNotificacion
import ni.edu.uam.uamlift.data.models.Notificacion
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
    
    // Estados para el diálogo de inicio de viaje
    var showStartConfirmDialog by remember { mutableStateOf(false) }
    var viajeIdAIniciar by remember { mutableStateOf<Long?>(null) }

    // ── CORRECCIÓN AQUÍ: RENDERIZACIÓN DEL DIÁLOGO ADAPTATIVO DE NOTIFICACIONES ──
    val notificacionPendiente = notificacionViewModel.notificacionPendiente
    if (notificacionPendiente != null) {
        HomeNotificationAdaptativeDialog(
            notificacion = notificacionPendiente,
            navController = navController,
            onDismissRequest = { notificacionViewModel.limpiarNotificacionPendiente() }
        )
    }

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
    
    // Diálogo de confirmación para iniciar viaje
    if (showStartConfirmDialog && viajeIdAIniciar != null) {
        AlertDialog(
            onDismissRequest = { showStartConfirmDialog = false },
            title = { Text("¿Iniciar Viaje?", fontWeight = FontWeight.Bold, color = UAMColor) },
            text = { Text("¿Estás seguro de que deseas iniciar este viaje ahora? Se notificará a los pasajeros que el conductor está en camino.") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = viajeIdAIniciar!!
                        showStartConfirmDialog = false
                        viajeViewModel.iniciarViaje(id, usuario?.id ?: 0L,
                            onExito = {
                                scope.launch { snackbarHostState.showSnackbar("¡Viaje iniciado!") }
                                ubicacionViewModel.conectar(id)
                            },
                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
                ) {
                    Text("Iniciar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartConfirmDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
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
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
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

                                NotificationBellButton(
                                    tieneNoLeidas = notificacionesNoLeidas > 0,
                                    tint = UAMColor,
                                    onClick = { navController?.navigate("notifications") }
                                )
                            }
                        }

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
                                    SecondaryIndicator(
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
                                        viajeIdAIniciar = id
                                        showStartConfirmDialog = true
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

// ── COMPONENTE DIÁLOGO ADAPTATIVO CON EL NUEVO DISEÑO ORIENTADO A LA FUNCIONALIDAD ──
@Composable
fun HomeNotificationAdaptativeDialog(
    notificacion: Notificacion,
    navController: NavController?,
    onDismissRequest: () -> Unit
) {
    val (colorTema, fondoItem, icono, etiquetaTexto, leyendaInformativa) = when (notificacion.tipo) {
        TipoNotificacion.USUARIO_UNIDO -> HomeQuintuple(
            UAMColor, UAMColor.copy(alpha = 0.10f), Icons.Default.PersonAdd, "NUEVO INTEGRANTE", "Un pasajero se ha reservado un asiento en tu vehículo:"
        )
        TipoNotificacion.CANCELACION_VIAJE, TipoNotificacion.USUARIO_ELIMINADO -> HomeQuintuple(
            Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.08f), Icons.Default.PersonRemove, "MODIFICACIÓN / CANCELADO", "Se han generado cambios críticos en el estado del viaje:"
        )
        TipoNotificacion.FINALIZACION_VIAJE -> HomeQuintuple(
            Color(0xFF22C55E), Color(0xFF22C55E).copy(alpha = 0.08f), Icons.Default.CheckCircle, "VIAJE COMPLETADO", "¡Tu ruta ha finalizado exitosamente! Detalles del cierre:"
        )
        TipoNotificacion.INICIO_VIAJE -> HomeQuintuple(
            UAMColor, UAMColor.copy(alpha = 0.08f), Icons.Default.DirectionsCar, "EN CURSO", "El conductor ha iniciado la ruta. Asegúrate de estar listo:"
        )
        else -> HomeQuintuple(
            UAMColor, Color(0xFFF8F9FA), Icons.Default.Person, "AVISO GENERAL", "Información importante sobre la plataforma:"
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f).wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificacion.titulo.ifBlank { etiquetaTexto.lowercase().replaceFirstChar { it.uppercase() } },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorTema
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = leyendaInformativa, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(fondoItem)
                        .border(1.dp, colorTema, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(colorTema),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icono, contentDescription = null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Estado de Ruta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = colorTema, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = etiquetaTexto,
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = notificacion.mensaje.ifBlank {
                                when (notificacion.tipo) {
                                    TipoNotificacion.USUARIO_ELIMINADO -> "El conductor te eliminó de este viaje."
                                    TipoNotificacion.CANCELACION_VIAJE -> "El conductor canceló este viaje."
                                    else -> "El viaje ha cambiado de estado."
                                }
                            },
                            fontSize = 12.5.sp,
                            color = Color(0xFF334155),
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (notificacion.tipo == TipoNotificacion.INICIO_VIAJE && notificacion.viajeId != null) {
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cerrar", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                onDismissRequest()
                                navController?.navigate("active_ride/${notificacion.viajeId}") {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
                        ) {
                            Text("Ver Mapa", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorTema)
                        ) {
                            Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

data class HomeQuintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)