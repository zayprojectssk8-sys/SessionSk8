package com.zayprojetcs.weeksk8.screens.start_day_skate

import android.app.Application
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.zayprojetcs.weeksk8.core.room.model.RoomRoundTrick
import com.zayprojetcs.weeksk8.core.room.model.StatusDay
import com.zayprojetcs.weeksk8.core.room.model.StatusRoundTrick
import com.zayprojetcs.weeksk8.core.room.model.StatusTrick
import com.zayprojetcs.weeksk8.core.room.model.TypeActivityDay
import com.zayprojetcs.weeksk8.core.room.model_relation.RoundTrick
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickGrindSlideUnlock
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickSwitchNollieUnlock
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickTriedUnlock
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetListTrickUnlock
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomGetStartDayFlow
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertListRoundTrick
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomInsertRoundTrick
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomUpdateTrick
import com.zayprojetcs.weeksk8.core.room.repo.repoRoomUpdateDay
import com.zayprojetcs.weeksk8.screens.start_day_skate.ui_state.StartDaySkateUiState
import com.zayprojetcs.weeksk8.screens.start_day_skate.ui_state.model.StartDaySkateUiStateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StartDaySkateViewModel(application: Application) : AndroidViewModel(application) {
    val dayWithRoundsTrick = application.repoRoomGetStartDayFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _startDaySkateUiState = MutableStateFlow(StartDaySkateUiStateModel())
    val startDaySkateUiState: StateFlow<StartDaySkateUiStateModel> =
        _startDaySkateUiState.asStateFlow()


    fun loadEvent(event: StartDaySkateUiState) {
        when (event) {
            StartDaySkateUiState.GetTricksDay -> getTricksDay()
            StartDaySkateUiState.StartDay -> startDay()
            StartDaySkateUiState.StopDay -> stopDay()
            is StartDaySkateUiState.SendRoundTrick -> event.sendRoundTrick()
            StartDaySkateUiState.CreateRound -> createRound()
            StartDaySkateUiState.CreateNextRound -> createNextRound()
        }
    }

    private fun StartDaySkateUiState.SendRoundTrick.sendRoundTrick() =
        viewModelScope.launch(Dispatchers.IO) {
            val trick = roundTrick.roomTrick

            val attempts = roundTrick.roomRoundTrick.attempts

            var countTriedSuccess = 0
            var countTriedFail = 0

            when (numTried) {
                0 -> countTriedFail = attempts
                else -> {
                    countTriedFail = numTried.minus(1)
                    countTriedSuccess = numTried
                }
            }

            val roundUpdate =
                roundTrick.roomRoundTrick.copy(
                    status = StatusRoundTrick.CLOSED.status,
                    countTriedSuccess = countTriedSuccess,
                    countTriedFail = countTriedFail
                )

            trick.apply {
                this ?: return@apply

                var startDateTrying = this.startDateTrying
                var countTriedAttempts = this.countTriedAttempts
                var status = this.status
                var dateUnlock = this.dateUnlock
                var countUnlockTriedFail = this.countUnlockTriedFail
                var countUnlockTriedSuccess = this.countUnlockTriedSuccess

                when (this.getStatusTrick()) {
                    StatusTrick.LOCK -> {

                    }

                    StatusTrick.TRYING -> {
                        if (startDateTrying == 0L) {
                            startDateTrying = Calendar.getInstance().timeInMillis
                        }

                        countTriedAttempts = countTriedAttempts.plus(countTriedFail)

                        if (countTriedSuccess > 0) {

                            dayWithRoundsTrick.value?.roomDay.let { itDay ->
                                itDay ?: return@let
                                if (itDay.getTypeActivityDay() == TypeActivityDay.GRIND_AND_SLIDE_TRICKS || itDay.getTypeActivityDay() == TypeActivityDay.SWITCH_TRICKS) {
                                    status = StatusTrick.UNLOCK.status
                                }
                            }

                            dateUnlock = Calendar.getInstance().timeInMillis
                        }
                    }

                    StatusTrick.UNLOCK -> {
                        countUnlockTriedFail = countUnlockTriedFail.plus(countTriedFail)
                        countUnlockTriedSuccess = countUnlockTriedSuccess.plus(1)
                    }
                }

                val trickUpdate = this.copy(
                    startDateTrying = startDateTrying,
                    countTriedAttempts = countTriedAttempts,
                    status = status,
                    dateUnlock = dateUnlock,
                    countUnlockTriedFail = countUnlockTriedFail,
                    countUnlockTriedSuccess = countUnlockTriedSuccess
                )

                application.repoRoomUpdateTrick(trickUpdate)

            }

            application.repoRoomInsertRoundTrick(roundUpdate)
        }

    private fun createRound() =
        viewModelScope.launch(Dispatchers.IO) {

            dayWithRoundsTrick.value.apply {
                this ?: return@apply

                _startDaySkateUiState.value.let { itUiState ->

                    if (itUiState.dayTrickList.isNotEmpty()) {

                        val listRoundTrick = itUiState.dayTrickList.map { trick ->
                            RoomRoundTrick(
                                idOwnerDay = this.roomDay.idDay,
                                idOwnerTrick = trick.idTrick,
                                status = StatusRoundTrick.STARTED.status,
                                round = this.roomDay.countRound
                            )
                        }

                        try {
                            application.repoRoomInsertListRoundTrick(listRoundTrick)
                        } catch (e: Exception) {
                            Log.e(javaClass.simpleName, "Error ${e.localizedMessage}")
                        }

                    }

                }


            }
        }

    private fun startDay() =
        viewModelScope.launch(Dispatchers.IO) {
            dayWithRoundsTrick.value.apply {
                this ?: return@apply


                val updateDay = this.roomDay.copy(
                    status = StatusDay.STARTED.status,
                    startDate = Calendar.getInstance().timeInMillis,
                    countRound = 1
                )

                application.repoRoomUpdateDay(updateDay)
            }
        }

    private fun createNextRound() =
        viewModelScope.launch(Dispatchers.IO) {
            dayWithRoundsTrick.value.apply {
                this ?: return@apply

                val updateDay = this.roomDay.copy(
                    countRound = this.roomDay.countRound.plus(1)
                )

                application.repoRoomUpdateDay(updateDay)
            }
        }

    private fun validateStatusTrick(roomRoundTrickList: List<RoundTrick>) =
        viewModelScope.launch(Dispatchers.IO) {
            roomRoundTrickList
                .mapNotNull { it.roomTrick }
                .filter { it.getStatusTrick() == StatusTrick.TRYING && it.dateUnlock != 0L }
                .forEach { trick ->

                    val trickUpdate = trick.copy(status = StatusTrick.UNLOCK.status)

                    try {
                        application.repoRoomUpdateTrick(trickUpdate)
                    } catch (e: Exception) {
                        Log.e(
                            "SK8_UPDATE",
                            "Error al actualizar truco ${trickUpdate.idTrick}: ${e.message}"
                        )
                    }
                }
        }

    private fun stopDay() =
        viewModelScope.launch(Dispatchers.IO) {
            dayWithRoundsTrick.value.apply {
                this ?: return@apply

                if (this.roomDay.getTypeActivityDay() == TypeActivityDay.UNLOCKED_FLOOR_TRICKS) {
                    validateStatusTrick(this.roomRoundTrickList)
                }


                val updateDay = this.roomDay.copy(
                    status = StatusDay.CLOSED.status,
                    closeDate = Calendar.getInstance().timeInMillis
                )

                application.repoRoomUpdateDay(updateDay)

            }
        }

    private fun getTricksDay() =
        viewModelScope.launch(Dispatchers.IO) {

            dayWithRoundsTrick.value.apply {
                this ?: return@apply

                val dayTrickList = when (this.roomDay.getTypeActivityDay()) {
                    TypeActivityDay.REVIEWING_FLOOR_TRICKS -> application.repoRoomGetListTrickUnlock()
                        .filterNot { it.realName.contains("Switch") || it.realName.contains("Nollie") }

                    TypeActivityDay.UNLOCKED_FLOOR_TRICKS -> application.repoRoomGetListTrickTriedUnlock()
                        .filterNot { it.realName.contains("Switch") || it.realName.contains("Nollie") }

                    TypeActivityDay.GRIND_AND_SLIDE_TRICKS -> application.repoRoomGetListTrickGrindSlideUnlock()
                        .filterNot { it.realName.contains("Switch") || it.realName.contains("Nollie") }

                    TypeActivityDay.SWITCH_TRICKS -> application.repoRoomGetListTrickSwitchNollieUnlock()
                        .filter { it.realName.contains("Switch") || it.realName.contains("Nollie") }
                }

                _startDaySkateUiState.update { itUpdate ->
                    itUpdate.copy(dayTrickList = dayTrickList)
                }
            }
        }


}