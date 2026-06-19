package ni.edu.uam.uamlift.ui.screens.create.createRide

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.uamlift.sesion.ControlSesion
import ni.edu.uam.uamlift.ui.components.MapLibreView
import ni.edu.uam.uamlift.ui.theme.Degradado2
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.viewmodel.ViajeViewModel
import java.util.*

// Definimos las constantes de la UAM fuera de la función para que cualquier sub-composable acceda sin problemas
private object UamDefaults {
    const val LAT = 12.1126
    const val LNG = -86.2435
    const val ADDRESS = "UAM Campus Central"
}

@Composable
fun CreateRideScreen(
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(),
    onViajeCreado: () -> Unit = {}
) {
    val context = LocalContext.current
    val session = remember { ControlSesion(context) }
    val userCif by session.obtenerCif.collectAsState(initial = "")

    var step by remember { mutableIntStateOf(1) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var isGoingToUam by remember { mutableStateOf(false) }

    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }
    var selectedAddressText by remember { mutableStateOf("") }

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableIntStateOf(3) }
    var price by remember { mutableStateOf("") }

    if (showSuccessDialog) {
        SuccessRideDialog {
            showSuccessDialog = false
            onViajeCreado()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .background(Degradado2)
                    .padding(20.dp)
            ) {
                Text("Publicar viaje", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Comparte tu ruta — solo gastos de combustible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    listOf("Ruta", "Horario", "Precio").forEachIndexed { index, label ->
                        val isDone = index + 1 < step
                        val isActive = index + 1 == step

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDone || isActive) Color.White else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isDone) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = UAMColor)
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            color = if (isActive) UAMColor else Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(label, fontSize = 12.sp, color = Color.White, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (step) {
            1 -> Step1Map(
                isToUam = isGoingToUam,
                onToggleDirection = {
                    isGoingToUam = !isGoingToUam
                    selectedLat = null
                    selectedLng = null
                    selectedAddressText = ""
                },
                uamLat = UamDefaults.LAT, uamLng = UamDefaults.LNG,
                selLat = selectedLat, selLng = selectedLng,
                onLocationSelected = { lat, lng ->
                    selectedLat = lat
                    selectedLng = lng
                    selectedAddressText = if (isGoingToUam) "Punto de salida marcado" else "Punto de destino marcado"
                },
                onContinue = { step = 2 }
            )
            2 -> Step2Schedule(
                date = date,
                onDateChange = { date = it },
                time = time,
                onTimeChange = { time = it },
                seats = seats,
                onSeatsChange = { seats = it },
                onBack = { step = 1 },
                onContinue = { step = 3 }
            )
            3 -> Step3Price(
                from = if (isGoingToUam) (if (selectedAddressText.isEmpty()) "Ubicación en mapa" else selectedAddressText) else UamDefaults.ADDRESS,
                to = if (isGoingToUam) UamDefaults.ADDRESS else (if (selectedAddressText.isEmpty()) "Ubicación en mapa" else selectedAddressText),
                date = date, time = time, seats = seats, price = price,
                onPriceChange = { price = it },
                onBack = { step = 2 },
                onPublish = {
                    if (userCif.isNullOrEmpty()) {
                        Toast.makeText(context, "Error: No se pudo identificar la sesión del usuario", Toast.LENGTH_SHORT).show()
                        return@Step3Price
                    }

                    val origenNombre = if (isGoingToUam) selectedAddressText else UamDefaults.ADDRESS
                    val origenLat = if (isGoingToUam) selectedLat else UamDefaults.LAT
                    val origenLng = if (isGoingToUam) selectedLng else UamDefaults.LNG

                    val destinoNombre = if (isGoingToUam) UamDefaults.ADDRESS else selectedAddressText
                    val destinoLat = if (isGoingToUam) UamDefaults.LAT else selectedLat
                    val destinoLng = if (isGoingToUam) UamDefaults.LNG else selectedLng

                    viajeViewModel.actualizarOrigen(nombre = origenNombre, lat = origenLat, lng = origenLng, esUam = !isGoingToUam)
                    viajeViewModel.actualizarDestino(nombre = destinoNombre, lat = destinoLat, lng = destinoLng, esUam = isGoingToUam)

                    viajeViewModel.actualizarFechaHoraSalida("${date}T${time}:00")

                    try {
                        val parts = time.split(":")
                        val hour = parts[0].toInt()
                        val min = parts[1].toInt()
                        val arrivalHour = (hour + 1) % 24
                        val arrivalTime = String.format("%02d:%02d", arrivalHour, min)
                        viajeViewModel.actualizarFechaHoraLlegada("${date}T${arrivalTime}:00")
                    } catch (e: Exception) {
                        viajeViewModel.actualizarFechaHoraLlegada("${date}T23:59:00")
                    }

                    viajeViewModel.actualizarNumeroAsientos(seats)
                    viajeViewModel.actualizarPrecio(price.toDoubleOrNull() ?: 0.0)

                    viajeViewModel.publicarViaje(
                        conductorCif = userCif!!,
                        onExito = {
                            showSuccessDialog = true
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun Step1Map(
    isToUam: Boolean,
    onToggleDirection: () -> Unit,
    uamLat: Double, uamLng: Double,
    selLat: Double?, selLng: Double?,
    onLocationSelected: (Double, Double) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("¿Hacia dónde vas?", fontWeight = FontWeight.Bold, color = UAMColor, fontSize = 18.sp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val buttonModifier = Modifier.weight(1f).height(40.dp)
            Button(
                onClick = { if (!isToUam) onToggleDirection() },
                modifier = buttonModifier,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isToUam) UAMColor else Color.Transparent,
                    contentColor = if (isToUam) Color.White else Color.Gray
                )
            ) { Text("Hacia UAM") }
            Button(
                onClick = { if (isToUam) onToggleDirection() },
                modifier = buttonModifier,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isToUam) UAMColor else Color.Transparent,
                    contentColor = if (!isToUam) Color.White else Color.Gray
                )
            ) { Text("Desde UAM") }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            MapLibreView(
                originLat = if (isToUam) selLat else uamLat,
                originLng = if (isToUam) selLng else uamLng,
                destLat = if (isToUam) uamLat else selLat,
                destLng = if (isToUam) uamLng else selLng,
                isSelectionEnabled = true,
                onLocationSelected = onLocationSelected,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Origen:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = UAMColor)
                        Text(
                            text = if (isToUam) {
                                if (selLat != null) "Ubicación marcada" else "Toca el mapa..."
                            } else {
                                UamDefaults.ADDRESS
                            },
                            fontSize = 11.sp,
                            color = if (isToUam && selLat == null) Color(0xFFD32F2F) else Color.DarkGray,
                            maxLines = 1
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Destino:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = UAMColor)
                        Text(
                            text = if (isToUam) {
                                UamDefaults.ADDRESS
                            } else {
                                if (selLat != null) "Ubicación marcada" else "Toca el mapa..."
                            },
                            fontSize = 11.sp,
                            color = if (!isToUam && selLat == null) Color(0xFFD32F2F) else Color.DarkGray,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            enabled = selLat != null && selLng != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
        ) {
            Text("Siguiente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun Step2Schedule(
    date: String, onDateChange: (String) -> Unit,
    time: String, onTimeChange: (String) -> Unit,
    seats: Int, onSeatsChange: (Int) -> Unit,
    onBack: () -> Unit, onContinue: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateChange(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            onTimeChange(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Horario", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = date,
            onValueChange = {},
            label = { Text("Fecha") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color(0xFF019AA8),
                disabledTextColor = Color.DarkGray,
                disabledContainerColor = Color.White,
                disabledLabelColor = UAMColor
            ),
            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF019AA8), modifier = Modifier.clickable { datePickerDialog.show() }) }
        )

        OutlinedTextField(
            value = time,
            onValueChange = {},
            label = { Text("Hora de salida") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { timePickerDialog.show() },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color(0xFF019AA8),
                disabledTextColor = Color.DarkGray,
                disabledContainerColor = Color.White,
                disabledLabelColor = UAMColor
            ),
            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF019AA8), modifier = Modifier.clickable { timePickerDialog.show() }) }
        )

        Text("Asientos disponibles", style = MaterialTheme.typography.titleSmall, color = UAMColor)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 4).forEach { num ->
                val isSelected = seats == num
                FilterChip(
                    selected = isSelected, onClick = { onSeatsChange(num) },
                    label = { Text(num.toString(), color = if (isSelected) Color.White else Color.DarkGray) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF019AA8), containerColor = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF06585E))) { Text("Atrás") }
            Button(
                onClick = onContinue,
                enabled = date.isNotEmpty() && time.isNotEmpty(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
            ) {
                Text("Continuar →", color = Color.White)
            }
        }
    }
}

@Composable
fun Step3Price(
    from: String, to: String, date: String, time: String, seats: Int, price: String,
    onPriceChange: (String) -> Unit, onBack: () -> Unit, onPublish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Aporte por persona", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = price, onValueChange = onPriceChange, label = { Text("Monto en Córdoba (C$)") },
            placeholder = { Text("Ej: 25.00") }, prefix = { Text("C$ ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF019AA8), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )
        Text("Solo gastos de combustible — sin lucro.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen del viaje", style = MaterialTheme.typography.titleMedium, color = UAMColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                SummaryRow("Origen", from)
                SummaryRow("Destino", to)
                SummaryRow("Fecha", date.ifEmpty { "No especificada" })
                SummaryRow("Hora", time.ifEmpty { "—" })
                SummaryRow("Asientos", seats.toString())
                SummaryRow("Aporte", if (price.isEmpty()) "C$ 0" else "C$ $price", isHighlight = true)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF06585E))) { Text("Atrás") }
            Button(
                onClick = onPublish,
                enabled = price.isNotEmpty() && (price.toDoubleOrNull() ?: 0.0) >= 0.0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
            ) {
                Text("Publicar viaje", color = Color.White)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, modifier = Modifier.weight(0.3f))
        Text(
            text = value,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) Color(0xFF019AA8) else UAMColor,
            modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
fun SuccessRideDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(UAMColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = UAMColor
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "¡Viaje Publicado!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = UAMColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Tu ruta ha sido creada con éxito. Los estudiantes ahora pueden ver tu oferta.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
                    ) {
                        Text("Ver mis viajes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}