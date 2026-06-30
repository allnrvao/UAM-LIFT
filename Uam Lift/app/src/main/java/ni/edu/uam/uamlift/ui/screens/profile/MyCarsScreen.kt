package ni.edu.uam.uamlift.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ni.edu.uam.uamlift.data.models.Carro
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
    
    // Usamos IDs y rememberSaveable para persistir el estado de los diálogos
    var carroAEditarId by rememberSaveable { mutableStateOf<Long?>(null) }
    var mostrarConfirmarEliminarId by rememberSaveable { mutableStateOf<Long?>(null) }

    val carroAEditar = remember(carroAEditarId, carroViewModel.listaCarros) {
        carroViewModel.listaCarros.find { it.id == carroAEditarId }
    }
    val mostrarConfirmarEliminar = remember(mostrarConfirmarEliminarId, carroViewModel.listaCarros) {
        carroViewModel.listaCarros.find { it.id == mostrarConfirmarEliminarId }
    }

    // Cargar los carros al entrar usando el ID del usuario actual
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
                // Estado vacío estético
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
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { navController.navigate("add_car") },
                        colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(50.dp).fillMaxWidth(0.7f)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Registrar Vehículo", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Lista de vehículos
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

    // Diálogo de Edición
    if (carroAEditar != null) {
        EditCarDialog(
            carro = carroAEditar!!,
            onDismiss = { carroAEditarId = null },
            onConfirm = { carroActualizado ->
                carroViewModel.actualizarCarro(carroActualizado) {
                    carroAEditarId = null
                }
            }
        )
    }

    // Confirmación de Eliminación
    if (mostrarConfirmarEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminarId = null },
            title = { Text("¿Eliminar vehículo?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de eliminar el vehículo ${mostrarConfirmarEliminar!!.marca} con placa ${mostrarConfirmarEliminar!!.placa}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val cid = mostrarConfirmarEliminar!!.id
                        val uid = usuario.id
                        if (cid != null && uid != null) {
                            carroViewModel.eliminarCarro(cid, uid)
                        }
                        mostrarConfirmarEliminarId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminarId = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
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
fun EditCarDialog(carro: Carro, onDismiss: () -> Unit, onConfirm: (Carro) -> Unit) {
    // Usamos rememberSaveable para que los cambios en el diálogo no se pierdan al rotar
    var placa by rememberSaveable { mutableStateOf(carro.placa) }
    var marca by rememberSaveable { mutableStateOf(carro.marca) }
    var modelo by rememberSaveable { mutableStateOf(carro.modelo) }
    var color by rememberSaveable { mutableStateOf(carro.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Vehículo", fontWeight = FontWeight.Black, color = UAMColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                val textFieldColors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                OutlinedTextField(value = placa, onValueChange = { if (it.matches(Regex("^[a-zA-Z0-9-]*\$"))) placa = it.uppercase() }, label = { Text("Placa") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors)
                OutlinedTextField(value = marca, onValueChange = { if (it.matches(Regex("^[a-zA-Z0-9\\s\\u00C0-\\u017F]*\$"))) marca = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors)
                OutlinedTextField(value = modelo, onValueChange = { if (it.matches(Regex("^[a-zA-Z0-9\\s\\u00C0-\\u017F]*\$"))) modelo = it }, label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors)
                OutlinedTextField(value = color, onValueChange = { if (it.matches(Regex("^[a-zA-Z0-9\\s\\u00C0-\\u017F]*\$"))) color = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = textFieldColors)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(carro.copy(placa = placa, marca = marca, modelo = modelo, color = color)) },
                colors = ButtonDefaults.buttonColors(containerColor = UAMColor),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Guardar Cambios", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}
