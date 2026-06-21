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
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableIntStateOf(1) }
    var price by remember { mutableStateOf("") }

    if (showSuccessDialog) {
        SuccessRideDialog {
            showSuccessDialog = false
            onViajeCreado()
            navController.navigate("home") { popUpTo("create") { inclusive = true } }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Gray)) {
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
                    // Inicializar coordenadas con el destino por defecto de la API al confirmar el nombre
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
                time = time, onTimeChange = { time = it },
                seats = seats, onSeatsChange = { seats = it },
                onBack = { step = 1 }, onContinue = { step = 3 }
            )
            3 -> Step3Price(
                from = if (isGoingToUam) (nombreLugarConfirmado ?: "") else (destinoViewModel.destinoDefecto?.nombre ?: "UAM"),
                to = if (isGoingToUam) (destinoViewModel.destinoDefecto?.nombre ?: "UAM") else (nombreLugarConfirmado ?: ""),
                date = date, time = time, seats = seats, price = price,
                onPriceChange = { price = it },
                onBack = { step = 2 },
                onPublish = {
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
                    
                    viajeViewModel.actualizarFechaHoraSalida("${date}T${time}:00")
                    
                    // Estimación automática de llegada para el modelo del backend (+1 hora)
                    try {
                        val parts = time.split(":")
                        val arrivalHour = (parts[0].toInt() + 1) % 24
                        val arrivalTime = String.format("%02d:%02d:00", arrivalHour, parts[1].toInt())
                        viajeViewModel.actualizarFechaHoraLlegada("${date}T$arrivalTime")
                    } catch (e: Exception) {
                        viajeViewModel.actualizarFechaHoraLlegada("${date}T23:59:00")
                    }

                    viajeViewModel.actualizarNumeroAsientos(seats)
                    viajeViewModel.actualizarPrecio(price.toDoubleOrNull() ?: 1.0)

                    viajeViewModel.publicarViaje(userCif!!, onExito = { showSuccessDialog = true })
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
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("¿Hacia dónde vas?", fontWeight = FontWeight.Bold, color = UAMColor, fontSize = 18.sp)
        
        Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(4.dp)) {
            val btnMod = Modifier.weight(1f).height(40.dp)
            Button(onClick = { if (!isToUam) onToggleDirection() }, modifier = btnMod, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isToUam) UAMColor else Color.Transparent, contentColor = if (isToUam) Color.White else Color.Gray)) { Text("Hacia UAM") }
            Button(onClick = { if (isToUam) onToggleDirection() }, modifier = btnMod, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if (!isToUam) UAMColor else Color.Transparent, contentColor = if (!isToUam) Color.White else Color.Gray)) { Text("Desde UAM") }
        }

        if (nombreConfirmado == null) {
            Text(text = if (isToUam) "1. Selecciona de dónde sales:" else "1. Selecciona a dónde vas:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
            Column(modifier = Modifier.weight(1f).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

            Box(modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(20.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(20.dp)).background(Color.White)) {
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
            
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onContinue, enabled = selLat != null, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) {
                Text("Confirmar Ruta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

@Composable
fun Step2Schedule(
    date: String, onDateChange: (String) -> Unit,
    time: String, onTimeChange: (String) -> Unit,
    seats: Int, onSeatsChange: (Int) -> Unit,
    onBack: () -> Unit, onContinue: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(context, { _, y, m, d -> onDateChange(String.format("%04d-%02d-%02d", y, m + 1, d)) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    val timePickerDialog = TimePickerDialog(context, { _, h, m -> onTimeChange(String.format("%02d:%02d", h, m)) }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Horario", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = date, onValueChange = {}, label = { Text("Fecha") }, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = UAMColor, disabledTextColor = Color.DarkGray, disabledContainerColor = Color.White, disabledLabelColor = UAMColor), leadingIcon = { Icon(Icons.Default.DateRange, null, tint = UAMColor) })
        OutlinedTextField(value = time, onValueChange = {}, label = { Text("Hora de salida") }, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = UAMColor, disabledTextColor = Color.DarkGray, disabledContainerColor = Color.White, disabledLabelColor = UAMColor), leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = UAMColor) })
        Text("Asientos disponibles (mínimo 1)", color = UAMColor, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 4).forEach { num ->
                FilterChip(selected = seats == num, onClick = { onSeatsChange(num) }, label = { Text(num.toString(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Atrás") }
            Button(onClick = onContinue, enabled = date.isNotEmpty() && time.isNotEmpty(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) { Text("Continuar →") }
        }
    }
}

@Composable
fun Step3Price(
    from: String, to: String, date: String, time: String, seats: Int, price: String,
    onPriceChange: (String) -> Unit, onBack: () -> Unit, onPublish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Aporte por persona", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = price, onValueChange = onPriceChange, label = { Text("Monto en Córdoba (C$) - Mín. C$ 1.0") }, prefix = { Text("C$ ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Resumen del viaje", style = MaterialTheme.typography.titleMedium, color = UAMColor, fontWeight = FontWeight.Bold)
                SummaryRow("Origen", from)
                SummaryRow("Destino", to)
                SummaryRow("Fecha", date)
                SummaryRow("Hora", time)
                SummaryRow("Asientos", seats.toString())
                SummaryRow("Aporte", "C$ $price", isHighlight = true)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Atrás") }
            Button(onClick = onPublish, enabled = price.isNotEmpty() && (price.toDoubleOrNull() ?: 0.0) >= 1.0, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) { Text("Publicar viaje") }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, modifier = Modifier.weight(0.3f))
        Text(text = value, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal, color = if (isHighlight) Color(0xFF019AA8) else UAMColor, modifier = Modifier.weight(0.7f))
    }
}

@Composable
fun SuccessRideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).background(UAMColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(50.dp), tint = UAMColor)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("¡Viaje Publicado!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = UAMColor)
                    Text("Tu ruta ha sido creada con éxito.", textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) { Text("Ver mis viajes", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
