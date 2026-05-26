package ni.edu.uam.UAM_LIFT.screens.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
// 1. AGREGA ESTE IMPORT EXPLÍCITO DE MATERIAL 3
import androidx.compose.material3.Typography

// 2. AGREGA TU PROPIA VARIABLE DE FUENTES (ajusta la ruta según tus carpetas si es necesario)
// Si creaste un objeto o variable llamado 'Typography' en un archivo Type.kt, impórtalo:
// import ni.edu.uam.UAM_LIFT.screens.theme.Typography

private val PrimaryColor = Color(0xFF019AA8)
private val PrimaryContainer = Color(0xFFE6F4F5)

val UamLiftColorScheme = lightColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryContainer,
    surface = Color(0xFFF6F8FA),
    background = Color(0xFFF6F8FA),
    onPrimary = Color.White,
    onSurface = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFF6B6B6B)
)

@Composable
fun UamLiftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UamLiftColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}