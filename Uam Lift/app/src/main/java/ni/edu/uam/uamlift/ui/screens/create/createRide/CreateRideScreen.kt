package ni.edu.uam.uamlift.ui.screens.create.createRide

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import ni.edu.uam.uamlift.data.enums.DepartamentosPacifico
import ni.edu.uam.uamlift.data.models.Carro
import ni.edu.uam.uamlift.data.models.Destino
import ni.edu.uam.uamlift.data.viewmodels.CarroViewModel
import ni.edu.uam.uamlift.data.viewmodels.DestinoViewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.sesion.ControlSesion
import ni.edu.uam.uamlift.ui.components.MapLibreView
import ni.edu.uam.uamlift.ui.theme.Degradado2
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateRideScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(),
    carroViewModel: CarroViewModel = viewModel(),
    destinoViewModel: DestinoViewModel = viewModel(),
    onViajeCreado: () -> Unit = {}
) {
    val context = LocalContext.current
    val usuario = usuarioViewModel.usuario

    LaunchedEffect(usuario.id) {
        usuario.id?.let { carroViewModel.obtenerCarrosPorUsuario(it) }
        destinoViewModel.obtenerDestinoDefecto()
    }

    if (carroViewModel.cargando && carroViewModel.listaCarros.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = UAMColor)
        }
        return
    }

    if (carroViewModel.listaCarros.isEmpty() && !carroViewModel.cargando) {
        VehicleRequiredPlaceholder { navController.navigate("my_cars") }
        return
    }

    val session = remember { ControlSesion(context) }
    val userCif by session.obtenerCif.collectAsState(initial = "")

    var step by remember { mutableIntStateOf(1) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isGoingToUam by remember { mutableStateOf(false) }

    // Ubicaciones definitivas
    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }
    var nombreLugarConfirmado by remember { mutableStateOf<String?>(null) }

    var date by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }
    var arrivalTime by remember { mutableStateOf("") }
    var selectedCar by remember { mutableStateOf<Carro?>(null) }
    var seats by remember { mutableIntStateOf(1) }
    var price by remember { mutableStateOf("") }

    // Auto-select first car if available
    LaunchedEffect(carroViewModel.listaCarros) {
        if (selectedCar == null && carroViewModel.listaCarros.isNotEmpty()) {
            selectedCar = carroViewModel.listaCarros.first()
        }
    }

    if (showSuccessDialog) {
        SuccessRideDialog {
            showSuccessDialog = false
            onViajeCreado()
            navController.navigate("home") { popUpTo("create") { inclusive = true } }
        }
    }

    // El scroll global ahora vive aquí para que responda bien en cualquier dispositivo
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSteps(step)

        when (step) {
            1 -> Step1LocationFlow(
                isToUam = isGoingToUam,
                onToggleDirection = {
                    isGoingToUam = !isGoingToUam
                    selectedLat = null; selectedLng = null; nombreLugarConfirmado = null
                },
                nombreConfirmado = nombreLugarConfirmado,
                onNombreConfirmado = { name ->
                    nombreLugarConfirmado = name
                    if (selectedLat == null) {
                        selectedLat = destinoViewModel.destinoDefecto?.latitud
                        selectedLng = destinoViewModel.destinoDefecto?.longitud
                    }
                },
                selLat = selectedLat, selLng = selectedLng,
                onLocationSelected = { lat, lng -> selectedLat = lat; selectedLng = lng },
                destinoDefecto = destinoViewModel.destinoDefecto,
                onContinue = { step = 2 }
            )
            2 -> Step2Schedule(
                date = date, onDateChange = { date = it },
                departureTime = departureTime, onDepartureTimeChange = { departureTime = it },
                arrivalTime = arrivalTime, onArrivalTimeChange = { arrivalTime = it },
                selectedCar = selectedCar, onCarChange = { selectedCar = it },
                cars = carroViewModel.listaCarros,
                seats = seats, onSeatsChange = { seats = it },
                onBack = { step = 1 }, onContinue = { step = 3 }
            )
            3 -> Step3Price(
                from = if (isGoingToUam) (nombreLugarConfirmado ?: "") else (destinoViewModel.destinoDefecto?.nombre ?: "UAM"),
                to = if (isGoingToUam) (destinoViewModel.destinoDefecto?.nombre ?: "UAM") else (nombreLugarConfirmado ?: ""),
                date = date, departureTime = departureTime, arrivalTime = arrivalTime,
                car = selectedCar, seats = seats, price = price,
                onPriceChange = { price = it },
                onBack = { step = 2 },
                onPublish = {
                    val userId = usuario.id ?: return@Step3Price
                    if (userCif.isNullOrEmpty()) return@Step3Price

                    val uam = destinoViewModel.destinoDefecto
                    val lugarUsuario = Destino(
                        nombre = nombreLugarConfirmado ?: "Lugar",
                        latitud = selectedLat,
                        longitud = selectedLng,
                        universidad = false
                    )

                    if (isGoingToUam) {
                        viajeViewModel.actualizarOrigen(lugarUsuario)
                        viajeViewModel.actualizarDestino(uam)
                    } else {
                        viajeViewModel.actualizarOrigen(uam)
                        viajeViewModel.actualizarDestino(lugarUsuario)
                    }

                    viajeViewModel.actualizarFechaHoraSalida("${date}T${departureTime}:00")
                    viajeViewModel.actualizarFechaHoraLlegada("${date}T${arrivalTime}:00")
                    viajeViewModel.actualizarCarro(selectedCar)
                    viajeViewModel.actualizarNumeroAsientos(seats)
                    viajeViewModel.actualizarPrecio(price.toDoubleOrNull() ?: 1.0)

                    viajeViewModel.publicarViaje(
                        usuarioId = userId,
                        conductorCif = userCif!!,
                        onExito = { showSuccessDialog = true },
                        onError = { razon ->
                            Toast.makeText(context, razon, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun Step1LocationFlow(
    isToUam: Boolean,
    onToggleDirection: () -> Unit,
    nombreConfirmado: String?,
    onNombreConfirmado: (String) -> Unit,
    selLat: Double?,
    selLng: Double?,
    onLocationSelected: (Double, Double) -> Unit,
    destinoDefecto: Destino?,
    onContinue: () -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var isCustomMode by remember { mutableStateOf(false) }
    val options = DepartamentosPacifico.values().map { it.name }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("¿Hacia dónde vas?", fontWeight = FontWeight.Bold, color = UAMColor, fontSize = 18.sp)

        Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(4.dp)) {
            val btnMod = Modifier.weight(1f).height(40.dp)
            Button(onClick = { if (!isToUam) onToggleDirection() }, modifier = btnMod, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isToUam) UAMColor else Color.Transparent, contentColor = if (isToUam) Color.White else Color.Gray)) { Text("Hacia UAM") }
            Button(onClick = { if (isToUam) onToggleDirection() }, modifier = btnMod, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (!isToUam) UAMColor else Color.Transparent, contentColor = if (!isToUam) Color.White else Color.Gray)) { Text("Desde UAM") }
        }

        if (nombreConfirmado == null) {
            Text(text = if (isToUam) "1. Selecciona de dónde sales:" else "1. Selecciona a dónde vas:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    SelectableLocationItem(text = option, isSelected = false) { onNombreConfirmado(option) }
                }
                SelectableLocationItem(text = "Otro (Lugar personalizado)", isSelected = isCustomMode) { isCustomMode = true }

                AnimatedVisibility(visible = isCustomMode, enter = expandVertically(), exit = shrinkVertically()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(value = customName, onValueChange = { customName = it }, label = { Text("Nombre del lugar") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { if (customName.isNotBlank()) onNombreConfirmado(customName) }, modifier = Modifier.size(48.dp).background(UAMColor, CircleShape)) { Icon(Icons.Default.Check, null, tint = Color.White) }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(UAMColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = UAMColor)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lugar seleccionado", fontSize = 11.sp, color = Color.Gray)
                        Text(nombreConfirmado, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 16.sp)
                    }
                    TextButton(onClick = { onToggleDirection() }) { Text("Cambiar", color = UAMColor, fontWeight = FontWeight.Bold) }
                }

                Text("2. Marca la ubicación exacta en el mapa:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)

                Box(modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(20.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(20.dp)).background(Color.White)) {
                    MapLibreView(
                        originLat = if (isToUam) (selLat ?: destinoDefecto?.latitud ?: 12.1126) else (destinoDefecto?.latitud ?: 12.1126),
                        originLng = if (isToUam) (selLng ?: destinoDefecto?.longitud ?: -86.2435) else (destinoDefecto?.longitud ?: -86.2435),
                        destLat = if (isToUam) (destinoDefecto?.latitud ?: 12.1126) else (selLat ?: destinoDefecto?.latitud ?: 12.1126),
                        destLng = if (isToUam) (destinoDefecto?.longitud ?: -86.2435) else (selLng ?: destinoDefecto?.longitud ?: -86.2435),
                        isSelectionEnabled = true,
                        onLocationSelected = onLocationSelected,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(modifier = Modifier.align(Alignment.TopStart).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LocationBadge(label = "Tu Punto", name = nombreConfirmado, isHighlight = selLat == null)
                        LocationBadge(label = "Punto Defecto", name = destinoDefecto?.nombre ?: "UAM", isHighlight = false)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onContinue, enabled = selLat != null, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) {
                    Text("Confirmar Ruta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SelectableLocationItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) UAMColor.copy(alpha = 0.1f) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) UAMColor else Color.LightGray.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Icon(Icons.Default.Place, null, tint = if (isSelected) UAMColor else Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = text, fontSize = 16.sp, color = if (isSelected) UAMColor else Color.DarkGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
fun HeaderSteps(step: Int) {
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 4.dp) {
        Column(modifier = Modifier.background(Degradado2).padding(20.dp)) {
            Text("Publicar viaje", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Comparte tu ruta — solo gastos de combustible", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                listOf("Ubicación", "Horario", "Precio").forEachIndexed { index, label ->
                    val isDone = index + 1 < step
                    val isActive = index + 1 == step
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDone || isActive) Color.White else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isDone) Icon(Icons.Default.Check, null, tint = UAMColor)
                                else Text(text = "${index + 1}", color = if (isActive) UAMColor else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(label, fontSize = 12.sp, color = Color.White, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleRequiredPlaceholder(onNavigate: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(64.dp), tint = UAMColor)
                Text("Vehículo requerido", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UAMColor)
                Text("Para publicar un viaje, primero debes registrar un vehículo en tu perfil.", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp)
                Button(onClick = onNavigate, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) {
                    Text("Gestionar Vehículos")
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
fun LocationBadge(label: String, name: String, isHighlight: Boolean) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$label: ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = UAMColor)
            Text(name, fontSize = 11.sp, color = if (isHighlight) Color.Red else Color.DarkGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Schedule(
    date: String, onDateChange: (String) -> Unit,
    departureTime: String, onDepartureTimeChange: (String) -> Unit,
    arrivalTime: String, onArrivalTimeChange: (String) -> Unit,
    selectedCar: Carro?, onCarChange: (Carro) -> Unit,
    cars: List<Carro>,
    seats: Int, onSeatsChange: (Int) -> Unit,
    onBack: () -> Unit, onContinue: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->
        onDateChange(String.format("%04d-%02d-%02d", y, m + 1, d))
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

    val depTimePicker = TimePickerDialog(context, { _, h, m ->
        onDepartureTimeChange(String.format("%02d:%02d", h, m))
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val arrTimePicker = TimePickerDialog(context, { _, h, m ->
        onArrivalTimeChange(String.format("%02d:%02d", h, m))
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val isValid = remember(date, departureTime, arrivalTime, selectedCar) {
        if (date.isEmpty() || departureTime.isEmpty() || arrivalTime.isEmpty() || selectedCar == null) false
        else {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val now = Calendar.getInstance().time
                val dep = sdf.parse("$date $departureTime") ?: return@remember false
                val arr = sdf.parse("$date $arrivalTime") ?: return@remember false

                dep.after(now) && arr.time >= (dep.time + 30 * 60 * 1000)
            } catch (e: Exception) {
                false
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Horario y Vehículo", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = date, onValueChange = {}, label = { Text("Fecha del viaje") }, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = UAMColor, disabledTextColor = Color.DarkGray, disabledContainerColor = Color.White, disabledLabelColor = UAMColor), leadingIcon = { Icon(Icons.Default.DateRange, null, tint = UAMColor) })

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = departureTime,
                onValueChange = {},
                label = { Text("Salida") },
                readOnly = true,
                modifier = Modifier.weight(1f).clickable { depTimePicker.show() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = UAMColor, disabledTextColor = Color.DarkGray, disabledContainerColor = Color.White, disabledLabelColor = UAMColor, disabledLeadingIconColor = UAMColor),
                leadingIcon = { Icon(Icons.Default.Schedule, null) }
            )
            OutlinedTextField(
                value = arrivalTime,
                onValueChange = {},
                label = { Text("Llegada") },
                readOnly = true,
                modifier = Modifier.weight(1f).clickable { arrTimePicker.show() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color(0xFFE65100), disabledTextColor = Color.DarkGray, disabledContainerColor = Color.White, disabledLabelColor = Color(0xFFE65100), disabledLeadingIconColor = Color(0xFFE65100)),
                leadingIcon = { Icon(Icons.Default.Timer, null) }
            )
        }

        if (!isValid && date.isNotEmpty() && departureTime.isNotEmpty() && arrivalTime.isNotEmpty()) {
            val now = Calendar.getInstance().time
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dep = try { sdf.parse("$date $departureTime") } catch(e: Exception) { null }

            val errorMsg = if (dep != null && !dep.after(now)) "La salida debe ser futura."
            else "La llegada debe ser al menos 30 min después de la salida."

            Text(errorMsg, color = Color.Red, fontSize = 12.sp)
        }

        Text("Selecciona tu vehículo", color = UAMColor, fontWeight = FontWeight.Bold)

        CarSelectionList(
            cars = cars,
            selectedCar = selectedCar,
            onCarSelected = onCarChange
        )

        Text("Asientos disponibles", color = UAMColor, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 4).forEach { num ->
                FilterChip(selected = seats == num, onClick = { onSeatsChange(num) }, label = { Text(num.toString(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Atrás") }
            Button(onClick = onContinue, enabled = isValid, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) { Text("Continuar →") }
        }
    }
}

@Composable
fun CarSelectionList(
    cars: List<Carro>,
    selectedCar: Carro?,
    onCarSelected: (Carro) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cars.forEach { car ->
            val isSelected = car.id == selectedCar?.id
            Surface(
                onClick = { onCarSelected(car) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) UAMColor.copy(alpha = 0.08f) else Color.White,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) UAMColor else Color.LightGray.copy(alpha = 0.5f)
                ),
                shadowElevation = if (isSelected) 2.dp else 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (isSelected) UAMColor.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = if (isSelected) UAMColor else Color.Gray, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${car.marca} ${car.modelo}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSelected) UAMColor else Color.DarkGray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Placa: ${car.placa}", fontSize = 13.sp, color = Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.size(4.dp).background(Color.LightGray, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text(text = car.color ?: "N/A", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Seleccionado", tint = UAMColor, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Step3Price(
    from: String, to: String, date: String, departureTime: String, arrivalTime: String,
    car: Carro?, seats: Int, price: String,
    onPriceChange: (String) -> Unit, onBack: () -> Unit, onPublish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Aporte por persona", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = price, onValueChange = onPriceChange, label = { Text("Monto en Córdoba (C$)") }, prefix = { Text("C$ ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Resumen del viaje", style = MaterialTheme.typography.titleMedium, color = UAMColor, fontWeight = FontWeight.Bold)
                SummaryRow("Ruta", "$from → $to")
                SummaryRow("Fecha", date)
                SummaryRow("Salida", departureTime)
                SummaryRow("Llegada", arrivalTime)
                SummaryRow("Vehículo", car?.let { "${it.marca} ${it.modelo}" } ?: "No seleccionado")
                SummaryRow("Asientos", seats.toString())
                SummaryRow("Aporte", "C$ $price", isHighlight = true)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Atrás") }
            Button(onClick = onPublish, enabled = price.isNotEmpty() && (price.toDoubleOrNull() ?: 0.0) >= 1.0, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) { Text("Publicar viaje") }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, modifier = Modifier.weight(0.35f), fontSize = 14.sp)
        Text(text = value, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium, color = if (isHighlight) Color(0xFF019AA8) else UAMColor, modifier = Modifier.weight(0.65f), textAlign = TextAlign.End, fontSize = 14.sp)
    }
}

@Composable
fun SuccessRideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF4CAF50))
                    Text("¡Viaje Publicado!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text("Tu viaje se ha registrado con éxito. Los pasajeros ahora podrán ver tu ruta disponible.", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp)
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
                    ) {
                        Text("Excelente", color = Color.White)
                    }
                }
            }
        }
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