package ni.edu.uam.uamlift.ui.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Componente de mapa OSM con:
 * - Desplazamiento fluido mejorado
 * - Soporte para selección de ubicación
 * - Dibuja ruta y ubicación actual del conductor
 */
@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    originLat: Double? = null,
    originLng: Double? = null,
    destLat: Double? = null,
    destLng: Double? = null,
    currentLat: Double? = null,
    currentLng: Double? = null,
    isSelectionEnabled: Boolean = false,
    isGesturesEnabled: Boolean = true,
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
            )
            Configuration.getInstance().userAgentValue = context.packageName
        }
    }

    val lastRoute = remember { mutableStateOf<String?>(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isHorizontalMapRepetitionEnabled = false
                isVerticalMapRepetitionEnabled = false
                controller.setZoom(13.0)
                controller.setCenter(GeoPoint(12.108038, -86.257292))
            }
        },
        modifier = modifier,
        update = { view ->
            val routeKey = "$originLat,$originLng,$destLat,$destLng,$currentLat,$currentLng,$isSelectionEnabled"

            if (lastRoute.value != routeKey) {
                lastRoute.value = routeKey

                view.setMultiTouchControls(isGesturesEnabled)
                view.overlays.removeAll { it is Marker || it is Polyline || it is MapEventsOverlay }

                if (isSelectionEnabled) {
                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            onLocationSelected(p.latitude, p.longitude)
                            view.controller.animateTo(p)
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    }
                    view.overlays.add(MapEventsOverlay(receiver))
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

                // Marcador de ubicación actual del conductor (ROJO y prominente)
                currentLat?.let { lat ->
                    currentLng?.let { lng ->
                        val p = GeoPoint(lat, lng)
                        points.add(p)
                        val marker = Marker(view).apply {
                            position = p
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = "Conductor"
                            setInfoWindow(null)
                            
                            val density = context.resources.displayMetrics.density
                            val size = (32 * density).toInt()
                            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                            
                            // Borde blanco para visibilidad
                            paint.color = android.graphics.Color.WHITE
                            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
                            
                            // Círculo rojo
                            paint.color = android.graphics.Color.RED
                            canvas.drawCircle(size / 2f, size / 2f, size / 2.6f, paint)
                            
                            // Punto blanco central
                            paint.color = android.graphics.Color.WHITE
                            canvas.drawCircle(size / 2f, size / 2f, size / 8f, paint)
                            
                            icon = android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
                        }
                        view.overlays.add(marker)
                    }
                }

                if (originLat != null && originLng != null && destLat != null && destLng != null) {
                    val line = Polyline(view).apply {
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
                    view.overlays.add(line)
                }

                if (points.isNotEmpty()) {
                    val uniquePoints = points.distinctBy { "${it.latitude},${it.longitude}" }
                    if (uniquePoints.size <= 1) {
                        view.controller.setZoom(17.0)
                        view.controller.animateTo(uniquePoints.firstOrNull() ?: points[0])
                    } else {
                        val boundingBox = BoundingBox.fromGeoPoints(uniquePoints)
                        view.post {
                            try {
                                view.zoomToBoundingBox(boundingBox, true, 120)
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
                view.onDetach()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
}