package com.zayprojetcs.weeksk8.ui.customs.view


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import com.zayprojetcs.weeksk8.ui.customs.TextBodyMedium


@Composable
fun ItemTrickCustom(
    trick: RoomTrick,
    newTrick: Boolean = false
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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(8f)) {

                Text(
                    text = trick.akaName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = trick.realName,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (newTrick) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .weight(2f)
                ) {
                    TextBodyMedium(
                        text = "NUEVO",
                        modifier = Modifier.padding(2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}