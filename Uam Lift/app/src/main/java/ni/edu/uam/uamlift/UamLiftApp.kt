package ni.edu.uam.uamlift

import android.annotation.SuppressLint
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ni.edu.uam.uamlift.data.enums.EstadoViaje
import ni.edu.uam.uamlift.data.enums.TipoNotificacion
import ni.edu.uam.uamlift.data.models.Notificacion
import ni.edu.uam.uamlift.data.viewmodels.AppViewModelFactory
import ni.edu.uam.uamlift.data.viewmodels.NotificacionViewModel
import ni.edu.uam.uamlift.data.viewmodels.UsuarioViewModel
import ni.edu.uam.uamlift.data.viewmodels.ViajeViewModel
import ni.edu.uam.uamlift.notifications.NotificationHelper
import ni.edu.uam.uamlift.ui.navegation.BottomNavigationBar
import ni.edu.uam.uamlift.ui.screens.LogIn.LogIn
import ni.edu.uam.uamlift.ui.screens.animation.SplashScreen
import ni.edu.uam.uamlift.ui.screens.create.CreateAccountScreen
import ni.edu.uam.uamlift.ui.screens.create.createRide.CreateRideScreen
import ni.edu.uam.uamlift.ui.screens.home.HomeScreen
import ni.edu.uam.uamlift.ui.screens.messages.MessagesScreen
import ni.edu.uam.uamlift.ui.screens.myRides.MyRidesScreen
import ni.edu.uam.uamlift.ui.screens.notifications.NotificationsScreen
import ni.edu.uam.uamlift.ui.screens.profile.AddCarScreen
import ni.edu.uam.uamlift.ui.screens.profile.EditProfileScreen
import ni.edu.uam.uamlift.ui.screens.profile.MyCarsScreen
import ni.edu.uam.uamlift.ui.screens.profile.ProfileScreen
import ni.edu.uam.uamlift.ui.screens.rideInfo.ActiveRideMapScreen
import ni.edu.uam.uamlift.ui.screens.search.SearchScreen

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun UamLiftApp(
    pendingNotificationIntent: Intent? = null,
    onPendingNotificationConsumed: () -> Unit = {}
) {

    // Usamos rememberSaveable para que la pestaña actual se mantenga al rotar el teléfono
    var currentTab by rememberSaveable { mutableStateOf("home") }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val appViewModelFactory = AppViewModelFactory()
    val usuarioViewModel: UsuarioViewModel = viewModel(factory = appViewModelFactory)
    val viajeViewModel: ViajeViewModel = viewModel(factory = appViewModelFactory)
    val notificacionViewModel: NotificacionViewModel = viewModel(factory = appViewModelFactory)

    val context = LocalContext.current

    // Pedimos el permiso de notificaciones (requerido desde Android 13) para poder
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* No requiere acción adicional: si se niega, simplemente no se muestran avisos del sistema */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!concedido) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Cuando llega una notificación nueva (inicio o cancelación de viaje), mostramos
    // el aviso del sistema en la bandeja del celular.
    DisposableEffect(notificacionViewModel) {
        notificacionViewModel.onNuevaNotificacion = { nueva ->
            NotificationHelper.mostrarNotificacion(context, nueva)
        }
        onDispose { notificacionViewModel.onNuevaNotificacion = null }
    }

    // Sondeamos periódicamente el backend para refrescar la lista de notificaciones
    // y el contador de no leídas (no hay infraestructura de push/WebSocket para esto).
    val usuarioId = usuarioViewModel.usuario.id
    LaunchedEffect(usuarioId) {
        if (usuarioId != null && usuarioId != 0L) {
            while (true) {
                notificacionViewModel.cargarNotificaciones(usuarioId)
                delay(15000)
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Cuando el usuario toca la notificación en el panel del sistema (con la app cerrada
    LaunchedEffect(pendingNotificationIntent, currentRoute) {
        val intent = pendingNotificationIntent
        if (intent != null && currentRoute != null && currentRoute != "splash") {
            val tipo = intent.getStringExtra(NotificationHelper.EXTRA_NOTIF_TIPO)
                ?.let { nombre -> runCatching { TipoNotificacion.valueOf(nombre) }.getOrNull() }
            val viajeId = intent.getLongExtra(NotificationHelper.EXTRA_NOTIF_VIAJE_ID, -1L)
                .takeIf { it != -1L }
            val notifId = intent.getLongExtra(NotificationHelper.EXTRA_NOTIF_ID, -1L)
                .takeIf { it != -1L }
            val titulo = intent.getStringExtra(NotificationHelper.EXTRA_NOTIF_TITULO) ?: ""
            val mensaje = intent.getStringExtra(NotificationHelper.EXTRA_NOTIF_MENSAJE) ?: ""

            val usuarioIdActual = usuarioViewModel.usuario.id
            if (usuarioIdActual != null && usuarioIdActual != 0L && notifId != null) {
                notificacionViewModel.marcarComoLeida(notifId, usuarioIdActual)
            }

            when (tipo) {
                TipoNotificacion.INICIO_VIAJE -> {
                    if (viajeId != null) {
                        navController.navigate("active_ride/$viajeId") {
                            launchSingleTop = true
                        }
                    }
                }
                TipoNotificacion.USUARIO_UNIDO -> {
                    navController.navigate("my_rides") {
                        launchSingleTop = true
                    }
                }
                TipoNotificacion.FINALIZACION_VIAJE,
                TipoNotificacion.CANCELACION_VIAJE,
                TipoNotificacion.USUARIO_ELIMINADO -> {
                    // Guardamos la notificación para que HomeScreen muestre el diálogo con la info una vez lleguemos ahí (igual que al tocarla dentro de la app).
                    notificacionViewModel.mostrarNotificacionPendiente(
                        Notificacion(
                            id = notifId,
                            viajeId = viajeId,
                            tipo = tipo,
                            titulo = titulo,
                            mensaje = mensaje
                        )
                    )
                    navController.navigate("home") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                else -> {
                    // GENERAL u otro caso sin pantalla específica: mostramos la pantalla de Notificaciones, igual que el ícono de campana.
                    navController.navigate("notifications") {
                        launchSingleTop = true
                    }
                }
            }

            onPendingNotificationConsumed()
        }
    }

    // Rutas en las que se muestra la barra inferior
    val bottomBarRoutes = setOf(
        "home", "search", "my_rides", "create", "messages?viajeId={viajeId}", "profile"
    )

    val viajeIdArgActual = navBackStackEntry?.arguments?.getString("viajeId")?.toLongOrNull()
    val enChatDeViajeActivo = currentRoute?.startsWith("messages") == true && viajeIdArgActual != null

    // La pestaña resaltada en la barra inferior se calcula siempre a partir de la ruta
    val currentTabForBar = when {
        currentRoute == "home" -> "home"
        currentRoute == "search" -> "search"
        currentRoute == "create" -> "create"
        currentRoute == "my_rides" -> "my_rides"
        currentRoute?.startsWith("messages") == true -> "messages"
        currentRoute == "profile" -> "profile"
        else -> currentTab
    }

    // ── Manejo global del botón/gesto "atrás" ───────────────────────────────
    val activity = LocalActivity.current
    var showExitDialog by remember { mutableStateOf(false) }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Cerrar aplicación") },
            text = { Text("¿Deseas cerrar la aplicación?") },
            confirmButton = {
                TextButton(onClick = { activity?.finish() }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("No") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute in bottomBarRoutes && !enChatDeViajeActivo) {
                BottomNavigationBar(currentTabForBar) { newTab ->
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
                            val usuarioId = usuarioViewModel.usuario.id
                            if (usuarioId != null) {
                                // Antes de ir a "home", revisamos si el usuario tiene un viaje
                                // EN_CURSO: si es así, lo mandamos directo a esa pantalla.
                                viajeViewModel.cargarViajesDesdeBackend(usuarioId) {
                                    val viajeEnCurso = viajeViewModel.misViajes.value.firstOrNull {
                                        it.estadoViaje == EstadoViaje.EN_CURSO
                                    }
                                    if (viajeEnCurso != null) {
                                        navController.navigate("active_ride/${viajeEnCurso.id}") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("home") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
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
                    viajeViewModel = viajeViewModel,
                    notificacionViewModel = notificacionViewModel
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
            // "viajeId" es opcional: si viene (p. ej. desde el mapa de un viaje activo),
            composable(
                route = "messages?viajeId={viajeId}",
                arguments = listOf(
                    navArgument("viajeId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val viajeIdArg = backStackEntry.arguments?.getString("viajeId")?.toLongOrNull()
                MessagesScreen(
                    usuarioViewModel = usuarioViewModel,
                    viajeViewModel = viajeViewModel,
                    initialViajeId = viajeIdArg,
                    onBack = { navController.popBackStack() } // <--- ¡AGREGA ESTA LÍNEA!
                )
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
                    usuarioViewModel = usuarioViewModel,
                    onBack = { navController.popBackStack() },
                    onChat = { id -> navController.navigate("messages?viajeId=$id") },
                    onNavigateToHome = { // <--- AGRÉGALO AQUÍ
                        navController.navigate("home") { // Cambia "home" por tu ruta principal
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }


            // ── NOTIFICACIONES ───────────────────────────────────────────────
            composable("notifications") {
                NotificationsScreen(
                    navController = navController,
                    usuarioViewModel = usuarioViewModel,
                    notificacionViewModel = notificacionViewModel
                )
            }
        }
    }
    BackHandler(enabled = currentRoute != null && currentRoute != "splash" && currentRoute != "login") {
        if (currentRoute == "home") {
            showExitDialog = true
        } else {
            navController.navigate("home") {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}