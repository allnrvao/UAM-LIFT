package ni.edu.uam.uamlift.screens.messages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ni.edu.uam.uamlift.screens.messages.MessageItem


@Composable
fun MessagesScreen(modifier: Modifier = Modifier) {
    var selectedChat by remember { mutableStateOf<Int?>(null) }

    if (selectedChat != null) {
        ChatDetailScreen(chatId = selectedChat!!, onBack = { selectedChat = null })
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            Text(
                text = "Mensajes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(20.dp)
            )

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(listOf(1, 2, 3, 4)) { id ->
                    MessageItem(
                        initials = if (id == 1) "MR" else if (id == 2) "JL" else "AP",
                        name = if (id == 1) "María Rodríguez" else if (id == 2) "Juan López" else "Andrea Pérez",
                        lastMessage = if (id == 1) "¡Perfecto, nos vemos mañana!" else "¿Puedes salir 10 min antes?",
                        time = if (id == 1) "9:41" else "8:20",
                        unread = if (id == 1) 2 else 0,
                        onClick = { selectedChat = id }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatDetailScreen(chatId: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chat ID: $chatId", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Volver a Mensajes")
        }
    }
}