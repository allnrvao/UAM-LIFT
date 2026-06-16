package ni.edu.uam.uamlift.ui.screens.debug

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.RetrofitClient

@Composable
fun DebugViajesScreen() {
    var resultado by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🔍 DEBUG - Prueba de Viajes",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            cargando = true
            error = null
            resultado = null

            scope.launch {
                try {
                    Log.d("DEBUG", "Iniciando prueba de API...")
                    val viajes = RetrofitClient.viajeApi.obtenerTodosLosViajes()

                    Log.d("DEBUG", "✅ Viajes recibidos: ${viajes.size}")

                    resultado = buildString {
                        appendLine("✅ ÉXITO: Se obtuvieron ${viajes.size} viajes")
                        appendLine("\n--- DETALLES ---")
                        viajes.forEachIndexed { index, viaje ->
                            appendLine("\nViaje ${index + 1}:")
                            appendLine("  ID: ${viaje.id}")
                            appendLine("  Origen: ${viaje.origen?.nombre}")
                            appendLine("  Destino: ${viaje.destino?.nombre}")
                            appendLine("  Conductor: ${viaje.conductor?.nombre}")
                            appendLine("  Precio: ${viaje.precioPorPersona}")
                            appendLine("  Asientos: ${viaje.numeroAsientosDisponibles}")
                            appendLine("  Salida: ${viaje.fechaHoraSalida}")
                            appendLine("  Estado: ${viaje.estadoViaje}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DEBUG", "❌ Error: ${e.message}", e)
                    error = "❌ ERROR:\n${e.javaClass.simpleName}\n${e.message}\n\nStackTrace:\n${e.stackTraceToString()}"
                } finally {
                    cargando = false
                }
            }
        }) {
            Text("Probar obtenerTodosLosViajes()")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RESULTADO
        if (cargando) {
            CircularProgressIndicator(color = Color.White)
            Text("Cargando...", color = Color.White)
        }

        error?.let {
            Text(
                it,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF7F2720))
                    .padding(12.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        resultado?.let {
            Text(
                it,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B5E20))
                    .padding(12.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

