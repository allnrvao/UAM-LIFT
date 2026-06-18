package ni.edu.uam.uamlift.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.* import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ni.edu.uam.uamlift.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
<<<<<<< Updated upstream
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
=======
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
>>>>>>> Stashed changes
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.data.models.Usuario
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.theme.UAMColor

@Composable
fun RideCard(
    viaje: Viaje,
<<<<<<< Updated upstream
    onConfirmarClick: (Long) -> Unit
=======
    usuarioViewModel: UsuarioViewModel, // 🌟 1. Pasamos el ViewModel para buscar datos
    esConductor: Boolean = false,
    onConfirmarClick: (Long) -> Unit = {},
    onIniciarViaje: (Long) -> Unit = {}
>>>>>>> Stashed changes
) {
    // 🌟 Línea 27: Aquí tienes guardada tu variable de control
    var mostrarDialogo by remember { mutableStateOf(false) }

    // 🌟 2. Estado local para guardar el usuario fresco obtenido de la API
    var conductorActualizado by remember { mutableStateOf<Usuario?>(null) }

    val lightTealBg = Color(0xFFE0F7FA)
    val lightTealSeat = Color(0xFFB2EBF2)
    val grayText = Color(0xFF757575)

    val nombreConductor = viaje.conductor?.nombre ?: "Conductor"
    val initials = nombreConductor.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    val origenTexto = viaje.origen?.nombre ?: "UAM Central"
    val destinoTexto = viaje.destino?.nombre ?: "UAM Central"
    val horaTexto = viaje.fechaHoraSalida
        ?.split(" ", "T")
        ?.getOrNull(1)
        ?.take(5) ?: "00:00"

<<<<<<< Updated upstream
    // 🌟 Línea 41: El diálogo ya está esperando a que cambie la variable
    if (mostrarDialogo) {
=======
    // 🌟 3. Efecto para buscar al usuario en la API cuando se monta la Card
    LaunchedEffect(viaje.conductor?.correo, viaje.conductor?.cif) {
        val correo = viaje.conductor?.correo
        val cif = viaje.conductor?.cif

        if (!correo.isNullOrBlank()) {
            usuarioViewModel.obtenerUsuarioPorCorreo(correo) { exito ->
                if (exito) {
                    // Almacenamos el usuario que el ViewModel cargó en su estado global
                    conductorActualizado = usuarioViewModel.usuario
                }
            }
        } else if (!cif.isNullOrBlank()) {
            usuarioViewModel.obtenerUsuarioPorCif(cif) { exito ->
                if (exito) {
                    conductorActualizado = usuarioViewModel.usuario
                }
            }
        }
    }

    // Lógica para habilitar "Iniciar viaje"
    val puedeIniciar = remember(viaje.fechaHoraSalida) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateSalida = sdf.parse(viaje.fechaHoraSalida ?: "")
            val now = Calendar.getInstance().time
            val fifteenMinsBefore = Calendar.getInstance().apply {
                time = dateSalida ?: now
                add(Calendar.MINUTE, -15)
            }.time
            now.after(fifteenMinsBefore)
        } catch (e: Exception) {
            true
        }
    }

    if (mostrarDialogo && !esConductor) {
>>>>>>> Stashed changes
        TakeRideDialog(
            viaje = viaje,
            onDismissRequest = { mostrarDialogo = false },
            onConfirmarViaje = {
                mostrarDialogo = false
                onConfirmarClick(viaje.id ?: 0L)
            }
        )
    }

    Card(
        // 🌟 LÍNEA 53 CORREGIDA: Quitamos el intento de meter el Diálogo aquí adentro.
        // Ahora solo cambiamos el valor de la variable a true.
        onClick = { mostrarDialogo = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // --- PARTE SUPERIOR (Avatar, Nombre, Precio) ---
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(lightTealBg)
                    ) {
                        Text(text = initials, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UAMColor)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(text = nombreConductor, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFBC02D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "4.8 · 15 viajes", fontSize = 14.sp, color = grayText)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "C$ ${viaje.precioPorPersona.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = UAMColor)
                    Text(text = "por persona", fontSize = 12.sp, color = grayText)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECCIÓN DE LA RUTA ---
            Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
                Canvas(modifier = Modifier.width(16.dp).height(60.dp)) {
                    val radius = 6.dp.toPx()
                    val center1 = Offset(size.width / 2, radius + 2.dp.toPx())
                    val center2 = Offset(size.width / 2, size.height - radius - 2.dp.toPx())
                    val strokeWidth = 2.dp.toPx()

                    drawLine(color = UAMColor, start = center1, end = center2, strokeWidth = strokeWidth)
                    drawCircle(color = UAMColor, radius = radius, center = center1)
                    drawCircle(color = UAMColor, radius = radius - (strokeWidth / 2), center = center2, style = Stroke(width = strokeWidth))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.height(60.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Text(text = origenTexto, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    Text(text = destinoTexto, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- PARTE INFERIOR (Hora y Asientos) ---
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
<<<<<<< Updated upstream
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Hora",
                        tint = grayText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = horaTexto, fontSize = 14.sp, color = grayText, fontWeight = FontWeight.Medium)
=======

                    // 🌟 4. Círculo de Avatar que lee del objeto recién buscado de la API
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(lightTealBg)
                    ) {
                        // Obtenemos el campo imagenUrl del usuario que consultamos dinámicamente
                        val fotoUrl = conductorActualizado?.imagenUrl ?: viaje.conductor?.imagenUrl

                        if (!fotoUrl.isNullOrEmpty()) {
                            SubcomposeAsyncImage(
                                model = fotoUrl,
                                contentDescription = "Foto de $nombreConductor",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(14.dp),
                                        color = UAMColor,
                                        strokeWidth = 2.dp
                                    )
                                },
                                error = {
                                    Text(text = initials.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UAMColor)
                                }
                            )
                        } else {
                            Text(text = initials.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UAMColor)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = nombreConductor, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text(
                            text = if (esConductor) "Tú eres el conductor" else "Conductor verificado",
                            fontSize = 12.sp, color = if (esConductor) UAMColor else Color(0xFF4CAF50)
                        )
                    }
>>>>>>> Stashed changes
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.padding(start = 10.dp).size(16.dp).clip(CircleShape).background(lightTealSeat).zIndex(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${viaje.numeroAsientosDisponibles} espacios", fontSize = 14.sp, color = grayText, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}