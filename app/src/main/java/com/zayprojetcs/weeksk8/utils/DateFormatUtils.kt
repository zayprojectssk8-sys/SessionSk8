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