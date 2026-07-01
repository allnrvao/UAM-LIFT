package ni.edu.uam.uamlift.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    // Control de diálogos dinámicos
    var mostrarDialogoDetalle by remember { mutableStateOf(false) }
    var notificacionSeleccionada by remember { mutableStateOf<Notificacion?>(null) }

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
                    items(notificaciones) { notificacion ->
                        NotificationItem(
                            notificacion = notificacion,
                            onClick = {
                                if (!notificacion.leida) {
                                    notificacion.id?.let { notificacionViewModel.marcarComoLeida(it, usuarioId) }
                                }
                                // Para todas las notificaciones críticas, abrimos su Modal adaptado
                                notificacionSeleccionada = notificacion
                                mostrarDialogoDetalle = true
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }

    // Inyección del diálogo inteligente y adaptativo
    if (mostrarDialogoDetalle && notificacionSeleccionada != null) {
        DetalleNotificacionAdaptativoDialog(
            notificacion = notificacionSeleccionada!!,
            navController = navController,
            onDismissRequest = {
                mostrarDialogoDetalle = false
                notificacionSeleccionada = null
            }
        )
    }
}

@Composable
private fun NotificationItem(
    notificacion: Notificacion,
    onClick: () -> Unit
) {
    val esCancelacion = notificacion.tipo == TipoNotificacion.CANCELACION_VIAJE ||
            notificacion.tipo == TipoNotificacion.USUARIO_ELIMINADO
    val esFinalizacion = notificacion.tipo == TipoNotificacion.FINALIZACION_VIAJE
    val esUsuarioUnido = notificacion.tipo == TipoNotificacion.USUARIO_UNIDO
    val colorIcono = when {
        esCancelacion -> Color(0xFFEF4444)
        esFinalizacion -> Color(0xFF22C55E)
        esUsuarioUnido -> UAMColor
        else -> UAMColor
    }
    val iconoNotificacion = when {
        esCancelacion -> Icons.Default.Cancel
        esFinalizacion -> Icons.Default.CheckCircle
        esUsuarioUnido -> Icons.Default.PersonAdd
        else -> Icons.Default.DirectionsCar
    }

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
                    imageVector = iconoNotificacion,
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

// ── DIÁLOGO ADAPTATIVO SEGÚN LA FUNCIONALIDAD DE LA NOTIFICACIÓN ──
@Composable
fun DetalleNotificacionAdaptativoDialog(
    notificacion: Notificacion,
    navController: NavController,
    onDismissRequest: () -> Unit
) {
    // Configuración de UI según el tipo de funcionalidad
    val (colorTema, fondoItem, icono, etiquetaTexto, leyendaInformativa) = when (notificacion.tipo) {
        TipoNotificacion.USUARIO_UNIDO -> Triple(
            UAMColor,
            UAMColor.copy(alpha = 0.10f),
            Icons.Default.PersonAdd
        ).let { (c, f, i) -> Quintuple(c, f, i, "NUEVO INTEGRANTE", "Un pasajero se ha reservado un asiento en tu vehículo:") }

        TipoNotificacion.CANCELACION_VIAJE, TipoNotificacion.USUARIO_ELIMINADO -> Triple(
            Color(0xFFEF4444),
            Color(0xFFEF4444).copy(alpha = 0.08f),
            Icons.Default.PersonRemove
        ).let { (c, f, i) -> Quintuple(c, f, i, "CANCELADO", "Se han cancelado tu viaje:") }

        TipoNotificacion.FINALIZACION_VIAJE -> Triple(
            Color(0xFF22C55E),
            Color(0xFF22C55E).copy(alpha = 0.08f),
            Icons.Default.CheckCircle
        ).let { (c, f, i) -> Quintuple(c, f, i, "VIAJE COMPLETADO", "¡Tu ruta ha finalizado exitosamente! Detalles del cierre:") }

        TipoNotificacion.INICIO_VIAJE -> Triple(
            UAMColor,
            UAMColor.copy(alpha = 0.08f),
            Icons.Default.DirectionsCar
        ).let { (c, f, i) -> Quintuple(c, f, i, "EN CURSO", "El conductor ha iniciado la ruta. Asegúrate de estar listo:") }

        else -> Triple(
            UAMColor,
            Color(0xFFF8F9FA),
            Icons.Default.Person
        ).let { (c, f, i) -> Quintuple(c, f, i, "AVISO GENERAL", "Información importante sobre la plataforma:") }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f).wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificacion.titulo,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorTema
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = leyendaInformativa, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))

                // Celda centralizada estilo PassengerItem orientada a la funcionalidad
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(fondoItem)
                        .border(1.5.dp, colorTema, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(colorTema),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icono, contentDescription = null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Estado de Ruta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = colorTema, shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = etiquetaTexto,
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = notificacion.mensaje,
                            fontSize = 12.5.sp,
                            color = Color(0xFF334155),
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Acciones dinámicas de botones según la funcionalidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Si el viaje inició, habilitamos un botón de acción directa para ver el mapa
                    if (notificacion.tipo == TipoNotificacion.INICIO_VIAJE && notificacion.viajeId != null) {
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cerrar", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                onDismissRequest()
                                navController.navigate("active_ride/${notificacion.viajeId}") {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
                        ) {
                            Text("Ver Mapa", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Botón por defecto para alertas de cancelación, unión o cierre
                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorTema)
                        ) {
                            Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Estructura de datos Helper para simplificar el código del Dialog estructurado
data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)