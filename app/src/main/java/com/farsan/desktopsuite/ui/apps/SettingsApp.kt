package com.farsan.desktopsuite.ui.apps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farsan.desktopsuite.ui.settings.AppSettings

@Composable
fun SettingsApp(settings: AppSettings) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Text(text = "PC Mode", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Full screen (hide system bars)", color = Color.White, fontSize = 12.sp)
            }
            Switch(checked = settings.hideBars, onCheckedChange = { settings.updateHideBars(it) })
        }
        Text(
            text = "Android may show the bars again when you swipe from the screen edge; they hide again automatically. Apps cannot fully disable your phone's gestures.",
            color = Color(0xFFB0B0C0),
            fontSize = 10.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Keep screen awake", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Switch(checked = settings.keepAwake, onCheckedChange = { settings.updateKeepAwake(it) })
        }
    }
}
