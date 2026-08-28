/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.sk8.appwatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.sk8.appwatch.presentation.screen.home_watch.WearAppNavigation
import com.sk8.appwatch.presentation.screen.link_device_phone.LinkDevicePhoneScreen
import com.sk8.appwatch.presentation.screen.skate_session.SkateSessionScreen
import com.sk8.appwatch.presentation.theme.WeekSk8Theme
import com.sk8.appwatch.presentation.utils.sendConnectionHandshakeToPhone
import com.zaysk8.core.utils.SOURCE_NODE_ID


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWatchCommunication = intent.getStringExtra(SOURCE_NODE_ID)

        setContent {
            WeekSk8Theme {
                WearAppNavigation(appWatchCommunication)
            }
        }
    }
}
