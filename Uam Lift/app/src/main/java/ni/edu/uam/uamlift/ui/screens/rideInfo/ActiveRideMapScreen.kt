package ni.edu.uam.uamlift.ui.screens.rideInfo

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.data.models.Usuario
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.UbicacionViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.ui.theme.UAMColor
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

@Composable
fun ActiveRideMapScreen(
    viajeId: Long,
    onBack: () -> Unit = {},
    viajeViewModel: ViajeViewModel = viewModel(factory = AppViewModelFactory()),
    ubicacionViewModel: UbicacionViewModel = viewModel(factory = AppViewModelFactory())
) {
    val context = LocalContext.current
    val misViajes by viajeViewModel.misViajes.collectAsState()
    val viajesOtros by viajeViewModel.viajesOtros.collectAsState()
    val pasajeros by viajeViewModel.pasajerosViaje.collectAsState()
    val ubicacion by ubicacionViewModel.ubicacion.collectAsState()

    // Buscar el viaje en ambas listas
    val viaje = remember(misViajes, viajesOtros) {
        (misViajes + viajesOtros).firstOrNull { it.id == viajeId }
    }

    var cardExpandida by remember { mutableStateOf(false) }

    // Conectar WebSocket para recibir ubicación en tiempo real
    LaunchedEffect(viajeId) {
        ubicacionViewModel.conectar(viajeId)
        viajeViewModel.obtenerPasajeros(viajeId)
    }

    // Cargar configuración OSM
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
            )
            Configuration.getInstance().userAgentValue = context.packageName
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── MAPA ──────────────────────────────────────────────────────────────
        val mapViewRef = remember { mutableStateOf<MapView?>(null) }

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    // Mejorar el desplazamiento del mapa: scroll y zoom sin interferencia
                    isHorizontalMapRepetitionEnabled = false
                    isVerticalMapRepetitionEnabled = false
                    // Velocidad de scroll mejorada
                    controller.setZoom(14.0)
                    val center = GeoPoint(
                        viaje?.origen?.latitud ?: 12.108038,
                        viaje?.origen?.longitud ?: -86.257292
                    )
                    controller.setCenter(center)
                    mapViewRef.value = this
                }.also { mv ->
                    // Dibujar ruta inicial si hay origen y destino
                    dibujarRuta(mv, viaje)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                // Actualizar posición del carro con la ubicación del WebSocket
                val lat = ubicacion?.latitud
                val lng = ubicacion?.longitud
                if (lat != null && lng != null) {
                    actualizarPosicionCarro(mv, lat, lng, context)
                }
                // Re-dibujar ruta si el viaje tiene coordenadas
                dibujarRuta(mv, viaje)
            },
            onRelease = { mv ->
                try { mv.onDetach() } catch (_: Exception) {}
            }
        )

        // ── BOTÓN VOLVER ──────────────────────────────────────────────────────
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
                .size(44.dp)
                .background(Color.White, CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.Black
            )
        }

        // ── CARD INFERIOR ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (viaje != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {

                        // Indicador de deslizamiento
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { cardExpandida = !cardExpandida }
                                .padding(top = 10.dp, bottom = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
                            )
                        }

                        // Información principal (siempre visible)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { cardExpandida = !cardExpandida }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            // Nombre del conductor
                            val nombreConductor =
                                viaje.conductor?.nombreUsuario?.takeIf { it.isNotBlank() }
                                    ?: "${viaje.conductor?.nombre ?: ""} ${viaje.conductor?.apellido ?: ""}".trim()
                                        .ifEmpty { "Conductor" }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Avatar conductor
                                val foto = viaje.conductor?.imagenUrl
                                val initials = (viaje.conductor?.nombre?.take(1) ?: "C") +
                                        (viaje.conductor?.apellido?.take(1) ?: "")
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0F7FA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!foto.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = foto,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = initials.uppercase(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = UAMColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nombreConductor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Conductor",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Icon(
                                    if (cardExpandida) Icons.Default.KeyboardArrowDown
                                    else Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Ruta origen → destino
                            val origen = viaje.origen?.nombre ?: "Origen"
                            val destino = viaje.destino?.nombre ?: "Destino"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = UAMColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = origen,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "  →  ",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = destino,
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Sección expandible: pasajeros
                        AnimatedVisibility(
                            visible = cardExpandida,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "PASAJEROS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Gray,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (pasajeros.isEmpty()) {
                                        Text(
                                            text = "Sin pasajeros aún",
                                            fontSize = 13.sp,
                                            color = Color.LightGray
                                        )
                                    } else {
                                        pasajeros.forEach { pasajero ->
                                            MiniPasajeroItem(pasajero)
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Espacio para el navigation bar
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPasajeroItem(usuario: Usuario) {
    val initials = (usuario.nombre?.take(1) ?: "") + (usuario.apellido?.take(1) ?: "")
    val displayName = when {
        !usuario.nombreUsuario.isNullOrEmpty() -> usuario.nombreUsuario!!
        "${usuario.nombre ?: ""} ${usuario.apellido ?: ""}".trim().isNotEmpty() ->
            "${usuario.nombre ?: ""} ${usuario.apellido ?: ""}".trim()
        else -> "Pasajero"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            if (!usuario.imagenUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = usuario.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials.uppercase().ifEmpty { "?" },
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Funciones de mapa ──────────────────────────────────────────────────────

private fun dibujarRuta(mapView: MapView, viaje: Viaje?) {
    if (viaje == null) return
    val originLat = viaje.origen?.latitud ?: return
    val originLng = viaje.origen?.longitud ?: return
    val destLat = viaje.destino?.latitud ?: return
    val destLng = viaje.destino?.longitud ?: return

    // Eliminar marcadores y rutas existentes (no el marcador del carro)
    mapView.overlays.removeAll { overlay ->
        (overlay is Marker && overlay.title != "Carro") || overlay is Polyline
    }

    // Marcador origen
    val markerOrigen = Marker(mapView).apply {
        position = GeoPoint(originLat, originLng)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Origen"
    }
    mapView.overlays.add(markerOrigen)

    // Marcador destino
    val markerDestino = Marker(mapView).apply {
        position = GeoPoint(destLat, destLng)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Destino"
    }
    mapView.overlays.add(markerDestino)

    // Línea de ruta
    val linea = Polyline(mapView).apply {
        setPoints(
            listOf(
                GeoPoint(originLat, originLng),
                GeoPoint(destLat, destLng)
            )
        )
        outlinePaint.color = android.graphics.Color.parseColor("#019AA8")
        outlinePaint.strokeWidth = 10f
        outlinePaint.isAntiAlias = true
    }
    mapView.overlays.add(linea)

    // Centrar mapa en la ruta
    val puntos = listOf(GeoPoint(originLat, originLng), GeoPoint(destLat, destLng))
    val boundingBox = BoundingBox.fromGeoPoints(puntos)
    mapView.post {
        try {
            mapView.zoomToBoundingBox(boundingBox, true, 120)
        } catch (_: Exception) {}
    }

    mapView.invalidate()
}

/** Dibuja o actualiza el marcador del carro en la posición GPS actual */
private fun actualizarPosicionCarro(
    mapView: MapView,
    lat: Double,
    lng: Double,
    context: Context
) {
    // Buscar marcador existente del carro
    val markerExistente = mapView.overlays.filterIsInstance<Marker>()
        .firstOrNull { it.title == "Carro" }

    val punto = GeoPoint(lat, lng)

    if (markerExistente != null) {
        markerExistente.position = punto
    } else {
        val marker = Marker(mapView).apply {
            position = punto
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "Carro"
            // Ícono de carro dibujado programáticamente
            icon = crearIconoCarro(context)
        }
        mapView.overlays.add(marker)
    }

    // Animar cámara hacia el carro
    mapView.controller.animateTo(punto)
    mapView.invalidate()
}

/** Crea un bitmap con forma de carro para el marcador del mapa */
private fun crearIconoCarro(context: Context): android.graphics.drawable.BitmapDrawable {
    val size = (context.resources.displayMetrics.density * 48).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paintCarro = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#019AA8")
        style = Paint.Style.FILL
    }
    val paintBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = size * 0.06f
    }

    val w = size.toFloat()
    val h = size.toFloat()

    // Cuerpo del carro (rectángulo redondeado)
    val carroPath = Path().apply {
        val left = w * 0.10f
        val right = w * 0.90f
        val top = h * 0.30f
        val bottom = h * 0.72f
        val r = h * 0.10f
        addRoundRect(
            android.graphics.RectF(left, top, right, bottom),
            r, r, Path.Direction.CW
        )
    }
    canvas.drawPath(carroPath, paintCarro)
    canvas.drawPath(carroPath, paintBorde)

    // Techo del carro
    val techoPath = Path().apply {
        moveTo(w * 0.28f, h * 0.30f)
        lineTo(w * 0.35f, h * 0.14f)
        lineTo(w * 0.65f, h * 0.14f)
        lineTo(w * 0.72f, h * 0.30f)
        close()
    }
    canvas.drawPath(techoPath, paintCarro)
    canvas.drawPath(techoPath, paintBorde)

    // Ruedas
    val paintRueda = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#333333")
        style = Paint.Style.FILL
    }
    val radioRueda = w * 0.11f
    // Rueda delantera
    canvas.drawCircle(w * 0.72f, h * 0.76f, radioRueda, paintRueda)
    canvas.drawCircle(w * 0.72f, h * 0.76f, radioRueda, paintBorde)
    // Rueda trasera
    canvas.drawCircle(w * 0.28f, h * 0.76f, radioRueda, paintRueda)
    canvas.drawCircle(w * 0.28f, h * 0.76f, radioRueda, paintBorde)

    // Ventana
    val paintVentana = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#B2EBF2")
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(
        android.graphics.RectF(w * 0.30f, h * 0.17f, w * 0.70f, h * 0.28f),
        w * 0.04f, w * 0.04f, paintVentana
    )

    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}