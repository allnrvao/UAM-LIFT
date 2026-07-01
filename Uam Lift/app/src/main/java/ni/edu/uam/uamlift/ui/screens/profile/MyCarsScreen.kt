package ni.edu.uam.uamlift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ni.edu.uam.uamlift.data.models.Carro
import ni.edu.uam.uamlift.data.models.Usuario
import ni.edu.uam.uamlift.data.viewmodels.CarroViewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCarsScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    carroViewModel: CarroViewModel = viewModel()
) {
    val usuario = usuarioViewModel.usuario

    var carroAEditarId by rememberSaveable { mutableStateOf<Long?>(null) }
    var mostrarConfirmarEliminarId by rememberSaveable { mutableStateOf<Long?>(null) }

    val carroAEditar = remember(carroAEditarId, carroViewModel.listaCarros) {
        carroViewModel.listaCarros.find { it.id == carroAEditarId }
    }
    val mostrarConfirmarEliminar = remember(mostrarConfirmarEliminarId, carroViewModel.listaCarros) {
        carroViewModel.listaCarros.find { it.id == mostrarConfirmarEliminarId }
    }

    LaunchedEffect(usuario.id) {
        usuario.id?.let { carroViewModel.obtenerCarrosPorUsuario(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Vehículos", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UAMColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_car") },
                containerColor = UAMColor,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Vehículo", modifier = Modifier.size(30.dp))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Gray)) {
            if (carroViewModel.cargando && carroViewModel.listaCarros.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UAMColor)
                }
            } else if (carroViewModel.listaCarros.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = UAMColor.copy(alpha = 0.05f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                null,
                                modifier = Modifier.size(60.dp),
                                tint = UAMColor.copy(alpha = 0.3f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Sin vehículos registrados",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        "Para compartir tu ruta y ser conductor, necesitas registrar tu vehículo primero.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp),
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "Tus carros registrados",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(carroViewModel.listaCarros) { carro ->
                        CarItem(
                            carro = carro,
                            onEdit = { carroAEditarId = carro.id },
                            onDelete = { mostrarConfirmarEliminarId = carro.id }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    carroAEditar?.let { carroSeguro ->
        EditCarDialog(
            carro = carroSeguro,
            usuarioPropietario = usuario,
            isLoading = carroViewModel.cargando,
            onDismiss = { carroAEditarId = null },
            onConfirm = { carroActualizado ->
                carroViewModel.actualizarCarro(carroActualizado) { exito ->
                    if (exito) carroAEditarId = null
                }
            }
        )
    }

    // CORRECCIÓN/MEJORA: Diálogo de eliminación adaptado al estilo institucional solicitado
    mostrarConfirmarEliminar?.let { carroAEliminar ->
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminarId = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "¿Eliminar vehículo?",
                    color = UAMColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = { Text("¿Estás seguro de que deseas eliminar permanentemente el vehículo ${carroAEliminar.marca} con placa ${carroAEliminar.placa}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val cid = carroAEliminar.id
                        val uid = usuario.id
                        if (cid != null && uid != null) {
                            carroViewModel.eliminarCarro(cid, uid)
                        }
                        mostrarConfirmarEliminarId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)) // Botón rojo para acciones destructivas
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmarEliminarId = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CarItem(carro: Carro, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = UAMColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DirectionsCar, null, tint = UAMColor, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${carro.marca} ${carro.modelo}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Placa: ${carro.placa} • ${carro.color}",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = UAMColor, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFFDA4AF), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun EditCarDialog(
    carro: Carro,
    usuarioPropietario: Usuario,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Carro) -> Unit
) {
    var placa by rememberSaveable { mutableStateOf(carro.placa) }
    var colorSeleccionado by rememberSaveable { mutableStateOf(carro.color) }

    var mostrarConfirmacionGuardar by remember { mutableStateOf(false) }

    val coloresMap = mapOf(
        "Blanco" to Color.White,
        "Negro" to Color.Black,
        "Gris" to Color.Gray,
        "Plateado" to Color(0xFFC0C0C0),
        "Rojo" to Color.Red,
        "Azul" to Color.Blue,
        "Vino" to Color(0xFF800000)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Vehículo", fontWeight = FontWeight.Black, color = UAMColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                val textFieldColors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                val disabledFieldColors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Gray,
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.Gray
                )
                OutlinedTextField(
                    value = placa,
                    onValueChange = { if (it.matches(Regex("^[a-zA-Z0-9-]*\$"))) placa = it.uppercase().take(8) },
                    label = { Text("Placa") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                OutlinedTextField(value = carro.marca, onValueChange = {}, enabled = false, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = disabledFieldColors)
                OutlinedTextField(value = carro.modelo, onValueChange = {}, enabled = false, label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = disabledFieldColors)

                Text("Color del vehículo", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(coloresMap.toList()) { (nombre, colorOp) ->
                        CarColorItem(
                            color = colorOp,
                            nombre = nombre,
                            seleccionado = colorSeleccionado == nombre,
                            onClick = { colorSeleccionado = nombre }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { mostrarConfirmacionGuardar = true },
                enabled = !isLoading && placa.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar Cambios", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )

    if (mostrarConfirmacionGuardar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionGuardar = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Guardar Cambios",
                    color = UAMColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = { Text("¿Estás seguro de que deseas guardar los cambios realizados en este vehículo?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionGuardar = false
                        val carroActualizado = Carro(
                            id = carro.id,
                            placa = placa.uppercase().trim(),
                            marca = carro.marca,
                            modelo = carro.modelo,
                            color = colorSeleccionado,
                            propietario = usuarioPropietario
                        )
                        onConfirm(carroActualizado)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacionGuardar = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CarColorItem(color: Color, nombre: String, seleccionado: Boolean, onClick: () -> Unit) {
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