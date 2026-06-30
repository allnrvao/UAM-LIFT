package ni.edu.uam.uamlift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ni.edu.uam.uamlift.data.viewmodels.CarroViewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    carroViewModel: CarroViewModel = viewModel()
) {
    // Usamos rememberSaveable para que los datos sobrevivan a la rotación de pantalla
    var placa by rememberSaveable { mutableStateOf("") }
    var marca by rememberSaveable { mutableStateOf("") }
    var modelo by rememberSaveable { mutableStateOf("") }
    var colorSeleccionado by rememberSaveable { mutableStateOf("Blanco") }

    val marcas = listOf("Toyota", "Nissan", "Hyundai", "Kia", "Honda", "Suzuki", "Mazda", "Chevrolet", "Ford", "Mitsubishi")
    val coloresMap = mapOf(
        "Blanco" to Color.White,
        "Negro" to Color.Black,
        "Gris" to Color.Gray,
        "Plateado" to Color(0xFFC0C0C0),
        "Rojo" to Color.Red,
        "Azul" to Color.Blue,
        "Vino" to Color(0xFF800000)
    )

    var expandidoMarca by rememberSaveable { mutableStateOf(false) }
    var mostrarConfirmacion by rememberSaveable { mutableStateOf(false) }
    var mostrarExito by rememberSaveable { mutableStateOf(false) }
    var errorPlaca by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registrar Vehículo", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = UAMColor
                )
            )
        },
        containerColor = Gray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Placa con validación
            OutlinedTextField(
                value = placa,
                onValueChange = { 
                    placa = it.uppercase().take(8)
                    errorPlaca = null
                },
                label = { Text("Número de Placa") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorPlaca != null,
                supportingText = { if (errorPlaca != null) Text(errorPlaca!!, color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Default.Pin, null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )

            // Selector de Marca (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expandidoMarca,
                onExpandedChange = { expandidoMarca = !expandidoMarca }
            ) {
                OutlinedTextField(
                    value = marca,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Marca") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoMarca) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
                ExposedDropdownMenu(
                    expanded = expandidoMarca,
                    onDismissRequest = { expandidoMarca = false }
                ) {
                    marcas.forEach { seleccion ->
                        DropdownMenuItem(
                            text = { Text(seleccion) },
                            onClick = {
                                marca = seleccion
                                expandidoMarca = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = modelo,
                onValueChange = { modelo = it },
                label = { Text("Modelo (Ej: Corolla, Yaris)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )

            // Paleta de Colores
            Text("Color del vehículo", fontWeight = FontWeight.Bold, color = Color.DarkGray)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(coloresMap.toList()) { (nombre, color) ->
                    ColorOption(
                        color = color,
                        nombre = nombre,
                        seleccionado = colorSeleccionado == nombre,
                        onClick = { colorSeleccionado = nombre }
                    )
                }
            }

            if (carroViewModel.mensajeError != null) {
                Text(
                    text = carroViewModel.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (carroViewModel.cargando) return@Button
                    carroViewModel.verificarPlacaUnica(placa) { unica ->
                        if (unica) {
                            mostrarConfirmacion = true
                        } else {
                            errorPlaca = "Esta placa ya está registrada"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !carroViewModel.cargando && placa.length >= 4 && marca.isNotEmpty() && modelo.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
            ) {
                if (carroViewModel.cargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Continuar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- DIÁLOGOS ---

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar Registro") },
            text = { Text("¿Deseas registrar este $marca $modelo con placa $placa?") },
            confirmButton = {
                Button(onClick = {
                    mostrarConfirmacion = false
                    carroViewModel.crearCarro(placa, marca, modelo, colorSeleccionado, usuarioViewModel.usuario) { exito ->
                        if (exito) mostrarExito = true
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarExito) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp)) },
            title = { Text("¡Vehículo Registrado!") },
            text = { Text("Tu carro ha sido añadido correctamente a tu perfil.") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarExito = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) { Text("Excelente") }
            }
        )
    }
}

@Composable
fun ColorOption(color: Color, nombre: String, seleccionado: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(color)
                .border(if (seleccionado) 3.dp else 1.dp, if (seleccionado) UAMColor else Color.LightGray, CircleShape)
                .clickable { onClick() }
        )
        Text(nombre, fontSize = 10.sp, color = if (seleccionado) UAMColor else Color.Gray)
    }
}
