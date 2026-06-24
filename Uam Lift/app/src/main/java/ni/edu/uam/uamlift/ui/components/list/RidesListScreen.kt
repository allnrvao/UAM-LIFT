package ni.edu.uam.uamlift.ui.components.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import ni.edu.uam.uamlift.data.models.Viaje
import ni.edu.uam.uamlift.ui.components.RideCard

@Composable
fun RidesListScreen(
    viajesList: List<Viaje>,
    usuarioIdActual: Long,
    onReservarClick: (Long) -> Unit
) {
    var selectedViaje by remember { mutableStateOf<Viaje?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(viajesList) { viaje ->
                RideCard(
                    viaje = viaje,
                    usuarioIdActual = usuarioIdActual,
                    esConductor = viaje.conductor?.id == usuarioIdActual,
                    onConfirmarClick = { idDelViaje ->
                        onReservarClick(idDelViaje)
                    }
                )
            }
        }

        selectedViaje?.let { viaje ->
            val nombreConductor = viaje.conductor?.nombre ?: "Conductor UAM"
            val origen = viaje.origen?.nombre ?: "Origen"
            val destino = viaje.destino?.nombre ?: "Destino"

            AlertDialog(
                onDismissRequest = { selectedViaje = null },
                confirmButton = {
                    TextButton(onClick = {
                        viaje.id?.let { onReservarClick(it) }
                        selectedViaje = null
                    }) {
                        Text("Reservar Asiento")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedViaje = null }) {
                        Text("Cancelar")
                    }
                },
                title = { Text(text = "Detalles del viaje de $nombreConductor") },
                text = {
                    Text(text = "Ruta: $origen -> $destino\n" +
                            "Precio: C$ ${viaje.precioPorPersona.toInt()}\n" +
                            "Asientos disponibles: ${viaje.numeroAsientosDisponibles}")
                }
            )
        }
    }
}
