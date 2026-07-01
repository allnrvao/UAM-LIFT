package ni.edu.uam.uamlift.ui.screens.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ni.edu.uam.uamlift.R

enum class SplashPhase { ENTER, LOADING, EXIT }

@Composable
fun SplashScreen(onDone: () -> Unit) { // CORREGIDO: Ahora es un callback estándar () -> Unit
    var phase by remember { mutableStateOf(SplashPhase.ENTER) }
    val progress = remember { Animatable(0f) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    LaunchedEffect(Unit) {
        // Reducimos los tiempos para que la navegación a login sea más rápida
        delay(150)
        phase = SplashPhase.LOADING
    }

    // Phase 2: Animación de la barra de progreso al entrar en modo LOADING
    LaunchedEffect(phase) {
        if (phase == SplashPhase.LOADING) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = LinearEasing)
            )
            // Pequeña espera al llegar al 100%
            delay(1500)
            phase = SplashPhase.EXIT
            // Duración de la animación de salida antes de llamar al onDone
            onDone()
        }
    }

    // ── Animaciones de Transición (Estilos Dinámicos) ──────────────────────

    // Salida de toda la pantalla hacia la izquierda (Slide left)
    val screenOffset by animateFloatAsState(
        targetValue = if (phase == SplashPhase.EXIT) -screenWidth.value else 0f,
        animationSpec = tween(durationMillis = 500, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
        label = "ExitTransition"
    )

    // Estructura limpia del if/else de Kotlin y cierre de paréntesis de la función
    val logoAlpha by animateFloatAsState(
        targetValue = if (phase == SplashPhase.ENTER) 0f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "LogoAlpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (phase == SplashPhase.ENTER) 0.92f else 1f,
        animationSpec = tween(durationMillis = 500, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)),
        label = "LogoScale"
    )
    val logoOffsetY by animateFloatAsState(
        targetValue = if (phase == SplashPhase.ENTER) 24f else 0f,
        animationSpec = tween(durationMillis = 500, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)),
        label = "LogoOffset"
    )

    // Opacidad de la barra de carga
    val loaderAlpha by animateFloatAsState(
        targetValue = if (phase == SplashPhase.ENTER) 0f else 1f,
        animationSpec = tween(durationMillis = 400, delayMillis = 150),
        label = "LoaderAlpha"
    )

    // ── Interfaz Gráfica (UI) ──────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = screenOffset.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF019AA8),
                        Color(0xFF018291),
                        Color(0xFF006473)
                    )
                )
            )
    ) {
        // Círculo decorativo superior derecho
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = (-60).dp)
                .size(192.dp)
                .background(Color.White.copy(alpha = 0.05f), shape = CircleShape)
                .align(Alignment.TopEnd)
        )

        // Círculo decorativo inferior izquierdo
        Box(
            modifier = Modifier
                .offset(x = (-40).dp, y = 40.dp)
                .size(144.dp)
                .background(Color.White.copy(alpha = 0.05f), shape = CircleShape)
                .align(Alignment.BottomStart)
        )

        // Bloque del LOGO
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = logoOffsetY.dp)
                .scale(logoScale)
                .alpha(logoAlpha)
                .padding(bottom = 64.dp)
        ) {
            // Icono "UL" con desenfoque de fondo simulado
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .blur(if (phase == SplashPhase.ENTER) 10.dp else 0.dp) // Simula el backdrop-blur
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(28.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(28.dp))
            ) {
                Image(

                    painter = painterResource(id = R.drawable.uam_lift_logo),

                    contentDescription = "Logo UAM Lift",

                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(40.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Título (UAM LIFT)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "UAM ",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "LIFT",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "Movilidad colaborativa estudiantil",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }

        // Barra de Carga (Loading Bar)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 40.dp, end = 40.dp)
                .fillMaxWidth()
                .alpha(loaderAlpha)
        ) {
            // Contenedor de la barra (Gris transparente)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                // Progreso real de la barra (Blanco fijo)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.value)
                        .height(4.dp)
                        .background(Color.White, shape = CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cargando...",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}