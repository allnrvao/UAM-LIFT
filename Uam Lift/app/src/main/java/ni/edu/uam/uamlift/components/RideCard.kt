package ni.edu.uam.uamlift.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor

@Composable
fun RideCard(
    initials: String,
    name: String,
    rating: String,
    trips: Int,
    from: String,
    to: String,
    time: String,
    price: String, // Ejemplo: "Q25"
    seats: Int
) {
    // Paleta de colores basada en la imagen
    val lightTealBg = Color(0xFFE0F7FA) // Fondo del avatar de iniciales
    val lightTealSeat = Color(0xFFB2EBF2) // Color de los círculos de asientos
    val grayText = Color(0xFF757575) // Color para textos secundarios

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(28.dp), // Esquinas bien redondeadas como la imagen
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // --- PARTE SUPERIOR (Avatar, Nombre, Precio) ---
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Circular
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(lightTealBg)
                    ) {
                        Text(
                            text = initials,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = UAMColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Nombre y Calificación
                    Column {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFBC02D), // Color dorado de la estrella
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$rating · $trips viajes",
                                fontSize = 14.sp,
                                color = grayText
                            )
                        }
                    }
                }

                // Precio
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = price,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = UAMColor
                    )
                    Text(
                        text = "por persona",
                        fontSize = 12.sp,
                        color = grayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECCIÓN DE LA RUTA (Línea conectora y puntos) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                // Canvas personalizado para recrear la línea del tiempo/ruta exacta
                Canvas(
                    modifier = Modifier
                        .width(16.dp)
                        .height(60.dp)
                ) {
                    val radius = 6.dp.toPx()
                    val center1 = Offset(size.width / 2, radius + 2.dp.toPx())
                    val center2 = Offset(size.width / 2, size.height - radius - 2.dp.toPx())
                    val strokeWidth = 2.dp.toPx()

                    // Línea conectora
                    drawLine(
                        color = UAMColor,
                        start = center1,
                        end = center2,
                        strokeWidth = strokeWidth
                    )
                    // Punto de Origen (Relleno)
                    drawCircle(
                        color = UAMColor,
                        radius = radius,
                        center = center1
                    )
                    // Punto de Destino (Solo borde / Outline)
                    drawCircle(
                        color = UAMColor,
                        radius = radius - (strokeWidth / 2),
                        center = center2,
                        style = Stroke(width = strokeWidth)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Textos de las direcciones (Mapeados verticalmente con el Canvas)
                Column(
                    modifier = Modifier.height(60.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = from,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = to,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Separador sutil antes del footer
            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- PARTE INFERIOR (Hora y Asientos) ---
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hora
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Puedes usar un Icon de reloj estándar. Si usas Material3 de manera extendida,
                    // puedes cambiar de DateRange a un icono de reloj real, por ahora mantengo uno de tiempo.
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_recent_history), // Reemplaza por tu icono de reloj preferido
                        contentDescription = "Hora",
                        tint = grayText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = time,
                        fontSize = 14.sp,
                        color = grayText,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Indicador de Espacios/Asientos Libres
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tres círculos solapados (Efecto visual de la imagen)
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp) // Adjusted to start where the overlap begins
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(lightTealSeat)
                            .zIndex(1f) // Ensure this stays on top of the first
                    )

                    // Third circle (overlaps the second)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$seats espacios",
                        fontSize = 14.sp,
                        color = grayText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}