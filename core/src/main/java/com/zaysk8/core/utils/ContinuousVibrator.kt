package com.zaysk8.core.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class ContinuousVibrator(private val context: Context) {
private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun startContinuousVibration() {
        if (!vibrator.hasVibrator()) return

        // Patrón: [espera 0ms, vibra 600ms, silencio 400ms], repeat = 0 (bucle infinito)
        val pattern = longArrayOf(0, 600, 400)

        // Definir atributos para que el sistema trate la vibración como ALARMA/ALERTA (evita que se bloquee en silencioso)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0) // 0 = repetir en bucle
            vibrator.vibrate(effect, audioAttributes)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }

    fun stopVibration() {
        if (vibrator.hasVibrator()) {
            vibrator.cancel()
        }
    }
}