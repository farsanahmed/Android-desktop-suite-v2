package com.farsan.desktopsuite.ui.apps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farsan.desktopsuite.ui.settings.AppSettings

@Composable
fun SettingsApp(settings: AppSettings) {
    val context = LocalContext.current
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
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Default launcher", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
        Button(onClick = {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }) {
            Text(text = "Choose default Home app")
        }
        Text(
            text = "Opens system settings so you can select HyperDroid as the Home app. HyperDroid cannot change this silently.",
            color = Color(0xFFB0B0C0),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
