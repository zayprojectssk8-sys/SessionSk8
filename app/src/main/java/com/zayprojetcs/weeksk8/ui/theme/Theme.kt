package com.zayprojetcs.weeksk8.ui.theme

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
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = JadeNeon,
    onSurfaceVariant = JadeNeonDisabled,
    secondary = DoradoBrillante,
    surfaceVariant = DoradoViejo,
    background = DarkSuperficie,
    surface = DarkFondo,
    onPrimary = Color.Black,
    onBackground = DarkTextoPrincipal,
    onSurface = DarkTextoPrincipal
)

// ESQUEMA CLARO
private val LightColorScheme = lightColorScheme(
    primary = JadeOscuro,
    onSurfaceVariant = DoradoBrillante,
    secondary = LightTextoPrincipal,
    surfaceVariant = DoradoViejo,
    background = JadeOscuroDisabled,
    surface = LightSuperficie,
    onPrimary = Color.White,
    onBackground = LightTextoPrincipal,
    onSurface = LightTextoPrincipal
)

@Composable
fun WeekSk8Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
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