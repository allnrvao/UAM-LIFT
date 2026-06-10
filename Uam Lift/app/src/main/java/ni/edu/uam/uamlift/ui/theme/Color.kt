package ni.edu.uam.uamlift.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

//Colores usados en el modo claro
val UAMColor = Color(0xFF019AA8)
val white = Color(0xFFFFFFFF)
val Gray = Color(0xFFF3F5F7)
val Degradado1 = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF019AA8),
        Color(0xFF76D0DA)
    ))
val Degradado2 = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF00BCD4),
        Color(0xFF06585E)
    ))

val UAMColorLight= Color(0xFF76D0DA)

//modo oscuro
// Fondos oscuros principales (Reemplazan al blanco y gris claro)
val backgroundDark = Color(0xFF121212) // Fondo principal estándar de Material Design
val surfaceDark = Color(0xFF1E1E1E)    // Tarjetas, menús y componentes sobre el fondo
val grayDark = Color(0xFF2C2C2C)       // Reemplaza al Gray (F3F5F7) para contenedores secundarios

// Colores de acento (Desaturados para evitar fatiga visual)
val UAMColorDark = Color(0xFF4DB6C1)    // Una versión más suave y pastel del 0xFF019AA8
val UAMColorLightDark = Color(0xFFB2EBF2) // Reemplaza al UAMColorLight para elementos muy brillantes

// Textos (Inversión del blanco/negro)
val textPrimaryDark = Color(0xFFF3F5F7)
val textSecondaryDark = Color(0xFFA0A5AA)

// Degradados adaptados para modo oscuro
val Degradado1Dark = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF33A9B4), // Un tono medio
        Color(0xFF88D8E2)  // Un tono claro pero suave
    )
)

val Degradado2Dark = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF00ACC1),
        Color(0xFF0D47A1) // Un azul/cian oscuro profundo que funde mejor con el fondo
    )
)

