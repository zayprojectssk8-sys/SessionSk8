package com.zayprojetcs.weeksk8.screens.start_day_skate

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import com.zayprojetcs.weeksk8.core.room.model.StatusDay
import com.zayprojetcs.weeksk8.core.room.model.StatusRoundTrick
import com.zayprojetcs.weeksk8.core.room.model_relation.DayWithRoundsTrick
import com.zayprojetcs.weeksk8.core.room.model_relation.RoundTrick
import com.zayprojetcs.weeksk8.screens.start_day_skate.ui_state.StartDaySkateUiState
import com.zayprojetcs.weeksk8.screens.start_day_skate.ui_state.model.StartDaySkateUiStateModel
import com.zayprojetcs.weeksk8.ui.customs.FloatingButtonCustom
import com.zayprojetcs.weeksk8.ui.customs.LabelSectionContentCustom
import com.zayprojetcs.weeksk8.ui.customs.LoadTitleSectionCustom
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustom
import com.zayprojetcs.weeksk8.ui.customs.TextBodyLarge
import com.zayprojetcs.weeksk8.ui.customs.TextBodyMedium
import com.zayprojetcs.weeksk8.ui.customs.view.ItemTrickCustom


@Composable
fun StartDaySkateScreen(viewModel: StartDaySkateViewModel = viewModel()) {

    val dayWithRoundsTrick by viewModel.dayWithRoundsTrick.collectAsStateWithLifecycle()

    val startDaySkateUiState by viewModel.startDaySkateUiState.collectAsStateWithLifecycle()

    LaunchedEffect(dayWithRoundsTrick) {
        if (dayWithRoundsTrick != null) {
            viewModel.loadEvent(StartDaySkateUiState.GetTricksDay)
        }
    }

    ScaffoldCustom(
        floatingActionButton = {
            CreateFabActionDay(
                dayWithRoundsTrick = dayWithRoundsTrick,
                onStart = {
                    viewModel.loadEvent(StartDaySkateUiState.StartDay)
                },
                onStop = {
                    viewModel.loadEvent(StartDaySkateUiState.StopDay)
                })
        },
        content = {
            StartDaySkateContent(
                dayWithRoundsTrick = dayWithRoundsTrick,
                startDaySkateUiState = startDaySkateUiState,
                onCreateNextRound = {
                    viewModel.loadEvent(StartDaySkateUiState.CreateNextRound)
                },
                onCreateRound = {
                    viewModel.loadEvent(StartDaySkateUiState.CreateRound)
                },
                onSendResult = { itRound, itTried ->
                    viewModel.loadEvent(StartDaySkateUiState.SendRoundTrick(itRound, itTried))
                }
            )
        }
    )

}

@Composable
fun StartDaySkateContent(
    dayWithRoundsTrick: DayWithRoundsTrick?,
    startDaySkateUiState: StartDaySkateUiStateModel,
    onSendResult: (RoundTrick, Int) -> Unit,
    onCreateRound: () -> Unit,
    onCreateNextRound: () -> Unit
) {

    dayWithRoundsTrick ?: return

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {

        LoadTitleSectionCustom(dayWithRoundsTrick.roomDay.getTypeActivityDay().statusText)

        Spacer(modifier = Modifier.height(20.dp))

        when (dayWithRoundsTrick.roomDay.getStatusDay()) {
            StatusDay.CREATED -> CreatedDaySkateContentCreated(dayTrickList = startDaySkateUiState.dayTrickList)

            StatusDay.STARTED -> StartedDaySkateContentCreated(
                dayWithRoundsTrick = dayWithRoundsTrick,
                onCreateNextRound = onCreateNextRound,
                onSendResult = onSendResult,
                onCreateRound = onCreateRound
            )

            StatusDay.CLOSED -> {}
        }

    }
}

@Composable
fun StartedDaySkateContentCreated(
    dayWithRoundsTrick: DayWithRoundsTrick,
    onSendResult: (RoundTrick, Int) -> Unit,
    onCreateRound: () -> Unit,
    onCreateNextRound: () -> Unit
) {

    LaunchedEffect(
        dayWithRoundsTrick.roomDay,
        dayWithRoundsTrick.roomRoundTrickList
    ) {
        if (dayWithRoundsTrick.roomDay.status == StatusDay.STARTED.status && dayWithRoundsTrick.roomRoundTrickList.none { it.roomRoundTrick.round == dayWithRoundsTrick.roomDay.countRound }) {
            onCreateRound()
        }
    }

    if (dayWithRoundsTrick.roomRoundTrickList.isEmpty()) return

    dayWithRoundsTrick.roomRoundTrickList
        .find { it.roomRoundTrick.status == StatusRoundTrick.STARTED.status }
        .let { itRound ->
            itRound ?: StartedDayNextRound(
                dayWithRoundsTrick = dayWithRoundsTrick,
                onCreateNextRound = onCreateNextRound
            )
            itRound ?: return@let

            itRound.roomTrick.let letTrick@{ itTrick ->
                itTrick ?: return@letTrick

                LabelSectionContentCustom(
                    label = "RONDA ${itRound.roomRoundTrick.round}",
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    Column {

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {


                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                            ) {
                                TextBodyMedium(
                                    text = itTrick.getStatusTrick().statusText,
                                    modifier = Modifier.padding(2.dp),
                                    color = itTrick.getStatusTrick().statusColor
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                            ) {
                                TextBodyMedium(
                                    text = itTrick.typeTrick,
                                    modifier = Modifier.padding(2.dp),
                                    color = MaterialTheme.colorScheme.surface
                                )
                            }

                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            TextBodyMedium(
                                text = "TRUCO",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = 14.dp)
                                    .background(MaterialTheme.colorScheme.background)
                                    .align(Alignment.CenterStart)
                            )
                        }

                        Column(modifier = Modifier.padding(horizontal = 10.dp)) {

                            Spacer(modifier = Modifier.height(10.dp))

                            TextBodyLarge(text = itTrick.realName)

                            Spacer(modifier = Modifier.height(5.dp))

                            TextBodyMedium(text = itTrick.akaName, fontStyle = FontStyle.Italic)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            TextBodyMedium(
                                text = "INTENTO",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = 14.dp)
                                    .background(MaterialTheme.colorScheme.background)
                                    .align(Alignment.CenterStart)
                            )
                        }

                        AttemptSelector(
                            attempts = itRound.roomRoundTrick.attempts,
                            onSendResult = { itTried ->
                                onSendResult(itRound, itTried)
                            })

                    }

                }

            }


        }

}

@Composable
fun StartedDayNextRound(dayWithRoundsTrick: DayWithRoundsTrick, onCreateNextRound: () -> Unit) {

    LabelSectionContentCustom(
        label = "RESULTADO RONDA ${dayWithRoundsTrick.roomDay.countRound}",
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Column {

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dayWithRoundsTrick.roomRoundTrickList.filter { it.roomRoundTrick.round == dayWithRoundsTrick.roomDay.countRound }) { trick ->
                    trick.roomTrick.let { itTrick ->
                        itTrick ?: return@let
                        Column {

                            Spacer(modifier = Modifier.height(5.dp))

                            Column(modifier = Modifier.padding(horizontal = 10.dp)) {

                                TextBodyLarge(text = itTrick.realName)

                                Spacer(modifier = Modifier.height(5.dp))

                                TextBodyMedium(
                                    text = itTrick.akaName,
                                    fontStyle = FontStyle.Italic
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = Color.Green,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                ) {

                                    TextBodyMedium(
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .align(Alignment.Center),
                                        text = "AL INTENTO: ${trick.roomRoundTrick.countTriedSuccess}"
                                    )

                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = Color.Red,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                ) {

                                    TextBodyMedium(
                                        modifier = Modifier
                                            .padding(5.dp)
                                            .align(Alignment.Center),
                                        text = "FALLIDOS: ${trick.roomRoundTrick.countTriedFail}"
                                    )

                                }

                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onCreateNextRound,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                TextBodyMedium(
                    "COMENZAR NUEVA RONDA ${dayWithRoundsTrick.roomDay.countRound.plus(1)}",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun AttemptSelector(
    attempts: Int = 5,
    onSendResult: (Int) -> Unit,
) {
    // Estado para saber cuál RadioButton está seleccionado
    var selectedAttempt by remember { mutableIntStateOf(-1) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Generamos los RadioButtons dinámicamente del 1 al N
        (0..attempts).forEach { index ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RadioButton(
                    selected = (selectedAttempt == index),
                    onClick = { selectedAttempt = index },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.onBackground,
                        unselectedColor = Color.Gray
                    )
                )
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedAttempt == index) MaterialTheme.colorScheme.onBackground else Color.Gray
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = {
            onSendResult(selectedAttempt)
            selectedAttempt = -1
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 10.dp),
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        TextBodyMedium(
            "MANDAR RESULTADO",
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun CreatedDaySkateContentCreated(dayTrickList: List<RoomTrick>) {
    LabelSectionContentCustom(
        label = "TRUCOS PARA ESTE DÍA (${dayTrickList.size})",
        modifier = Modifier
            .fillMaxWidth()
    ) {

        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dayTrickList) { trick ->
                ItemTrickCustom(trick = trick)
            }
        }
    }
}

@Composable
fun CreateFabActionDay(
    dayWithRoundsTrick: DayWithRoundsTrick?,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    dayWithRoundsTrick ?: return

    dayWithRoundsTrick.roomDay.apply {

        FloatingButtonCustom(
            imageVector = if (getStatusDay() == StatusDay.CREATED) Icons.Outlined.PlayArrow else Icons.Outlined.Stop,
            onClick = {
                if (getStatusDay() == StatusDay.CREATED) onStart()
                else onStop()
            })

    }
}
