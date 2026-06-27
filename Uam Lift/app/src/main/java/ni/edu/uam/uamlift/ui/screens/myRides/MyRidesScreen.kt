package ni.edu.uam.uamlift.ui.screens.myRides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.viewmodels.UbicacionViewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.ui.components.RideCard
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor

@Composable
fun MyRidesScreen(
    viajeViewModel: ViajeViewModel,
    usuarioViewModel: UsuarioViewModel,
    ubicacionViewModel: UbicacionViewModel = viewModel()
) {
    val viajes by viajeViewModel.viajes.collectAsState()
    val cargando by viajeViewModel.isLoading.collectAsState()
    val userCif = usuarioViewModel.usuario.cif ?: ""
    val userId = usuarioViewModel.usuario.id ?: 0L

    // Cargar viajes al entrar para asegurar que la lista esté actualizada
    LaunchedEffect(userId) {
        viajeViewModel.cargarViajesDesdeBackend(userId)
    }

    val misViajes = remember(viajes) {
        viajes.filter { it.conductor?.cif == userCif }
    }

    // Lógica para enviar ubicación si el conductor tiene un viaje en curso
    val viajeActivo = misViajes.find { it.estadoViaje == EstadoViaje.EN_CURSO }
    
    LaunchedEffect(viajeActivo) {
        if (viajeActivo != null) {
            ubicacionViewModel.conectar(viajeActivo.id!!)
            while (true) {
                // Simulación de coordenadas (UAM)
                val latSimulada = 12.1276
                val lngSimulada = -86.2713
                ubicacionViewModel.enviar(viajeActivo.id!!, latSimulada, lngSimulada)
                delay(5000)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Gray)) {
        Surface(color = Color.White, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Mis Viajes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = UAMColor
                )
                Text(
                    "Gestiona tus rutas publicadas",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        if (cargando && misViajes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UAMColor)
            }
        } else if (misViajes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Aún no has creado ningún viaje", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                items(misViajes) { viaje ->
                    RideCard(
                        viaje = viaje,
                        usuarioIdActual = userId,
                        esConductor = true,
                        onIniciarViaje = { id ->
                            viajeViewModel.iniciarViaje(
                                viajeId = id,
                                conductorId = userId,
                                onExito = {
                                    ubicacionViewModel.conectar(id)
                                }
                            )
                        },
                        onFinalizarViaje = { id ->
                            viajeViewModel.finalizarViaje(id, userId)
                        },
                        onCancelarViaje = { id ->
                            viajeViewModel.cancelarViaje(id, userId)
                        },
                        onVerPasajeros = { id ->
                            viajeViewModel.obtenerPasajeros(id)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
