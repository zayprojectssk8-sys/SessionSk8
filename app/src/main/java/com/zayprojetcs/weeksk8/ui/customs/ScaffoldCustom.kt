package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScaffoldCustom(
    floatingActionButton: @Composable (() -> Unit) = {},
    content: @Composable (() -> Unit) = {},
) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = floatingActionButton,
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                content()
            }
        })
}
