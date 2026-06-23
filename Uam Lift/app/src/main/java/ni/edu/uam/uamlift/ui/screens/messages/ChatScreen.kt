package ni.edu.uam.uamlift.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
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
import ni.edu.uam.uamlift.data.viewmodels.ChatViewModel
import ni.edu.uam.uamlift.data.viewmodels.ChatViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viajeId: Long,
    currentUserId: Long,
    name: String,
    initials: String,
    isOnline: Boolean = true,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(RetrofitClient2.chatApi))
) {
    val messagesList by viewModel.mensajes.collectAsState()
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Paleta de colores UAM LIFT
    val uamColor = Color(0xFF019AA8)
    val avatarBgColor = Color(0xFFE4F2F3)
    val chatBgColor = Color(0xFFF8FAFC)
    val onlineGreen = Color(0xFF00E676)

    LaunchedEffect(viajeId) {
        viewModel.iniciarChat(viajeId)
    }

    LaunchedEffect(messagesList.size) {
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(messagesList.size - 1)
        }
    }

    Scaffold(
        topBar = {
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
        },
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
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
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                viewModel.enviarMensaje(viajeId, currentUserId, textState)
                                textState = ""
                            }
                        },
                        containerColor = uamColor,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(chatBgColor)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            items(messagesList) { message ->
                val isMe = message.usuarioId == currentUserId
                val alignment = if (isMe) Alignment.End else Alignment.Start

                val bubbleShape = if (isMe) {
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    if (!isMe) {
                        Text(
                            text = message.nombreUsuario,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )
                    }
                    Card(
                        shape = bubbleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) uamColor else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isMe) 2.dp else 1.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = message.contenido,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            color = if (isMe) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                        )
                    }
                }
            }
        }
    }
}
