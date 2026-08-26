package com.zayprojetcs.weeksk8.screens.menu


import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zayprojetcs.weeksk8.screens.link_device_smartwatch.LoadDeviceDetectedScreen
import com.zayprojetcs.weeksk8.screens.menu.ui_state.MenuUiState
import com.zayprojetcs.weeksk8.ui.customs.OutlineCardIInfoCustom


@Composable
fun MenuScreen(onNavigateLinkSmartWatch: () -> Unit, viewModel: MenuViewModel = viewModel()) {

    val mapUiState by viewModel.menuUiState.collectAsStateWithLifecycle()


    // --- UI Principal de tu App ---
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (mapUiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 4.dp
                    )
                }
                return@Scaffold
            }

            if (mapUiState.connectedDevice == null) {
                SmartwatchPromoCard(onConnectClick = onNavigateLinkSmartWatch, onDismissClick = {})
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    LoadDeviceDetectedScreen(
                        mapUiState.connectedDevice!!,
                        null
                    )
                }
            }

        }

    }
}


@Composable
fun SmartwatchPromoCard(
    modifier: Modifier = Modifier,
    onConnectClick: () -> Unit,
    onDismissClick: (() -> Unit)? = null
) {
    OutlineCardIInfoCustom(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        imageVector = Icons.Outlined.Watch,
        title = "Vincular mi reloj",
        description = "Conecta tu reloj para registrar impactos, detectar caídas y medir tu ritmo cardíaco en tiempo real durante tus sesiones.",
        buttonLabel = "Vincular mi reloj",
        onConnectClick = onConnectClick,
        onDismissClick = onDismissClick
    )
}
