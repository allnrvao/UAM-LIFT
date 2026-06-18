package ni.edu.uam.uamlift.ui.screens.create.createRide

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.uamlift.data.models.Destino
import ni.edu.uam.uamlift.sesion.ControlSesion
import ni.edu.uam.uamlift.ui.components.MapLibreView
import ni.edu.uam.uamlift.ui.theme.Degradado2
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import java.util.*

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

    // Ubicación Estática UAM (Salida por defecto)
    val uamLat = 12.1126
    val uamLng = -86.2435
    val uamAddress = "UAM Campus Central"

    // Por defecto: "Desde UAM" (isGoingToUam = false) como pidió el usuario
    var isGoingToUam by remember { mutableStateOf(false) }

    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }
    var selectedAddressText by remember { mutableStateOf("") }

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableIntStateOf(3) }
    var price by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().background(Gray)) {
        // Header
        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 4.dp) {
            Column(modifier = Modifier.background(Degradado2).padding(20.dp)) {
                Text("Publicar viaje", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Crea una ruta compartida — Comunidad UAM", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    listOf("Ruta", "Horario", "Precio").forEachIndexed { index, label ->
                        val active = index + 1 == step
                        val done = index + 1 < step
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(32.dp).background(if (active || done) Color.White else Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (done) Icon(Icons.Default.Check, null, tint = UAMColor)
                                else Text("${index+1}", color = if (active) UAMColor else Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text(label, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        when (step) {
            1 -> Step1Map(
                isToUam = isGoingToUam,
                onToggleDirection = { isGoingToUam = !isGoingToUam; selectedLat = null; selectedLng = null },
                uamLat = uamLat, uamLng = uamLng,
                selLat = selectedLat, selLng = selectedLng,
                onLocationSelected = { lat, lng ->
                    selectedLat = lat; selectedLng = lng
                    selectedAddressText = "Ubicación seleccionada"
                },
                onContinue = { step = 2 }
            )
            2 -> Step2Schedule(
                date = date, onDateChange = { date = it },
                time = time, onTimeChange = { time = it },
                seats = seats, onSeatsChange = { seats = it },
                onBack = { step = 1 },
                onContinue = { step = 3 }
            )
            3 -> Step3Price(
                from = if (isGoingToUam) selectedAddressText else uamAddress,
                to = if (isGoingToUam) uamAddress else selectedAddressText,
                date = date, time = time, seats = seats, price = price,
                onPriceChange = { price = it },
                onBack = { step = 2 },
                onPublish = {
                    // --- CORRECCIÓN AQUÍ ---
                    // Determinamos nombres y coordenadas según la dirección
                    val origenNombre = if (isGoingToUam) selectedAddressText else uamAddress
                    val origenLat = if (isGoingToUam) selectedLat else uamLat
                    val origenLng = if (isGoingToUam) selectedLng else uamLng

                    val destinoNombre = if (isGoingToUam) uamAddress else selectedAddressText
                    val destinoLat = if (isGoingToUam) uamLat else selectedLat
                    val destinoLng = if (isGoingToUam) uamLng else selectedLng

                    // Llamamos a las funciones pasando los 3 parámetros correctos
                    viajeViewModel.actualizarOrigen(nombre = origenNombre, lat = origenLat, lng = origenLng)
                    viajeViewModel.actualizarDestino(nombre = destinoNombre, lat = destinoLat, lng = destinoLng)

                    viajeViewModel.actualizarFechaHoraSalida("${date}T${time}:00")
                    viajeViewModel.actualizarNumeroAsientos(seats)
                    viajeViewModel.actualizarPrecio(price.toDoubleOrNull() ?: 0.0)

                    viajeViewModel.publicarViaje(userCif) {
                        onViajeCreado()
                    }
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
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("¿Hacia dónde vas?", fontWeight = FontWeight.Bold, color = UAMColor)

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val modifier = Modifier.weight(1f).height(40.dp)
            Button(
                onClick = { if (!isToUam) onToggleDirection() },
                modifier = modifier,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isToUam) UAMColor else Color.Transparent, contentColor = if (isToUam) Color.White else Color.Gray)
            ) { Text("Hacia UAM") }
            Button(
                onClick = { if (isToUam) onToggleDirection() },
                modifier = modifier,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (!isToUam) UAMColor else Color.Transparent, contentColor = if (!isToUam) Color.White else Color.Gray)
            ) { Text("Desde UAM") }
        }

        Text(
            text = if (isToUam) "Toca el mapa para marcar tu punto de SALIDA" else "Toca el mapa para marcar tu punto de DESTINO",
            fontSize = 13.sp, color = Color.Gray
        )

        Box(modifier = Modifier.weight(1f).border(1.dp, Color.LightGray, RoundedCornerShape(16.dp)).padding(2.dp)) {
            MapLibreView(
                originLat = if (isToUam) selLat else uamLat,
                originLng = if (isToUam) selLng else uamLng,
                destLat = if (isToUam) uamLat else selLat,
                destLng = if (isToUam) uamLng else selLng,
                isSelectionEnabled = true,
                onLocationSelected = onLocationSelected,
            )
        }

        Button(
            onClick = onContinue,
            enabled = selLat != null,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
        ) {
            Text("Siguiente", fontWeight = FontWeight.Bold)
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

    val datePicker = DatePickerDialog(context, { _, y, m, d -> onDateChange(String.format("%04d-%02d-%02d", y, m + 1, d)) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    val timePicker = TimePickerDialog(context, { _, h, min -> onTimeChange(String.format("%02d:%02d", h, min)) }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Fecha y Hora", fontWeight = FontWeight.Bold, color = UAMColor)

        OutlinedTextField(
            value = date, onValueChange = {}, readOnly = true, label = { Text("Fecha de salida") },
            modifier = Modifier.fillMaxWidth().clickable { datePicker.show() },
            enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.Gray, disabledTextColor = Color.Black, disabledLabelColor = UAMColor),
            leadingIcon = { Icon(Icons.Default.DateRange, null) }
        )

        OutlinedTextField(
            value = time, onValueChange = {}, readOnly = true, label = { Text("Hora de salida") },
            modifier = Modifier.fillMaxWidth().clickable { timePicker.show() },
            enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.Gray, disabledTextColor = Color.Black, disabledLabelColor = UAMColor),
            leadingIcon = { Icon(Icons.Default.Schedule, null) }
        )

        Text("Asientos disponibles", fontWeight = FontWeight.Bold, color = UAMColor)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..4).forEach { num ->
                val sel = seats == num
                Button(
                    onClick = { onSeatsChange(num) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (sel) UAMColor else Color.White, contentColor = if (sel) Color.White else Color.Black),
                    border = if (!sel) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null
                ) { Text(num.toString()) }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Atrás") }
            Button(onClick = onContinue, enabled = date.isNotEmpty() && time.isNotEmpty(), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) { Text("Siguiente") }
        }
    }
}

@Composable
fun Step3Price(
    from: String, to: String, date: String, time: String, seats: Int, price: String,
    onPriceChange: (String) -> Unit, onBack: () -> Unit, onPublish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Costo del aporte", fontWeight = FontWeight.Bold, color = UAMColor)

        OutlinedTextField(
            value = price, onValueChange = { if (it.all { c -> c.isDigit() }) onPriceChange(it) },
            label = { Text("Precio por persona (C$)") },
            prefix = { Text("C$ ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Resumen", fontWeight = FontWeight.Bold, color = UAMColor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ruta:"); Text("$from → $to", color = Color.Gray, fontSize = 12.sp) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Fecha/Hora:"); Text("$date a las $time", color = Color.Gray) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Asientos:"); Text(seats.toString(), color = Color.Gray) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Total Aporte:"); Text("C$ $price", fontWeight = FontWeight.Bold, color = UAMColor) }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Atrás") }
            Button(onClick = onPublish, enabled = price.isNotEmpty(), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) { Text("Publicar Viaje") }
        }
    }
}