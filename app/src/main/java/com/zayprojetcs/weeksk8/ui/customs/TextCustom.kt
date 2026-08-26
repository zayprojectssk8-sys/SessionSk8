package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TextBodyLarge(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Visible,
    color: Color = MaterialTheme.colorScheme.onBackground
) {

    Text(
        modifier = modifier,
        text = text,
        lineHeight = lineHeight,
        maxLines = maxLines,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        overflow = overflow,
    )
}

@Composable
fun TextBodyMedium(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Visible,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontStyle: FontStyle = FontStyle.Normal
) {

    Text(
        modifier = modifier,
        text = text,
        lineHeight = lineHeight,
        maxLines = maxLines,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        overflow = overflow,
        fontStyle = fontStyle
    )
}