package ni.edu.uam.uamlift

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ni.edu.uam.uamlift.ui.screens.animation.SplashScreen
import ni.edu.uam.UAM_LIFT.screens.search.SearchScreen
import ni.edu.uam.uamlift.screens.messages.MessagesScreen
import ni.edu.uam.uamlift.ui.navegation.BottomNavigationBar
import ni.edu.uam.uamlift.ui.screens.auth.LogIn
import ni.edu.uam.uamlift.ui.screens.create.createRide.CreateRideScreen
import ni.edu.uam.uamlift.ui.screens.home.HomeScreen
import ni.edu.uam.uamlift.ui.screens.profile.EditProfileScreen
import ni.edu.uam.uamlift.ui.screens.profile.ProfileScreen
import ni.edu.uam.uamlift.viewmodel.UsuarioViewModel

// Asegúrate de importar tu pantalla de edición si ya existe:
// import ni.edu.uam.uamlift.ui.screens.profile.EditProfileScreen

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun UamLiftApp() {
    var currentTab by remember { mutableStateOf("home") }
    // 1. Creamos el navController central de la aplicación
    val navController = rememberNavController()
    // Observamos la ruta actual para decidir si mostramos la BottomBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas en las que deseamos mostrar la barra inferior
    val bottomBarRoutes = setOf("home", "search", "create", "messages", "profile")

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
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
        }
    ) { paddingValues ->
        // 2. Reemplazamos el 'when' por un NavHost oficial
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Pantalla de inicio/animación
            composable("splash") {
                SplashScreen(
                    onDone = {
                        navController.navigate("login") {
                            popUpTo("splash") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // Pantalla de autenticación
            composable("login") {
                LogIn(
                    onLogin = {
                        navController.navigate("home") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("home") { HomeScreen() }
            composable("search") { SearchScreen() }
            composable("create") { CreateRideScreen() }
            composable("messages") { MessagesScreen() }
            composable("profile") { ProfileScreen(
                navController = navController,
                usuarioViewModel = UsuarioViewModel(),
                modifier = Modifier
            ) }
            composable("edit_profile") { EditProfileScreen( usuarioViewModel = UsuarioViewModel()) }
        }
    }
}