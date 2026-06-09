package ni.edu.uam.uamlift.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.uamlift.ui.components.RideCard
import ni.edu.uam.uamlift.ui.theme.Degradado1
import ni.edu.uam.uamlift.ui.theme.Degradado2
import ni.edu.uam.uamlift.ui.theme.Gray
import ni.edu.uam.uamlift.ui.theme.UAMColor


@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val backgroundColor = Gray

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(backgroundColor)
    ) {
        // Cabeza de pagina
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "UAM ",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = "LIFT",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF019AA8)
                    )
                }
                Text(
                    text = "Movilidad colaborativa",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        }

        // Saludo al estudiante con fondo Verde Azulado y caja flotante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Bloque decorativo superior verde azulado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(255.dp)
                    .background(UAMColor)
            )

            // Contenido que se encima en la Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                // Textos de saludo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "¡Hola, Estudiante! 👋",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = "Encuentra o comparte un viaje hoy",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Caja blanca flotante de búsqueda
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    // DEFINIMOS LA ELEVACIÓN DE LA TARJETA
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 20.dp, // Sombra base idéntica a tu imagen de referencia
                        pressedElevation = 20.dp  // Al pulsar la tarjeta, se hunde ligeramente de forma realista
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("¿Desde dónde sales?") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Botón Buscar Viajes
                        Button(
                            //espacio entre el boton y outlinetexfield
                            contentPadding = PaddingValues(vertical = 6.dp),

                            onClick = { /* abrir búsqueda */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp), // Redondea la interacción del botón
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent // Fondo base invisible
                            )
                        ) {

                            // El Box va ADENTRO del botón para que el degradado respete el tamaño y la forma
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Degradado2,
                                        shape = RoundedCornerShape(16.dp) // Redondea el Degradado
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Buscar viajes",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // [Burbuja de información va aquí si decides agregarla]



        // 1. Encabezado con Título y Botón "Ver todos"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Viajes disponibles",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
            TextButton(
                onClick = { /* Navegar a ver todos */ },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Ver todos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF019AA8)
                )
            }
        }

        // 2. El listado de tarjetas sueltas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RideCard(
                initials = "MR",
                name = "María Rodríguez",
                rating = "4.9",
                trips = 23,
                from = "Plaza Inter, Managua",
                to = "UAM Campus Central",
                time = "Hoy, 7:30 AM",
                price = "C$50",
                seats = 3
            )

            RideCard(
                initials = "JL",
                name = "Juan López",
                rating = "5.0",
                trips = 45,
                from = "Entrada a Masaya",
                to = "UAM Campus Central",
                time = "Hoy, 8:00 AM",
                price = "C$70",
                seats = 2
            )

            RideCard(
                initials = "AC",
                name = "Ana Castillo",
                rating = "4.8",
                trips = 12,
                from = "Parque Central de Granada",
                to = "UAM Campus Central",
                time = "Hoy, 6:45 AM",
                price = "C$120",
                seats = 4
            )

            RideCard(
                initials = "GZ",
                name = "Gabriel Zelaya",
                rating = "4.9",
                trips = 31,
                from = "Carretera a Masaya Km 10",
                to = "UAM Campus Central",
                time = "Hoy, 8:15 AM",
                price = "C$40",
                seats = 2
            )
        }
    }
}