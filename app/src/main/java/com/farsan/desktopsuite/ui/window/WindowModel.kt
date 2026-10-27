package com.farsan.desktopsuite.ui.window

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

enum class AppKind { SYSTEM_INFO }

data class DesktopWindow(
    val id: Int,
    val kind: AppKind,
    val title: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val minimized: Boolean = false,
    val maximized: Boolean = false
)

class WindowManagerState {
    val windows: SnapshotStateList<DesktopWindow> = mutableStateListOf()
    private var nextId = 1
    private var cascade = 0

    val focusedId: Int? get() = windows.lastOrNull { !it.minimized }?.id

    fun open(kind: AppKind, title: String) {
        val existing = windows.indexOfFirst { it.kind == kind }
        if (existing >= 0) {
            focus(windows[existing].id)
            return
        }
        val offset = (cascade % 5) * 24f
        cascade++
        windows.add(
            DesktopWindow(
                id = nextId++,
                kind = kind,
                title = title,
                x = 40f + offset,
                y = 40f + offset,
                width = 320f,
                height = 380f
            )
        )
    }

    fun close(id: Int) {
        windows.removeAll { it.id == id }
    }

    fun focus(id: Int) {
        val idx = windows.indexOfFirst { it.id == id }
        if (idx < 0) return
        val w = windows[idx]
        if (w.minimized) return
        windows[idx] = w.copy(minimized = false)
        val w2 = windows.removeAt(idx)
        windows.add(w2)
    }

    fun minimize(id: Int) {
        val idx = windows.indexOfFirst { it.id == id }
        if (idx >= 0) {
            windows[idx] = windows[idx].copy(minimized = true)
        }
    }

    fun toggleMaximize(id: Int) {
        val idx = windows.indexOfFirst { it.id == id }
        if (idx >= 0) {
            windows[idx] = windows[idx].copy(maximized = !windows[idx].maximized)
        }
    }

    fun restoreFromMinimized(id: Int) {
        val idx = windows.indexOfFirst { it.id == id }
        if (idx < 0) return
        windows[idx] = windows[idx].copy(minimized = false)
        val w = windows.removeAt(idx)
        windows.add(w)
    }

    fun taskbarTap(id: Int) {
        val idx = windows.indexOfFirst { it.id == id }
        if (idx < 0) return
        val w = windows[idx]
        if (w.minimized) {
            restoreFromMinimized(id)
        } else if (id == focusedId) {
            minimize(id)
        } else {
            focus(id)
        }
    }

    fun move(id: Int, dxDp: Float, dyDp: Float, maxX: Float, maxY: Float) {
        val idx = windows.indexOfFirst { it.id == id }
        if (idx < 0) return
        val w = windows[idx]
        if (w.maximized) return
        val nx = (w.x + dxDp).coerceIn(0f, (maxX - 80f).coerceAtLeast(0f))
        val ny = (w.y + dyDp).coerceIn(0f, (maxY - 36f).coerceAtLeast(0f))
        windows[idx] = w.copy(x = nx, y = ny)
    }
}
