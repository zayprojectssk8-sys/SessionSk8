package com.zayprojetcs.weeksk8.screens.create_day_skate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zaysk8.core.model.TFMTrick
import com.zaysk8.core.model.TypeStanceTrick
import com.zaysk8.core.model.TypeTrick
import com.zayprojetcs.weeksk8.core.room.model.TypeActivityDay
import com.zayprojetcs.weeksk8.screens.create_day_skate.create_review_floor_trick.CreateReviewFloorTrickScreen
import com.zayprojetcs.weeksk8.screens.create_day_skate.create_review_unloked_slides_grind.CreateReviewAndUnlockedFloorTrickScreen
import com.zayprojetcs.weeksk8.screens.create_day_skate.create_unlocked_floor_trick.CreateUnlockedFloorTrickScreen
import com.zayprojetcs.weeksk8.screens.create_day_skate.create_unloked_switch.CreateReviewAndUnlockedSwitchTrickScreen
import com.zayprojetcs.weeksk8.screens.create_day_skate.ui_state.CreateDaySkateUiState
import com.zayprojetcs.weeksk8.screens.create_day_skate.ui_state.model.CreateDaySkateUiStateModel
import com.zayprojetcs.weeksk8.ui.customs.FloatingButtonCustom
import com.zayprojetcs.weeksk8.ui.customs.LabelSectionCustom
import com.zayprojetcs.weeksk8.ui.customs.LoadTitleSectionCustom
import com.zayprojetcs.weeksk8.ui.customs.ScaffoldCustom
import com.zayprojetcs.weeksk8.ui.customs.view.SelectTrickScreen

@Composable
fun CreateDaySkateScreen(
    viewModel: CreateDaySkateViewModel = viewModel()
) {

    val createDaySkateUiStateModel by viewModel.createDaySkateUiStateModel.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadEvent(CreateDaySkateUiState.GetLastDaySkate)
    }

    ScaffoldCustom(
        floatingActionButton = {
            if (!createDaySkateUiStateModel.showViewAddUnlockTrick) {
                CreateFabActionSave(
                    createDaySkateUiStateModel = createDaySkateUiStateModel,
                    onSaveActivity = {
                        viewModel.loadEvent(CreateDaySkateUiState.RegisterDay)
                    })
            }
        },
        content = {
            if (createDaySkateUiStateModel.showViewAddUnlockTrick) {
                SelectUnlockTrick(
                    createDaySkateUiStateModel = createDaySkateUiStateModel,
                    onSendSelection = { itTrickList ->
                        viewModel.loadEvent(CreateDaySkateUiState.AddUnlockTrick(false))

                        createDaySkateUiStateModel.currentRoomDay.let { itCurrentDay ->
                            itCurrentDay ?: return@let
                            val event = when (itCurrentDay.getTypeActivityDay()) {
                                TypeActivityDay.REVIEWING_FLOOR_TRICKS ->
                                    CreateDaySkateUiState.SetUnlockTrickSelectionList(
                                        itTrickList
                                    )

                                TypeActivityDay.UNLOCKED_FLOOR_TRICKS ->
                                    CreateDaySkateUiState.SetTriedUnlockTrickSelectionList(
                                        itTrickList
                                    )

                                TypeActivityDay.GRIND_AND_SLIDE_TRICKS ->
                                    CreateDaySkateUiState.SetTriedUnlockGrindSlideTrickSelectionList(
                                        itTrickList
                                    )

                                TypeActivityDay.SWITCH_TRICKS ->
                                    CreateDaySkateUiState.SetTriedUnlockSwitchNollieTrickSelectionList(
                                        itTrickList
                                    )
                            }
                            viewModel.loadEvent(event)
                        }
                    })
                return@ScaffoldCustom
            }

            CreateDaySkateContent(
                createDaySkateUiStateModel = createDaySkateUiStateModel,
                onGetUnlockTricks = {
                    viewModel.loadEvent(CreateDaySkateUiState.GetUnlockTrickList)
                },
                onAddUnlockTrick = {
                    viewModel.loadEvent(CreateDaySkateUiState.AddUnlockTrick(true))
                },
                onGetTriedUnlockTricks = {
                    viewModel.loadEvent(CreateDaySkateUiState.GetTriedUnlockTrickList)
                },
                onGetSlideGrindUnlockTricks = {
                    viewModel.loadEvent(CreateDaySkateUiState.GetTriedGrindSlideUnlockTrickList)
                },
                onGetSlideSwitchUnlockTricks = {
                    viewModel.loadEvent(CreateDaySkateUiState.GetTriedSwitchNollieUnlockTrickList)
                })
        }
    )
}

@Composable
fun CreateFabActionSave(
    createDaySkateUiStateModel: CreateDaySkateUiStateModel,
    onSaveActivity: () -> Unit
) {

    createDaySkateUiStateModel.currentRoomDay ?: return
    val enabled = when (createDaySkateUiStateModel.currentRoomDay.getTypeActivityDay()) {
        TypeActivityDay.REVIEWING_FLOOR_TRICKS -> createDaySkateUiStateModel.unlockTrickList.isNotEmpty() || createDaySkateUiStateModel.unlockNewTrickList.isNotEmpty()
        TypeActivityDay.UNLOCKED_FLOOR_TRICKS -> (createDaySkateUiStateModel.unlockTriedTrickList.size.plus(
            createDaySkateUiStateModel.unlockNewTriedTrickList.size
        )) == 4

        TypeActivityDay.GRIND_AND_SLIDE_TRICKS -> createDaySkateUiStateModel.unlockTriedGrindSlideTrickList.isNotEmpty() || createDaySkateUiStateModel.unlockNewTriedGrindSlideTrickList.size == 4
        TypeActivityDay.SWITCH_TRICKS -> createDaySkateUiStateModel.unlockTriedSwitchNollieTrickList.isNotEmpty() || createDaySkateUiStateModel.unlockNewTriedSwitchNollieTrickList.size == 4
    }

    if (enabled) FloatingButtonCustom(
        imageVector = Icons.Outlined.Save,
        onClick = onSaveActivity
    )
}

@Composable
fun CreateDaySkateContent(
    createDaySkateUiStateModel: CreateDaySkateUiStateModel,
    onGetUnlockTricks: () -> Unit,
    onAddUnlockTrick: () -> Unit,
    onGetTriedUnlockTricks: () -> Unit,
    onGetSlideGrindUnlockTricks: () -> Unit,
    onGetSlideSwitchUnlockTricks: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LoadTitleSectionCustom("CREATE DAY SKATE")

        Spacer(modifier = Modifier.height(20.dp))

        createDaySkateUiStateModel.currentRoomDay ?: return

        LabelSectionCustom(
            title = "TIPO DE ACTIVIDAD",
            value = createDaySkateUiStateModel.currentRoomDay.getTypeActivityDay().statusText
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (createDaySkateUiStateModel.currentRoomDay.getTypeActivityDay()) {
            TypeActivityDay.REVIEWING_FLOOR_TRICKS -> CreateReviewFloorTrickScreen(
                createDaySkateUiStateModel = createDaySkateUiStateModel,
                onAddUnlockTrick = onAddUnlockTrick,
                onGetUnlockTricks = onGetUnlockTricks
            )

            TypeActivityDay.UNLOCKED_FLOOR_TRICKS -> CreateUnlockedFloorTrickScreen(
                createDaySkateUiStateModel = createDaySkateUiStateModel,
                onAddUnlockTrick = onAddUnlockTrick,
                onGetTriedUnlockTricks = onGetTriedUnlockTricks
            )

            TypeActivityDay.GRIND_AND_SLIDE_TRICKS -> CreateReviewAndUnlockedFloorTrickScreen(
                createDaySkateUiStateModel = createDaySkateUiStateModel,
                onAddUnlockTrick = onAddUnlockTrick,
                onGetSlideGrindUnlockTricks = onGetSlideGrindUnlockTricks
            )

            TypeActivityDay.SWITCH_TRICKS -> CreateReviewAndUnlockedSwitchTrickScreen(
                createDaySkateUiStateModel = createDaySkateUiStateModel,
                onAddUnlockTrick = onAddUnlockTrick,
                onGetSlideSwitchUnlockTricks = onGetSlideSwitchUnlockTricks
            )
        }

    }
}


@Composable
fun SelectUnlockTrick(
    createDaySkateUiStateModel: CreateDaySkateUiStateModel,
    onSendSelection: (List<TFMTrick>) -> Unit
) {
    createDaySkateUiStateModel.currentRoomDay ?: return

    val listTricks = when (createDaySkateUiStateModel.currentRoomDay.getTypeActivityDay()) {
        TypeActivityDay.REVIEWING_FLOOR_TRICKS, TypeActivityDay.UNLOCKED_FLOOR_TRICKS -> TypeTrick.FLOOR.trickList
        TypeActivityDay.GRIND_AND_SLIDE_TRICKS -> TypeTrick.GRIND.trickList + TypeTrick.SLIDE.trickList
        TypeActivityDay.SWITCH_TRICKS -> TypeTrick.FLOOR.trickList + TypeTrick.GRIND.trickList + TypeTrick.SLIDE.trickList
    }

    val limitSelected = when (createDaySkateUiStateModel.currentRoomDay.getTypeActivityDay()) {
        TypeActivityDay.REVIEWING_FLOOR_TRICKS -> 0
        TypeActivityDay.UNLOCKED_FLOOR_TRICKS, TypeActivityDay.GRIND_AND_SLIDE_TRICKS, TypeActivityDay.SWITCH_TRICKS -> 4
    }

    val registeredNames = remember(
        createDaySkateUiStateModel.unlockTrickList,
        createDaySkateUiStateModel.unlockNewTrickList,
        createDaySkateUiStateModel.trickEnableList
    ) {
        (createDaySkateUiStateModel.unlockTrickList.map { it.realName } + createDaySkateUiStateModel.unlockNewTrickList.map { it.realName } + createDaySkateUiStateModel.trickEnableList.map { it.realName }).toSet()
    }

    val typeStance = when (createDaySkateUiStateModel.currentRoomDay.getTypeActivityDay()) {
        TypeActivityDay.SWITCH_TRICKS -> TypeStanceTrick.SWITCH
        else -> TypeStanceTrick.NORMAL
    }

    var selectedStanceType by remember { mutableStateOf(typeStance) }

    val trickList = remember(registeredNames, selectedStanceType) {
        listTricks.map { trick ->
            val prefix = when (selectedStanceType) {
                TypeStanceTrick.NOLLIE -> "Nollie "
                TypeStanceTrick.FAKIE -> "Fakie "
                TypeStanceTrick.SWITCH -> "Switch "
                TypeStanceTrick.NORMAL -> ""
            }

            trick.copy(
                realName = "$prefix${trick.realName}",
                akaName = "$prefix${trick.akaName}"
            )
        }
            .filter { it.realName !in registeredNames }
    }


    val availableStances = when (createDaySkateUiStateModel.currentRoomDay.getTypeActivityDay()) {
        TypeActivityDay.SWITCH_TRICKS -> listOf(TypeStanceTrick.SWITCH, TypeStanceTrick.NOLLIE)
        else -> TypeStanceTrick.entries

    }

    /*SelectTrickScreen(
        trickList = trickList,
        limitSelected = limitSelected,
        availableStances = availableStances,
        selectedType = selectedStanceType,
        onTypeSelected = { selectedStanceType = it },
        onSendSelection = onSendSelection
    )*/

}





