package com.zaysk8.core.data_store

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "sk8_session_prefs")