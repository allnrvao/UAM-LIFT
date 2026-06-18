package ni.edu.uam.uamlift.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
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
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    
    // Configuración básica de OSMDroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
    Configuration.getInstance().userAgentValue = context.packageName

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(17.0)
            
            // Punto inicial por defecto (UAM)
            val uamPoint = GeoPoint(12.1126, -86.2435)
            controller.setCenter(uamPoint)
        }
    }

    // Overlay de mi ubicación
    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            // Esto hará que el mapa siga la ubicación del usuario al inicio
            runOnFirstFix {
                mapView.post {
                    if (originLat == null && destLat == null) {
                        mapView.controller.animateTo(myLocation)
                    }
                }
            }
        }
    }

    DisposableEffect(mapView) {
        mapView.overlays.add(myLocationOverlay)
        
        if (isSelectionEnabled) {
            val receive = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    onLocationSelected(p.latitude, p.longitude)
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
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            // Limpiar marcadores anteriores
            view.overlays.removeAll { it is Marker }
            
            originLat?.let { lat ->
                originLng?.let { lng ->
                    val marker = Marker(view)
                    marker.position = GeoPoint(lat, lng)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Origen"
                    view.overlays.add(marker)
                }
            }

            destLat?.let { lat ->
                destLng?.let { lng ->
                    val marker = Marker(view)
                    marker.position = GeoPoint(lat, lng)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Destino"
                    view.overlays.add(marker)
                }
            }
            
            view.invalidate()
        }
    )
}
