package com.farsan.desktopsuite.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

var closeStartPanelRequest by mutableStateOf(0)

@Composable
fun DesktopScreen() {
    var startOpen by remember { mutableStateOf(false) }

    LaunchedEffect(closeStartPanelRequest) {
        if (closeStartPanelRequest > 0) startOpen = false
    }

    BackHandler(enabled = startOpen) {
        startOpen = false
    }

    Box(
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
        Taskbar(
            startOpen = startOpen,
            onStartToggle = { startOpen = !startOpen },
            onCloseStart = { startOpen = false }
        )
    }
}
