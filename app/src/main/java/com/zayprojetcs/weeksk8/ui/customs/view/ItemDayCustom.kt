package com.zayprojetcs.weeksk8.ui.customs.view


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zayprojetcs.weeksk8.core.room.model.RoomDay


@Composable
fun ItemDayCustom(
    roomDay: RoomDay
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        border = BorderStroke(
            1.dp, MaterialTheme.colorScheme.primary
        )
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {

            Text(
                text = roomDay.getTypeActivityDay().statusText,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${roomDay.countRound} RONDAS",
                style = MaterialTheme.typography.bodySmall
            )

        }
    }
}