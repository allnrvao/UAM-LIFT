package ni.edu.uam.uamlift.ui.screens.rideInfo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
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
                        val center = GeoPoint(
                            viaje?.origen?.latitud ?: 12.108038,
                            viaje?.origen?.longitud ?: -86.257292
                        )
                        controller.setCenter(center)
                        mapViewInstance = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mv ->
                    val lat = ubicacion?.latitud ?: miUbicacionActual?.latitude
                    val lng = ubicacion?.longitud ?: miUbicacionActual?.longitude

                    if (viaje != null) {
                        dibujarRuta(mv, viaje, lat, lng, roadManager, scope, colorPrincipal)
                    }
                }
            )

            // Botón de Chat alineado a la DERECHA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (esParticipante) {
                    IconButton(
                        onClick = { onChat(viajeId) },
                        modifier = Modifier.size(44.dp).background(UAMColor, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, "Chat Grupal", tint = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                if (viaje != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, bottom = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        FloatingActionButton(
                            onClick = {
                                miUbicacionActual?.let {
                                    mapViewInstance?.controller?.animateTo(it)
                                }
                            },
                            containerColor = Color.White,
                            contentColor = UAMColor,
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { cardExpandida = !cardExpandida }
                                    .padding(top = 10.dp, bottom = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier.width(40.dp).height(4.dp)
                                        .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { cardExpandida = !cardExpandida }
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                val nombreConductor =
                                    viaje.conductor?.nombreUsuario?.takeIf { it.isNotBlank() }
                                        ?: "${viaje.conductor?.nombre ?: ""} ${viaje.conductor?.apellido ?: ""}".trim()
                                            .ifEmpty { "Conductor" }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val foto = viaje.conductor?.imagenUrl
                                    val modelConductor = remember(foto) {
                                        if (foto.isNullOrBlank()) null
                                        else if (foto.startsWith("http")) foto
                                        else "${RetrofitClient.BASE_URL.trimEnd('/')}/${
                                            foto.trimStart('/')
                                        }"
                                    }
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape)
                                            .background(Color(0xFFE0F7FA)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (modelConductor != null) {
                                            AsyncImage(
                                                model = modelConductor,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                (viaje.conductor?.nombre?.take(1)
                                                    ?: "C").uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = UAMColor
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            nombreConductor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text("Conductor", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Icon(
                                        if (cardExpandida) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        null,
                                        tint = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Asientos disponibles: $asientosLibres",
                                    fontSize = 13.sp,
                                    color = UAMColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            AnimatedVisibility(
                                visible = cardExpandida,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            "PASAJEROS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Gray,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (pasajeros.isEmpty()) {
                                            Text(
                                                "Sin pasajeros aún",
                                                fontSize = 13.sp,
                                                color = Color.LightGray
                                            )
                                        } else {
                                            pasajeros.forEach { pasajero ->
                                                MiniPasajeroItem(
                                                    usuario = pasajero,
                                                    puedeEliminar = viaje.conductor?.id == usuarioActual.id,
                                                    onEliminar = {
                                                        viajeViewModel.eliminarPasajero(
                                                            viajeId,
                                                            usuarioActual.id ?: 0L,
                                                            pasajero.cif ?: "",
                                                            onExito = {
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(
                                                                        "Asiento liberado"
                                                                    )
                                                                }
                                                            },
                                                            onError = { e ->
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar(e)
                                                                }
                                                            }
                                                        )
                                                    }
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Botón de Finalizar Viaje
                            if (esConductor) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val miId = usuarioActual.id
                                        if (miId != null) {
                                            viajeViewModel.finalizarViaje(
                                                viajeId = viajeId,
                                                usuarioId = miId,
                                                onExito = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Viaje finalizado. Se notificó a los pasajeros.")
                                                    }
                                                    onNavigateToHome()
                                                },
                                                onError = { error ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(error)
                                                    }
                                                }
                                            )
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Error: Usuario no identificado") }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE53935)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Finalizar Viaje",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        } // 1. Cierra el Column dentro del Card
                    } // 2. Cierra el Card
                } // 3. Cierra el if (viaje != null)
            } // 4. Cierra el Column de BottomCenter
        } // 5. Cierra el Box que envuelve AndroidView y todo lo demás
    } // 6. Cierra el Scaffold
} // 7. CIERRA ActiveRideMapScreen FINALMENTE!


// AHORA SÍ, ESTAS FUNCIONES ESTÁN FUERA, DONDE DEBEN ESTAR

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

private fun dibujarRuta(mapView: MapView, viaje: Viaje, currentLat: Double?, currentLng: Double?, roadManager: RoadManager, scope: CoroutineScope, colorPrincipal: Int) {
    val context = mapView.context

    val originLat = currentLat ?: viaje.origen?.latitud ?: return
    val originLng = currentLng ?: viaje.origen?.longitud ?: return

    val destLat = viaje.destino?.latitud ?: return
    val destLng = viaje.destino?.longitud ?: return

    val originalViajeLat = viaje.origen?.latitud
    val originalViajeLng = viaje.origen?.longitud

    val drawableOriginalCarro = ContextCompat.getDrawable(context, R.drawable.car_icon)
    val density = context.resources.displayMetrics.density
    val sizeInPx = (40 * density).toInt()
    val carBitmap = android.graphics.Bitmap.createBitmap(
        sizeInPx,
        sizeInPx,
        android.graphics.Bitmap.Config.ARGB_8888
    )
    val carCanvas = android.graphics.Canvas(carBitmap)
    carCanvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
    drawableOriginalCarro?.setBounds(0, 0, carCanvas.width, carCanvas.height)
    drawableOriginalCarro?.draw(carCanvas)
    val carIconDrawable = android.graphics.drawable.BitmapDrawable(context.resources, carBitmap)

    val existingPolylineActiva =
        mapView.overlays.filterIsInstance<Polyline>().find { it.id == "RutaActiva" }
    if (existingPolylineActiva != null) {
        val primerPuntoRuta = existingPolylineActiva.actualPoints.firstOrNull()
        if (primerPuntoRuta != null) {
            val distanciaMetros = primerPuntoRuta.distanceToAsDouble(GeoPoint(originLat, originLng))
            if (distanciaMetros < 15.0) {
                val startMarker =
                    mapView.overlays.filterIsInstance<Marker>().find { it.title == "Origen" }
                if (startMarker != null) {
                    startMarker.position = GeoPoint(originLat, originLng)
                    startMarker.icon = carIconDrawable
                }
                mapView.invalidate()
                return
            }
        }
    }

    val startMarker = mapView.overlays.filterIsInstance<Marker>().find { it.title == "Origen" }
    if (startMarker != null) {
        startMarker.position = GeoPoint(originLat, originLng)
        startMarker.icon = carIconDrawable
    } else {
        val newStartMarker = Marker(mapView).apply {
            position = GeoPoint(originLat, originLng)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "Origen"
            icon = carIconDrawable
            setInfoWindow(null)
        }
        mapView.overlays.add(newStartMarker)
    }

    val esHaciaUam = viaje.destino?.nombre?.contains("UAM", ignoreCase = true) == true
    val endMarker = mapView.overlays.filterIsInstance<Marker>().find { it.title == "Destino" }
    if (endMarker == null) {
        val newEndMarker = Marker(mapView).apply {
            position = GeoPoint(destLat, destLng)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "Destino"
            setInfoWindow(null)

            if (esHaciaUam) {
                val uamIconId = context.resources.getIdentifier("uam_icon_location", "drawable", context.packageName)
                if (uamIconId != 0) {
                    val drawableOriginal = ContextCompat.getDrawable(context, uamIconId)
                    if (drawableOriginal != null) {
                        val bitmapUam = Bitmap.createBitmap(sizeInPx, sizeInPx, Bitmap.Config.ARGB_8888)
                        val canvasUam = Canvas(bitmapUam)
                        canvasUam.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
                        drawableOriginal.setBounds(0, 0, canvasUam.width, canvasUam.height)
                        drawableOriginal.draw(canvasUam)
                        icon = BitmapDrawable(context.resources, bitmapUam)
                    }
                } else {
                    icon = android.graphics.drawable.ShapeDrawable(android.graphics.drawable.shapes.OvalShape()).apply {
                        intrinsicWidth = 40; intrinsicHeight = 40; paint.color = AndroidColor.parseColor("#E74C3C")
                    }
                }
            } else {
                icon = android.graphics.drawable.ShapeDrawable(android.graphics.drawable.shapes.OvalShape()).apply {
                    intrinsicWidth = 40; intrinsicHeight = 40; paint.color = AndroidColor.parseColor("#E74C3C")
                }
            }
        }
        mapView.overlays.add(newEndMarker)
    } else {
        endMarker.position = GeoPoint(destLat, destLng)
    }

    scope.launch(Dispatchers.IO) {
        try {
            if (originalViajeLat != null && originalViajeLng != null) {
                val existingEstatica = mapView.overlays.filterIsInstance<Polyline>().find { it.id == "RutaEstatica" }
                if (existingEstatica == null) {
                    val waypointsEstaticos = ArrayList<GeoPoint>()
                    waypointsEstaticos.add(GeoPoint(originalViajeLat, originalViajeLng))
                    waypointsEstaticos.add(GeoPoint(destLat, destLng))
                    val roadEstatica = roadManager.getRoad(waypointsEstaticos)

                    if (roadEstatica.mRouteHigh.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            val polylineEstatica = Polyline().apply {
                                id = "RutaEstatica"
                                setPoints(roadEstatica.mRouteHigh)
                                outlinePaint.color = AndroidColor.LTGRAY
                                outlinePaint.strokeWidth = 6f
                            }
                            mapView.overlays.add(0, polylineEstatica)
                        }
                    }
                }
            }

            val waypointsDinamicos = ArrayList<GeoPoint>()
            waypointsDinamicos.add(GeoPoint(originLat, originLng))
            waypointsDinamicos.add(GeoPoint(destLat, destLng))
            val roadDinamica = roadManager.getRoad(waypointsDinamicos)

            if (roadDinamica.mRouteHigh.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    existingPolylineActiva?.let { mapView.overlays.remove(it) }

                    val polylineActiva = Polyline().apply {
                        id = "RutaActiva"
                        setPoints(roadDinamica.mRouteHigh)
                        outlinePaint.color = colorPrincipal
                        outlinePaint.strokeWidth = 8f
                    }
                    mapView.overlays.add(polylineActiva)
                    mapView.invalidate()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}