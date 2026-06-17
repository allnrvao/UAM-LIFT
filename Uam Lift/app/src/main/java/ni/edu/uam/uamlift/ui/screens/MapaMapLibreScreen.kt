package ni.edu.uam.uamlift.ui.screens

import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ni.edu.uam.uamlift.viewmodel.ViajeViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MapaMapLibreScreen(viewModel: ViajeViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Inicializamos MapLibre con el contexto
    remember { MapLibre.getInstance(context) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                getMapAsync { map ->
                    // Cargamos un estilo (puedes usar MapTiler o cualquier servidor de mapas)
                    map.setStyle(Style.getPredefinedStyle("Streets")) { style ->
                        // Aquí podrías añadir capas para los carritos
                    }
                }
            }
        },
        update = { mapView ->
            // Aquí puedes actualizar los marcadores usando viewModel.viajesActivos
            // MapLibre usa 'Annotations' o 'SymbolLayer' para los íconos de los carritos
        }
    )
}