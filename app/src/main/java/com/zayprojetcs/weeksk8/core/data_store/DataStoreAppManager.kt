package com.zayprojetcs.weeksk8.core.data_store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zayprojetcs.weeksk8.utils.DetectedWearable
import com.zaysk8.core.data_store.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json


class DataStoreAppManager(private val context: Context) {

    companion object {
        val PAIRED_WEAR_DEVICE_KEY = stringPreferencesKey("paired_wear_device")
    }


    suspend fun saveDeviceConnectBluetooth(data: DetectedWearable) {
        context.dataStore.edit { preferences ->
            preferences[PAIRED_WEAR_DEVICE_KEY] = Json.encodeToString(data)
        }
    }

    /** Flow que deserializa los datos del estado de validacion de la cuenta */
    val deviceConnectBluetooth: Flow<DetectedWearable?> =
        context.dataStore.data.map { preferences ->
            val json = preferences[PAIRED_WEAR_DEVICE_KEY]
            if (json != null) Json.decodeFromString<DetectedWearable>(json)
            else null
        }

    suspend fun clearDeviceConnectBluetooth() {
        context.dataStore.edit { prefs ->
            prefs.remove(PAIRED_WEAR_DEVICE_KEY)
        }
    }
}