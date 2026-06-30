package ni.edu.uam.uamlift.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ni.edu.uam.uamlift.ui.theme.UAMColor

/**
 * Diálogo de dos pasos que se muestra cuando el conductor quiere cancelar
 * un viaje:
 *  1. Confirmación: "¿Estás seguro de que quieres cancelar el viaje?"
 *  2. Motivo: el conductor escribe la razón, la cual se enviará como
 *     notificación a todos los pasajeros del viaje.
 */
@Composable
fun CancelRideDialog(
    onDismissRequest: () -> Unit,
    onConfirmarCancelacion: (motivo: String) -> Unit
) {
    // paso = 1 → confirmación, paso = 2 → pedir motivo
    var paso by rememberSaveable { mutableStateOf(1) }
    var motivo by rememberSaveable { mutableStateOf("") }

    if (paso == 1) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("¿Cancelar viaje?", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Estás seguro de que quieres cancelar este viaje? Esta acción no se puede deshacer y se notificará a todos los pasajeros.")
            },
            confirmButton = {
                Button(
                    onClick = { paso = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Sí, cancelar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text("No") }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("Motivo de la cancelación", fontWeight = FontWeight.Bold, color = UAMColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Cuéntanos por qué cancelas este viaje. Este mensaje se enviará a los pasajeros para que sepan el motivo.",
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { motivo = it },
                        placeholder = { Text("Ej. Imprevisto personal, problema con el vehículo...") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val motivoFinal = motivo.trim().ifEmpty { "No se especificó un motivo." }
                        onConfirmarCancelacion(motivoFinal)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirmar cancelación", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}
