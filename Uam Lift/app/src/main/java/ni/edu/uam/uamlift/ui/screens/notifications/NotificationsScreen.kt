package ni.edu.uam.uamlift.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ni.edu.uam.uamlift.data.enums.TipoNotificacion
import ni.edu.uam.uamlift.data.models.Notificacion
import ni.edu.uam.uamlift.data.viewmodels.NotificacionViewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    notificacionViewModel: NotificacionViewModel,
    onBack: () -> Unit = { navController.popBackStack() }
) {
    val usuarioId = usuarioViewModel.usuario.id ?: 0L
    val notificaciones by notificacionViewModel.notificaciones.collectAsState()
    val noLeidas by notificacionViewModel.noLeidas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (noLeidas > 0) {
                        TextButton(onClick = { notificacionViewModel.marcarTodasComoLeidas(usuarioId) }) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("Marcar todas", color = Color.White, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UAMColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Gray)) {
            if (notificaciones.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes notificaciones por ahora", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // La lista ya viene ordenada de la más reciente a la más antigua.
                    items(notificaciones) { notificacion ->
                        NotificationItem(
                            notificacion = notificacion,
                            onClick = {
                                if (!notificacion.leida) {
                                    notificacion.id?.let { notificacionViewModel.marcarComoLeida(it, usuarioId) }
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notificacion: Notificacion,
    onClick: () -> Unit
) {
    val esCancelacion = notificacion.tipo == TipoNotificacion.CANCELACION_VIAJE
    val colorIcono = if (esCancelacion) Color(0xFFEF4444) else UAMColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) Color.White else UAMColor.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notificacion.leida) 1.dp else 3.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(colorIcono.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (esCancelacion) Icons.Default.Cancel else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = colorIcono,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notificacion.titulo,
                        fontWeight = if (notificacion.leida) FontWeight.SemiBold else FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                    if (!notificacion.leida) {
                        Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notificacion.mensaje,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
