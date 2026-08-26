package com.zayprojetcs.weeksk8.core.data_store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zayprojetcs.weeksk8.utils.DetectedWearable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "wearable_prefs")

class WearablePreferences(private val context: Context) {

    companion object {
        fun Context.wearablePreferencesInstance() = WearablePreferences(this)
        val PAIRED_DEVICE_KEY = stringPreferencesKey("paired_device")
    }


    suspend fun saveDeviceConnectBluetooth(data: DetectedWearable) {
        context.dataStore.edit { preferences ->
            preferences[PAIRED_DEVICE_KEY] = Json.encodeToString(data)
        }
    }

    /** Flow que deserializa los datos del estado de validacion de la cuenta */
    val deviceConnectBluetooth: Flow<DetectedWearable?> =
        context.dataStore.data.map { preferences ->
            val json = preferences[PAIRED_DEVICE_KEY]
            if (json != null) Json.decodeFromString<DetectedWearable>(json)
            else null
        }

    suspend fun clearDeviceConnectBluetooth() {
        context.dataStore.edit { prefs ->
            prefs.remove(PAIRED_DEVICE_KEY)
        }
    }
}