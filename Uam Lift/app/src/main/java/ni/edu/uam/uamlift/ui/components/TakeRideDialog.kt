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
    // 🕒 Formatear la hora de salida
    val horaFormateada = remember(viaje.fechaHoraSalida) {
        try {
            val s = viaje.fechaHoraSalida ?: ""
            if (s.contains("T")) {
                s.split("T")[1].substring(0, 5)
            } else {
                s
            }
        } catch (_: Exception) {
            viaje.fechaHoraSalida ?: "00:00"
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header con botón de cerrar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                    Text(
                        text = "Detalles del Viaje",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Box(modifier = Modifier.size(48.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    // 🗺️ MAPA (Visualización de la ruta)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        MapLibreView(
                            modifier = Modifier.fillMaxSize(),
                            originLat = viaje.origen?.latitud ?: 12.108038,
                            originLng = viaje.origen?.longitud ?: -86.257292,
                            destLat = viaje.destino?.latitud ?: 12.1150,
                            destLng = viaje.destino?.longitud ?: -86.2500,
                            isSelectionEnabled = false,
                            isGesturesEnabled = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 👤 INFORMACIÓN DEL CONDUCTOR
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(UAMColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = UAMColor,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${viaje.conductor?.nombre ?: ""} ${viaje.conductor?.apellido ?: ""}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Conductor verificado",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(24.dp))

                    // 🚗 DETALLES DEL VEHÍCULO
                    Text(
                        text = "Vehículo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = UAMColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailItem(
                        icon = Icons.Default.DirectionsCar,
                        text = "${viaje.carro?.marca ?: "Marca"} ${viaje.carro?.modelo ?: "Modelo"}"
                    )
                    DetailItem(
                        icon = Icons.Default.Numbers,
                        text = "Placa: ${viaje.carro?.placa ?: "N/A"}"
                    )
                    DetailItem(
                        icon = Icons.Default.Palette,
                        text = "Color: ${viaje.carro?.color ?: "N/A"}"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 📍 RUTA Y COSTO DEL VIAJE
                    Text(
                        text = "Ruta y Aporte",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = UAMColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailItem(
                        icon = Icons.Default.Schedule,
                        text = "Salida programada: $horaFormateada"
                    )
                    DetailItem(
                        icon = Icons.Default.EventSeat,
                        text = "${viaje.numeroAsientosDisponibles} asientos disponibles"
                    )
                    DetailItem(
                        icon = Icons.Default.Payments,
                        text = "Aporte: C$ ${viaje.precioPorPersona.toInt()} por pasajero"
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // 🔘 BOTÓN DE ACCIÓN DINÁMICO
                    if (esPasajero) {
                        // Si ya es pasajero, permite cancelar la participación
                        Button(
                            onClick = onCancelarParticipacion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFEBEE),
                                contentColor = Color.Red
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancelar mi participación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        // Si no es pasajero, permite tomar el viaje
                        Button(
                            onClick = onConfirmarViaje,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UAMColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = viaje.numeroAsientosDisponibles > 0
                        ) {
                            Text(
                                text = if (viaje.numeroAsientosDisponibles > 0) "Tomar Viaje" else "Sin asientos disponibles",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color.DarkGray
        )
    }
}
