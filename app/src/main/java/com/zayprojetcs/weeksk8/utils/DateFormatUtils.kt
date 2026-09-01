package com.zayprojetcs.weeksk8.utils

import java.util.Locale

const val DATE_FORMAT_dd_MM_yyyy_HH_mm_ss = "dd/MM/yyyy HH:mm:ss"

fun String.dateTimeFormat(lastLoginMillis: Long?): String {
    lastLoginMillis ?: return "fecha nula"
    // Convertir el timestamp (Long) a una fecha legible
    val date = java.util.Date(lastLoginMillis)
    val format = java.text.SimpleDateFormat(this, Locale.getDefault())
    val lastLoginFormatted = format.format(date)
    return lastLoginFormatted
}

/**
 * Convierte segundos totales a formato "MM:SS" (Ej: 05:30, 12:05)
 */
fun formatSecondsToMMSS(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

/**
 * Convierte segundos totales a formato "HH:MM:SS" para el temporizador general (Ej: 01:15:30)
 */
fun formatSecondsToHHMMSS(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds)
}