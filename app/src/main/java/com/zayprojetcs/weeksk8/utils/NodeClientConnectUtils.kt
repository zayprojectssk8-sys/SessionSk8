package com.zayprojetcs.weeksk8.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun Context.sendConnectionPhoneToWatch(nodeId: String) {
    try {
        Wearable.getMessageClient(this)
            .sendMessage(nodeId, "/open_app", byteArrayOf())
            .await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}