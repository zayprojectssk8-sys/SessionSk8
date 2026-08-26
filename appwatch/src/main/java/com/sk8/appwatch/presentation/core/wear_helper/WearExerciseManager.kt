package com.sk8.appwatch.presentation.core.wear_helper

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import com.sk8.appwatch.presentation.core.model.MetricsSessionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class WearExerciseManager(val context: Context) {
    private val exerciseClient = HealthServices.getClient(context).exerciseClient
    private val messageSender = WearMessageSender(context)

    // Guardamos la referencia para poder limpiarla al detener
    private var activeCallback: ExerciseUpdateCallback? = null
    private var impactDetector: ImpactDetector? = null

    // Guardamos la marca de tiempo de inicio
    private var sessionStartTime: Instant? = null

    // --- CONTEO DE CAÍDAS ---
    private var fallCount = 0
    private val hrSamples = mutableListOf<Int>()


    // --- NUEVAS VARIABLES PARA CONTROL DE SEGMETOS Y TIEMPO ---
    private val ACTIVE_SPEED_THRESHOLD_KMH = 1.5f
    private val recordedSegments = mutableListOf<ExerciseSegment>()
    private var currentSegmentStartTime: Instant? = null
    private var isCurrentlyActive: Boolean = false
    // --------------------------------------------------------


    // 1. Verificar el estado del SDK antes de instanciar
    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    // 2. Inicialización diferida y segura (retorna null si no está disponible)
    private val healthConnectClient: HealthConnectClient? by lazy {
        if (isHealthConnectAvailable()) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }
    private var maxSpeedMs: Double = 0.0
    private val speedSamples = mutableListOf<Double>()


    fun startSkateSession(): Flow<MetricsSessionModel> = callbackFlow {
        val now = Instant.now()
        sessionStartTime = now

        // Inicializar primer segmento (comienza en reposo)
        currentSegmentStartTime = now
        isCurrentlyActive = false
        recordedSegments.clear()
        speedSamples.clear()
        maxSpeedMs = 0.0
        fallCount = 0

        // Iniciar detector de caídas con acelerómetro
        impactDetector = ImpactDetector(context) { gForce ->
            fallCount++
            Log.w(javaClass.simpleName, "Caída/Impacto #$fallCount registrado. Fuerza: ${gForce}G")

            // Transmitir alerta al teléfono
            CoroutineScope(Dispatchers.IO).launch {
                messageSender.sendImpactAlertToPhone(gForce)
            }
        }.also { detector ->
            detector.startListening()
        }

        val callback = object : ExerciseUpdateCallback {
            override fun onAvailabilityChanged(
                dataType: DataType<*, *>,
                availability: Availability
            ) {
                Log.wtf(javaClass.simpleName, " startSkateSession onAvailabilityChanged")
                // Opcional: Manejar cambios de disponibilidad del sensor (ej. si el reloj se afloja de la muñeca)
            }

            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                Log.wtf(javaClass.simpleName, " startSkateSession onExerciseUpdateReceived")
                val metrics = update.latestMetrics

                // Ritmo Cardíaco (BPM)
                val bpm = metrics.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value?.toInt() ?: 0

                // Guardar la lectura para el cálculo de TRIMP al final
                if (bpm > 0) {
                    hrSamples.add(bpm)
                }

                // Calorías (Acumuladas)
                val calories = metrics.getData(DataType.CALORIES_TOTAL)?.let { point ->
                    // Soporte según versión de SDK (.total o .value)
                    point.total
                } ?: 0.0

                // Velocidad en m/s (GPS)
                val currentSpeedMs = metrics.getData(DataType.SPEED).lastOrNull()?.value ?: 0.0
                val currentSpeedKmH = (currentSpeedMs * 3.6).toFloat()

                if (currentSpeedMs > 0.0) {
                    speedSamples.add(currentSpeedMs)
                    if (currentSpeedMs > maxSpeedMs) {
                        maxSpeedMs = currentSpeedMs
                    }
                }

                val avgSpeedMs = if (speedSamples.isNotEmpty()) speedSamples.average() else 0.0

                val distancePoints = metrics.getData(DataType.DISTANCE)
                val totalMeters = distancePoints.lastOrNull()?.value ?: 0.0

                // Conversión a kilómetros
                val totalKilometers = totalMeters / 1000.0

                // --- LOGICA DE TIEMPO ACTIVO VS REPOSO Y SEGMENTOS ---
                val currentTime = Instant.now()
                val isMoving = currentSpeedKmH >= ACTIVE_SPEED_THRESHOLD_KMH

                // Cambio de estado: Transición Activo <-> Reposo
                if (isMoving != isCurrentlyActive) {
                    currentSegmentStartTime?.let { startTime ->
                        recordedSegments.add(
                            ExerciseSegment(
                                startTime = startTime,
                                endTime = currentTime,
                                segmentType = if (isCurrentlyActive) {
                                    ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT
                                } else {
                                    ExerciseSegment.EXERCISE_SEGMENT_TYPE_REST
                                }
                            )
                        )
                    }
                    isCurrentlyActive = isMoving
                    currentSegmentStartTime = currentTime
                }

                // Calcular tiempo acumulado de los segmentos ya cerrados + el segmento actual en curso
                val (activeSecs, restSecs) = calculateAccumulatedTimes(currentTime)
                // -----------------------------------------------------

                trySend(
                    MetricsSessionModel(
                        bpm = bpm,
                        calories = calories,
                        currentSpeedKmH = currentSpeedKmH,
                        maxSpeedKmH = (maxSpeedMs * 3.6).toFloat(),
                        avgSpeedKmH = (avgSpeedMs * 3.6).toFloat(),
                        distanceMeters = totalMeters,
                        distanceKm = totalKilometers,
                        activeTimeSeconds = activeSecs,
                        restTimeSeconds = restSecs,
                        isCurrentlyActive = isCurrentlyActive,
                        fallCount = fallCount // Se actualiza dinámicamente
                    )
                )
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                Log.wtf(javaClass.simpleName, " startSkateSession onLapSummaryReceived")
                // Opcional: Manejar resúmenes de vueltas si los configuras
            }

            override fun onRegistered() {
                Log.wtf(javaClass.simpleName, " startSkateSession onRegistered")
                // Se ejecuta cuando el callback se registra correctamente en el ExerciseClient
            }

            override fun onRegistrationFailed(throwable: Throwable) {
                Log.wtf(
                    javaClass.simpleName,
                    " startSkateSession onRegistrationFailed ${throwable.localizedMessage}"
                )
                // Manejar errores de registro si llegaran a ocurrir
            }

        }

        activeCallback = callback

        val config = ExerciseConfig(
            exerciseType = ExerciseType.ROLLER_SKATING,
            dataTypes = setOf(
                DataType.HEART_RATE_BPM,
                DataType.CALORIES_TOTAL,
                DataType.SPEED,
                DataType.DISTANCE
            ),
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = true // Si no usas GPS en el reloj para ahorrar batería
        )

        // Registrar listener e iniciar ejercicio
        exerciseClient.setUpdateCallback(callback)
        exerciseClient.startExerciseAsync(config).await()

        awaitClose {
            impactDetector?.stopListening()
            impactDetector = null
        }
    }

    @SuppressLint("RestrictedApi")
    suspend fun stopSkateSession() {
        // Obtenemos la hora exacta de cierre y recuperamos el inicio
        val endTime = Instant.now()
        val startTime = sessionStartTime ?: return // Si no había sesión activa, finalizamos

        impactDetector?.stopListening()
        impactDetector = null

        // Cerrar el último segmento abierto
        currentSegmentStartTime?.let { segmentStart ->
            recordedSegments.add(
                ExerciseSegment(
                    startTime = segmentStart,
                    endTime = endTime,
                    segmentType = if (isCurrentlyActive) {
                        ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT
                    } else {
                        ExerciseSegment.EXERCISE_SEGMENT_TYPE_REST
                    }
                )
            )
        }

        // Detener sensores del reloj
        activeCallback?.let { callback ->
            exerciseClient.endExerciseAsync().await()
            exerciseClient.clearUpdateCallbackAsync(callback).await()
            activeCallback = null
        }

        // Calcular duración y promedios
        val durationMinutes = Duration.between(startTime, endTime).seconds / 60.0
        val avgBpm = if (hrSamples.isNotEmpty()) hrSamples.average() else 0.0

        // Obtener RHR desde Health Connect
        val rhr = getRestingHeartRate()

        // Llamar al calculador de TRIMP
        val finalTrimp = TrimpCalculator.calculate(
            durationMinutes = durationMinutes,
            avgBpm = avgBpm,
            restBpm = rhr
        )


        //  Crear Metadata con el parámetro entero obligatorio
        val skateRecord = ExerciseSessionRecord(
            startTime = startTime,
            startZoneOffset = ZoneId.systemDefault().rules.getOffset(startTime),
            endTime = endTime,
            endZoneOffset = ZoneId.systemDefault().rules.getOffset(endTime),
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT,
            title = "Sesión de Skate",
            notes = "Caídas: $fallCount | TRIMP: $finalTrimp | BPM Prom: ${avgBpm.toInt()}",
            segments = recordedSegments.toList(), // <--- AQUÍ SE GUARDAN LOS SEGMENTOS
            metadata = Metadata.activelyRecorded(
                device = Device(type = Device.TYPE_WATCH)
            )
        )

        healthConnectClient?.insertRecords(listOf(skateRecord))

        // Limpieza de estado
        sessionStartTime = null
        currentSegmentStartTime = null
        recordedSegments.clear()
        fallCount = 0
    }


    private suspend fun getRestingHeartRate(): Int {
        val client = healthConnectClient ?: return 60
        return try {
            // Referencia explícita a la clase de Jetpack androidx
            val request = ReadRecordsRequest(
                recordType = RestingHeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.after(Instant.now().minus(7, ChronoUnit.DAYS))
            )

            val response = client.readRecords(request)
            response.records.lastOrNull()?.beatsPerMinute?.toInt() ?: 60
        } catch (e: Exception) {
            Log.e("WearExerciseManager", "Error al leer RHR, usando fallback 60", e)
            60
        }
    }

    // Helper para sumar los segundos activos y en reposo
    private fun calculateAccumulatedTimes(now: Instant): Pair<Long, Long> {
        var activeSecs = 0L
        var restSecs = 0L

        // Sumar segmentos pasados
        for (segment in recordedSegments) {
            val duration = Duration.between(segment.startTime, segment.endTime).seconds
            if (segment.segmentType == ExerciseSegment.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT) {
                activeSecs += duration
            } else {
                restSecs += duration
            }
        }

        // Sumar tiempo transcurrido del segmento actual
        currentSegmentStartTime?.let { startTime ->
            val currentDuration = Duration.between(startTime, now).seconds
            if (isCurrentlyActive) {
                activeSecs += currentDuration
            } else {
                restSecs += currentDuration
            }
        }

        return Pair(activeSecs, restSecs)
    }
}