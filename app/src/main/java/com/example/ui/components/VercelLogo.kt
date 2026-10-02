package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.VercelWhitePure

@Composable
fun VercelLogo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = VercelWhitePure
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Equilateral/Isosceles triangle pointing upwards
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = path, color = color)
    }
}
