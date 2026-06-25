package ni.edu.uam.uamlift

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.screens.messages.MessagesScreen
import ni.edu.uam.uamlift.ui.navegation.BottomNavigationBar
import ni.edu.uam.uamlift.ui.screens.animation.SplashScreen
import ni.edu.uam.uamlift.ui.screens.LogIn.LogIn
import ni.edu.uam.uamlift.ui.screens.create.CreateAccountScreen
import ni.edu.uam.uamlift.ui.screens.create.createRide.CreateRideScreen
import ni.edu.uam.uamlift.ui.screens.home.HomeScreen
import ni.edu.uam.uamlift.ui.screens.myRides.MyRidesScreen
import ni.edu.uam.uamlift.ui.screens.profile.EditProfileScreen
import ni.edu.uam.uamlift.ui.screens.profile.MyCarsScreen
import ni.edu.uam.uamlift.ui.screens.profile.ProfileScreen


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun UamLiftApp() {

    var currentTab by remember { mutableStateOf("home") }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ViewModels compartidos para toda la App para asegurar la sincronización de datos
    val usuarioViewModel: UsuarioViewModel = viewModel()
    val viajeViewModel: ViajeViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        "home",
        "search",
        "create",
        "messages",
        "profile"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {

                BottomNavigationBar(currentTab) { newTab ->
                    navController.navigate(newTab) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {

            // Splash
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

            // Login
            composable("login") {

                LogIn(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel,
                    onLogin = {

                        navController.navigate("home") {

                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // Create Account
            composable("createAccount") {
                CreateAccountScreen(
                    usuarioViewModel = usuarioViewModel,
                    onAccountCreated = {
                        navController.navigate("home") {
                            popUpTo("createAccount") {
                                inclusive = true
                            }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }


            // Home
            composable("home") {
                HomeScreen(
                    usuarioViewModel = usuarioViewModel
                )
            }

            composable("create") {
                CreateRideScreen(
                    viajeViewModel = viajeViewModel,
                    onViajeCreado = {
                        // Al publicar con éxito, vamos a ver la lista de Mis Viajes
                        navController.navigate("my_rides") {
                            popUpTo("create") { inclusive = true }
                        }
                    }
                )
            }

            // Messages
            composable("messages") {
                MessagesScreen()
            }

            // Profile
            composable("profile") {

                ProfileScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel,
                    modifier = Modifier
                )
            }

            // Edit Profile
            composable("edit_profile") {

                EditProfileScreen(
                    usuarioViewModel = usuarioViewModel
                )
            }

            // Add Car
            composable("add_car") {
                AddCarScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel
                )
            }

            // My Cars
            composable("my_cars") {
                MyCarsScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel
                )
            }
        }
    }
}