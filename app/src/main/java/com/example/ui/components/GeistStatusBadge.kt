package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GeistStatusBadge(
    state: String,
    modifier: Modifier = Modifier
) {
    val upperState = state.uppercase()

    val (accentColor, bgColor, label) = when {
        upperState == "READY" -> Triple(StatusReady, StatusReadyBg, "Ready")
        upperState in listOf("BUILDING", "INITIALIZING") -> Triple(StatusBuilding, StatusBuildingBg, "Building")
        upperState in listOf("ERROR", "FAILED") -> Triple(StatusError, StatusErrorBg, "Error")
        upperState in listOf("CANCELED") -> Triple(StatusCanceled, StatusCanceledBg, "Canceled")
        upperState in listOf("QUEUED") -> Triple(StatusQueued, StatusQueuedBg, "Queued")
        else -> Triple(VercelGrayLight, VercelBorder, upperState)
    }

    val isPulsing = upperState in listOf("BUILDING", "INITIALIZING", "QUEUED")
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        rememberUpdatedState(1f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .testTag("status_badge_${upperState.lowercase()}")
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = alphaAnim))
        )
        Text(
            text = label,
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
