/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.sk8.appwatch.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sk8.appwatch.presentation.screen.home_watch.WearAppNavigation
import com.sk8.appwatch.presentation.theme.WeekSk8Theme
import com.zaysk8.core.utils.SOURCE_NODE_ID


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permitir que la pantalla se encienda y se muestre sobre el bloqueo
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        handleIntent(intent = intent)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val nodeId = intent?.getStringExtra(SOURCE_NODE_ID)

        setContent {
            WeekSk8Theme {
                WearAppNavigation(nodeId)
            }
        }
    }
}
