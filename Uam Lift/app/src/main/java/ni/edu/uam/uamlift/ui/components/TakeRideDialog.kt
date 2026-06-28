package ni.edu.uam.uamlift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.ui.theme.UAMColor

@Composable
fun TakeRideDialog(
    viaje: Viaje,
    esPasajero: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmarViaje: () -> Unit,
    onCancelarParticipacion: () -> Unit
) {
    val horaFormateada = remember(viaje.fechaHoraSalida) {
        try {
            val s = viaje.fechaHoraSalida ?: ""
            if (s.contains("T")) s.split("T")[1].substring(0, 5) else s
        } catch (_: Exception) {
            viaje.fechaHoraSalida ?: "00:00"
        }
    }

    val asientosLibres = remember(viaje.numeroAsientosDisponibles, viaje.pasajeros?.size) {
        val pasajerosCount = viaje.pasajeros?.size ?: 0
        val libres = viaje.numeroAsientosDisponibles - pasajerosCount
        if (libres < 0) 0 else libres
    }

    // Priorizamos nombreUsuario sobre el nombre real
    val nombreConductor = viaje.conductor?.nombreUsuario?.takeIf { it.isNotBlank() }
        ?: "${viaje.conductor?.nombre ?: "Conductor"} ${viaje.conductor?.apellido ?: ""}".trim()

    val viajeEnCurso = viaje.estadoViaje == EstadoViaje.EN_CURSO

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismissRequest) { Icon(Icons.Default.Close, "Cerrar") }
                    Text(text = "Detalles del Viaje", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Box(modifier = Modifier.size(48.dp))
                }

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                    Card(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(24.dp)) {
                        MapLibreView(
                            modifier = Modifier.fillMaxSize(),
                            originLat = viaje.origen?.latitud ?: 12.108,
                            originLng = viaje.origen?.longitud ?: -86.257,
                            destLat = viaje.destino?.latitud ?: 12.115,
                            destLng = viaje.destino?.longitud ?: -86.250,
                            isSelectionEnabled = false,
                            isGesturesEnabled = true
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(54.dp).clip(CircleShape).background(UAMColor.copy(alpha = 0.1f)), Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = UAMColor, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = nombreConductor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(text = "Conductor verificado", color = Color.Gray, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(24.dp))

                    Text(text = "Vehículo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UAMColor)
                    DetailItem(Icons.Default.DirectionsCar, "${viaje.carro?.marca ?: ""} ${viaje.carro?.modelo ?: ""}")
                    DetailItem(Icons.Default.Numbers, "Placa: ${viaje.carro?.placa ?: "N/A"}")

                    Spacer(Modifier.height(24.dp))

                    Text(text = "Ruta y Aporte", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UAMColor)
                    DetailItem(Icons.Default.Schedule, "Salida programada: $horaFormateada")
                    DetailItem(Icons.Default.EventSeat, "$asientosLibres asientos libres actualmente")
                    DetailItem(Icons.Default.Payments, "Aporte: C$ ${viaje.precioPorPersona.toInt()} p/p")

                    Spacer(Modifier.height(40.dp))

                    if (esPasajero) {
                        Button(
                            onClick = onCancelarParticipacion,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !viajeEnCurso,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viajeEnCurso) Color.LightGray else Color(0xFFFFEBEE),
                                contentColor = if (viajeEnCurso) Color.Gray else Color.Red
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Cancel, null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (viajeEnCurso) "Viaje en curso" else "Cancelar participación",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (viajeEnCurso) {
                            Text(
                                text = "No puedes cancelar una vez iniciado el viaje",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                            )
                        }
                    } else {
                        Button(
                            onClick = onConfirmarViaje,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UAMColor, contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            enabled = asientosLibres > 0 && !viajeEnCurso
                        ) {
                            Text(
                                text = if (viajeEnCurso) "Viaje en curso" else if (asientosLibres > 0) "Tomar Viaje" else "Viaje Lleno",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 15.sp, color = Color.Black)
    }
}
