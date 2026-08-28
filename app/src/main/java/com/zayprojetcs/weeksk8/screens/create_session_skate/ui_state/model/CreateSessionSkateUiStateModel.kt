package com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import com.zayprojetcs.weeksk8.utils.DetectedWearable
import com.zaysk8.core.model.TFMTrick


enum class TrickSelectionMode(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    FREE(
        "Sesión libre",
        "Rueda sin metas fijas ni trucos asignados",
        Icons.AutoMirrored.Filled.DirectionsRun
    ),
    CUSTOM(
        "Trucos definidos",
        "Elige exactamente qué trucos quieres practicar",
        Icons.Default.FormatListNumbered
    ),
    RANDOM_UNLOCKED(
        "Trucos al azar",
        "La app elegirá trucos aleatorios de tus desbloqueados",
        Icons.Default.Shuffle
    )
}


enum class RoundPreset(
    val title: String,
    val description: String,
    val skateMinutes: Int,
    val restMinutes: Int,
    val icon: ImageVector,
    val isRecommended: Boolean = false
) {
    TECNICO(
        title = "Técnico / Líneas",
        description = "Bloques cortos de alta precisión para combos en spot o trucos específicos.",
        skateMinutes = 10,
        restMinutes = 3,
        icon = Icons.Default.PrecisionManufacturing
    ),
    ESTANDAR(
        title = "Estándar",
        description = "Equilibrio óptimo entre desgaste físico e hidratación para calle o park.",
        skateMinutes = 15,
        restMinutes = 5,
        icon = Icons.Default.FitnessCenter,
        isRecommended = true
    ),
    RESISTENCIA(
        title = "Resistencia / Flow",
        description = "Sesiones continuas ideales para bowl, rampas o tránsito fluido.",
        skateMinutes = 20,
        restMinutes = 5,
        icon = Icons.Default.Speed
    );

    val totalRoundMinutes: Int get() = skateMinutes + restMinutes
}

enum class TrickDistributionMode(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    ROUNDS_PER_TRICK(
        "Rondas divididas por truco",
        "Asigna bloques de rondas específicas para practicar cada truco",
        Icons.Outlined.PieChart
    ),
    ALL_TRICKS_PER_ROUND(
        "Todos los trucos por ronda",
        "En cada ronda intentarás todos los trucos de tu lista",
        Icons.Default.AllInclusive
    )
}

enum class TrickOrderMode(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    SEQUENTIAL(
        "Orden manual / personalizado",
        "Los trucos se presentarán en el orden que los definiste",
        Icons.Default.FormatLineSpacing
    ),
    RANDOM(
        "Orden aleatorio",
        "La app mezclará el orden de los trucos en cada sesión",
        Icons.Default.Shuffle
    )
}

enum class TypeConfig {
    CONFIG_SESSION,
    CONFIG_ROUNDS,
    CONFIG_PROCESS_TRICK,
    CONFIG_DISTRIBUTION_TRICK,
    SUMMARY_SESSION,
}

enum class TrickTrackingMode(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isRealtime: Boolean
) {
    REST_CHECKIN(
        title = "Check-In en Descansos",
        description = "Patinas sin tocar la pantalla. En cada pausa de hidratación registras lo logrado en la ronda anterior.",
        icon = Icons.Default.Timer,
        isRealtime = false
    ),
    POST_SESSION(
        title = "Check-In Post-Sesión",
        description = "Evalúas qué trucos cayeron y tu efectividad general al terminar la sesión completa.",
        icon = Icons.Default.AssignmentTurnedIn,
        isRealtime = false
    )
}

data class CreateSessionSkateUiStateModel(
    //navegation
    val typeConfig: TypeConfig = TypeConfig.CONFIG_SESSION,

    //Config time session
    val durationSessionMinutes: Int? = null,

    //Config time calentamiento
    val warmupEnabled: Boolean = false,
    val warmupMinutes: Int = 0,

    //Config time estiramiento
    val cooldownEnabled: Boolean = false,
    val cooldownMinutes: Int = 0,

    //Config rondas
    val roundPreset: RoundPreset = RoundPreset.ESTANDAR,
    val calculatedRounds: Int = 0,
    val totalSkateTime: Int = 0,
    val totalRestTime: Int = 0,
    val marginMinutes: Int? = null,

    //Config process trick
    val trickModeSession: TrickSelectionMode = TrickSelectionMode.FREE,
    val showSelectTrick: Boolean = false,
    val countLimitSelectTrick: Int = 0,
    val selectedCustomTricksCount: Int = 0,
    val selectedTrickList: List<TFMTrick> = arrayListOf(),
    val unlockTrickList: List<RoomTrick> = arrayListOf(),
    val unlockedTricksCount: Int = 0,
    val trickTrackingMode: TrickTrackingMode = TrickTrackingMode.POST_SESSION,

    //Config distribution trick
    val trickDistributionMode: TrickDistributionMode = TrickDistributionMode.ROUNDS_PER_TRICK,
    val trickOrderMode: TrickOrderMode = TrickOrderMode.SEQUENTIAL,

    val detectedWearable: DetectedWearable? = null,

    val titleErrorDialog: String? = null,
    val descErrorDialog: String? = null,
)