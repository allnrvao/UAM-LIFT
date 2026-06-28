package ni.edu.uam.uamlift.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.RetrofitClient2
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.ChatViewModel
import ni.edu.uam.uamlift.data.viewmodels.ChatViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.ui.components.PassengersDialog
import ni.edu.uam.uamlift.ui.theme.UAMColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viajeId: Long,
    currentUserId: Long,
    name: String,
    initials: String,
    isOnline: Boolean = false,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            RetrofitClient2.chatApi,
            RetrofitClient.usuarioApi
        )
    ),
    viajeViewModel: ViajeViewModel = viewModel(factory = AppViewModelFactory())
) {
    val messagesList by viewModel.mensajesUi.collectAsState()
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val chatBgColor = Color(0xFFF8FAFC)

    val misViajes by viajeViewModel.misViajes.collectAsState()
    val viajeActual = remember(misViajes, viajeId) {
        misViajes.find { it.id == viajeId }
    }

    val pasajeros by viajeViewModel.pasajerosViaje.collectAsState()

    // Precarga de nombres mejorada: Incluye a pasajeros y al conductor
    LaunchedEffect(pasajeros, viajeActual) {
        val todosLosUsuarios = pasajeros.toMutableList()
        viajeActual?.conductor?.let { todosLosUsuarios.add(it) }
        if (todosLosUsuarios.isNotEmpty()) {
            viewModel.precargarNombres(todosLosUsuarios)
        }
    }

    LaunchedEffect(viajeId, currentUserId) {
        if (currentUserId != 0L) {
            viajeViewModel.obtenerPasajeros(viajeId)
            viewModel.iniciarChat(viajeId, currentUserId)
        }
    }

    var mostrarPasajeros by remember { mutableStateOf(false) }

    if (mostrarPasajeros) {
        PassengersDialog(
            conductor = viajeActual?.conductor,
            pasajeros = pasajeros,
            onDismissRequest = { mostrarPasajeros = false }
        )
    }

    LaunchedEffect(messagesList.size) {
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(messagesList.size - 1)
        }
    }

    Scaffold(
        containerColor = chatBgColor,
        topBar = {
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.statusBarsPadding().fillMaxWidth()
                        .clickable { mostrarPasajeros = true }
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) }
                    Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = Color(0xFFF1F5F9)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Groups, null, tint = UAMColor) }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Grupo de viaje", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 16.dp) {
                Row(
                    modifier = Modifier.navigationBarsPadding().padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textState, onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9), 
                            unfocusedContainerColor = Color(0xFFF1F5F9), 
                            focusedIndicatorColor = Color.Transparent, 
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledIconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                viewModel.enviarMensaje(viajeId, currentUserId, textState)
                                textState = "" 
                            } 
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = UAMColor)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(messagesList) { message ->
                val isMe = message.isMe
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                    if (!isMe) {
                        Text(
                            text = message.usuarioNombre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = UAMColor,
                            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 16.dp
                        ),
                        color = if (isMe) UAMColor else Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(text = message.contenido, fontSize = 15.sp, color = if (isMe) Color.White else Color(0xFF1E293B))
                            Text(
                                text = formatChatTime(message.fechaEnvio),
                                fontSize = 10.sp,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatChatTime(fecha: String): String {
    return try {
        val timestamp = fecha.toLongOrNull()
        if (timestamp != null) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
        if (fecha.contains("T")) {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputSdf.parse(fecha)
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date!!)
        }
        fecha.takeLast(5)
    } catch (e: Exception) {
        "00:00"
    }
}