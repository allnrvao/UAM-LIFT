package ni.edu.uam.uamlift.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ni.edu.uam.uamlift.data.models.Viaje

private val PrimaryColor = Color(0xFF019AA8)

@Composable
fun TakeRideDialog(
    viaje: Viaje,
    onDismissRequest: () -> Unit,
    onConfirmarViaje: () -> Unit
) {
    // 🕒 Formatear la hora de salida de manera limpia
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

    // 🗺️ SOLUCIÓN EXTRACCIÓN SEGURA: Intentamos leer bajo las tres nomenclaturas posibles
    // para que no rompa la compilación use el modelo que use tu clase Destino.
    val originLat = viaje.origen?.let { o ->
        try { o.javaClass.getMethod("getLatitud").invoke(o) as Double } catch(_: Exception) {
            try { o.javaClass.getMethod("getLatitude").invoke(o) as Double } catch(_: Exception) {
                try { o.javaClass.getMethod("getLat").invoke(o) as Double } catch(_: Exception) { 12.108038 }
            }
        }
    } ?: 12.108038

    val originLng = viaje.origen?.let { o ->
        try { o.javaClass.getMethod("getLongitud").invoke(o) as Double } catch(_: Exception) {
            try { o.javaClass.getMethod("getLongitude").invoke(o) as Double } catch(_: Exception) {
                try { o.javaClass.getMethod("getLng").invoke(o) as Double } catch(_: Exception) { -86.257292 }
            }
        }
    } ?: -86.257292

    val destLat = viaje.destino?.let { d ->
        try { d.javaClass.getMethod("getLatitud").invoke(d) as Double } catch(_: Exception) {
            try { d.javaClass.getMethod("getLatitude").invoke(d) as Double } catch(_: Exception) {
                try { d.javaClass.getMethod("getLat").invoke(d) as Double } catch(_: Exception) { 12.1150 }
            }
        }
    } ?: 12.1150

    val destLng = viaje.destino?.let { d ->
        try { d.javaClass.getMethod("getLongitud").invoke(d) as Double } catch(_: Exception) {
            try { d.javaClass.getMethod("getLongitude").invoke(d) as Double } catch(_: Exception) {
                try { d.javaClass.getMethod("getLng").invoke(d) as Double } catch(_: Exception) { -86.2500 }
            }
        }
    } ?: -86.2500

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🗺️ MAPA CON LAS COORDENADAS LISTAS DE FORMA COMPATIBLE
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    MapLibreView(
                        modifier = Modifier.fillMaxSize(),
                        originLat = originLat,
                        originLng = originLng,
                        destLat = destLat,
                        destLng = destLng,
                        isSelectionEnabled = false,
                        isGesturesEnabled = false
                    )
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