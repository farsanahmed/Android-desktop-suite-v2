package com.farsan.desktopsuite.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap?
)

suspend fun loadInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PackageManager.ResolveInfoFlags.of(0)
    } else {
        null
    }
    val resolved = if (flags != null) {
        pm.queryIntentActivities(intent, flags)
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(intent, 0)
    }
    resolved.mapNotNull { ri ->
        val ai = ri.activityInfo ?: return@mapNotNull null
        if (ai.packageName == context.packageName) return@mapNotNull null
        val label = try { ri.loadLabel(pm).toString() } catch (e: Exception) { ai.name }
        val icon = try {
            val drawable = ri.loadIcon(pm)
            val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
            val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
        AppInfo(label, ai.packageName, ai.name, icon)
    }.sortedBy { it.label.lowercase() }
}
