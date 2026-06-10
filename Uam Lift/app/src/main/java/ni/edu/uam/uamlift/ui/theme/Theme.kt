package ni.edu.uam.uamlift.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- PALETA MODO CLARO ---
val LightColors = lightColorScheme(
    primary = Color(0xFF019AA8),
    secondary = Color(0xFF76D0DA),
    background = Color(0xFFFFFFFF), // Tu 'white'
    surface = Color(0xFFF3F5F7),    // Tu 'Gray'
    onPrimary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

// --- PALETA MODO OSCURO ---
val DarkColors = darkColorScheme(
    primary = Color(0xFF4DB6C1),       // Versión desaturada
    secondary = Color(0xFFB2EBF2),
    background = Color(0xFF121212),    // Fondo oscuro
    surface = Color(0xFF1E1E1E),       // Superficies/Tarjetas oscuras
    onPrimary = Color(0xFF121212),
    onBackground = Color(0xFFF3F5F7),  // Texto claro sobre fondo oscuro
    onSurface = Color(0xFFF3F5F7)
)

@Composable
fun UamLiftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}