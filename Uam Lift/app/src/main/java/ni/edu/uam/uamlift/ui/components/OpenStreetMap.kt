package ni.edu.uam.uamlift.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import ni.edu.uam.uamlift.ui.theme.UAMColor
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    originLat: Double? = null,
    originLng: Double? = null,
    destLat: Double? = null,
    destLng: Double? = null,
    isSelectionEnabled: Boolean = false,
    isGesturesEnabled: Boolean = true,
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    // Cargar configuración de forma asíncrona una sola vez
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Configuration.getInstance().load(context, context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
            Configuration.getInstance().userAgentValue = context.packageName
        }
    }

    var myLocationOverlayState by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    
    // Configuración básica de OSMDroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
    Configuration.getInstance().userAgentValue = context.packageName

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(16.0)
            
            // Punto inicial por defecto (UAM)
            val uamPoint = GeoPoint(12.1126, -86.2435)
            controller.setCenter(uamPoint)
        }
    }

    // Overlay de mi ubicación
    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    DisposableEffect(mapView) {
        mapView.overlays.add(myLocationOverlay)
        
        if (isSelectionEnabled) {
            val receive = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    onLocationSelected(p.latitude, p.longitude)
                    mapView.controller.animateTo(p)
                    return true
                }
                override fun longPressHelper(p: GeoPoint): Boolean = false
            }
            mapView.overlays.add(MapEventsOverlay(receive))
        }

        onDispose {
            myLocationOverlay.disableMyLocation()
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(isGesturesEnabled)
                controller.setZoom(12.0)

                val uamPoint = GeoPoint(12.108038, -86.257292)
                controller.setCenter(uamPoint)

                val overlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                // No activar por defecto para ahorrar recursos, solo si es necesario
                // overlay.enableMyLocation() 
                myLocationOverlayState = overlay
                overlays.add(overlay)
            }
        },
        modifier = modifier,
        update = { view ->
            // Limpiar marcadores y líneas anteriores
            view.overlays.removeAll { it is Marker || it is Polyline }
            
            val points = mutableListOf<GeoPoint>()

            originLat?.let { lat ->
                originLng?.let { lng ->
                    val p = GeoPoint(lat, lng)
                    points.add(p)
                    val marker = Marker(view)
                    marker.position = p
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Origen"
                    view.overlays.add(marker)
                }
            }

            destLat?.let { lat ->
                destLng?.let { lng ->
                    val p = GeoPoint(lat, lng)
                    points.add(p)
                    val marker = Marker(view)
                    marker.position = p
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Destino"
                    view.overlays.add(marker)
                }

                val points = mutableListOf<GeoPoint>()

                originLat?.let { lat ->
                    originLng?.let { lng ->
                        val p = GeoPoint(lat, lng)
                        points.add(p)
                        val marker = Marker(view).apply {
                            position = p
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Origen"
                        }
                        view.overlays.add(marker)
                    }
                }

                destLat?.let { lat ->
                    destLng?.let { lng ->
                        val p = GeoPoint(lat, lng)
                        points.add(p)
                        val marker = Marker(view).apply {
                            position = p
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Destino"
                        }
                        view.overlays.add(marker)
                    }
                }

                if (originLat != null && originLng != null && destLat != null && destLng != null) {
                    val line = Polyline(view).apply {
                        setPoints(listOf(GeoPoint(originLat, originLng), GeoPoint(destLat, destLng)))
                        outlinePaint.color = android.graphics.Color.parseColor("#00796B")
                        outlinePaint.strokeWidth = 8f
                    }
                    view.overlays.add(line)
                }

                if (points.isNotEmpty()) {
                    val uniquePoints = points.distinctBy { "${it.latitude},${it.longitude}" }
                    if (uniquePoints.size <= 1) {
                        view.controller.setZoom(15.0)
                        view.controller.animateTo(uniquePoints.firstOrNull() ?: points[0])
                    } else {
                        val boundingBox = BoundingBox.fromGeoPoints(uniquePoints)
                        view.post {
                            try {
                                // Reducir el padding y usar animación suave solo si es necesario
                                view.zoomToBoundingBox(boundingBox, true, 150)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                view.invalidate()
            }
        },
        onRelease = { view ->
            try {
                myLocationOverlayState?.disableMyLocation()
                view.onDetach()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Dibujar la ruta si tenemos ambos puntos
            if (originLat != null && originLng != null && destLat != null && destLng != null) {
                val line = Polyline(view)
                line.setPoints(listOf(GeoPoint(originLat, originLng), GeoPoint(destLat, destLng)))
                line.outlinePaint.color = android.graphics.Color.parseColor("#00796B") // Color UAM o similar
                line.outlinePaint.strokeWidth = 8f
                view.overlays.add(line)
            }

            // Ajustar la vista para mostrar los puntos
            if (points.isNotEmpty()) {
                if (points.size == 1) {
                    view.controller.animateTo(points[0])
                } else {
                    // Zoom para mostrar ambos puntos
                    val boundingBox = BoundingBox.fromGeoPoints(points)
                    view.zoomToBoundingBox(boundingBox, true, 100)
                }
            }
            
            view.invalidate()
        }
    )
}
