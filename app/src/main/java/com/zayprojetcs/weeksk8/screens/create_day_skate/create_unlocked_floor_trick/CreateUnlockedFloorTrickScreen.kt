package com.zayprojetcs.weeksk8.screens.create_day_skate.create_unlocked_floor_trick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zayprojetcs.weeksk8.screens.create_day_skate.ui_state.model.CreateDaySkateUiStateModel
import com.zayprojetcs.weeksk8.ui.customs.LabelSectionContentCustom
import com.zayprojetcs.weeksk8.ui.customs.TextBodyMedium
import com.zayprojetcs.weeksk8.ui.customs.view.ItemTrickCustom


@Composable
fun CreateUnlockedFloorTrickScreen(
    createDaySkateUiStateModel: CreateDaySkateUiStateModel,
    onGetTriedUnlockTricks: () -> Unit,
    onAddUnlockTrick: () -> Unit,
) {

    LaunchedEffect(Unit) {
        onGetTriedUnlockTricks()
    }


    LabelSectionContentCustom(
        label = "DESBLOQUEAR TRUCOS",
        modifier = Modifier
            .fillMaxWidth()
    ) {

        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if ((createDaySkateUiStateModel.unlockTriedTrickList.size + createDaySkateUiStateModel.unlockNewTriedTrickList.size) < 4) {
                item {
                    Button(
                        onClick = onAddUnlockTrick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        TextBodyMedium(
                            "AGREGAR TRUCO DESBLOQUEADO",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            items(createDaySkateUiStateModel.unlockTriedTrickList) { trick ->
                ItemTrickCustom(trick = trick)
            }
            items(createDaySkateUiStateModel.unlockNewTriedTrickList) { trick ->
                ItemTrickCustom(trick = trick, newTrick = true)
            }
        }
    }
}