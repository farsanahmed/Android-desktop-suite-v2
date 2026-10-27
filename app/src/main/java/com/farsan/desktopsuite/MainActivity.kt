package com.farsan.desktopsuite

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.farsan.desktopsuite.ui.DesktopScreen
import com.farsan.desktopsuite.ui.closeStartPanelRequest
import com.farsan.desktopsuite.ui.settings.AppSettings
import com.farsan.desktopsuite.ui.settings.applyPcMode

class MainActivity : ComponentActivity() {
    private lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        applyPcMode(this, settings.hideBars, settings.keepAwake)
        setContent {
            MaterialTheme {
                Surface {
                    DesktopScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        closeStartPanelRequest++
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::settings.isInitialized) {
            applyPcMode(this, settings.hideBars, settings.keepAwake)
        }
    }
}
