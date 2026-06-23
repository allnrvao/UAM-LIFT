package ni.edu.uam.uamlift.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel

@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(),
    usuarioViewModel: UsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val misViajes by viajeViewModel.misViajes.collectAsState()
    val usuario = usuarioViewModel.usuario
    val currentUserId = usuario.id ?: 0L

    // Paleta de colores
    val chatBgColor = Color(0xFFF8FAFC)

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        usuarioViewModel.verificarSesion(context)
    }

    LaunchedEffect(usuario.id) {
        if (usuario.id != null) {
            viajeViewModel.cargarViajesDesdeBackend(usuario.id)
        }
    }

    // Estado para controlar la navegación interna
    var selectedViajeId by remember { mutableStateOf<Long?>(null) }
    var selectedChatName by remember { mutableStateOf("") }
    var selectedChatInitials by remember { mutableStateOf("") }

    if (selectedViajeId != null) {
        ChatScreen(
            viajeId = selectedViajeId!!,
            currentUserId = currentUserId,
            name = selectedChatName,
            initials = selectedChatInitials,
            onBackClick = { selectedViajeId = null }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(chatBgColor)
        ) {
            Text(
                text = "Mensajes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                color = Color.Black
            )

            if (misViajes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes chats activos",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(misViajes) { viaje ->
                        val destino = viaje.destino?.nombre ?: "UAM"
                        val chatName = "Viaje a $destino"
                        val initials = if (destino.isNotEmpty()) destino.take(1).uppercase() else "V"

                        MessageItem(
                            initials = initials,
                            name = chatName,
                            lastMessage = "Chat grupal del viaje",
                            time = "",
                            unread = 0,
                            onClick = {
                                selectedViajeId = viaje.id
                                selectedChatName = chatName
                                selectedChatInitials = initials
                            }
                        )
                    }
                }
            }
        }
    }
}
