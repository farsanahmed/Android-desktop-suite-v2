package com.farsan.desktopsuite.ui.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("desktop_settings", Context.MODE_PRIVATE)
    var hideBars by mutableStateOf(prefs.getBoolean("hideBars", false))
        private set
    var keepAwake by mutableStateOf(prefs.getBoolean("keepAwake", false))
        private set

    fun setHideBars(value: Boolean) {
        hideBars = value
        prefs.edit().putBoolean("hideBars", value).apply()
    }

    fun setKeepAwake(value: Boolean) {
        keepAwake = value
        prefs.edit().putBoolean("keepAwake", value).apply()
    }
}
