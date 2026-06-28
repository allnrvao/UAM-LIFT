package ni.edu.uam.uamlift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import ni.edu.uam.uamlift.data.models.Usuario
import ni.edu.uam.uamlift.ui.theme.UAMColor

@Composable
fun PassengersDialog(
    conductor: Usuario?,
    pasajeros: List<Usuario>,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Participantes",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = UAMColor
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "CONDUCTOR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (conductor != null) {
                            PassengerItem(conductor, esConductor = true)
                        } else {
                            Text("Información no disponible", fontSize = 14.sp, color = Color.LightGray)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Text(
                            text = "PASAJEROS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (pasajeros.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay pasajeros aún", color = Color.LightGray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(pasajeros) { pasajero ->
                            PassengerItem(pasajero, esConductor = false)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UAMColor)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PassengerItem(usuario: Usuario, esConductor: Boolean) {
    val initials = (usuario.nombre?.take(1) ?: "") + (usuario.apellido?.take(1) ?: "")
    val foto = usuario.imagenUrl

    val displayName = when {
        !usuario.nombreUsuario.isNullOrEmpty() -> usuario.nombreUsuario!!
        "${usuario.nombre ?: ""} ${usuario.apellido ?: ""}".trim().isNotEmpty() ->
            "${usuario.nombre ?: ""} ${usuario.apellido ?: ""}".trim()
        else -> "Usuario #${usuario.id}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (esConductor) UAMColor.copy(alpha = 0.05f) else Color(0xFFF8F9FA),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── AVATAR CON FOTO DE PERFIL ──
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (esConductor) UAMColor else Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            if (!foto.isNullOrEmpty()) {
                AsyncImage(
                    model = foto,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (initials.isNotEmpty()) {
                Text(
                    text = initials.uppercase(),
                    color = if (esConductor) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = if (esConductor) Color.White else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )
            Text(
                text = "CIF: ${usuario.cif ?: "N/A"}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        if (esConductor) {
            Icon(
                Icons.Default.Stars,
                contentDescription = "Conductor",
                tint = UAMColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}