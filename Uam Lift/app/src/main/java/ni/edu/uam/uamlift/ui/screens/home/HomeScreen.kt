package ni.edu.uam.uamlift.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.uamlift.ui.components.RideCard
import ni.edu.uam.uamlift.ui.theme.Degradado2
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor
import ni.edu.uam.uamlift.viewmodel.UsuarioViewModel
import ni.edu.uam.uamlift.viewmodel.ViajeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viajeViewModel: ViajeViewModel = viewModel(),
    usuarioViewModel: UsuarioViewModel
) {
    val backgroundColor = Gray

    // Observadores del estado del Backend de Spring Boot
    val viajesDisponibles by viajeViewModel.viajes.collectAsState()
    val cargando by viajeViewModel.isLoading.collectAsState()

    // ✂️ SE ELIMINÓ: var selectedViaje ya no es necesario aquí

    Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    // Cabeza de página
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(text = "UAM ", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                Text(text = "LIFT", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF019AA8))
                            }
                            Text(text = "Movilidad colaborativa", color = Color.Gray, fontSize = 18.sp)
                        }
                    }

                    // Saludo al estudiante con fondo Verde Azulado y caja flotante
                    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                        Box(modifier = Modifier.fillMaxWidth().height(255.dp).background(UAMColor))

                        Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                                Text(text = "¡Hola, ${usuarioViewModel.usuario.nombre}! 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(7.dp))
                                Text(text = "Encuentra o comparte un viaje hoy", fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Caja blanca flotante de búsqueda
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                            ) {
                                Column(modifier = Modifier.padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    OutlinedTextField(
                                        value = "",
                                        onValueChange = {},
                                        placeholder = { Text("¿Desde dónde sales?") },
                                        leadingIcon = { Icon(Icons.Default.Search, null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        contentPadding = PaddingValues(vertical = 6.dp),
                                        onClick = { /* abrir búsqueda */ },
                                        modifier = Modifier.fillMaxWidth().height(54.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(brush = Degradado2, shape = RoundedCornerShape(16.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                                Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = "Buscar viajes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Encabezado "Viajes disponibles"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Viajes disponibles", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        TextButton(onClick = { /* Navegar a ver todos */ }, contentPadding = PaddingValues(0.dp)) {
                            Text(text = "Ver todos", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF019AA8))
                        }
                    }
                }
            }

            // LISTADO DINÁMICO
            if (cargando) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UAMColor)
                    }
                }
            } else {
                items(viajesDisponibles) { viaje ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        // El callback ejecuta directamente la acción de reserva delegada del mapa
                        RideCard(
                            viaje = viaje,
                            onConfirmarClick = { idViaje ->
                                viajeViewModel.unirseAlViaje(
                                    viajeId = idViaje,
                                    usuarioCif = usuarioViewModel.usuario.cif ?: ""// Cif dinámico configurado
                                )
                            }
                        )
                    }
                }
            }
        }

    }
}