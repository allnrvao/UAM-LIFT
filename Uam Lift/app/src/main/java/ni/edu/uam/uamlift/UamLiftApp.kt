package ni.edu.uam.uamlift

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.ui.navegation.BottomNavigationBar
import ni.edu.uam.uamlift.ui.screens.LogIn.LogIn
import ni.edu.uam.uamlift.ui.screens.animation.SplashScreen
import ni.edu.uam.uamlift.ui.screens.create.CreateAccountScreen
import ni.edu.uam.uamlift.ui.screens.create.createRide.CreateRideScreen
import ni.edu.uam.uamlift.ui.screens.home.HomeScreen
import ni.edu.uam.uamlift.ui.screens.messages.MessagesScreen
import ni.edu.uam.uamlift.ui.screens.myRides.MyRidesScreen
import ni.edu.uam.uamlift.ui.screens.profile.AddCarScreen
import ni.edu.uam.uamlift.ui.screens.profile.EditProfileScreen
import ni.edu.uam.uamlift.ui.screens.profile.MyCarsScreen
import ni.edu.uam.uamlift.ui.screens.profile.ProfileScreen
import ni.edu.uam.uamlift.ui.screens.rideInfo.ActiveRideMapScreen
import ni.edu.uam.uamlift.ui.screens.search.SearchScreen

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun UamLiftApp() {

    var currentTab by remember { mutableStateOf("home") }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val appViewModelFactory = AppViewModelFactory()
    val usuarioViewModel: UsuarioViewModel = viewModel(factory = appViewModelFactory)
    val viajeViewModel: ViajeViewModel = viewModel(factory = appViewModelFactory)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas en las que se muestra la barra inferior
    val bottomBarRoutes = setOf(
        "home", "search", "my_rides", "create", "messages", "profile"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavigationBar(currentTab) { newTab ->
                    if (newTab == "create") {
                        val usuarioId = usuarioViewModel.usuario.id ?: 0L
                        if (usuarioId != 0L) {
                            viajeViewModel.validarNumViajes(usuarioId) { esValido ->
                                if (esValido) {
                                    currentTab = newTab
                                    navController.navigate(newTab) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Has alcanzado el límite de viajes permitidos."
                                        )
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Cargando datos de usuario... Inténtalo de nuevo."
                                )
                            }
                        }
                    } else {
                        currentTab = newTab
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
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {

            // ── SPLASH ────────────────────────────────────────────────────────
            composable("splash") {
                val context = LocalContext.current
                val estaLogeado = usuarioViewModel.estaLogeado
                val sesionVerificada = usuarioViewModel.sesionVerificada

                LaunchedEffect(Unit) {
                    usuarioViewModel.verificarSesion(context)
                }

                SplashScreen(onDone = { })

                LaunchedEffect(sesionVerificada) {
                    if (sesionVerificada) {
                        if (estaLogeado) {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                }
            }

            // ── LOGIN ─────────────────────────────────────────────────────────
            composable("login") {
                LogIn(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel,
                    onLogin = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // ── CREAR CUENTA ──────────────────────────────────────────────────
            composable("createAccount") {
                CreateAccountScreen(
                    usuarioViewModel = usuarioViewModel,
                    onAccountCreated = {
                        navController.navigate("home") {
                            popUpTo("createAccount") { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }

            // ── HOME ──────────────────────────────────────────────────────────
            composable("home") {
                HomeScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel,
                    viajeViewModel = viajeViewModel
                )
            }

            // ── SEARCH ────────────────────────────────────────────────────────
            composable("search") {
                SearchScreen(usuarioViewModel = usuarioViewModel)
            }

            // ── MY RIDES ─────────────────────────────────────────────────────
            composable("my_rides") {
                MyRidesScreen(
                    viajeViewModel = viajeViewModel,
                    usuarioViewModel = usuarioViewModel
                )
            }

            // ── CREATE RIDE ───────────────────────────────────────────────────
            composable("create") {
                CreateRideScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel
                )
            }

            // ── MESSAGES ──────────────────────────────────────────────────────
            composable("messages") {
                MessagesScreen()
            }

            // ── PROFILE ───────────────────────────────────────────────────────
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel,
                    modifier = Modifier
                )
            }

            // ── EDIT PROFILE (con onBack correcto → regresa a "profile") ──────
            composable("edit_profile") {
                EditProfileScreen(
                    usuarioViewModel = usuarioViewModel,
                    onBack = {
                        // Siempre regresa a la pantalla principal de perfil
                        navController.navigate("profile") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                )
            }

            // ── ADD CAR ───────────────────────────────────────────────────────
            composable("add_car") {
                AddCarScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel
                )
            }

            // ── MY CARS ───────────────────────────────────────────────────────
            composable("my_cars") {
                MyCarsScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel
                )
            }

            // ── MAPA DEL VIAJE ACTIVO ─────────────────────────────────────────
            composable("active_ride/{viajeId}") { backStackEntry ->
                val viajeId = backStackEntry.arguments?.getString("viajeId")?.toLongOrNull() ?: 0L
                ActiveRideMapScreen(
                    viajeId = viajeId,
                    viajeViewModel = viajeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
