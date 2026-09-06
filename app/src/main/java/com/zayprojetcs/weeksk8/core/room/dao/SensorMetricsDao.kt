package com.zayprojetcs.weeksk8.core.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.zayprojetcs.weeksk8.core.room.model.combine.CombinedMinuteMetrics
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorMetricsDao {

    @Query("""
        SELECT 
            acc.sessionId AS sessionId,
            acc.phaseId AS phaseId,
            acc.minuteIndex AS minuteIndex,
            
            -- Acelerómetro
            acc.minImpactG AS minImpactG,
            acc.maxImpactG AS maxImpactG,
            acc.minBodyAccel AS minBodyAccel,
            acc.maxBodyAccel AS maxBodyAccel,
            acc.minVibration AS minVibration,
            acc.maxVibration AS maxVibration,
            
            -- Activity Recognition
            act.detectedActivity AS detectedActivity,
            act.maxConfidence AS maxActivityConfidence,
            
            -- Barómetro
            bar.minPressureHpa AS minPressureHpa,
            bar.maxPressureHpa AS maxPressureHpa,
            bar.minRelativeAltitudeM AS minRelativeAltitudeM,
            bar.maxRelativeAltitudeM AS maxRelativeAltitudeM,
            
            -- GPS
            gps.minSpeedKmh AS minSpeedKmh,
            gps.maxSpeedKmh AS maxSpeedKmh,
            gps.totalDistanceMeters AS totalDistanceMeters,
            gps.maxAccuracyMeters AS gpsAccuracyMeters,
            
            -- Giroscopio
            gyr.minRotationDps AS minRotationDps,
            gyr.maxRotationDps AS maxRotationDps,
            gyr.avgRotationDps AS avgRotationDps,
            
            -- Magnetómetro
            mag.minFieldMicroTesla AS minFieldMicroTesla,
            mag.maxFieldMicroTesla AS maxFieldMicroTesla,
            mag.maxHeadingDegrees AS maxHeadingDegrees,
            
            -- Step Detector
            stp.stepCount AS stepCount,
            stp.maxCadenceSpm AS maxCadenceSpm
            
        FROM accel_minute_metrics acc
        LEFT JOIN activity_minute_metrics act 
            ON acc.sessionId = act.sessionId AND acc.phaseId = act.phaseId AND acc.minuteIndex = act.minuteIndex
        LEFT JOIN baro_minute_metrics bar 
            ON acc.sessionId = bar.sessionId AND acc.phaseId = bar.phaseId AND acc.minuteIndex = bar.minuteIndex
        LEFT JOIN gps_minute_metrics gps 
            ON acc.sessionId = gps.sessionId AND acc.phaseId = gps.phaseId AND acc.minuteIndex = gps.minuteIndex
        LEFT JOIN gyro_minute_metrics gyr 
            ON acc.sessionId = gyr.sessionId AND acc.phaseId = gyr.phaseId AND acc.minuteIndex = gyr.minuteIndex
        LEFT JOIN mag_minute_metrics mag 
            ON acc.sessionId = mag.sessionId AND acc.phaseId = mag.phaseId AND acc.minuteIndex = mag.minuteIndex
        LEFT JOIN step_minute_metrics stp 
            ON acc.sessionId = stp.sessionId AND acc.phaseId = stp.phaseId AND acc.minuteIndex = stp.minuteIndex
            
        WHERE acc.sessionId = :sessionId AND acc.phaseId = :phaseId
        ORDER BY acc.minuteIndex ASC
    """)
    fun getCombinedMetricsForPhase(sessionId: Long, phaseId: String): Flow<List<CombinedMinuteMetrics>>
}