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
import ni.edu.uam.uamlift.data.models.Viaje // Corregido: Importación de tu modelo real
import ni.edu.uam.uamlift.ui.components.RideCard

@Composable
fun RidesListScreen(
    viajesList: List<Viaje>, // Corregido: Usamos tu lista de la base de datos
    onReservarClick: (Long) -> Unit // Callback recomendado para avisar al ViewModel la reserva
) {
    // Estado dinámico que guarda el objeto Viaje completo seleccionado
    var selectedViaje by remember { mutableStateOf<Viaje?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn {
            // Corregido: Utilizamos 'items' mapeando tu modelo Viaje
            items(viajesList) { viaje ->
                RideCard(
                    viaje = viaje,
                    onConfirmarClick = { idDelViaje ->
                        // Cuando el usuario confirma dentro del diálogo nativo,
                        // este callback se dispara automáticamente hacia arriba.
                        onReservarClick(idDelViaje)
                    }
                )
            }
        }

        // Un solo diálogo reactivo para todos los elementos
        selectedViaje?.let { viaje ->
            val nombreConductor = viaje.conductor?.nombre ?: "Conductor UAM"
            val origen = viaje.origen?.nombre ?: "Origen"      // Ajusta '.nombre' según tu clase Destino
            val destino = viaje.destino?.nombre ?: "Destino"   // Ajusta '.nombre' según tu clase Destino

            AlertDialog(
                onDismissRequest = { selectedViaje = null },
                confirmButton = {
                    TextButton(onClick = {
                        // Si el id no es nulo, ejecutamos la acción hacia tu API
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