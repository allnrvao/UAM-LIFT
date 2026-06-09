package ni.edu.uam.uamlift

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ni.edu.uam.UAM_LIFT.screens.search.SearchScreen
import ni.edu.uam.uamlift.screens.messages.MessagesScreen
import ni.edu.uam.uamlift.ui.navegation.BottomNavigationBar
import ni.edu.uam.uamlift.ui.screens.create.createRide.CreateRideScreen
import ni.edu.uam.uamlift.ui.screens.home.HomeScreen
import ni.edu.uam.uamlift.ui.screens.profile.EditProfileScreen
import ni.edu.uam.uamlift.ui.screens.profile.ProfileScreen
// Asegúrate de importar tu pantalla de edición si ya existe:
// import ni.edu.uam.uamlift.ui.screens.profile.EditProfileScreen

@Composable
fun UamLiftApp() {
    var currentTab by remember { mutableStateOf("home") }
    // 1. Creamos el navController central de la aplicación
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(currentTab) { newTab ->
                currentTab = newTab
                // Al cambiar de pestaña abajo, también obligamos al navController a moverse
                navController.navigate(newTab) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { paddingValues ->
        // 2. Reemplazamos el 'when' por un NavHost oficial
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen() }
            composable("search") { SearchScreen() }
            composable("create") { CreateRideScreen() }
            composable("messages") { MessagesScreen() }
            composable("profile") { ProfileScreen(navController = navController) }
            composable("edit_profile") { EditProfileScreen() }
        }
    }
}