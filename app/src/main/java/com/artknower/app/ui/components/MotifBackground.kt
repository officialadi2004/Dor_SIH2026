package com.artknower.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.artknower.app.ui.theme.ThinBorderColor

@Composable
fun HeaderMandalaMotif(
    modifier: Modifier = Modifier,
    color: Color = ThinBorderColor
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val centerX = size.width * 0.85f
        val centerY = size.height * 0.3f

        // Draw delicate concentric motif circles & petals
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = 70.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )

        drawCircle(
            color = color.copy(alpha = 0.25f),
            radius = 45.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )

        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = 20.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
