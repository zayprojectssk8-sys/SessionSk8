package com.zaysk8.core.helper

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.zaysk8.core.model.SkateSessionPhase
import com.zaysk8.core.model.SkateSessionStateModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class SessionSyncManager(context: Context) {

    private val appContext = context.applicationContext
    private val dataClient: DataClient by lazy { Wearable.getDataClient(appContext) }
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(appContext) }

    companion object {
        private const val TAG = "SessionSyncManager"
        private const val SESSION_SYNC_PATH = "/skate_session/state_sync"
    }

    /**
     * Comprueba si la API de Wearable está disponible y si existe al menos
     * un reloj Wear OS actualmente emparejado y conectado.
     */
    suspend fun isWearableConnected(): Boolean {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            nodes.isNotEmpty()
        } catch (e: ApiException) {
            Log.w(TAG, "Wearable API no disponible en este dispositivo (código ${e.statusCode}): ${e.message}")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Error verificando nodos de Wear OS: ${e.message}")
            false
        }
    }

    /**
     * Escucha reactivamente las actualizaciones enviadas desde el Celular o el Reloj.
     * Si no hay reloj conectado, cierra el flujo de forma limpia.
     */
    fun observeSessionState(): Flow<Pair<SkateSessionStateModel, SyncTimestamps>> = callbackFlow {
        if (!isWearableConnected()) {
            Log.i(TAG, "Sin dispositivos Wear OS conectados. Cancelando suscripción.")
            close()
            return@callbackFlow
        }

        val listener = DataClient.OnDataChangedListener { dataEvents ->
            for (event in dataEvents) {
                if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == SESSION_SYNC_PATH) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val parsedData = parseDataMap(dataMap)
                    trySend(parsedData)
                }
            }
        }

        try {
            dataClient.addListener(listener)

            // Lectura inicial del último estado persistido
            fetchCurrentState { initialData ->
                if (initialData != null) trySend(initialData)
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Error al registrar listener de Wear OS: ${e.message}")
            close(e)
            return@callbackFlow
        }

        awaitClose {
            try {
                dataClient.removeListener(listener)
            } catch (e: Exception) {
                // Previene crashes al desuscribir si la API no está disponible
            }
        }
    }.catch { e ->
        Log.w(TAG, "Excepción capturada en observeSessionState: ${e.message}")
    }

    /**
     * Publica el nuevo estado a DataClient únicamente si existe un dispositivo conectado.
     */
    suspend fun updateSessionState(
        state: SkateSessionStateModel,
        timestamps: SyncTimestamps = SyncTimestamps()
    ) {
        if (!isWearableConnected()) return

        try {
            val putDataMapReq = PutDataMapRequest.create(SESSION_SYNC_PATH).apply {
                dataMap.putBoolean("is_running", state.isRunning)
                dataMap.putBoolean("is_paused", state.isPaused)
                dataMap.putBoolean("is_session_started", state.isSessionStarted)
                dataMap.putBoolean("is_waiting_manual_start", state.isWaitingManualStart)
                dataMap.putString("current_phase", state.currentPhase.name)
                dataMap.putInt("current_round", state.currentRound)
                dataMap.putInt("total_rounds", state.totalRounds)
                dataMap.putLong("general_elapsed_time_sec", state.generalElapsedTimeSec)
                dataMap.putLong("phase_time_remaining_sec", state.phaseTimeRemainingSec)
                dataMap.putLong("phase_total_duration_sec", state.phaseTotalDurationSec)

                // Timestamps absolutos para evitar desincronización de cronómetros
                dataMap.putLong("overall_start_time_ms", timestamps.overallStartTimeMs)
                dataMap.putLong("phase_start_time_ms", timestamps.phaseStartTimeMs)
                dataMap.putLong("updated_at", System.currentTimeMillis())
            }

            val request = putDataMapReq.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()
        } catch (e: ApiException) {
            Log.e(TAG, "Error de la API de Wear OS al publicar estado (${e.statusCode}): ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado al publicar estado: ${e.message}")
        }
    }

    private fun fetchCurrentState(onResult: (Pair<SkateSessionStateModel, SyncTimestamps>?) -> Unit) {
        dataClient.getDataItems("wear://$SESSION_SYNC_PATH".toUri())
            .addOnSuccessListener { items ->
                val item = items.firstOrNull()
                if (item != null) {
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    onResult(parseDataMap(dataMap))
                } else {
                    onResult(null)
                }
                items.release()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "No se pudo consultar el estado inicial: ${e.message}")
                onResult(null)
            }
    }

    private fun parseDataMap(dataMap: DataMap): Pair<SkateSessionStateModel, SyncTimestamps> {
        val phaseName = dataMap.getString("current_phase") ?: SkateSessionPhase.WARMUP.name
        val phase = try {
            SkateSessionPhase.valueOf(phaseName)
        } catch (_: Exception) {
            SkateSessionPhase.WARMUP
        }

        val model = SkateSessionStateModel(
            isRunning = dataMap.getBoolean("is_running"),
            isPaused = dataMap.getBoolean("is_paused"),
            isSessionStarted = dataMap.getBoolean("is_session_started"),
            isWaitingManualStart = dataMap.getBoolean("is_waiting_manual_start"),
            currentPhase = phase,
            currentRound = dataMap.getInt("current_round"),
            totalRounds = dataMap.getInt("total_rounds"),
            generalElapsedTimeSec = dataMap.getLong("general_elapsed_time_sec"),
            phaseTimeRemainingSec = dataMap.getLong("phase_time_remaining_sec"),
            phaseTotalDurationSec = dataMap.getLong("phase_total_duration_sec")
        )

        val timestamps = SyncTimestamps(
            overallStartTimeMs = dataMap.getLong("overall_start_time_ms"),
            phaseStartTimeMs = dataMap.getLong("phase_start_time_ms")
        )

        return Pair(model, timestamps)
    }
}

/**
 * Estructura auxiliar para guardar tiempos de referencia absolutos en milisegundos.
 */
data class SyncTimestamps(
    val overallStartTimeMs: Long = 0L,
    val phaseStartTimeMs: Long = 0L
)