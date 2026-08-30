package com.zayprojetcs.weeksk8.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// --- COLORES RAW ---
val ToxicSlime = Color(0xFF39FF14)      // Verde Neón Slime
val HazardOrange = Color(0xFFFF4500)    // Naranja Neón / Alerta
val PoisonViolet = Color(0xFF8A2BE2)

val DarkGraphite = Color(0xFF16161A)
val DarkCard = Color(0xFF242429)
val RawWhite = Color(0xFFFFFFFE)

// --- COLOR SCHEMES ---
val DeepPinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF1493),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6B003B),
    onPrimaryContainer = Color(0xFFFFD8EC),
    secondary = Color(0xFFFFB300),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF573E00),
    onSecondaryContainer = Color(0xFFFFDF9E),
    background = Color(0xFF161214),
    onBackground = Color(0xFFEDE0E4),
    surface = Color(0xFF201A1D),
    onSurface = Color(0xFFEDE0E4),
    surfaceVariant = Color(0xFF33282E),
    onSurfaceVariant = Color(0xFFFFB0D8)
)

val DeepPinkLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF1493),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD8EC),
    onPrimaryContainer = Color(0xFF3B001E),
    secondary = Color(0xFF8B5000),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC1),
    onSecondaryContainer = Color(0xFF2E1500),
    background = Color(0xFFFAF7F9),
    onBackground = Color(0xFF201A1D),
    surface = Color.White,
    onSurface = Color(0xFF201A1D),
    surfaceVariant = Color(0xFFF2DDE6),
    onSurfaceVariant = Color(0xFF514349)
)

val MagentaDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF00FF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF6E006E),
    onPrimaryContainer = Color(0xFFFFD6FF),
    secondary = Color(0xFF00E5FF),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFFA6F5FF),
    background = Color(0xFF151015),
    onBackground = Color(0xFFF0E0F0),
    surface = Color(0xFF1E171E),
    onSurface = Color(0xFFF0E0F0),
    surfaceVariant = Color(0xFF322432),
    onSurfaceVariant = Color(0xFFFFB3FF)
)

val MagentaLightColorScheme = lightColorScheme(
    primary = Color(0xFFB800B8), // Ajustado para accesibilidad sobre claro
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD6FF),
    onPrimaryContainer = Color(0xFF3B003B),
    secondary = Color(0xFF006975),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA6F5FF),
    onSecondaryContainer = Color(0xFF002024),
    background = Color(0xFFFDF7FD),
    onBackground = Color(0xFF201520),
    surface = Color.White,
    onSurface = Color(0xFF201520),
    surfaceVariant = Color(0xFFEEDAEF),
    onSurfaceVariant = Color(0xFF4F4050)
)

val CobaltDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4D78FF), // Ajustado para brillo sobre oscuro
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF002399),
    onPrimaryContainer = Color(0xFFDBE1FF),
    secondary = Color(0xFFFF9100),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5C3300),
    onSecondaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF10131B),
    onBackground = Color(0xFFE2E2EC),
    surface = Color(0xFF181C26),
    onSurface = Color(0xFFE2E2EC),
    surfaceVariant = Color(0xFF262B3B),
    onSurfaceVariant = Color(0xFF91B0FF)
)

val CobaltLightColorScheme = lightColorScheme(
    primary = Color(0xFF0047FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE1FF),
    onPrimaryContainer = Color(0xFF000F5C),
    secondary = Color(0xFF8B4A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2E1500),
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF181C26),
    surface = Color.White,
    onSurface = Color(0xFF181C26),
    surfaceVariant = Color(0xFFDFE2F2),
    onSurfaceVariant = Color(0xFF434654)
)

val CyanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5CC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF005047),
    onPrimaryContainer = Color(0xFF73FAF0),
    secondary = Color(0xFFFF3D00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF681200),
    onSecondaryContainer = Color(0xFFFFDBD2),
    background = Color(0xFF0E1514),
    onBackground = Color(0xFFDEE5E3),
    surface = Color(0xFF151E1C),
    onSurface = Color(0xFFDEE5E3),
    surfaceVariant = Color(0xFF23322E),
    onSurfaceVariant = Color(0xFF66F2E2)
)

val CyanLightColorScheme = lightColorScheme(
    primary = Color(0xFF006B5F), // Tono saturado para texto claro
    onPrimary = Color.White,
    primaryContainer = Color(0xFF73FAF0),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFFAD2B00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBD2),
    onSecondaryContainer = Color(0xFF3B0900),
    background = Color(0xFFF5FAFA),
    onBackground = Color(0xFF151E1C),
    surface = Color.White,
    onSurface = Color(0xFF151E1C),
    surfaceVariant = Color(0xFFD6E5E2),
    onSurfaceVariant = Color(0xFF3F4947)
)
val OrangeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5E00),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF6E2300),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFF00E676),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF005227),
    onSecondaryContainer = Color(0xFF83FFAF),
    background = Color(0xFF181210),
    onBackground = Color(0xFFECE0DC),
    surface = Color(0xFF221A16),
    onSurface = Color(0xFFECE0DC),
    surfaceVariant = Color(0xFF362822),
    onSurfaceVariant = Color(0xFFFFB592)
)

val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFBF4300), // Ajustado para contraste
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF3B0A00),
    secondary = Color(0xFF006D37),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF83FFAF),
    onSecondaryContainer = Color(0xFF00210C),
    background = Color(0xFFFCF7F5),
    onBackground = Color(0xFF221A16),
    surface = Color.White,
    onSurface = Color(0xFF221A16),
    surfaceVariant = Color(0xFFF5DED6),
    onSurfaceVariant = Color(0xFF52443E)
)

val AcidGreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00CC33),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF005211),
    onPrimaryContainer = Color(0xFF72FF80),
    secondary = Color(0xFFE600FF),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF590063),
    onSecondaryContainer = Color(0xFFFFD6FE),
    background = Color(0xFF0E150F),
    onBackground = Color(0xFFDEE5DF),
    surface = Color(0xFF141F16),
    onSurface = Color(0xFFDEE5DF),
    surfaceVariant = Color(0xFF223325),
    onSurfaceVariant = Color(0xFF62E675)
)

val AcidGreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF006E18),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF72FF80),
    onPrimaryContainer = Color(0xFF002203),
    secondary = Color(0xFF9800AA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD6FE),
    onSecondaryContainer = Color(0xFF35003D),
    background = Color(0xFFF5FAF5),
    onBackground = Color(0xFF141F16),
    surface = Color.White,
    onSurface = Color(0xFF141F16),
    surfaceVariant = Color(0xFFD6E5D8),
    onSurfaceVariant = Color(0xFF3F4941)
)

val BlueVioletDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA855FF), // Ajustado para visibilidad
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF450085),
    onPrimaryContainer = Color(0xFFEFDBFF),
    secondary = Color(0xFFCCFF00),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF425400),
    onSecondaryContainer = Color(0xFFE0FF75),
    background = Color(0xFF131018),
    onBackground = Color(0xFFE7E0EC),
    surface = Color(0xFF1D1724),
    onSurface = Color(0xFFE7E0EC),
    surfaceVariant = Color(0xFF2D2338),
    onSurfaceVariant = Color(0xFFC799FF)
)

val BlueVioletLightColorScheme = lightColorScheme(
    primary = Color(0xFF8A2BE2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFDBFF),
    onPrimaryContainer = Color(0xFF280058),
    secondary = Color(0xFF5A6B00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0FF75),
    onSecondaryContainer = Color(0xFF192200),
    background = Color(0xFFFAF7FD),
    onBackground = Color(0xFF1D1724),
    surface = Color.White,
    onSurface = Color(0xFF1D1724),
    surfaceVariant = Color(0xFFE7DDED),
    onSurfaceVariant = Color(0xFF494450)
)

val ElectricPurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC466FF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF53008E),
    onPrimaryContainer = Color(0xFFF2D6FF),
    secondary = Color(0xFF00F5D4),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF005045),
    onSecondaryContainer = Color(0xFF73FFEC),
    background = Color(0xFF140F18),
    onBackground = Color(0xFFE8E0EC),
    surface = Color(0xFF1E1524),
    onSurface = Color(0xFFE8E0EC),
    surfaceVariant = Color(0xFF31223A),
    onSurfaceVariant = Color(0xFFD48BFF)
)

val ElectricPurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF7B00E0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF2D6FF),
    onPrimaryContainer = Color(0xFF30005A),
    secondary = Color(0xFF006B5C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF73FFEC),
    onSecondaryContainer = Color(0xFF00201B),
    background = Color(0xFFFCF7FD),
    onBackground = Color(0xFF1E1524),
    surface = Color.White,
    onSurface = Color(0xFF1E1524),
    surfaceVariant = Color(0xFFEADDF2),
    onSurfaceVariant = Color(0xFF4B4252)
)
@Composable
fun WeekSk8Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DeepPinkDarkColorScheme
        else -> DeepPinkLightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            val insetsController = WindowCompat.getInsetsController(window, view)

            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme

        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun ForceOrientationPortrait() {
    val view = LocalView.current

    val activity = (view.context as Activity)

    DisposableEffect(Unit) {
        // 1. Guardamos la orientación original para restaurarla después
        val originalOrientation = activity.requestedOrientation

        // 2. Aplicamos la nueva lógica de orientación
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}