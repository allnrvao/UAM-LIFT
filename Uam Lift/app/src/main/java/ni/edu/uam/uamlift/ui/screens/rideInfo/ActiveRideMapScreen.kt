package ni.edu.uam.uamlift.ui.screens.rideInfo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ni.edu.uam.uamlift.R
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.models.Usuario
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.UbicacionViewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.ui.theme.UAMColor
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun ActiveRideMapScreen(
    viajeId: Long,
    onBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onChat: (Long) -> Unit = {},
    viajeViewModel: ViajeViewModel = viewModel(factory = AppViewModelFactory()),
    usuarioViewModel: UsuarioViewModel,
    ubicacionViewModel: UbicacionViewModel = viewModel(factory = AppViewModelFactory())
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val usuarioActual = usuarioViewModel.usuario
    val misViajes by viajeViewModel.misViajes.collectAsState()
    val viajesOtros by viajeViewModel.viajesOtros.collectAsState()
    val pasajeros by viajeViewModel.pasajerosViaje.collectAsState()
    val ubicacion by ubicacionViewModel.ubicacion.collectAsState()
    val isLoadingRides by viajeViewModel.isLoading.collectAsState()

    val roadManager: RoadManager = remember { OSRMRoadManager(context, "UamLift") }

    val viaje = remember(misViajes, viajesOtros, viajeId) {
        (misViajes + viajesOtros).firstOrNull { it.id == viajeId }
    }

    val esPasajero = remember(pasajeros, usuarioActual.id) { pasajeros.any { it.id == usuarioActual.id } }
    val esConductor = remember(viaje, usuarioActual.id) { viaje?.conductor?.id == usuarioActual.id }
    val esParticipante = esPasajero || esConductor

    BackHandler(enabled = viaje?.estadoViaje == EstadoViaje.EN_CURSO && esParticipante) { }

    val asientosLibres = remember(viaje?.numeroAsientosDisponibles, pasajeros.size) {
        val total = viaje?.numeroAsientosDisponibles ?: 0
        val ocupados = pasajeros.size
        if (total - ocupados < 0) 0 else total - ocupados
    }

    var cardExpandida by rememberSaveable { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var miUbicacionActual by remember { mutableStateOf<GeoPoint?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by rememberSaveable {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(viajeId, usuarioActual.id) {
        if (usuarioActual.id != null) {
            ubicacionViewModel.conectar(viajeId)
            viajeViewModel.obtenerPasajeros(viajeId)
            viajeViewModel.cargarViajesDesdeBackend(usuarioActual.id)
        }
    }

    LaunchedEffect(ubicacion) {
        if (ubicacion?.tipo == "ELIMINADO") {
            viajeViewModel.obtenerPasajeros(viajeId)
            usuarioActual.id?.let { viajeViewModel.cargarViajesDesdeBackend(it) }
        }
    }

    LaunchedEffect(misViajes, isLoadingRides) {
        val myId = usuarioActual.id ?: return@LaunchedEffect
        if (!isLoadingRides && viaje != null && viaje.conductor?.id != myId) {
            val sigueUnido = misViajes.any { it.id == viajeId }
            if (!sigueUnido) onBack()
        }
    }

    DisposableEffect(hasLocationPermission, viaje?.estadoViaje) {
        var locationCallback: LocationCallback? = null
        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build()
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(res: LocationResult) {
                    res.lastLocation?.let { location ->
                        if (location.latitude != 0.0 && location.longitude != 0.0) {
                            miUbicacionActual = GeoPoint(location.latitude, location.longitude)
                            if (esConductor && viaje?.estadoViaje == EstadoViaje.EN_CURSO) {
                                ubicacionViewModel.enviar(viajeId, location.latitude, location.longitude)
                            }
                        }
                    }
                }
            }
            try { fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper()) }
            catch (e: SecurityException) { }
        } else {
            if (!permissionRequested) {
                permissionRequested = true
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
        onDispose { locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) } }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Configuration.getInstance().load(context, context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
            Configuration.getInstance().userAgentValue = context.packageName
        }
    }

    val colorPrincipal = UAMColor.toArgb()

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text("¿Finalizar Viaje?", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = UAMColor)
            },
            text = {
                Text("¿Estás seguro de que quieres finalizar el viaje ahora? Se notificará a los pasajeros que han llegado a su destino.", fontSize = 15.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val miId = usuarioActual.id
                        if (miId != null) {
                            viajeViewModel.finalizarViaje(
                                viajeId = viajeId,
                                usuarioId = miId,
                                onExito = {
                                    scope.launch { snackbarHostState.showSnackbar("¡Viaje finalizado!") }
                                    onNavigateToHome()
                                },
                                onError = { error ->
                                    scope.launch { snackbarHostState.showSnackbar(error) }
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sí, finalizar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Volver", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        val center = GeoPoint(viaje?.origen?.latitud ?: 12.108038, viaje?.origen?.longitud ?: -86.257292)
                        controller.setCenter(center)
                        mapViewInstance = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mv ->
                    val latConductor = if (esConductor) (miUbicacionActual?.latitude ?: ubicacion?.latitud) else ubicacion?.latitud
                    val lngConductor = if (esConductor) (miUbicacionActual?.longitude ?: ubicacion?.longitud) else ubicacion?.longitud
                    if (viaje != null) {
                        dibujarRuta(mv, viaje, latConductor, lngConductor, roadManager, scope, colorPrincipal, miUbicacionActual, esConductor)
                    }
                }
            )

            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.End) {
                if (esParticipante) {
                    IconButton(onClick = { onChat(viajeId) }, modifier = Modifier.size(44.dp).background(UAMColor, CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.Chat, "Chat", tint = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                if (viaje != null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 16.dp, bottom = 16.dp), contentAlignment = Alignment.CenterEnd) {
                        FloatingActionButton(onClick = { miUbicacionActual?.let { mapViewInstance?.controller?.animateTo(it) } }, containerColor = Color.White, contentColor = UAMColor, modifier = Modifier.size(54.dp), shape = CircleShape) {
                            Icon(Icons.Default.MyLocation, "Ubicación")
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(16.dp)) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.fillMaxWidth().clickable { cardExpandida = !cardExpandida }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp)))
                            }

                            Row(modifier = Modifier.fillMaxWidth().clickable { cardExpandida = !cardExpandida }.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                val foto = viaje.conductor?.imagenUrl
                                val modelConductor = remember(foto) {
                                    if (foto.isNullOrBlank()) null else if (foto.startsWith("http")) foto else "${RetrofitClient.BASE_URL.trimEnd('/')}/${foto.trimStart('/')}"
                                }
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE0F7FA)), contentAlignment = Alignment.Center) {
                                    if (modelConductor != null) AsyncImage(model = modelConductor, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    else Text((viaje.conductor?.nombre?.take(1) ?: "C").uppercase(), fontWeight = FontWeight.Bold, color = UAMColor)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(viaje.conductor?.nombreUsuario ?: "Conductor", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("En camino", fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(if (cardExpandida) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, null, tint = Color.Gray)
                            }

                            AnimatedVisibility(visible = cardExpandida) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                                    Text("PASAJEROS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                                    if (pasajeros.isEmpty()) Text("Sin pasajeros", fontSize = 13.sp, color = Color.LightGray)
                                    else pasajeros.forEach { MiniPasajeroItem(it, esConductor, { viajeViewModel.eliminarPasajero(viajeId, usuarioActual.id ?: 0L, it.cif ?: "", {}, {}) }) }
                                }
                            }

                            if (esConductor) {
                                Button(onClick = { showConfirmDialog = true }, modifier = Modifier.fillMaxWidth().padding(20.dp).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), shape = RoundedCornerShape(12.dp)) {
                                    Text("Finalizar Viaje", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPasajeroItem(usuario: Usuario, puedeEliminar: Boolean, onEliminar: () -> Unit) {
    val foto = usuario.imagenUrl
    val model = remember(foto) { if (foto.isNullOrBlank()) null else if (foto.startsWith("http")) foto else "${RetrofitClient.BASE_URL.trimEnd('/')}/${foto.trimStart('/')}" }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
            if (model != null) AsyncImage(model = model, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text((usuario.nombre?.take(1) ?: "").uppercase(), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(usuario.nombreUsuario ?: "Pasajero", fontSize = 13.sp, modifier = Modifier.weight(1f))
        if (puedeEliminar) IconButton(onClick = onEliminar) { Icon(Icons.Default.PersonRemove, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) }
    }
}

private fun dibujarRuta(mapView: MapView, viaje: Viaje, conductorLat: Double?, conductorLng: Double?, roadManager: RoadManager, scope: CoroutineScope, colorPrincipal: Int, miUbicacion: GeoPoint?, soyElConductor: Boolean) {
    val context = mapView.context
    val cLat = conductorLat ?: viaje.origen?.latitud ?: return
    val cLng = conductorLng ?: viaje.origen?.longitud ?: return
    val dLat = viaje.destino?.latitud ?: return
    val dLng = viaje.destino?.longitud ?: return

    val carMarker = mapView.overlays.filterIsInstance<Marker>().find { it.title == "Conductor" }
    if (carMarker != null) {
        carMarker.position = GeoPoint(cLat, cLng)
    } else {
        val marker = Marker(mapView).apply {
            position = GeoPoint(cLat, cLng)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "Conductor"
            setInfoWindow(null)
            val d = ContextCompat.getDrawable(context, R.drawable.car_icon)
            val b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            d?.setBounds(0, 0, 100, 100)
            d?.draw(c)
            icon = BitmapDrawable(context.resources, b)
        }
        mapView.overlays.add(marker)
    }

    val destMarker = mapView.overlays.filterIsInstance<Marker>().find { it.snippet == "DEST" }
    if (destMarker == null) {
        val marker = Marker(mapView).apply {
            position = GeoPoint(dLat, dLng)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = viaje.destino?.nombre ?: "Destino"
            snippet = "DEST"
            setInfoWindow(null)
            icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)
        }
        mapView.overlays.add(marker)
    }

    scope.launch(Dispatchers.IO) {
        val road = roadManager.getRoad(arrayListOf(GeoPoint(cLat, cLng), GeoPoint(dLat, dLng)))
        if (road.mRouteHigh.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                mapView.overlays.filterIsInstance<Polyline>().forEach { mapView.overlays.remove(it) }
                val line = Polyline().apply { setPoints(road.mRouteHigh); outlinePaint.color = colorPrincipal; outlinePaint.strokeWidth = 8f }
                mapView.overlays.add(line)
                mapView.invalidate()
            }
        }
    }
}
