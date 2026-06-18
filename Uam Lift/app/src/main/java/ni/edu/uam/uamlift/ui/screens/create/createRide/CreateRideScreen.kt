package ni.edu.uam.uamlift.ui.screens.create.createRide

<<<<<<< Updated upstream
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
=======
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
>>>>>>> Stashed changes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// GOOGLE MAPS CORE & PLACES
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

// GOOGLE MAPS COMPOSE (Librería nativa declarativa)
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

import ni.edu.uam.uamlift.ui.theme.Degradado2
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor

<<<<<<< Updated upstream
@Composable
fun CreateRideScreen(modifier: Modifier = Modifier) {
    // Optimización: mutableIntStateOf para evitar autoboxing
    var step by remember { mutableIntStateOf(1) }

    var fromLocation by remember { mutableStateOf<LatLng?>(null) }
    var fromAddressText by remember { mutableStateOf("") }

    var toLocation by remember { mutableStateOf<LatLng?>(null) }
    var toAddressText by remember { mutableStateOf("") }
=======
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

        // ¡ACTUALIZADO! Coordenadas oficiales de la Universidad Americana (UAM) Managua solicitadas
        val uamLat = 12.108038
        val uamLng = -86.257292
        val uamAddress = "Universidad Americana (UAM), Sector Suroeste Camino De Oriente"

        var isGoingToUam by remember { mutableStateOf(false) }

    // Estas variables representarán el punto dinámico que el usuario marca en el mapa
    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }
    var selectedAddressText by remember { mutableStateOf("") }
>>>>>>> Stashed changes

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableIntStateOf(3) }
    var price by remember { mutableStateOf("") }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, "YOUR_API_KEY_HERE")
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
                                shape = MaterialTheme.shapes.small,
                                color = if (isDone || isActive) Color.White else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isDone) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF019AA8))
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            color = if (isActive) Color(0xFF019AA8) else Color.White,
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
<<<<<<< Updated upstream
            1 -> Step1Route(
                onContinue = { step = 2 },
                fromLocation = fromLocation,
                fromAddressText = fromAddressText,
                toLocation = toLocation,
                toAddressText = toAddressText,
                onLocationsUpdated = { fromLoc, fromText, toLoc, toText ->
                    fromLocation = fromLoc
                    fromAddressText = fromText
                    toLocation = toLoc
                    toAddressText = toText
                }
=======
            1 -> Step1Map(
                isToUam = isGoingToUam,
                onToggleDirection = {
                    isGoingToUam = !isGoingToUam
                    selectedLat = null // Reseteamos selección para forzar nueva marca limpia en mapa
                    selectedLng = null
                    selectedAddressText = ""
                },
                uamLat = uamLat, uamLng = uamLng,
                selLat = selectedLat, selLng = selectedLng,
                onLocationSelected = { lat, lng ->
                    selectedLat = lat
                    selectedLng = lng
                    selectedAddressText = if (isGoingToUam) "Punto de salida seleccionado" else "Punto de destino seleccionado"
                },
                onContinue = { step = 2 }
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
                onBack = { step = 2 },
                onPublish = { /* TODO: Lógica API */ },
                from = fromAddressText.ifEmpty { "No seleccionada" },
                to = toAddressText.ifEmpty { "No seleccionada" },
                date = date,
                time = time,
                seats = seats,
                price = price,
                onPriceChange = { price = it }
=======
                from = if (isGoingToUam) (if (selectedAddressText.isEmpty()) "Ubicación en mapa" else selectedAddressText) else uamAddress,
                to = if (isGoingToUam) uamAddress else (if (selectedAddressText.isEmpty()) "Ubicación en mapa" else selectedAddressText),
                date = date, time = time, seats = seats, price = price,
                onPriceChange = { price = it },
                onBack = { step = 2 },
                onPublish = {
                    val origenNombre = if (isGoingToUam) selectedAddressText else uamAddress
                    val origenLat = if (isGoingToUam) selectedLat else uamLat
                    val origenLng = if (isGoingToUam) selectedLng else uamLng

                    val destinoNombre = if (isGoingToUam) uamAddress else selectedAddressText
                    val destinoLat = if (isGoingToUam) uamLat else selectedLat
                    val destinoLng = if (isGoingToUam) uamLng else selectedLng

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
                        conductorCif = userCif,
                        onExito = {
                            showSuccessDialog = true
                        },
                        onError = {
                            android.widget.Toast
                                .makeText(context, it, android.widget.Toast.LENGTH_LONG)
                                .show()
                        }
                    )
                }
>>>>>>> Stashed changes
            )
        }
    }
}

@Composable
fun Step1Route(
    fromLocation: LatLng?,
    fromAddressText: String,
    toLocation: LatLng?,
    toAddressText: String,
    onLocationsUpdated: (LatLng?, String, LatLng?, String) -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val defaultCenter = LatLng(12.1226, -86.2411) // Managua Central

    // Estado de cámara nativo de Google Maps Compose
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 13f)
    }

    // Reubicar y encuadrar cámara automáticamente cuando cambien los marcadores
    LaunchedEffect(fromLocation, toLocation) {
        if (fromLocation != null && toLocation != null) {
            val bounds = LatLngBounds.Builder()
                .include(fromLocation)
                .include(toLocation)
                .build()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(bounds.center, 12f)
        } else if (fromLocation != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(fromLocation, 15f)
        } else if (toLocation != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(toLocation, 15f)
        }
    }

    var targetFieldSelection by remember { mutableStateOf("FROM") }

    val startAutocomplete = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                val latLngValue = place.latLng
                if (latLngValue != null) {
                    val fullAddress = place.address ?: place.name ?: "Ubicación seleccionada"
                    if (targetFieldSelection == "FROM") {
                        onLocationsUpdated(latLngValue, fullAddress, toLocation, toAddressText)
                    } else {
                        onLocationsUpdated(fromLocation, fromAddressText, latLngValue, fullAddress)
                    }
                }
            }
        } else if (result.resultCode == AutocompleteActivityMode.OVERLAY.hashCode()) {
            Toast.makeText(context, "Error al recuperar la ubicación", Toast.LENGTH_SHORT).show()
        }
    }

<<<<<<< Updated upstream
    val launchGooglePlaces = { target: String ->
        targetFieldSelection = target
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountries(listOf("NI"))
            .build(context)
        startAutocomplete.launch(intent)
    }
=======
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
>>>>>>> Stashed changes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Configura la ruta de viaje",
            style = MaterialTheme.typography.titleSmall,
            color = UAMColor,
            fontWeight = FontWeight.SemiBold
        )

<<<<<<< Updated upstream
        OutlinedTextField(
            value = fromAddressText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Punto de Salida / Origen") },
            placeholder = { Text("Escribe el origen de tu viaje...") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF019AA8)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { launchGooglePlaces("FROM") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019AA8),
                unfocusedBorderColor = Color.Gray,
                disabledBorderColor = Color(0xFF019AA8),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White
            )
        )

        OutlinedTextField(
            value = toAddressText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Punto de Llegada / Destino") },
            placeholder = { Text("Escribe el destino de tu viaje...") },
            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFE91E63)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { launchGooglePlaces("TO") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE91E63),
                unfocusedBorderColor = Color.Gray,
                disabledBorderColor = Color(0xFFE91E63),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White
            )
        )

        // MAPA DINÁMICO DE GOOGLE MAPS COMPOSE NATIVO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.LightGray, shape = MaterialTheme.shapes.medium)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                fromLocation?.let { origen ->
                    Marker(
                        state = MarkerState(position = origen),
                        title = "Origen",
                        snippet = fromAddressText
                    )
                }

                toLocation?.let { destino ->
                    Marker(
                        state = MarkerState(position = destino),
                        title = "Destino",
                        snippet = toAddressText
                    )
                }

                if (fromLocation != null && toLocation != null) {
                    Polyline(
                        points = listOf(fromLocation, toLocation),
                        color = Color(0xFF019AA8),
                        width = 8f,
                        geodesic = true
                    )
=======
        // Contenedor visual del mapa encapsulado y delimitado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(16.dp)) // Hace que el mapa respete las esquinas redondeadas sin salirse
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

            // Cajas flotantes informativas dentro de los límites del mapa
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Caja indicadora de Origen
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
                                "Universidad Americana (UAM)"
                            },
                            fontSize = 11.sp,
                            color = if (isToUam && selLat == null) Color(0xFFD32F2F) else Color.DarkGray,
                            maxLines = 1
                        )
                    }
                }

                // Caja indicadora de Destino
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
                                "Universidad Americana (UAM)"
                            } else {
                                if (selLat != null) "Ubicación marcada" else "Toca el mapa..."
                            },
                            fontSize = 11.sp,
                            color = if (!isToUam && selLat == null) Color(0xFFD32F2F) else Color.DarkGray,
                            maxLines = 1
                        )
                    }
>>>>>>> Stashed changes
                }
            }
        }

<<<<<<< Updated upstream
        val amboPuntosListos = fromLocation != null && toLocation != null

        Text(
            text = if (!amboPuntosListos) "⚠️ Ingresa Origen y Destino arriba para trazar la ruta." else "✅ ¡Ruta calculada con éxito!",
            fontSize = 13.sp,
            color = if (!amboPuntosListos) Color.Red else Color(0xFF019AA8),
            fontWeight = if (amboPuntosListos) FontWeight.Medium else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Degradado2, shape = MaterialTheme.shapes.extraLarge)
        ) {
            Button(
                onClick = onContinue,
                enabled = amboPuntosListos,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Text("Continuar →", color = Color.White, fontSize = 16.sp)
            }
=======
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
>>>>>>> Stashed changes
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
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Horario", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = date, onValueChange = onDateChange, label = { Text("Fecha") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF019AA8), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF019AA8)) }
        )
        OutlinedTextField(
            value = time, onValueChange = onTimeChange, label = { Text("Hora de salida") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF019AA8), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF019AA8)) }
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
            Box(modifier = Modifier.weight(1f).background(Degradado2, shape = MaterialTheme.shapes.extraLarge)) {
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) { Text("Continuar →", color = Color.White) }
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
            Box(modifier = Modifier.weight(1f).background(Degradado2, shape = MaterialTheme.shapes.extraLarge)) {
                Button(onClick = onPublish, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) { Text("Publicar viaje", color = Color.White) }
            }
        }
    }
<<<<<<< Updated upstream
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, modifier = Modifier.weight(0.3f))
        Text(text = value, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal, color = if (isHighlight) Color(0xFF019AA8) else UAMColor, modifier = Modifier.weight(0.7f))
    }
=======
>>>>>>> Stashed changes
}