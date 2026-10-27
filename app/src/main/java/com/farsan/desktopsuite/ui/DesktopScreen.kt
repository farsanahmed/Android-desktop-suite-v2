package com.farsan.desktopsuite.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.farsan.desktopsuite.ui.apps.NotepadApp
import com.farsan.desktopsuite.ui.apps.SettingsApp
import com.farsan.desktopsuite.ui.apps.SystemInfoApp
import com.farsan.desktopsuite.ui.settings.AppSettings
import com.farsan.desktopsuite.ui.settings.applyPcMode
import com.farsan.desktopsuite.ui.window.AppKind
import com.farsan.desktopsuite.ui.window.WindowFrame
import com.farsan.desktopsuite.ui.window.WindowManagerState

var closeStartPanelRequest by mutableStateOf(0)

private fun findActivity(context: Context): Activity? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun DesktopScreen(settings: AppSettings) {
    val context = LocalContext.current
    var startOpen by remember { mutableStateOf(false) }
    val wm = remember { WindowManagerState() }
    val activity = remember(context) { findActivity(context) }

    LaunchedEffect(closeStartPanelRequest) {
        if (closeStartPanelRequest > 0) startOpen = false
    }

    LaunchedEffect(settings.hideBars, settings.keepAwake) {
        activity?.let { applyPcMode(it, settings.hideBars, settings.keepAwake) }
    }

    BackHandler(enabled = startOpen) {
        startOpen = false
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B3E),
                        Color(0xFF1A3A6B),
                        Color(0xFF0A1428)
                    )
                )
            )
    ) {
        val desktopW = maxWidth.value
        val desktopH = (maxHeight - 48.dp).value
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp)
                .clipToBounds()
        ) {
            wm.windows.filter { !it.minimized }.forEach { w ->
                if (w.maximized) {
                    Box(modifier = Modifier.offset(x = 0.dp, y = 0.dp)) {
                        WindowFrame(
                            window = w,
                            focused = w.id == wm.focusedId,
                            onFocus = { wm.focus(w.id) },
                            onClose = { wm.close(w.id) },
                            onMinimize = { wm.minimize(w.id) },
                            onMaximize = { wm.toggleMaximize(w.id) },
                            onDrag = { _, _ -> },
                            onResize = { _, _ -> },
                            widthDp = desktopW,
                            heightDp = desktopH,
                            draggable = false
                        ) {
                            when (w.kind) {
                                AppKind.SYSTEM_INFO -> SystemInfoApp(context)
                                AppKind.NOTEPAD -> NotepadApp(
                                    text = wm.notes[w.id] ?: "",
                                    onTextChange = { wm.notes[w.id] = it }
                                )
                                AppKind.SETTINGS -> SettingsApp(settings)
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.offset(x = w.x.dp, y = w.y.dp)) {
                        WindowFrame(
                            window = w,
                            focused = w.id == wm.focusedId,
                            onFocus = { wm.focus(w.id) },
                            onClose = { wm.close(w.id) },
                            onMinimize = { wm.minimize(w.id) },
                            onMaximize = { wm.toggleMaximize(w.id) },
                            onDrag = { dx, dy -> wm.move(w.id, dx, dy, desktopW, desktopH) },
                            onResize = { dw, dh -> wm.resize(w.id, dw, dh, desktopW, desktopH) },
                            widthDp = w.width,
                            heightDp = w.height,
                            draggable = true
                        ) {
                            when (w.kind) {
                                AppKind.SYSTEM_INFO -> SystemInfoApp(context)
                                AppKind.NOTEPAD -> NotepadApp(
                                    text = wm.notes[w.id] ?: "",
                                    onTextChange = { wm.notes[w.id] = it }
                                )
                                AppKind.SETTINGS -> SettingsApp(settings)
                            }
                        }
                    }
                }
            }
        }
        Taskbar(
            startOpen = startOpen,
            onStartToggle = { startOpen = !startOpen },
            onCloseStart = { startOpen = false },
            openWindows = wm.windows,
            focusedId = wm.focusedId,
            onTaskbarTap = { wm.taskbarTap(it) },
            onOpenSystemInfo = { wm.open(AppKind.SYSTEM_INFO, "System Info", desktopW, desktopH); startOpen = false },
            onOpenNotepad = { wm.open(AppKind.NOTEPAD, "Notepad", desktopW, desktopH); startOpen = false },
            onOpenSettings = { wm.open(AppKind.SETTINGS, "Settings", desktopW, desktopH); startOpen = false }
        )
    }
}
