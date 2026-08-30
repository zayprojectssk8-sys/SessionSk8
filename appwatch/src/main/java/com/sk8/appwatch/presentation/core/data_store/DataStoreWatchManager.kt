package com.sk8.appwatch.presentation.core.data_store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zaysk8.core.data_store.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreWatchManager(private val context: Context) {

    companion object {
        val PAIRED_PHONE_DEVICE_KEY = stringPreferencesKey("paired_phone_device")
    }

    suspend fun saveDeviceConnectNodeId(nodeId: String) {
        context.dataStore.edit { preferences ->
            preferences[PAIRED_PHONE_DEVICE_KEY] = nodeId
        }
    }

    /** Flow que deserializa los datos del estado de validacion de la cuenta */
    val deviceConnectNodeId: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[PAIRED_PHONE_DEVICE_KEY]
        }

    suspend fun clearDeviceConnectNodeId() {
        context.dataStore.edit { prefs ->
            prefs.remove(PAIRED_PHONE_DEVICE_KEY)
        }
    }
}