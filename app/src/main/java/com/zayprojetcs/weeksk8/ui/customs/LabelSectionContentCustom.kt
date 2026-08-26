package com.zayprojetcs.weeksk8.ui.customs


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LabelSectionContentCustom(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {

        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            content()
        }

        Box(
            modifier = Modifier
                .padding(start = 14.dp)
        ) {

            TextBodyMedium(
                label,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
