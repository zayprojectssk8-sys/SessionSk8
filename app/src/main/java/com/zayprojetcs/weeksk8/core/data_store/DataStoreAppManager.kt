package com.zayprojetcs.weeksk8.core.data_store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.zaysk8.core.data_store.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class DataStoreAppManager(private val context: Context) {

    companion object {
        val CURRENT_ID_SESSION_KEY = longPreferencesKey("currentIdSession")
    }


    suspend fun saveCurrentIdSession(data: Long) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_ID_SESSION_KEY] = data
        }
    }

    /** Flow que deserializa los datos del estado de validacion de la cuenta */
    val currentIdSession: Flow<Long?> =
        context.dataStore.data.map { preferences ->
            preferences[CURRENT_ID_SESSION_KEY]
        }

    suspend fun clearDeviceConnectBluetooth() {
        context.dataStore.edit { prefs ->
            prefs.remove(CURRENT_ID_SESSION_KEY)
        }
    }
}