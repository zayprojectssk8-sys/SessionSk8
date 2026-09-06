/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.sk8.appwatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sk8.appwatch.presentation.screen.home_watch.WearAppNavigation
import com.sk8.appwatch.presentation.theme.WeekSk8Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permitir que la pantalla se encienda y se muestre sobre el bloqueo
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        setContent {
            WeekSk8Theme {
                WearAppNavigation()
            }
        }
    }


}
