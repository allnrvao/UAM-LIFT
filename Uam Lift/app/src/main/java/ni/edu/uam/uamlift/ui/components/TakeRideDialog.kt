package ni.edu.uam.uamlift.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ni.edu.uam.uamlift.data.models.Viaje

private val PrimaryColor = Color(0xFF019AA8)

@Composable
fun TakeRideDialog(
    viaje: Viaje,
    onDismissRequest: () -> Unit,
    onConfirmarViaje: () -> Unit
) {
<<<<<<< Updated upstream
    // 🗺️ Coordenadas quemadas de prueba según el conductor para mitigar que tu modelo Destino no tenga lat/long.
    val ubicacionOrigen = when (viaje.conductor?.nombre) {
        "Luis Casco" -> LatLng(12.128, -86.265)    // Coordenadas aproximadas de Metrocentro
        "Fernando Gomez" -> LatLng(11.930, -85.955) // Coordenadas aproximadas de Granada
        else -> LatLng(12.112, -86.243)             // UAM por defecto
    }

    val cameraPositionState = rememberCameraPositionState {
        // ✨ Corregido: Sin el import erróneo de SnapPosition, ahora toma la propiedad nativa del mapa.
        position = CameraPosition.fromLatLngZoom(ubicacionOrigen, 14f)
    }

    // Actualiza la cámara del mapa si el viaje cambia dinámicamente.
    LaunchedEffect(viaje) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(ubicacionOrigen, 14f)
    }

    // 🕒 Formatear la hora de salida de manera limpia.
=======
    // 📍 1. Obtener las coordenadas reales del viaje (con respaldos seguros por si vienen nulos)
    val originLat = viaje.origen?.latitud ?: 12.108038
    val originLng = viaje.origen?.longitud ?: -86.257292
    val destLat = viaje.destino?.latitud ?: 12.1150
    val destLng = viaje.destino?.longitud ?: -86.2500

    // 🕒 Formatear la hora de salida de manera limpia
>>>>>>> Stashed changes
    val horaFormateada = try {
        val s = viaje.fechaHoraSalida ?: ""
        if (s.contains("T")) {
            s.split("T")[1].substring(0, 5)
        } else {
            s
        }
    } catch (_: Exception) {
        viaje.fechaHoraSalida ?: "Hora no disponible"
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
<<<<<<< Updated upstream
            border = androidx.compose.foundation.BorderStroke(8.dp, PrimaryColor)
=======
            border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryColor) // 👈 Reducido de 8.dp a 2.dp para evitar overlapping visual
>>>>>>> Stashed changes
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
<<<<<<< Updated upstream
                // 🗺️ MAPA RENDERIZADO CORRECTAMENTE
=======
                // 🗺️ CORRECCIÓN: MAPA CON PARÁMETROS REALES ASIGNADOS
>>>>>>> Stashed changes
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
<<<<<<< Updated upstream
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = com.google.maps.android.compose.MapUiSettings(
                            zoomControlsEnabled = false,
                            scrollGesturesEnabled = false,
                            zoomGesturesEnabled = false
                        )
                    ) {
                        Marker(
                            state = MarkerState(position = ubicacionOrigen),
                            title = "Origen: ${viaje.origen?.nombre}",
                            snippet = "Destino: ${viaje.destino?.nombre}"
                        )
                    }
=======
                    MapLibreView(
                        modifier = Modifier.fillMaxSize(),
                        originLat = originLat,
                        originLng = originLng,
                        destLat = destLat,
                        destLng = destLng,
                        isSelectionEnabled = false,
                        isGesturesEnabled = false // Evita interferir con el comportamiento del Dialog
                    )
>>>>>>> Stashed changes
                }

                // 💵 INFORMACIÓN DE VIAJE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viaje.conductor?.nombre ?: "Conductor Desconocido",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Salida: $horaFormateada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = "C$ ${viaje.precioPorPersona.toInt()}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }

                // 🔘 BOTÓN "TOMAR VIAJE"
                Button(
                    onClick = onConfirmarViaje,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Tomar Viaje",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}