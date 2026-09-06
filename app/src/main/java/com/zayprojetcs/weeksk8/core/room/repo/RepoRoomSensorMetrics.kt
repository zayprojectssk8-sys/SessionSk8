package com.zayprojetcs.weeksk8.core.room.repo

import android.content.Context
import com.zayprojetcs.weeksk8.core.room.AppDatabase.Companion.roomSensorMetricsDb
import com.zayprojetcs.weeksk8.core.room.model.combine.CombinedMinuteMetrics
import kotlinx.coroutines.flow.Flow

fun Context.repoRoomGetSensorMetrics(
    sessionId: Long,
    phaseId: String
): Flow<List<CombinedMinuteMetrics>> {
    return roomSensorMetricsDb().getCombinedMetricsForPhase(sessionId, phaseId)
}
