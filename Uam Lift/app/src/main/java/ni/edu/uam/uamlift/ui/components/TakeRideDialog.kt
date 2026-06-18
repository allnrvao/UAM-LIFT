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
    // Coordenadas reales del objeto Viaje
    val originLat = viaje.origen?.latitud
    val originLng = viaje.origen?.longitud
    val destLat = viaje.destino?.latitud
    val destLng = viaje.destino?.longitud

    //🕒 Formatear la hora de salida
    val horaFormateada = try {
        val s = viaje.fechaHoraSalida ?: ""
        if (s.contains("T")) s.split("T")[1].substring(0, 5) else s
    } catch (_: Exception) {
        "Hora no disponible"
    }

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
                Text(
                    text = "Detalles del Viaje",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )

                // 🗺️ MAPA CON OSMDROID
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    MapLibreView(
                        originLat = originLat,
                        originLng = originLng,
                        destLat = destLat,
                        destLng = destLng
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
                            text = viaje.conductor?.nombre ?: "Conductor",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Hacia: ${viaje.destino?.nombre ?: "UAM"}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Salida: $horaFormateada",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "C$ ${viaje.precioPorPersona.toInt()}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Text(text = "por persona", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                // 🔘 BOTONES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Cerrar")
                    }

                    Button(
                        onClick = onConfirmarViaje,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Tomar Viaje", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
