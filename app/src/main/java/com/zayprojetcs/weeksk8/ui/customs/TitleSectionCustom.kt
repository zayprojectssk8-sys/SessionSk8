package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadTitleSectionCustom(title: String, color: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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