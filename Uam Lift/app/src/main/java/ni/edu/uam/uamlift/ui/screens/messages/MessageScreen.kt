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

    // Filtrar chats permitidos:
    // - El usuario es el conductor (creador)
    // - O el usuario es un pasajero del viaje (está participando)
    //
    // "misViajes" ya viene filtrado desde el backend (conductor u obtenerViajesPorUsuario),
    // así que la pertenencia al chat ya está garantizada por esa consulta. Antes se exigía
    // además que el propio usuario apareciera dentro de la lista anidada "pasajeros" del
    // viaje con estado == ACEPTADO, pero esa lista casi nunca viaja completa en las
    // respuestas de listado (se carga aparte, bajo demanda, con obtenerPasajerosPorViaje),
    // así que la condición fallaba siempre y el chat se descartaba: por eso Mensajes
    // mostraba "No tienes chats activos" aunque sí hubiera viajes con chat activo.
    // Ahora solo excluimos un viaje si, cuando la lista de pasajeros SÍ viene poblada,
    // consta explícitamente que el usuario fue rechazado o canceló su participación.
    val chatsPermitidos = remember(misViajes, currentUserId) {
        misViajes.filter { viaje ->
            val esConductor = viaje.conductor?.id == currentUserId
            if (esConductor) return@filter true

            val miParticipacion = viaje.pasajeros?.firstOrNull { it.usuario?.id == currentUserId }
            miParticipacion == null ||
                    (miParticipacion.estado != EstadoViajeUsuario.RECHAZADO &&
                            miParticipacion.estado != EstadoViajeUsuario.CANCELADO)
        }.sortedByDescending { viaje ->
            // Orden: más reciente primero. Usamos el último mensaje guardado localmente
            // (si el usuario ya entró a ese chat alguna vez) y, si no hay ninguno todavía,
            // caemos al id del viaje (a mayor id, viaje más reciente) para que igual quede
            // ordenado de forma consistente.
            val idViaje = viaje.id
            val ultimoMensaje = idViaje?.let {
                ChatLocalCache.obtenerUltimos(context, it).lastOrNull()
            }
            ultimoMensaje?.fechaEnvio ?: (idViaje?.toString()?.padStart(20, '0') ?: "")
        }
    }

    // Usamos rememberSaveable para controlar la navegación interna tras rotaciones
    var selectedViajeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedChatName by rememberSaveable { mutableStateOf("") }
    var selectedChatInitials by rememberSaveable { mutableStateOf("") }
    // Evita que se vuelva a autoseleccionar el chat si el usuario ya lo cerró manualmente
    var autoAperturaRealizada by rememberSaveable { mutableStateOf(false) }

    // Si llegamos a Mensajes viniendo del mapa del viaje activo (initialViajeId != null),
    // estamos "dentro" de un viaje en curso: en ese estado solo existen dos pantallas
    // posibles (el mapa y este chat), así que el botón de regresar debe llevar siempre
    // de vuelta al mapa y no a la lista de chats.
    val enViajeActivo = initialViajeId != null

    // Si llegamos aquí desde "Chat del viaje" en el mapa del viaje activo, abrimos
    // ese chat automáticamente en cuanto encontremos el viaje correspondiente.
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

    // Mientras estamos dentro de un viaje activo, cualquier gesto/botón de "atrás" del
    // sistema debe regresar directamente al mapa del viaje, nunca a la lista de chats.
    BackHandler(enabled = enViajeActivo) {
        onBack()
    }

    // Si llegamos a un chat desde la lista normal de Mensajes (no desde un viaje activo),
    // el chat y la lista viven en la MISMA ruta de navegación (solo cambia un estado
    // interno). Sin este BackHandler, el gesto/botón de "atrás" no cerraba el chat hacia
    // la lista: se le escapaba al manejo global de navegación y saltaba directo a otra
    // pantalla, saltándose el paso intermedio de "volver a la lista de chats".
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
                    // Dentro de un viaje activo solo se puede ver el mapa o el chat:
                    // el botón de regresar del chat debe llevar al mapa, no a la lista.
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
                        val previewMensaje = ultimoMensaje?.contenido?.takeIf { it.isNotBlank() }
                            ?: "Chat grupal del viaje"
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