package com.hydrotrack.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FlameAnimation(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    if (isActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "flamePulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flameScale"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flameAlpha"
        )

        Text(
            text = "🔥",
            fontSize = (size.value * 0.8f).sp,
            modifier = modifier
                .size(size)
                .scale(scale)
                .alpha(alpha)
        )
    } else {
        Text(
            text = "🔥",
            fontSize = (size.value * 0.8f).sp,
            modifier = modifier
                .size(size)
                .alpha(0.35f)
        )
    }
}
