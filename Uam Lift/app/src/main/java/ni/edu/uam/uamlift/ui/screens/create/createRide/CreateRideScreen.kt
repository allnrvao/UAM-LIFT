package ni.edu.uam.uamlift.ui.screens.create.createRide

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

@Composable
fun CreateRideScreen(modifier: Modifier = Modifier) {
    // Optimización: mutableIntStateOf para evitar autoboxing
    var step by remember { mutableIntStateOf(1) }

    var fromLocation by remember { mutableStateOf<LatLng?>(null) }
    var fromAddressText by remember { mutableStateOf("") }

    var toLocation by remember { mutableStateOf<LatLng?>(null) }
    var toAddressText by remember { mutableStateOf("") }

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
                onBack = { step = 2 },
                onPublish = { /* TODO: Lógica API */ },
                from = fromAddressText.ifEmpty { "No seleccionada" },
                to = toAddressText.ifEmpty { "No seleccionada" },
                date = date,
                time = time,
                seats = seats,
                price = price,
                onPriceChange = { price = it }
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

    val launchGooglePlaces = { target: String ->
        targetFieldSelection = target
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountries(listOf("NI"))
            .build(context)
        startAutocomplete.launch(intent)
    }

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
                }
            }
        }

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
}