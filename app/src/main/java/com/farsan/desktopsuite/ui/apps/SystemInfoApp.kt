package com.farsan.desktopsuite.ui.apps

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun bytesToGb(bytes: Long): String =
    if (bytes > 0) String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0)) else "Unavailable"

@Composable
fun SystemInfoApp(context: Context) {
    val info = remember {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val totalRam = memInfo.totalMem
        val dataDir = Environment.getDataDirectory()
        val stat = StatFs(dataDir.absolutePath)
        val totalStorage = stat.totalBytes
        val freeStorage = stat.availableBytes
        listOf(
            "Device model" to Build.MODEL,
            "Manufacturer" to Build.MANUFACTURER,
            "Android version" to Build.VERSION.RELEASE,
            "SDK level" to Build.VERSION.SDK_INT.toString(),
            "CPU architecture" to (Build.SUPPORTED_ABIS.firstOrNull() ?: "Unavailable"),
            "CPU cores" to Runtime.getRuntime().availableProcessors().toString(),
            "Total RAM" to bytesToGb(totalRam),
            "Storage total" to bytesToGb(totalStorage),
            "Storage free" to bytesToGb(freeStorage)
        )
    }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        info.forEach { (k, v) ->
            Text(text = "$k: $v", color = Color.White, fontSize = 12.sp)
        }
    }
}
