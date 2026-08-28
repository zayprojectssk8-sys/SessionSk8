package com.zayprojetcs.weeksk8.utils

import android.content.Context
import com.google.android.gms.wearable.Wearable
import com.zaysk8.core.utils.MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP
import kotlinx.coroutines.tasks.await

suspend fun Context.sendConnectionPhoneToWatch(nodeId: String) {
    try {
        Wearable.getMessageClient(this)
            .sendMessage(nodeId, MESSAGE_PATH_PHONE_TO_WEAR_OPEN_APP, byteArrayOf())
            .await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}