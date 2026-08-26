package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LabelSectionCustom(title: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        label = {
            TextBodyMedium(
                title,
                color = MaterialTheme.colorScheme.primary
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onBackground,
            disabledBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}