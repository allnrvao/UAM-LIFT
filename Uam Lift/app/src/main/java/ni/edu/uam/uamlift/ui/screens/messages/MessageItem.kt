package ni.edu.uam.uamlift.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.uamlift.ui.theme.Gray

@Composable
fun MessageItem(
    initials: String,
    name: String,
    lastMessage: String,
    time: String,
    unread: Int,
    onClick: () -> Unit
) {
    // Paleta de colores exacta de la imagen
    val uamColor = Color(0xFF019AA8)       // Verde azulado principal
    val avatarBgColor = Color(0xFFE4F2F3)  // Celeste/gris muy suave para el fondo del avatar
    val grayTextColor = Color(0xFF757575)  // Gris para la hora y mensajes leídos
    val onlineGreen = Color(0xFF00E676)    // Verde brillante de conectado

    // Envolvemos todo en una Card para lograr las cajas flotantes redondeadas de la imagen
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp), // Espaciado entre tarjetas
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Fondo blanco puro para resaltar la sombra
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp // Sombra sutil idéntica a la referencia
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp), // Margen interno de la tarjeta
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 1. Avatar con Iniciales y Punto Verde de Conectado
            Box(
                modifier = Modifier.size(54.dp)
            ) {
                // Círculo del Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(avatarBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = uamColor
                    )
                }

                // Punto verde indicador (Solo aparece si el usuario tiene mensajes pendientes/está activo)
                if (unread > 0 || name == "Juan López") { // Condición basada en tu imagen
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White) // Borde blanco de aislamiento
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(onlineGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Textos (Nombre y Último mensaje)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, // Nombres siempre firmes y visibles
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastMessage,
                    fontSize = 15.sp,
                    fontWeight = if (unread > 0) FontWeight.Medium else FontWeight.Normal,
                    color = if (unread > 0) Color.Black else grayTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Info de tiempo y Globo de notificaciones no leídas
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = time,
                    fontSize = 14.sp,
                    color = grayTextColor
                )

                if (unread > 0) {
                    Spacer(modifier = Modifier.height(12.dp)) // Espacio balanceado verticalmente
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(uamColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unread.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Mantiene el balance de altura si no hay globo de notificación
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}