package com.zayprojetcs.weeksk8.ui.customs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zayprojetcs.weeksk8.screens.create_session_skate.modules.LoadConfigSessionData
import com.zayprojetcs.weeksk8.screens.create_session_skate.modules.LoadResultTypeRoundSelected
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.CreateSessionSkateUiStateModel
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.RoundPreset
import com.zayprojetcs.weeksk8.screens.create_session_skate.ui_state.model.TypeConfig

@Composable
fun ScaffoldCustom(
    title: String = "TITULO",
    floatingActionButton: @Composable (() -> Unit) = {},
    bottomBar: @Composable (() -> Unit) = {},
    content: @Composable (() -> Unit) = {},
) {
    val cornerRadius = RoundedCornerShape(24.dp)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f), contentAlignment = Alignment.Center
                ) {
                    LoadTitleSectionCustom(title)
                }


                Box(
                    modifier = Modifier
                        .weight(9.3f)
                        .clip(cornerRadius)
                        .background(MaterialTheme.colorScheme.background)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.background,
                            shape = cornerRadius
                        )

                ) {
                    content()
                }
            }
        })
}


@Composable
fun ScaffoldCustomCreateSession(
    title: String = "TITULO",
    hideTitle: Boolean = false,
    hideButton: Boolean = false,
    floatingActionButton: @Composable (() -> Unit) = {},
    bottomBar: @Composable (() -> Unit) = {},
    content: @Composable (() -> Unit) = {},
    onButtonClick: () -> Unit = {}
) {

    val cornerRadius = RoundedCornerShape(24.dp)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!hideTitle) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f), contentAlignment = Alignment.Center
                    ) {
                        LoadTitleSectionCustom(title)
                    }
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(8.3f)
                        .clip(cornerRadius)
                        .background(MaterialTheme.colorScheme.background)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.background,
                            shape = cornerRadius
                        )

                ) {
                    content()
                }

                if (!hideButton) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onButtonClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .height(54.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        })
}
