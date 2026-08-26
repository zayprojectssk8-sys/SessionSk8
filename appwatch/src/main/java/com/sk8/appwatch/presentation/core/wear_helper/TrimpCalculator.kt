package com.sk8.appwatch.presentation.core.wear_helper

object TrimpCalculator {

    fun calculate(
        durationMinutes: Double,
        avgBpm: Double,
        restBpm: Int,
        userAge: Int = 25
    ): Int {
        val maxBpm = (220 - userAge).toDouble()
        if (avgBpm <= restBpm || maxBpm <= restBpm) return 0

        val hrr = ((avgBpm - restBpm) / (maxBpm - restBpm)).coerceIn(0.0, 1.0)
        val trimpValue = durationMinutes * hrr * Math.exp(1.92 * hrr)
        return trimpValue.toInt()
    }
}