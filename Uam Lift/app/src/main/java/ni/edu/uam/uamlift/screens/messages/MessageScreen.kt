package ni.edu.uam.uamlift.screens.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// IMPORTANTE: Asegúrate de importar tu ChatScreen correctamente
import ni.edu.uam.uamlift.screens.chat.ChatScreen

// Modelo de datos para empaquetar la información del usuario
data class ChatUser(
    val id: Int,
    val name: String,
    val initials: String,
    val lastMessage: String,
    val time: String,
    val unread: Int,
    val isOnline: Boolean
)

@Composable
fun MessagesScreen(modifier: Modifier = Modifier) {
    // 1. Datos simulados
    val mockChats = remember {
        listOf(
            ChatUser(1, "María Rodríguez", "MR", "¡Perfecto, nos vemos mañana! 👍", "9:41", 2, isOnline = true),
            ChatUser(2, "Juan López", "JL", "¿Puedes salir 10 min antes?", "8:20", 0, isOnline = true),
            ChatUser(3, "Andrea Pérez", "AP", "Hola! Confirmas el viaje de mañana?", "Ayer", 0, isOnline = false)
        )
    }

    // 2. Estado que controla qué chat está abierto (null = lista de mensajes)
    var selectedChatUser by remember { mutableStateOf<ChatUser?>(null) }

    // 3. Condicional de Navegación
    if (selectedChatUser != null) {
        // Si hay un usuario seleccionado, saltamos a tu ChatScreen con sus datos reales
        ChatScreen(
            name = selectedChatUser!!.name,
            initials = selectedChatUser!!.initials,
            isOnline = selectedChatUser!!.isOnline,
            onBackClick = { selectedChatUser = null } // Al dar atrás, volvemos a la lista
        )
    } else {
        // Si es null, mostramos la lista de chats
        Column(modifier = modifier.fillMaxSize()) {
            Text(
                text = "Mensajes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(20.dp)
            )

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(mockChats) { chat ->
                    MessageItem(
                        initials = chat.initials,
                        name = chat.name,
                        lastMessage = chat.lastMessage,
                        time = chat.time,
                        unread = chat.unread,
                        onClick = { selectedChatUser = chat } // <--- AQUÍ SE DETONA EL CAMBIO
                    )
                }
            }
        }
    }
}