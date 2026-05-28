package ni.edu.uam.uamlift.navegation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MailOutline // Ideal para Mensajes si no compila el de enviar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.uamlift.ui.theme.Degradado1
import ni.edu.uam.uamlift.ui.theme.Degradado2

@Composable
fun BottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    val brandColor = Color(0xFF019AA8)
    val selectedBgColor = Color(0xFFE0F4F5) // Burbuja celeste de "Inicio"
    val unselectedColor = Color(0xFF757575)

    NavigationBar(
        modifier = Modifier.height(95.dp), // Aumentado a 95.dp para que NUNCA se corte el texto "Crear"
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.Create,
            BottomNavItem.Messages,
            BottomNavItem.Profile
        )

        items.forEach { item ->
            val selected = currentTab == item.route

            if (item.route == "create") {
                //Botón "Crear"
                Box(
                    modifier = Modifier
                        .weight(1.5f) // Le damos un poquito más de espacio a los lados para que respire
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(item.route) },
                    contentAlignment = Alignment.TopCenter // Alineamos arriba para controlar el espaciado descendente
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp) // Controla qué tan arriba empieza el botón
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(elevation = 6.dp, shape = RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(Degradado1),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp)) // Espacio seguro entre el botón y el texto

                        Text(
                            text = item.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) brandColor else unselectedColor
                        )
                    }
                }
            } else {
                // Pestañas normales: Inicio, Buscar, Mensajes, Perfil
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(item.route) },
                    icon = {
                        // Selección de iconos basada exactamente en tu imagen
                        val iconVector = when (item.route) {
                            "home" -> Icons.Default.Home
                            "search" -> Icons.Default.Search
                            "messages" -> Icons.Default.Send // El icono de flecha/avión de papel que tienes
                            "profile" -> Icons.Default.Person // El icono de silueta humana
                            else -> item.icon
                        }

                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = brandColor,
                        selectedTextColor = brandColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = selectedBgColor
                    )
                )
            }
        }
    }
}