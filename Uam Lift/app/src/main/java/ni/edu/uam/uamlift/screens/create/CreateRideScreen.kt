package ni.edu.uam.UAM_LIFT.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateRideScreen(modifier: Modifier = Modifier) {
    var step by remember { mutableStateOf(1) }
    var from by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf(3) }
    var price by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Surface(color = Color.White, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Publicar viaje", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Comparte tu ruta — solo gastos de combustible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Step Indicator
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    listOf("Ruta", "Horario", "Precio").forEachIndexed { index, label ->
                        val isDone = index + 1 < step
                        val isActive = index + 1 == step

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (isDone || isActive) MaterialTheme.colorScheme.primary else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isDone) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    } else {
                                        Text("${index + 1}", color = Color.White)
                                    }
                                }
                            }
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control de navegación de pantallas
        when (step) {
            1 -> Step1Route(onContinue = { step = 2 }, from = from, onFromChange = { from = it })
            2 -> Step2Schedule(
                onBack = { step = 1 },
                onContinue = { step = 3 },
                date = date,
                onDateChange = { date = it },
                time = time,
                onTimeChange = { time = it },
                seats = seats,
                onSeatsChange = { seats = it }
            )
            3 -> Step3Price(
                onBack = { step = 2 },
                onPublish = { /* Publicar */ },
                from = from,
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
    from: String,
    onFromChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Punto de salida", style = MaterialTheme.typography.titleSmall)

        OutlinedTextField(
            value = from,
            onValueChange = onFromChange,
            placeholder = { Text("¿Desde dónde sales?") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider() // Corrección para Material 3 (reemplaza a Divider)

        Text(text = "Destino", style = MaterialTheme.typography.titleSmall)

        OutlinedTextField(
            value = "UAM - Campus Central",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Face, null, tint = MaterialTheme.colorScheme.primary) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar →")
        }
    }
}

@Composable
fun Step2Schedule(
    date: String,
    onDateChange: (String) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    seats: Int,
    onSeatsChange: (Int) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Horario", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = date,
            onValueChange = onDateChange,
            label = { Text("Fecha") },
            placeholder = { Text("Seleccionar fecha") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.DateRange, null) }
        )

        OutlinedTextField(
            value = time,
            onValueChange = onTimeChange,
            label = { Text("Hora de salida") },
            placeholder = { Text("Ej: 07:30") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.CheckCircle, null) }
        )

        Text("Asientos disponibles", style = MaterialTheme.typography.titleSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 4).forEach { num ->
                FilterChip(
                    selected = seats == num,
                    onClick = { onSeatsChange(num) },
                    label = { Text(num.toString()) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Atrás") }
            Button(onClick = onContinue, modifier = Modifier.weight(1f)) { Text("Continuar →") }
        }
    }
}

@Composable
fun Step3Price(
    from: String,
    date: String,
    time: String,
    seats: Int,
    price: String,
    onPriceChange: (String) -> Unit,
    onBack: () -> Unit,
    onPublish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Aporte por persona", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            label = { Text("Monto en Quetzales (Q)") },
            placeholder = { Text("Ej: 25.00") },
            prefix = { Text("Q") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Solo gastos de combustible — sin lucro.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen del viaje", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                SummaryRow("Ruta", "$from → UAM - Campus Central")
                SummaryRow("Hora", time.ifEmpty { "—" })
                SummaryRow("Asientos", seats.toString())
                SummaryRow("Aporte", if (price.isEmpty()) "Q 0" else "Q $price", isHighlight = true)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Atrás") }
            Button(onClick = onPublish, modifier = Modifier.weight(1f)) { Text("Publicar viaje") }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(
            text = value,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}