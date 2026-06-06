package ni.edu.uam.uamlift.ui.screens.create.createRide

import androidx.compose.foundation.background
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
import ni.edu.uam.uamlift.ui.theme.Degradado1
import ni.edu.uam.uamlift.ui.theme.Degradado2
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor

@Composable
fun CreateRideScreen(modifier: Modifier = Modifier) {
    var step by remember { mutableStateOf(1) }
    var from by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf(3) }
    var price by remember { mutableStateOf("") }

    // Aplicamos el fondo Gris (0xFFF3F5F7) a toda la pantalla
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray)
    ) {
        // Header con el Degradado1 (Turquesa a azul claro)
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

                // Indicador de Pasos sobre el fondo degradado
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

        // Control de navegación de pantallas secundarias
        when (step) {
            1 -> Step1Route(
                onContinue = { step = 2 },
                from = from,
                onFromChange = { from = it }
            )
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
                onPublish = { /* TODO: Integrar lógica de persistencia/API */ },
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
        Text(text = "Punto de salida", style = MaterialTheme.typography.titleSmall, color = UAMColor, fontWeight = FontWeight.SemiBold)

        OutlinedTextField(
            value = from,
            onValueChange = onFromChange,
            placeholder = { Text("¿Desde dónde sales?") },
            modifier = Modifier.fillMaxWidth(),
            // CORREGIDO: Sintaxis compatible con nuevas versiones de Material 3
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019AA8),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF019AA8)) }
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        Text(text = "Destino", style = MaterialTheme.typography.titleSmall, color = UAMColor, fontWeight = FontWeight.SemiBold)

        OutlinedTextField(
            value = "UAM - Campus Central",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            // CORREGIDO: Sintaxis compatible con nuevas versiones de Material 3 para solo lectura
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.6f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                disabledContainerColor = Color.White.copy(alpha = 0.6f)
            ),
            leadingIcon = { Icon(Icons.Default.Place, null, tint = Color(0xFF06585E)) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Degradado2, shape = MaterialTheme.shapes.extraLarge)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Continuar →", color = Color.White, fontSize = 16.sp)
            }
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
        Text("Horario", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = date,
            onValueChange = onDateChange,
            label = { Text("Fecha") },
            placeholder = { Text("Seleccionar fecha") },
            modifier = Modifier.fillMaxWidth(),
            // CORREGIDO: Sintaxis compatible con nuevas versiones de Material 3
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019AA8),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF019AA8)) }
        )

        OutlinedTextField(
            value = time,
            onValueChange = onTimeChange,
            label = { Text("Hora de salida") },
            placeholder = { Text("Ej: 07:30") },
            modifier = Modifier.fillMaxWidth(),
            // CORREGIDO: Sintaxis compatible con nuevas versiones de Material 3
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019AA8),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF019AA8)) }
        )

        Text("Asientos disponibles", style = MaterialTheme.typography.titleSmall, color = UAMColor)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3, 4).forEach { num ->
                val isSelected = seats == num
                FilterChip(
                    selected = isSelected,
                    onClick = { onSeatsChange(num) },
                    label = { Text(num.toString(), color = if (isSelected) Color.White else Color.DarkGray) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF019AA8),
                        containerColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF06585E))
            ) {
                Text("Atrás")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Degradado2, shape = MaterialTheme.shapes.extraLarge)
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Continuar →", color = Color.White)
                }
            }
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
        Text("Aporte por persona", style = MaterialTheme.typography.titleLarge, color = UAMColor, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            label = { Text("Monto en Quetzales (Q)") },
            placeholder = { Text("Ej: 25.00") },
            prefix = { Text("Q ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            // CORREGIDO: Sintaxis compatible con nuevas versiones de Material 3
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF019AA8),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Text("Solo gastos de combustible — sin lucro.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen del viaje", style = MaterialTheme.typography.titleMedium, color = UAMColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                SummaryRow("Ruta", "$from → UAM Central")
                SummaryRow("Hora", time.ifEmpty { "—" })
                SummaryRow("Asientos", seats.toString())
                SummaryRow("Aporte", if (price.isEmpty()) "Q 0" else "Q $price", isHighlight = true)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF06585E))
            ) {
                Text("Atrás")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Degradado2, shape = MaterialTheme.shapes.extraLarge)
            ) {
                Button(
                    onClick = onPublish,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Publicar viaje", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(
            text = value,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) Color(0xFF019AA8) else UAMColor
        )
    }
}