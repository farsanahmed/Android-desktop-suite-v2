package com.farsan.desktopsuite.ui.window

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WindowFrame(
    window: DesktopWindow,
    focused: Boolean,
    onFocus: () -> Unit,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit,
    widthDp: Float,
    heightDp: Float,
    draggable: Boolean,
    content: @Composable () -> Unit
) {
    val titleColor = if (focused) Color(0xFF2A4A7E) else Color(0xFF1A2A4E)
    val interaction = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    val shape = if (window.maximized) RectangleShape else RoundedCornerShape(10.dp)
    val titleShape = if (window.maximized) RectangleShape else RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    Box(
        modifier = Modifier
            .width(widthDp.dp)
            .height(heightDp.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xDD1E2A44), shape)
                .clickable(interactionSource = interaction, indication = null) { onFocus() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(titleColor, titleShape)
                    .padding(horizontal = 10.dp)
                    .then(
                        if (draggable) Modifier.pointerInput(window.id) {
                            detectDragGestures(onDragStart = { onFocus() }) { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.x / density.density, dragAmount.y / density.density)
                            }
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = window.title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (window.maximized) "[][]" else "[]",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { onMaximize() }
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = "—",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onMinimize() }
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = "X",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onClose() }
                        .padding(horizontal = 4.dp)
                )
            }
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                content()
            }
        }
        if (!window.maximized) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
                    .pointerInput(window.id) {
                        detectDragGestures(onDragStart = { onFocus() }) { change, dragAmount ->
                            change.consume()
                            onResize(dragAmount.x / density.density, dragAmount.y / density.density)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 2.dp.toPx()
                    drawLine(Color.White, Offset(size.width - 12.dp.toPx(), size.height - 4.dp.toPx()), Offset(size.width - 4.dp.toPx(), size.height - 12.dp.toPx()), stroke)
                    drawLine(Color.White, Offset(size.width - 16.dp.toPx(), size.height - 4.dp.toPx()), Offset(size.width - 4.dp.toPx(), size.height - 16.dp.toPx()), stroke)
                    drawLine(Color.White, Offset(size.width - 20.dp.toPx(), size.height - 4.dp.toPx()), Offset(size.width - 4.dp.toPx(), size.height - 20.dp.toPx()), stroke)
                }
            }
        }
    }
}
