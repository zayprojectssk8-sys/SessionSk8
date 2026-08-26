package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SubFabItem(
    label: String, icon: ImageVector, color: Color, onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)
    ) {
        // Etiqueta del botón
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary,
            contentColor =  MaterialTheme.colorScheme.onBackground,
            border = BorderStroke(width = 1.dp, color = color),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Botón pequeño
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Icon(imageVector = icon, contentDescription = null)
        }
    }
}