package com.farsan.desktopsuite.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun Taskbar(
    startOpen: Boolean,
    onStartToggle: () -> Unit,
    onCloseStart: () -> Unit,
    openWindows: List<com.farsan.desktopsuite.ui.window.DesktopWindow>,
    focusedId: Int?,
    onTaskbarTap: (Int) -> Unit,
    onOpenSystemInfo: () -> Unit,
    onOpenNotepad: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    var batteryText by remember { mutableStateOf<String?>(null) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                batteryText = if (level >= 0 && scale > 0) {
                    "${(level * 100 / scale)}%"
                } else {
                    null
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = context.registerReceiver(receiver, filter)
        if (sticky != null) receiver.onReceive(context, sticky)
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            val millisToNextMinute = (60 - now.second) * 1000L - now.nano / 1_000_000L
            delay(millisToNextMinute.coerceAtLeast(100L))
        }
    }

    val is24 = DateFormat.is24HourFormat(context)
    val timePattern = if (is24) "HH:mm" else "h:mm a"
    val datePattern = "EEE, MMM d"
    val timeFormatter = remember(timePattern) { DateTimeFormatter.ofPattern(timePattern) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern(datePattern) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (startOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onCloseStart() }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 8.dp, bottom = 48.dp)
                    .background(Color(0xDD1E1E2E), RoundedCornerShape(12.dp))
                    .clickable { }
            ) {
                StartPanel(onClose = onCloseStart, onOpenSystemInfo = onOpenSystemInfo, onOpenNotepad = onOpenNotepad, onOpenSettings = onOpenSettings)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(48.dp)
                .background(Color(0xFF101018)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onStartToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.size(8.dp).background(Color(0xFF4FC3F7), RoundedCornerShape(2.dp)))
                        Box(Modifier.size(8.dp).background(Color(0xFF4FC3F7), RoundedCornerShape(2.dp)))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.size(8.dp).background(Color(0xFF4FC3F7), RoundedCornerShape(2.dp)))
                        Box(Modifier.size(8.dp).background(Color(0xFF4FC3F7), RoundedCornerShape(2.dp)))
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                openWindows.forEach { w ->
                    Column(
                        modifier = Modifier
                            .background(
                                if (w.id == focusedId) Color(0xFF3A5A8A) else Color(0xFF1A2A4A),
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onTaskbarTap(w.id) }
                    ) {
                        Text(
                            text = w.title,
                            color = if (w.minimized) Color(0xFF8A98B0) else Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        if (w.minimized) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(Color(0xFF4FC3F7))
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Battery: ${batteryText ?: "Unavailable"}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(now.format(timeFormatter), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(now.format(dateFormatter), color = Color(0xFFB0B0C0), fontSize = 10.sp)
                }
            }
        }
    }
}
