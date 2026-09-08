package com.zaysk8.core.utils

import java.util.Locale

fun getFormatDurationSession(timeMinutes: Int): String {
// Formateo dinámico del texto de duración
    return when (timeMinutes) {
        0 -> "Abierta (Sin límite)"
        30 -> "30 min"
        60 -> "1 hora"
        else -> {
            if (timeMinutes % 60 == 0) {
                "${timeMinutes / 60} horas"
            } else {
                val hours = timeMinutes / 60f
                val formattedHours =String.format(Locale.getDefault(), "%.1f", hours)
                "$formattedHours horas"
            }
        }
    }

}

fun String.formatLocale(vararg args: Any?): String {
    return String.format(Locale.getDefault(), this, args)
}

