package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadTitleSectionCustom1(
    title: String,
    color: Color = MaterialTheme.colorScheme.surface
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .clip(shape)
            // 1. Fondo base (ligeramente oscurecido para simular que está en el fondo)
            .background(color)
            // 2. Sombra interna superior (Efecto de hendidura/hundido)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f), // Sombra más densa arriba
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.5f)
                        ),
                        startY = 0f,
                        endY = 20.dp.toPx()
                    )
                )
            }
            // 3. Borde asimétrico: Oscuro arriba (sombra del marco) y Primary abajo (reflejo)
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.primary
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TextBodyLarge(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@Composable
fun LoadTitleSectionCustom(title: String, color: Color = MaterialTheme.colorScheme.background) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = color,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        TextBodyLarge(
            text = title,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}