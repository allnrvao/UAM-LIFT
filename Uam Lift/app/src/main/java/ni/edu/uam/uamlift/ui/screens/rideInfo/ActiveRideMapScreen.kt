package ni.edu.uam.uamlift.ui.screens.rideInfo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    val roadManager: RoadManager = remember { OSRMRoadManager(context, "UamLift") }

    val viaje = remember(misViajes, viajesOtros, viajeId) {
        (misViajes + viajesOtros).firstOrNull { it.id == viajeId }
    }

    // Cálculo dinámico de asientos libres para el mapa
    val asientosLibres = remember(viaje?.numeroAsientosDisponibles, pasajeros.size) {
        val total = viaje?.numeroAsientosDisponibles ?: 0
        val ocupados = pasajeros.size
        if (total - ocupados < 0) 0 else total - ocupados
    }

    var cardExpandida by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Bandera para no repetir la solicitud de permisos en esta pantalla
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(viajeId) {
        ubicacionViewModel.conectar(viajeId)
        viajeViewModel.obtenerPasajeros(viajeId)
    }

    LaunchedEffect(viaje, hasLocationPermission) {
        val esConductor = viaje?.conductor?.id == usuarioActual.id
        val esViajeActivo = viaje?.estadoViaje == EstadoViaje.EN_CURSO
        
        if (esConductor && esViajeActivo && viaje != null) {
            if (!hasLocationPermission) {
                if (!permissionRequested) {
                    permissionRequested = true
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            } else {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).setMinUpdateIntervalMillis(3000).build()
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {}
                }
                try {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper())
                    while (true) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) ubicacionViewModel.enviar(viajeId, location.latitude, location.longitude)
                        }
                        delay(5000)
                    }
                } catch (e: SecurityException) {
                } finally {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Configuration.getInstance().load(context, context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
            Configuration.getInstance().userAgentValue = context.packageName
        }
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
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mv ->
                    if (viaje != null) dibujarRuta(mv, viaje, roadManager, scope)
                    val lat = ubicacion?.latitud
                    val lng = ubicacion?.longitud
                    if (lat != null && lng != null) actualizarPosicionCarro(mv, lat, lng, context)
                }
            )

            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(16.dp)
                    .statusBarsPadding()
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .align(Alignment.TopStart)
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.Black) }

            Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                if (viaje != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().clickable { cardExpandida = !cardExpandida }.padding(top = 10.dp, bottom = 4.dp),
                                contentAlignment = Alignment.Center
                            ) { Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))) }

                            Column(
                                modifier = Modifier.fillMaxWidth().clickable { cardExpandida = !cardExpandida }.padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                val nombreConductor = viaje.conductor?.nombreUsuario?.takeIf { it.isNotBlank() } 
                                    ?: "${viaje.conductor?.nombre ?: ""} ${viaje.conductor?.apellido ?: ""}".trim().ifEmpty { "Conductor" }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    val foto = viaje.conductor?.imagenUrl
                                    val modelConductor = remember(foto) {
                                        if (foto.isNullOrBlank()) null
                                        else if (foto.startsWith("http")) foto
                                        else "${RetrofitClient.BASE_URL.trimEnd('/')}/${foto.trimStart('/')}"
                                    }
                                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE0F7FA)), contentAlignment = Alignment.Center) {
                                        if (modelConductor != null) {
                                            AsyncImage(model = modelConductor, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Text((viaje.conductor?.nombre?.take(1) ?: "C").uppercase(), fontWeight = FontWeight.Bold, color = UAMColor)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(nombreConductor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("Conductor", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Icon(if (cardExpandida) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, null, tint = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                // MOSTRAR ASIENTOS LIBRES REALES
                                Text("Asientos disponibles: $asientosLibres", fontSize = 13.sp, color = UAMColor, fontWeight = FontWeight.Bold)
                            }

                            AnimatedVisibility(visible = cardExpandida, enter = expandVertically(), exit = shrinkVertically()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                                        Text("PASAJEROS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray, letterSpacing = 1.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (pasajeros.isEmpty()) {
                                            Text("Sin pasajeros aún", fontSize = 13.sp, color = Color.LightGray)
                                        } else {
                                            pasajeros.forEach { pasajero ->
                                                MiniPasajeroItem(
                                                    usuario = pasajero,
                                                    puedeEliminar = viaje.conductor?.id == usuarioActual.id,
                                                    onEliminar = {
                                                        viajeViewModel.eliminarPasajero(viajeId, usuarioActual.id ?: 0L, pasajero.cif ?: "",
                                                            onExito = { scope.launch { snackbarHostState.showSnackbar("Asiento liberado") } },
                                                            onError = { e -> scope.launch { snackbarHostState.showSnackbar(e) } }
                                                        )
                                                    }
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                            }
                                        }
                                    }
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
    val modelPasajero = remember(foto) {
        if (foto.isNullOrBlank()) null
        else if (foto.startsWith("http")) foto
        else "${RetrofitClient.BASE_URL.trimEnd('/')}/${foto.trimStart('/')}"
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
            if (modelPasajero != null) AsyncImage(model = modelPasajero, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text((usuario.nombre?.take(1) ?: "").uppercase(), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(usuario.nombreUsuario ?: "Pasajero", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (puedeEliminar) {
            IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.PersonRemove, "Eliminar", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun dibujarRuta(mapView: MapView, viaje: Viaje, roadManager: RoadManager, scope: CoroutineScope) {
    val originLat = viaje.origen?.latitud ?: return
    val originLng = viaje.origen.longitud ?: return
    val destLat = viaje.destino?.latitud ?: return
    val destLng = viaje.destino.longitud ?: return
    val existingPolyline = mapView.overlays.filterIsInstance<Polyline>().firstOrNull()
    if (existingPolyline != null) return 
    if (!mapView.overlays.any { it is Marker && it.title == "Origen" }) {
        val startMarker = Marker(mapView).apply { position = GeoPoint(originLat, originLng); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); title = "Origen" }
        mapView.overlays.add(startMarker)
    }
    if (!mapView.overlays.any { it is Marker && it.title == "Destino" }) {
        val endMarker = Marker(mapView).apply { position = GeoPoint(destLat, destLng); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); title = "Destino" }
        mapView.overlays.add(endMarker)
    }
    scope.launch(Dispatchers.IO) {
        try {
            val waypoints = ArrayList<GeoPoint>()
            waypoints.add(GeoPoint(originLat, originLng))
            waypoints.add(GeoPoint(destLat, destLng))
            val road = roadManager.getRoad(waypoints)
            if (road.mRouteHigh.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val polyline = Polyline().apply { setPoints(road.mRouteHigh); outlinePaint.color = AndroidColor.BLUE; outlinePaint.strokeWidth = 5f }
                    mapView.overlays.add(polyline)
                    mapView.invalidate()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}

private fun actualizarPosicionCarro(mapView: MapView, lat: Double, lng: Double, context: Context) {
    val existingMarker = mapView.overlays.filterIsInstance<Marker>().find { it.title == "Carro" }
    if (existingMarker != null) existingMarker.position = GeoPoint(lat, lng)
    else {
        val marker = Marker(mapView).apply {
            position = GeoPoint(lat, lng); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            val drawable = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_compass)
            if (drawable != null) icon = drawable
            title = "Carro"
        }
        mapView.overlays.add(marker)
    }
    mapView.invalidate()
}
