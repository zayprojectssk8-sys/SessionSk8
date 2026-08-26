package com.zayprojetcs.weeksk8.screens.create_day_skate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.room.model.RoomDay
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import com.zayprojetcs.weeksk8.core.room.model.StatusDay
import com.zayprojetcs.weeksk8.core.room.model.StatusTrick
import com.zayprojetcs.weeksk8.core.room.model.TypeActivityDay
import com.zayprojetcs.weeksk8.core.room.model.TypeActivityDay.*
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetLastDaySkateByDate
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickFloor
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickGrindSlideUnlock
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickSwitchNollieUnlock
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickTriedUnlock
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickUnlock
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertDay
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertListTrick
import com.zayprojetcs.weeksk8.screens.create_day_skate.ui_state.CreateDaySkateUiState
import com.zayprojetcs.weeksk8.screens.create_day_skate.ui_state.model.CreateDaySkateUiStateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateDaySkateViewModel(application: Application) : AndroidViewModel(application) {


    private val _createDaySkateUiStateModel = MutableStateFlow(CreateDaySkateUiStateModel())
    val createDaySkateUiStateModel: StateFlow<CreateDaySkateUiStateModel> =
        _createDaySkateUiStateModel.asStateFlow()

    fun loadEvent(event: CreateDaySkateUiState) {
        when (event) {
            CreateDaySkateUiState.GetLastDaySkate -> getLastDaySkate()
            CreateDaySkateUiState.GetUnlockTrickList -> getUnlockTrickList()
            is CreateDaySkateUiState.AddUnlockTrick -> event.addUnlockTrick()
            is CreateDaySkateUiState.SetUnlockTrickSelectionList -> event.setUnlockTrickSelectionList()
            is CreateDaySkateUiState.SetTriedUnlockTrickSelectionList -> event.setTriedUnlockTrickSelectionList()
            CreateDaySkateUiState.GetTriedUnlockTrickList -> getTriedUnlockTrickList()
            CreateDaySkateUiState.GetTriedGrindSlideUnlockTrickList -> getTriedGrindSlideUnlockTrickList()
            is CreateDaySkateUiState.SetTriedUnlockGrindSlideTrickSelectionList -> event.setTriedUnlockGrindSlideTrickSelectionList()
            CreateDaySkateUiState.GetTriedSwitchNollieUnlockTrickList -> getTriedSwitchNollieUnlockTrickList()
            is CreateDaySkateUiState.SetTriedUnlockSwitchNollieTrickSelectionList -> event.setTriedUnlockSwitchNollieTrickSelectionList()
            CreateDaySkateUiState.RegisterDay -> registerDay()
        }
    }

    private fun registerDay() = viewModelScope.launch(Dispatchers.IO) {

        createDaySkateUiStateModel.value.apply {

            currentRoomDay ?: return@launch

            val listTrick = when (currentRoomDay.getTypeActivityDay()) {
                REVIEWING_FLOOR_TRICKS -> unlockNewTrickList
                UNLOCKED_FLOOR_TRICKS -> unlockNewTriedTrickList
                GRIND_AND_SLIDE_TRICKS -> unlockNewTriedGrindSlideTrickList
                SWITCH_TRICKS -> unlockNewTriedSwitchNollieTrickList
            }

            application.repoRoomInsertListTrick(listTrick)

            application.repoRoomInsertDay(currentRoomDay)
        }
    }

    private fun CreateDaySkateUiState.AddUnlockTrick.addUnlockTrick() {
        _createDaySkateUiStateModel.update { itUpdate ->
            itUpdate.copy(showViewAddUnlockTrick = showView)
        }

    }

    private fun CreateDaySkateUiState.SetUnlockTrickSelectionList.setUnlockTrickSelectionList() =
        viewModelScope.launch(Dispatchers.IO) {
            val listRoomTrick = unlockTrickList.map { trick ->

                RoomTrick(
                    realName = trick.realName,
                    akaName = trick.akaName,
                    difficulty = trick.getRealDifficulty(),
                    typeTrick = trick.typeTrick,
                    status = StatusTrick.UNLOCK.status,
                    dateUnlock = Calendar.getInstance().timeInMillis
                )
            }

            _createDaySkateUiStateModel.update { itUpdate ->
                itUpdate.copy(
                    unlockNewTrickList = itUpdate.unlockNewTrickList + listRoomTrick
                )
            }
        }

    private fun CreateDaySkateUiState.SetTriedUnlockTrickSelectionList.setTriedUnlockTrickSelectionList() =
        viewModelScope.launch(Dispatchers.IO) {
            val listRoomTrick = unlockTriedTrickList.map { trick ->
                RoomTrick(
                    realName = trick.realName,
                    akaName = trick.akaName,
                    difficulty = trick.getRealDifficulty(),
                    typeTrick = trick.typeTrick,
                    status = StatusTrick.TRYING.status
                )
            }

            _createDaySkateUiStateModel.update { itUpdate ->
                itUpdate.copy(
                    unlockNewTriedTrickList = itUpdate.unlockNewTrickList + listRoomTrick
                )
            }
        }


    private fun CreateDaySkateUiState.SetTriedUnlockGrindSlideTrickSelectionList.setTriedUnlockGrindSlideTrickSelectionList() =
        viewModelScope.launch(Dispatchers.IO) {
            val listRoomTrick = unlockGrindSlideTrickList.map { trick ->
                RoomTrick(
                    realName = trick.realName,
                    akaName = trick.akaName,
                    difficulty = trick.getRealDifficulty(),
                    typeTrick = trick.typeTrick,
                    status = StatusTrick.TRYING.status
                )
            }

            _createDaySkateUiStateModel.update { itUpdate ->
                itUpdate.copy(
                    unlockNewTriedGrindSlideTrickList = itUpdate.unlockNewTrickList + listRoomTrick
                )
            }
        }

    private fun CreateDaySkateUiState.SetTriedUnlockSwitchNollieTrickSelectionList.setTriedUnlockSwitchNollieTrickSelectionList() =
        viewModelScope.launch(Dispatchers.IO) {
            val listRoomTrick = unlockSwitchNollieTrickList.map { trick ->
                RoomTrick(
                    realName = trick.realName,
                    akaName = trick.akaName,
                    difficulty = trick.getRealDifficulty(),
                    typeTrick = trick.typeTrick,
                    status = StatusTrick.TRYING.status
                )
            }

            _createDaySkateUiStateModel.update { itUpdate ->
                itUpdate.copy(
                    unlockNewTriedSwitchNollieTrickList = itUpdate.unlockNewTrickList + listRoomTrick
                )
            }
        }

    private fun getUnlockTrickList() =
        viewModelScope.launch(Dispatchers.IO) {
            val unlockTrick = application.repoRoomGetListTrickUnlock().filterNot { it.realName.contains("Switch") || it.realName.contains("Nollie") }
            _createDaySkateUiStateModel.update { itUpdate ->
                itUpdate.copy(unlockTrickList = unlockTrick)
            }
        }

    private fun getTriedUnlockTrickList() =
        viewModelScope.launch(Dispatchers.IO) {
            val unlockTrick = application.repoRoomGetListTrickTriedUnlock().filterNot { it.realName.contains("Switch") || it.realName.contains("Nollie") }
            _createDaySkateUiStateModel.update { itUpdate ->
                itUpdate.copy(unlockTriedTrickList = unlockTrick)
            }
        }

    private fun getTriedGrindSlideUnlockTrickList() =
        viewModelScope.launch(Dispatchers.IO) {
            val unlockTrick = application.repoRoomGetListTrickGrindSlideUnlock().filterNot { it.realName.contains("Switch") || it.realName.contains("Nollie") }
            _createDaySkateUiStateModel.update { itUpdate ->
                itUpdate.copy(unlockTriedGrindSlideTrickList = unlockTrick)
            }
        }

    private fun getTriedSwitchNollieUnlockTrickList() =
        viewModelScope.launch(Dispatchers.IO) {
            val unlockTrick = application.repoRoomGetListTrickSwitchNollieUnlock().filter { it.realName.contains("Switch") || it.realName.contains("Nollie") }
            _createDaySkateUiStateModel.update { itUpdate ->
                itUpdate.copy(unlockTriedSwitchNollieTrickList = unlockTrick)
            }
        }

    private fun getLastDaySkate() = viewModelScope.launch(Dispatchers.IO) {
        val lastDaySkate = application.repoRoomGetLastDaySkateByDate()

        lastDaySkate.let { itLastDay ->
            itLastDay ?: return@let
            if (itLastDay.status == StatusDay.CREATED.status) {
                _createDaySkateUiStateModel.update { itUpdate ->
                    itUpdate.copy(currentRoomDay = lastDaySkate)
                }
                return@launch
            }
        }

        val typeActivityDay =
            lastDaySkate?.getNextTypeDay() ?: REVIEWING_FLOOR_TRICKS.numDay

        val roomDay = RoomDay(
            status = StatusDay.CREATED.status,
            createDate = Calendar.getInstance().timeInMillis,
            typeActivityDay = typeActivityDay
        )

        getTricksEnables(roomDay.getTypeActivityDay())

        _createDaySkateUiStateModel.update { itUpdate ->
            itUpdate.copy(currentRoomDay = roomDay)
        }

    }

    private fun getTricksEnables(typeActivityDay: TypeActivityDay) {

        val dayTrickList = when (typeActivityDay) {
            REVIEWING_FLOOR_TRICKS,
            UNLOCKED_FLOOR_TRICKS -> application.repoRoomGetListTrickFloor()

            GRIND_AND_SLIDE_TRICKS -> application.repoRoomGetListTrickGrindSlideUnlock()
            SWITCH_TRICKS -> application.repoRoomGetListTrickSwitchNollieUnlock()
        }

        _createDaySkateUiStateModel.update { itUpdate ->
            itUpdate.copy(trickEnableList = dayTrickList)
        }

    }


}