package com.sk8.appwatch.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.zaysk8.core.ui.DarkFondo
import com.zaysk8.core.ui.DarkSuperficie
import com.zaysk8.core.ui.DarkTextoPrincipal
import com.zaysk8.core.ui.DoradoBrillante
import com.zaysk8.core.ui.DoradoViejo
import com.zaysk8.core.ui.JadeNeon
import com.zaysk8.core.ui.JadeNeonDisabled

val WearColors = Colors(
    primary = JadeNeon,
    onPrimary = Color.Black,
    secondary = DoradoBrillante,
    background = DarkSuperficie, // En Wear OS se recomienda Color.Black para paneles OLED
    surface = DarkFondo,
    onBackground = DarkTextoPrincipal,
    onSurface = DarkTextoPrincipal,
    onSurfaceVariant = JadeNeonDisabled,

    // 'surfaceVariant' no existe en Wear Colors; puedes mapearlo a primaryVariant o secondaryVariant
    primaryVariant = DoradoViejo
)

@Composable
fun WeekSk8Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColors
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            timeText = { TimeText() }
        ) {
            content()
        }
    }
}