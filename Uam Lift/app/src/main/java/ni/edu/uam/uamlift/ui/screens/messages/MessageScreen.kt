package ni.edu.uam.uamlift.ui.screens.messages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.uamlift.data.ChatLocalCache
import ni.edu.uam.uamlift.data.enums.EstadoViajeUsuario
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel

@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(factory = AppViewModelFactory()),
    usuarioViewModel: UsuarioViewModel = viewModel(),
    initialViajeId: Long? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val misViajes by viajeViewModel.misViajes.collectAsState()
    val usuario = usuarioViewModel.usuario
    val currentUserId = usuario.id ?: 0L

    val chatBgColor = Color(0xFFF8FAFC)

    LaunchedEffect(Unit) {
        usuarioViewModel.verificarSesion(context)
    }

    LaunchedEffect(usuario.id) {
        if (usuario.id != null) {
            viajeViewModel.cargarViajesDesdeBackend(usuario.id)
        }
    }

    // ── SOLUCIÓN: Forzar la inclusión instantánea en cuanto el usuario se une ──
    val chatsPermitidos = remember(misViajes, currentUserId) {
        misViajes.filter { viaje ->
            val esConductor = viaje.conductor?.id == currentUserId
            if (esConductor) return@filter true

            // Buscamos si el usuario actual está en la lista de pasajeros asignados a este objeto viaje
            val miParticipacion = viaje.pasajeros?.firstOrNull { it.usuario?.id == currentUserId }

            if (miParticipacion != null) {
                // Si existe registro explícito en la lista, permitimos el chat excepto si fue cancelado/rechazado
                miParticipacion.estado != EstadoViajeUsuario.RECHAZADO &&
                        miParticipacion.estado != EstadoViajeUsuario.CANCELADO
            } else {
                // COMPORTAMIENTO AGRESIVO: Si el backend asignó el viaje a la lista general pero la sublista de pasajeros
                // viene vacía provisionalmente, asumimos que se acaba de unir y le permitimos ver el chat de inmediato.
                true
            }
        }.sortedByDescending { viaje ->
            val idViaje = viaje.id
            val ultimoMensaje = idViaje?.let {
                ChatLocalCache.obtenerUltimos(context, it).lastOrNull()
            }
            ultimoMensaje?.fechaEnvio ?: (idViaje?.toString()?.padStart(20, '0') ?: "")
        }
    }

    var selectedViajeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedChatName by rememberSaveable { mutableStateOf("") }
    var selectedChatInitials by rememberSaveable { mutableStateOf("") }
    var autoAperturaRealizada by rememberSaveable { mutableStateOf(false) }

    val enViajeActivo = initialViajeId != null

    LaunchedEffect(initialViajeId, chatsPermitidos) {
        if (initialViajeId != null && !autoAperturaRealizada) {
            val viajeDestino = chatsPermitidos.firstOrNull { it.id == initialViajeId }
            if (viajeDestino != null) {
                val destino = viajeDestino.destino?.nombre ?: "UAM"
                selectedChatName = "Viaje a $destino"
                selectedChatInitials = if (destino.isNotEmpty()) destino.take(1).uppercase() else "V"
                selectedViajeId = viajeDestino.id
                autoAperturaRealizada = true
            }
        }
    }

    BackHandler(enabled = enViajeActivo) {
        onBack()
    }

    BackHandler(enabled = !enViajeActivo && selectedViajeId != null) {
        selectedViajeId = null
    }

    if (selectedViajeId != null) {
        ChatScreen(
            viajeId = selectedViajeId!!,
            currentUserId = currentUserId,
            name = selectedChatName,
            initials = selectedChatInitials,
            onBackClick = {
                if (enViajeActivo) {
                    onBack()
                } else {
                    selectedViajeId = null
                }
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(chatBgColor)
        ) {
            FragmentHeader(title = "Mensajes")

            if (chatsPermitidos.isEmpty()) {
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
                    items(chatsPermitidos) { viaje ->
                        val destino = viaje.destino?.nombre ?: "UAM"
                        val chatName = "Viaje a $destino"
                        val initials = if (destino.isNotEmpty()) destino.take(1).uppercase() else "V"

                        val ultimoMensaje = viaje.id?.let {
                            ChatLocalCache.obtenerUltimos(context, it).lastOrNull()
                        }

                        // Si no hay mensajes guardados en caché, avisamos que el chat se acaba de inicializar
                        val previewMensaje = ultimoMensaje?.contenido?.takeIf { it.isNotBlank() }
                            ?: "¡Te has unido al viaje! Escribe un mensaje..."

                        val horaMensaje = ultimoMensaje?.fechaEnvio
                            ?.let { fecha -> fecha.substringAfter('T').take(5).ifBlank { null } }
                            ?: ""

                        MessageItem(
                            imageUrl = viaje.conductor?.imagenUrl,
                            initials = initials,
                            name = chatName,
                            lastMessage = previewMensaje,
                            time = horaMensaje,
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

@Composable
fun FragmentHeader(title: String) {
    Text(
        text = title,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        color = Color.Black
    )
}