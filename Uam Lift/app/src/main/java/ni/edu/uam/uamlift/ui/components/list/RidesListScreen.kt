package ni.edu.uam.uamlift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.ui.components.RideCard

@Composable
fun RidesListScreen(
    viajesList: List<Viaje>,
    usuarioIdActual: Long,
    onReservarClick: (Long) -> Unit
) {
    var selectedViaje by remember { mutableStateOf<Viaje?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(viajesList) { viaje ->
                RideCard(
                    viaje = viaje,
                    usuarioIdActual = usuarioIdActual,
                    esConductor = viaje.conductor?.id == usuarioIdActual,
                    onConfirmarClick = { idDelViaje ->
                        onReservarClick(idDelViaje)
                    }
                )
            }
        }
    }

        selectedViaje?.let { viaje ->
            val nombreConductor = viaje.conductor?.nombre ?: "Conductor UAM"
            val origen = viaje.origen?.nombre ?: "Origen"
            val destino = viaje.destino?.nombre ?: "Destino"

            AlertDialog(
                onDismissRequest = { selectedViaje = null },
                confirmButton = {
                    TextButton(onClick = {
                        viaje.id?.let { onReservarClick(it) }
                        selectedViaje = null
                    }) {
                        Text("Reservar Asiento")
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = nombreConductor, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text(
                            text = if (esConductor) "Tú eres el conductor" else "Conductor verificado",
                            fontSize = 12.sp, color = if (esConductor) UAMColor else Color(0xFF4CAF50)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "C$ ${viaje.precioPorPersona.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = UAMColor)
                    Text(text = "p/p", fontSize = 11.sp, color = grayText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(UAMColor, CircleShape))
                    Box(modifier = Modifier.width(2.dp).height(30.dp).background(UAMColor.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.size(8.dp).border(2.dp, UAMColor, CircleShape))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.height(56.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Text(text = origenTexto, fontSize = 14.sp, maxLines = 1, color = Color.DarkGray)
                    Text(text = destinoTexto, fontSize = 14.sp, maxLines = 1, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = grayText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Hoy, $horaTexto", fontSize = 13.sp, color = grayText)
                }

                if (esConductor) {
                    Button(
                        onClick = { onIniciarViaje(viaje.id ?: 0L) },
                        enabled = puedeIniciar,
                        colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Iniciar viaje", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(color = lightTealSeat.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "${viaje.numeroAsientosDisponibles} asientos libres",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UAMColor
                        )
                    }
                }
            }
        }
    }
}
