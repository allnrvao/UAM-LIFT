package ni.edu.uam.uamlift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ni.edu.uam.uamlift.data.RetrofitClient
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.ui.theme.UAMColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RideCard(
    viaje: Viaje,
    usuarioIdActual: Long,
    esConductor: Boolean = false,
    onCardEnCursoClick: (Viaje) -> Unit = {},
    onConfirmarClick: (Long) -> Unit = {},
    onIniciarViaje: (Long) -> Unit = {},
    onFinalizarViaje: (Long) -> Unit = {},
    onCancelarViaje: (Long, String) -> Unit = { _, _ -> },
    onVerPasajeros: (Long) -> Unit = {},
    onCancelarParticipacion: (Long) -> Unit = {}
) {
    // Usamos rememberSaveable para que el diálogo no se cierre al rotar el dispositivo
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoCancelarViaje by rememberSaveable { mutableStateOf(false) }

    val lightTealBg = Color(0xFFE0F7FA)
    val lightTealSeat = Color(0xFFB2EBF2)
    val grayText = Color(0xFF424242)

    val esPasajero = remember(viaje.pasajeros) {
        viaje.pasajeros?.any { it.usuario?.id == usuarioIdActual } ?: false
    }

    val pasajerosCount = viaje.pasajeros?.size ?: 0
    val asientosLibres = remember(viaje.numeroAsientosDisponibles, pasajerosCount) {
        val libres = viaje.numeroAsientosDisponibles - pasajerosCount
        if (libres < 0) 0 else libres
    }

    val nombreConductor = viaje.conductor?.nombreUsuario?.takeIf { it.isNotBlank() }
        ?: "${viaje.conductor?.nombre ?: ""} ${viaje.conductor?.apellido ?: ""}".trim()
            .ifEmpty { "Estudiante UAM" }

    val initials = (viaje.conductor?.nombre?.take(1) ?: "U") +
            (viaje.conductor?.apellido?.take(1) ?: "")
    val fotoConductor = viaje.conductor?.imagenUrl

    val modelFoto = remember(fotoConductor) {
        if (fotoConductor.isNullOrBlank()) null
        else if (fotoConductor.startsWith("http")) fotoConductor
        else "${RetrofitClient.BASE_URL.trimEnd('/')}/${fotoConductor.trimStart('/')}"
    }

    val origenTexto = viaje.origen?.nombre ?: "Origen"
    val destinoTexto = viaje.destino?.nombre ?: "Destino"
    val horaTexto = viaje.fechaHoraSalida?.substringAfter("T")?.take(5) ?: "00:00"

    // Lógica para determinar el texto de la fecha
    val fechaTexto = remember(viaje.fechaHoraSalida) {
        try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateSalida = sdfInput.parse(viaje.fechaHoraSalida ?: "") ?: return@remember "Hoy, $horaTexto"
            
            val calSalida = Calendar.getInstance().apply { time = dateSalida }
            val calHoy = Calendar.getInstance()
            
            val esHoy = calSalida.get(Calendar.YEAR) == calHoy.get(Calendar.YEAR) &&
                        calSalida.get(Calendar.DAY_OF_YEAR) == calHoy.get(Calendar.DAY_OF_YEAR)
            
            if (esHoy) {
                "Hoy, $horaTexto"
            } else {
                val sdfOutput = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                sdfOutput.format(dateSalida)
            }
        } catch (e: Exception) {
            "Hoy, $horaTexto"
        }
    }

    // Lógica para habilitar "Iniciar viaje" compatible con API 24
    val puedeIniciar = remember(viaje.fechaHoraSalida) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateSalida = sdf.parse(viaje.fechaHoraSalida ?: "")
            val now = Calendar.getInstance().time
            // Habilitar 15 minutos antes
            val fifteenMinsBefore = Calendar.getInstance().apply {
                time = dateSalida ?: now
                add(Calendar.MINUTE, -15)
            }.time
            now.after(fifteenMinsBefore)
        } catch (e: Exception) {
            true
        }
    }

    // Si el viaje está FINALIZADO o CANCELADO, no abrimos diálogos al hacer clic
    val estaTerminado = viaje.estadoViaje == EstadoViaje.FINALIZADO ||
            viaje.estadoViaje == EstadoViaje.CANCELADO

    if (mostrarDialogo && !esConductor && !estaTerminado) {
        TakeRideDialog(
            viaje = viaje,
            esPasajero = esPasajero,
            onDismissRequest = { mostrarDialogo = false },
            onConfirmarViaje = {
                mostrarDialogo = false
                onConfirmarClick(viaje.id ?: 0L)
            },
            onCancelarParticipacion = {
                mostrarDialogo = false
                onCancelarParticipacion(viaje.id ?: 0L)
            }
        )
    }

    if (mostrarDialogoCancelarViaje) {
        CancelRideDialog(
            onDismissRequest = { mostrarDialogoCancelarViaje = false },
            onConfirmarCancelacion = { motivo ->
                mostrarDialogoCancelarViaje = false
                onCancelarViaje(viaje.id ?: 0L, motivo)
            }
        )
    }

    // Colores según estado: terminado = fondo levemente gris pero mantiene estructura
    val cardBgColor = when (viaje.estadoViaje) {
        EstadoViaje.FINALIZADO -> Color(0xFFF0F0F0)
        EstadoViaje.CANCELADO  -> Color(0xFFF5EDED)   // tinte rojizo muy sutil
        else                   -> Color.White
    }

    Card(
        onClick = {
            when {
                viaje.estadoViaje == EstadoViaje.EN_CURSO -> onCardEnCursoClick(viaje)
                esConductor && !estaTerminado -> onVerPasajeros(viaje.id ?: 0L)
                !esConductor && !estaTerminado -> mostrarDialogo = true
                else -> {}
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 2.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        // Mantener siempre elevación para que se vea el contenedor
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: conductor + precio
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar conductor
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(lightTealBg)
                    ) {
                        if (modelFoto != null) {
                            AsyncImage(
                                model = modelFoto,
                                contentDescription = "Foto del conductor",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = initials.uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = UAMColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nombreConductor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (estaTerminado) Color(0xFF757575) else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val statusText = when (viaje.estadoViaje) {
                            EstadoViaje.EN_CURSO -> "En proceso"
                            EstadoViaje.FINALIZADO -> "Finalizado"
                            EstadoViaje.CANCELADO -> "Cancelado"
                            else -> if (esConductor) "Tú eres el conductor"
                            else if (esPasajero) "Estás unido"
                            else "Conductor verificado"
                        }
                        val statusColor = when (viaje.estadoViaje) {
                            EstadoViaje.EN_CURSO -> Color(0xFFF44336)
                            EstadoViaje.FINALIZADO -> Color.Gray
                            EstadoViaje.CANCELADO -> Color.Red
                            else -> if (esConductor || esPasajero) UAMColor else Color(0xFF4CAF50)
                        }
                        Text(text = statusText, fontSize = 11.sp, color = statusColor)
                    }
                }

                // Precio — siempre visible, pero gris si está terminado
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "C$ ${viaje.precioPorPersona.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (estaTerminado) Color(0xFF9E9E9E) else UAMColor
                    )
                    Text(
                        text = "p/p",
                        fontSize = 10.sp,
                        color = if (estaTerminado) Color(0xFFBDBDBD) else grayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ruta: origen → destino
            Row(modifier = Modifier.fillMaxWidth()) {
                val routeColor = if (estaTerminado) Color(0xFFBDBDBD) else UAMColor
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(16.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).background(routeColor, CircleShape))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(24.dp)
                            .background(routeColor.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .border(2.dp, routeColor, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = origenTexto,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (estaTerminado) Color(0xFF757575) else Color.Black
                    )
                    Text(
                        text = destinoTexto,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (estaTerminado) Color(0xFF757575) else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(10.dp))

            // Fila inferior: hora + acciones
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        tint = grayText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = fechaTexto, fontSize = 12.sp, color = grayText)
                }

                if (esConductor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!estaTerminado) {
                            if (viaje.estadoViaje != EstadoViaje.EN_CURSO) {
                                TextButton(
                                    onClick = { mostrarDialogoCancelarViaje = true },
                                    colors = ButtonDefaults.buttonColors(contentColor = Color.Red),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Cancelar", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { onIniciarViaje(viaje.id ?: 0L) },
                                    enabled = puedeIniciar,
                                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Iniciar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onFinalizarViaje(viaje.id ?: 0L) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Finalizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(
                            onClick = { onVerPasajeros(viaje.id ?: 0L) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(lightTealBg, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = "Pasajeros",
                                tint = UAMColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (!estaTerminado) {
                    Surface(
                        color = if (esPasajero && viaje.estadoViaje == EstadoViaje.EN_CURSO)
                            UAMColor
                        else if (esPasajero)
                            UAMColor.copy(alpha = 0.1f)
                        else
                            lightTealSeat.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val textoPasajero = when {
                            esPasajero && viaje.estadoViaje == EstadoViaje.EN_CURSO -> "📍 Ver Mapa en Vivo"
                            esPasajero -> "Ya estás unido"
                            else -> "$asientosLibres asientos libres"
                        }
                        Text(
                            text = textoPasajero,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (esPasajero && viaje.estadoViaje == EstadoViaje.EN_CURSO)
                                Color.White
                            else
                                UAMColor
                        )
                    }
                }
            }
        }
    }
}
