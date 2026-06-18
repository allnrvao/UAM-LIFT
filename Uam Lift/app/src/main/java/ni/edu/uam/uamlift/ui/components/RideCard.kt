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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.ui.theme.UAMColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RideCard(
    viaje: Viaje,
    esConductor: Boolean = false,
    onConfirmarClick: (Long) -> Unit = {},
    onIniciarViaje: (Long) -> Unit = {}
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    val lightTealBg = Color(0xFFE0F7FA)
    val lightTealSeat = Color(0xFFB2EBF2)
    val grayText = Color(0xFF757575)

    val nombreConductor = "${viaje.conductor?.nombre ?: ""} ${viaje.conductor?.apellido ?: ""}".trim().ifEmpty { "Estudiante UAM" }
    val initials = (viaje.conductor?.nombre?.take(1) ?: "U") + (viaje.conductor?.apellido?.take(1) ?: "")

    val origenTexto = viaje.origen?.nombre ?: "Origen"
    val destinoTexto = viaje.destino?.nombre ?: "Destino"
    val horaTexto = viaje.fechaHoraSalida?.substringAfter("T")?.take(5) ?: "00:00"

    // Lógica para habilitar "Iniciar viaje" compatible con API 24
    val puedeIniciar = remember(viaje.fechaHoraSalida) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateSalida = sdf.parse(viaje.fechaHoraSalida ?: "")
            val now = Calendar.getInstance().time
            // Habilitar 15 minutos antes
            val fifteenMinsBefore = Calendar.getInstance().apply {
                time = dateSalida ?: now
                add(Calendar.MINUTE, -15)
            }.time
            now.after(fifteenMinsBefore)
        } catch (e: Exception) {
            true
        }
    }

    if (mostrarDialogo && !esConductor) {
        TakeRideDialog(
            viaje = viaje,
            onDismissRequest = { mostrarDialogo = false },
            onConfirmarViaje = {
                mostrarDialogo = false
                onConfirmarClick(viaje.id ?: 0L)
            }
        )
    }

    Card(
        onClick = { if (!esConductor) mostrarDialogo = true },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(lightTealBg)
                    ) {
                        Text(text = initials.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UAMColor)
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
