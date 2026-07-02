package ni.edu.uam.uamlift.ui.screens.create.createRide

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import ni.edu.uam.uamlift.data.enums.DepartamentosPacifico
import ni.edu.uam.uamlift.data.enums.Departamento
import ni.edu.uam.uamlift.data.models.Carro
import ni.edu.uam.uamlift.data.models.Destino
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRideScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(factory = AppViewModelFactory()),
    carroViewModel: CarroViewModel = viewModel(),
    destinoViewModel: DestinoViewModel = viewModel(),
    onViajeCreado: () -> Unit = {}
) {
    val context = LocalContext.current
    val usuario = usuarioViewModel.usuario
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
    val userCifState = session.obtenerCif.collectAsState(initial = "")

    val step = rememberSaveable { mutableIntStateOf(1) }
    val showSuccessDialog = rememberSaveable { mutableStateOf(false) }
    val isGoingToUam = rememberSaveable { mutableStateOf(false) }

    val selectedLat = rememberSaveable { mutableStateOf<Double?>(null) }
    val selectedLng = rememberSaveable { mutableStateOf<Double?>(null) }
    val nombreLugarConfirmado = rememberSaveable { mutableStateOf<String?>(null) }

    val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val sdfDateTime = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

    val calendarInitial = remember { Calendar.getInstance() }
    val date = rememberSaveable { mutableStateOf(sdfDate.format(calendarInitial.time)) }

    val departureTime = rememberSaveable {
        val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
        mutableStateOf(sdfTime.format(cal.time))
    }

    val arrivalTime = rememberSaveable {
        val cal = Calendar.getInstance().apply { add(Calendar.MINUTE, 40) }
        mutableStateOf(sdfTime.format(cal.time))
    }

    val selectedCar = remember { mutableStateOf<Carro?>(null) }
    val seats = rememberSaveable { mutableIntStateOf(1) }
    val price = rememberSaveable { mutableStateOf("") }

    LaunchedEffect(carroViewModel.listaCarros) {
        if (selectedCar.value == null && carroViewModel.listaCarros.isNotEmpty()) {
            selectedCar.value = carroViewModel.listaCarros.first()
        }
    }

    if (showSuccessDialog.value) {
        SuccessRideDialog {
            showSuccessDialog.value = false
            onViajeCreado()
            navController.navigate("home") { popUpTo("create") { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Publicar Viaje", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = UAMColor)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Gray
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HeaderSteps(step.intValue)

            val scrollState = rememberScrollState()

            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    when (step.intValue) {
                        1 -> Step1LocationFlow(
                            isToUam = isGoingToUam.value,
                            onToggleDirection = { isGoingToUam.value = !isGoingToUam.value },
                            nombreConfirmado = nombreLugarConfirmado.value,
                            onNombreConfirmado = { name ->
                                nombreLugarConfirmado.value = name.takeIf { it.isNotBlank() }
                                if (name.isBlank()) {
                                    selectedLat.value = null
                                    selectedLng.value = null
                                }
                            },
                            selLat = selectedLat.value, selLng = selectedLng.value,
                            onLocationSelected = { lat, lng -> selectedLat.value = lat; selectedLng.value = lng },
                            destinoDefecto = destinoViewModel.destinoDefecto,
                            onContinue = { step.intValue = 2 }
                        )
                        2 -> Step2Schedule(
                            date = date.value, onDateChange = { date.value = it },
                            departureTime = departureTime.value, onDepartureTimeChange = { departureTime.value = it },
                            arrivalTime = arrivalTime.value, onArrivalTimeChange = { arrivalTime.value = it },
                            selectedCar = selectedCar.value, onCarChange = { selectedCar.value = it },
                            cars = carroViewModel.listaCarros,
                            seats = seats.intValue, onSeatsChange = { seats.intValue = it },
                            onBack = { step.intValue = 1 }, onContinue = { step.intValue = 3 },
                            sdfDateTime = sdfDateTime
                        )
                        3 -> Step3Price(
                            from = if (isGoingToUam.value) (nombreLugarConfirmado.value ?: "") else (destinoViewModel.destinoDefecto?.nombre ?: "UAM"),
                            to = if (isGoingToUam.value) (destinoViewModel.destinoDefecto?.nombre ?: "UAM") else (nombreLugarConfirmado.value ?: ""),
                            date = date.value, departureTime = departureTime.value, arrivalTime = arrivalTime.value,
                            car = selectedCar.value, seats = seats.intValue, price = price.value,
                            onPriceChange = { price.value = it },
                            onBack = { step.intValue = 2 },
                            onPublish = {
                                val userId = usuario.id ?: return@Step3Price
                                if (userCifState.value.isBlank()) return@Step3Price

                                scope.launch {
                                    try {
                                        val uam = destinoViewModel.destinoDefecto ?: Destino(nombre = "UAM", latitud = 12.1126, longitud = -86.2435, universidad = true)
                                        val lugarUsuarioBase = Destino(nombre = nombreLugarConfirmado.value ?: "Lugar", latitud = selectedLat.value, longitud = selectedLng.value, universidad = false)
                                        val lugarUsuarioFinal = destinoViewModel.agregarDestino(lugarUsuarioBase) ?: lugarUsuarioBase
                                        val (origenFinal, destinoFinal) = if (isGoingToUam.value) lugarUsuarioFinal to uam else uam to lugarUsuarioFinal

                                        viajeViewModel.publicarViaje(
                                            usuarioId = userId, conductorCif = userCifState.value,
                                            origen = origenFinal, destino = destinoFinal,
                                            fechaSalida = "${date.value}T${departureTime.value}:00",
                                            fechaLlegada = "${date.value}T${arrivalTime.value}:00",
                                            asientos = seats.intValue, precio = price.value.toDoubleOrNull() ?: 1.0, carro = selectedCar.value,
                                            onExito = { showSuccessDialog.value = true },
                                            onError = { razon -> scope.launch { snackbarHostState.showSnackbar(razon) } }
                                        )
                                    } catch (_: Exception) {
                                        scope.launch { snackbarHostState.showSnackbar("Error al procesar el destino") }
                                    }
                                }
                            }
                        )
                    }
                }

                // Sticky Footer for step 1 button if needed, but Step1LocationFlow handles its own button.
                // However, to ensure it "appears", we can move buttons here or ensure they are at the end of the content.
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
    val customName = rememberSaveable { mutableStateOf("") }
    val isCustomMode = rememberSaveable { mutableStateOf(false) }
    val selectedDept = rememberSaveable { mutableStateOf<Departamento?>(null) }
    val deptListScrollState = rememberScrollState()
    val view = LocalView.current

    val departamentos = DepartamentosPacifico.getAll()
    val options = departamentos.map { it.nombre }

    val isLocationSelected = !nombreConfirmado.isNullOrBlank()

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

        if (!isLocationSelected) {
            Text(text = if (isToUam) "1. Selecciona de dónde sales:" else "1. Selecciona a dónde vas:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(deptListScrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    SelectableLocationItem(text = option, isSelected = selectedDept.value?.nombre == option) {
                        val dept = DepartamentosPacifico.getByName(option)
                        if (dept != null) {
                            selectedDept.value = dept
                            isCustomMode.value = false
                            onNombreConfirmado(option)
                            onLocationSelected(dept.lat, dept.lng)
                        }
                    }
                }
                SelectableLocationItem(text = "Otro (Lugar personalizado)", isSelected = isCustomMode.value) { isCustomMode.value = true }

                AnimatedVisibility(visible = isCustomMode.value, enter = expandVertically(), exit = shrinkVertically()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(value = customName.value, onValueChange = { customName.value = it }, label = { Text("Nombre del lugar") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { if (customName.value.isNotBlank()) onNombreConfirmado(customName.value) }, modifier = Modifier.size(48.dp).background(UAMColor, CircleShape)) { Icon(Icons.Default.Check, null, tint = Color.White) }
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(UAMColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Icon(Icons.Default.LocationOn, null, tint = UAMColor)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Lugar base seleccionado", fontSize = 11.sp, color = Color.Gray)
                    Text(nombreConfirmado ?: "", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 16.sp)
                }
                TextButton(onClick = {
                    selectedDept.value = null
                    isCustomMode.value = false
                    onNombreConfirmado("")
                }) { Text("Cambiar", color = UAMColor, fontWeight = FontWeight.Bold) }
            }

            Text("2. Manten presionado el pin rojo y arrástralo para ubicar tu casa exacta:", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            android.view.MotionEvent.ACTION_DOWN -> { view.parent?.requestDisallowInterceptTouchEvent(true) }
                            android.view.MotionEvent.ACTION_UP,
                            android.view.MotionEvent.ACTION_CANCEL -> { view.parent?.requestDisallowInterceptTouchEvent(false) }
                        }
                        false
                    }
            ) {
                val uamLat = destinoDefecto?.latitud ?: 12.108512
                val uamLng = destinoDefecto?.longitud ?: -86.257050

                val destinoLat = selLat ?: selectedDept.value?.lat ?: uamLat
                val destinoLng = selLng ?: selectedDept.value?.lng ?: uamLng

                val origenLat = if (isToUam) destinoLat else uamLat
                val origenLng = if (isToUam) destinoLng else uamLng
                val destinoMapaLat = if (isToUam) uamLat else destinoLat
                val destinoMapaLng = if (isToUam) uamLng else destinoLng

                val marcadorArrastrable = if (isToUam) "ORIGIN" else "DEST"

                MapLibreView(
                    originLat = origenLat,
                    originLng = origenLng,
                    destLat = destinoMapaLat,
                    destLng = destinoMapaLng,
                    isSelectionEnabled = true,
                    draggableMarkerType = marcadorArrastrable,
                    onLocationSelected = onLocationSelected,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onContinue,
                enabled = selLat != null,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Schedule(
    date: String, onDateChange: (String) -> Unit,
    departureTime: String, onDepartureTimeChange: (String) -> Unit,
    arrivalTime: String, onArrivalTimeChange: (String) -> Unit,
    selectedCar: Carro?, onCarChange: (Carro) -> Unit,
    cars: List<Carro>,
    seats: Int, onSeatsChange: (Int) -> Unit,
    onBack: () -> Unit, onContinue: () -> Unit,
    sdfDateTime: SimpleDateFormat
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->
        onDateChange(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d))
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    val depTimePicker = TimePickerDialog(context, { _, h, m ->
        onDepartureTimeChange(String.format(Locale.US, "%02d:%02d", h, m))
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val arrTimePicker = TimePickerDialog(context, { _, h, m ->
        onArrivalTimeChange(String.format(Locale.US, "%02d:%02d", h, m))
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val isValid = remember(date, departureTime, arrivalTime, selectedCar) {
        if (date.isEmpty() || departureTime.isEmpty() || arrivalTime.isEmpty() || selectedCar == null) false
        else {
            try {
                val dep = sdfDateTime.parse("$date $departureTime") ?: return@remember false
                val arr = sdfDateTime.parse("$date $arrivalTime") ?: return@remember false
                arr.time >= (dep.time + 30 * 60 * 1000) // Al menos 30 min de diferencia
            } catch (_: Exception) { false }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Horario y Vehículo", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)

        Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
            OutlinedTextField(
                value = date, onValueChange = {}, label = { Text("Fecha del viaje") },
                readOnly = true, enabled = false, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = UAMColor, disabledTextColor = Color.Black, disabledContainerColor = Color.White, disabledLabelColor = UAMColor),
                leadingIcon = { Icon(Icons.Default.DateRange, null, tint = UAMColor) }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f).clickable { depTimePicker.show() }) {
                OutlinedTextField(
                    value = departureTime, onValueChange = {}, label = { Text("Salida") },
                    readOnly = true, enabled = false, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = UAMColor, disabledTextColor = Color.Black, disabledContainerColor = Color.White, disabledLabelColor = UAMColor, disabledLeadingIconColor = UAMColor),
                    leadingIcon = { Icon(Icons.Default.Schedule, null) }
                )
            }
            Box(modifier = Modifier.weight(1f).clickable { arrTimePicker.show() }) {
                OutlinedTextField(
                    value = arrivalTime, onValueChange = {}, label = { Text("Llegada Estimada") },
                    readOnly = true, enabled = false, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = UAMColor, disabledTextColor = Color.Black, disabledContainerColor = Color.White, disabledLabelColor = UAMColor, disabledLeadingIconColor = UAMColor),
                    leadingIcon = { Icon(Icons.Default.Timer, null) }
                )
            }
        }

        Text("Selecciona tu vehículo", color = UAMColor, fontWeight = FontWeight.Bold)
        CarSelectionList(cars = cars, selectedCar = selectedCar, onCarSelected = onCarChange)

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
fun CarSelectionList(cars: List<Carro>, selectedCar: Carro?, onCarSelected: (Carro) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cars.forEach { car ->
            val isSelected = car.id == selectedCar?.id
            Surface(
                onClick = { onCarSelected(car) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) UAMColor.copy(alpha = 0.08f) else Color.White,
                border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) UAMColor else Color.LightGray.copy(alpha = 0.5f)),
                shadowElevation = if (isSelected) 2.dp else 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(if (isSelected) UAMColor.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DirectionsCar, null, tint = if (isSelected) UAMColor else Color.Gray, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${car.marca} ${car.modelo}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSelected) UAMColor else Color.DarkGray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Placa: ${car.placa}", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = UAMColor, modifier = Modifier.size(24.dp))
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
        OutlinedTextField(
            value = price,
            onValueChange = { nuevoValor -> onPriceChange(nuevoValor.replace("\n", "")) },
            label = { Text("Monto en Córdoba (C$)") },
            prefix = { Text("C$ ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

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
        Text(text = value, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium, color = UAMColor, modifier = Modifier.weight(0.65f), textAlign = TextAlign.End, fontSize = 14.sp)
    }
}

@Composable
fun SuccessRideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(64.dp), tint = Color(0xFF4CAF50))
                    Text("¡Viaje Publicado!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UAMColor)) {
                        Text("Excelente", color = Color.White)
                    }
                }
            }
        }
    }
}
