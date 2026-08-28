package com.zayprojetcs.weeksk8.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.zaysk8.core.ui.DarkFondo
import com.zaysk8.core.ui.DarkSuperficie
import com.zaysk8.core.ui.DarkTextoPrincipal
import com.zaysk8.core.ui.DoradoBrillante
import com.zaysk8.core.ui.DoradoViejo
import com.zaysk8.core.ui.JadeNeon
import com.zaysk8.core.ui.JadeNeonDisabled
import com.zaysk8.core.ui.JadeOscuro
import com.zaysk8.core.ui.JadeOscuroDisabled
import com.zaysk8.core.ui.LightSuperficie
import com.zaysk8.core.ui.LightTextoPrincipal

// --- COLORES RAW ---
val RosaMexicano = Color(0xFFE91E63)
val RosaMexicanoHot = Color(0xFFFF1493)
val JadeNeon = Color(0xFF5FFB9F)
val JadeOscuro = Color(0xFF1B4D3E)

val NegroLija = Color(0xFF121212)
val GrisAsfalto = Color(0xFF1E1E24)
val BlancoPapelCrudo = Color(0xFFF7F5F0)

// --- COLOR SCHEMES ---
val StreetArtDarkColorScheme = darkColorScheme(
    primary = RosaMexicano,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6B0024),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = JadeNeon,
    onSecondary = Color.Black,
    secondaryContainer = JadeOscuro,
    onSecondaryContainer = JadeNeon,
    background = NegroLija,
    onBackground = Color(0xFFF5F5F0),
    surface = GrisAsfalto,
    onSurface = Color(0xFFF5F5F0),
    surfaceVariant = Color(0xFF2C2C35),
    onSurfaceVariant = JadeNeon
)

val StreetArtLightColorScheme = lightColorScheme(
    primary = RosaMexicano,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E0012),
    secondary = JadeOscuro,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFAAF5C7),
    onSecondaryContainer = Color(0xFF002115),
    background = BlancoPapelCrudo,
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFEFE8E2),
    onSurfaceVariant = Color(0xFF4A454E)
)


// --- COLORES RAW ---
val ToxicSlime = Color(0xFF39FF14)      // Verde Neón Slime
val HazardOrange = Color(0xFFFF4500)    // Naranja Neón / Alerta
val PoisonViolet = Color(0xFF8A2BE2)

val DarkGraphite = Color(0xFF16161A)
val DarkCard = Color(0xFF242429)
val RawWhite = Color(0xFFFFFFFE)

// --- COLOR SCHEMES ---
val ToxicSkateDarkColorScheme = darkColorScheme(
    primary = ToxicSlime,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0D5200),
    onPrimaryContainer = ToxicSlime,
    secondary = HazardOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF661A00),
    onSecondaryContainer = Color(0xFFFFDBCF),
    background = DarkGraphite,
    onBackground = RawWhite,
    surface = DarkCard,
    onSurface = RawWhite,
    surfaceVariant = Color(0xFF2E2E38),
    onSurfaceVariant = ToxicSlime
)

val ToxicSkateLightColorScheme = lightColorScheme(
    primary = Color(0xFF1B8000),         // Verde tóxico oscuro para legibilidad en claro
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB4FF9A),
    onPrimaryContainer = Color(0xFF042100),
    secondary = HazardOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF3B0900),
    background = Color(0xFFF4F6F0),
    onBackground = DarkGraphite,
    surface = Color.White,
    onSurface = DarkGraphite,
    surfaceVariant = Color(0xFFE0E5D8),
    onSurfaceVariant = Color(0xFF2B3325)
)

// --- COLORES RAW ---
val FlameYellow = Color(0xFFFFE600)    // Amarillo Fuego Neón
val FlameRed = Color(0xFFFF1E00)       // Rojo Llama
val UVPurple = Color(0xFF7000FF)       // Violeta Eléctrico

val CharcoalBlack = Color(0xFF18181C)
val CharcoalSurface = Color(0xFF25252B)
val FanzineBg = Color(0xFFF7F3E9)      // Papel periódico / fanzine crudo

// --- COLOR SCHEMES ---
val FlameDarkColorScheme = darkColorScheme(
    primary = FlameYellow,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF524A00),
    onPrimaryContainer = FlameYellow,
    secondary = FlameRed,
    onSecondary = Color.White,
    tertiary = UVPurple,
    onTertiary = Color.White,
    background = CharcoalBlack,
    onBackground = Color.White,
    surface = CharcoalSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF33333D),
    onSurfaceVariant = FlameYellow
)

val FlameLightColorScheme = lightColorScheme(
    primary = FlameRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410001),
    secondary = UVPurple,
    onSecondary = Color.White,
    tertiary = Color(0xFF857700),
    background = FanzineBg,
    onBackground = CharcoalBlack,
    surface = Color.White,
    onSurface = CharcoalBlack,
    surfaceVariant = Color(0xFFEBE3D3),
    onSurfaceVariant = Color(0xFF4D4639)
)

@Composable
fun WeekSk8Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> ToxicSkateDarkColorScheme
        else -> ToxicSkateLightColorScheme
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