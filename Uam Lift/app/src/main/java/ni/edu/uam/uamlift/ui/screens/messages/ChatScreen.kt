package ni.edu.uam.uamlift.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Estructura de datos para simular los mensajes
data class MessageData(
    val text: String,
    val isMe: Boolean // true = Enviado por mí (Derecha), false = Recibido (Izquierda)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    name: String,
    initials: String,
    isOnline: Boolean = true,
    onBackClick: () -> Unit
) {
    // Paleta de colores exacta de UAM LIFT
    val uamColor = Color(0xFF019AA8)
    val avatarBgColor = Color(0xFFE4F2F3)
    val chatBgColor = Color(0xFFF8FAFC) // Fondo grisáceo muy limpio para los chats
    val onlineGreen = Color(0xFF00E676)

    // Mensajes quemados/fijos basados exactamente en tu imagen
    val messagesList = listOf(
        MessageData("Hola! Confirmas el viaje de mañana?", isMe = false),
        MessageData("Sí, estaré listo a las 7:20", isMe = true),
        MessageData("¡Perfecto, nos vemos mañana! 👍", isMe = false)
    )

    Scaffold(
        topBar = {
            // --- TOP BAR PERSONALIZADA (Flecha, Avatar, Info de Línea) ---
            Surface(
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Avatar con Indicador Verde
                    Box(modifier = Modifier.size(52.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(avatarBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = uamColor
                            )
                        }
                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(onlineGreen)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Nombre y Estado "En línea"
                    Column {
                        Text(
                            text = name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (isOnline) {
                            Text(
                                text = "En línea",
                                fontSize = 14.sp,
                                color = onlineGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // --- CUERPO DEL CHAT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(chatBgColor)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            messagesList.forEach { message ->
                val alignment = if (message.isMe) Alignment.End else Alignment.Start

                // Formas de burbujas asimétricas
                val bubbleShape = if (message.isMe) {
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Card(
                        shape = bubbleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.isMe) uamColor else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (message.isMe) 2.dp else 1.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp) // Evita que la burbuja ocupe toda la pantalla
                    ) {
                        Text(
                            text = message.text,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            color = if (message.isMe) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                        )
                    }
                }
            }
        }
    }
}