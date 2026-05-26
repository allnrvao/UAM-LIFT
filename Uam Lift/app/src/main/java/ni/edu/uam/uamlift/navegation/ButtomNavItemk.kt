package ni.edu.uam.uamlift.navegation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    data object Search : BottomNavItem("search", Icons.Default.Search, "Buscar")
    data object Create : BottomNavItem("create", Icons.Default.Add, "Crear")
    data object Messages : BottomNavItem("messages", Icons.Default.Send, "Mensajes")
    data object Profile : BottomNavItem("profile", Icons.Default.Person, "Perfil")
}