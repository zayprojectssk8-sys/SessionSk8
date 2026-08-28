package com.sk8.appwatch.presentation.screen.app_empty_communication

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.PhonelinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*


@Composable
fun AppEmptyCommunicationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhonelinkErase,
            contentDescription = "Sin celular vinculado",
            tint = MaterialTheme.colors.error,
            modifier = Modifier
                .size(50.dp)
                .padding(bottom = 4.dp)
        )

        Text(
            text = "No hay celular vinculado",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

    }
}