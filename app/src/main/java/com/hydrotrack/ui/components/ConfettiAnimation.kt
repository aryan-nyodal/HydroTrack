package com.hydrotrack.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val xRatio: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    val initialAngle: Float
)

@Composable
fun ConfettiAnimation(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val progress = remember { Animatable(0f) }

    val colors = listOf(
        Color(0xFF0099CC),
        Color(0xFF4DB6AC),
        Color(0xFFFF8F00),
        Color(0xFFFFD54F),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF4CAF50)
    )

    val particles = remember {
        List(60) {
            ConfettiParticle(
                xRatio = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.5f,
                size = Random.nextFloat() * 12f + 8f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                initialAngle = Random.nextFloat() * 360f
            )
        }
    }

    LaunchedEffect(isVisible) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
        onDismiss()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            val y = progress.value * height * particle.speed * 1.2f
            val x = particle.xRatio * width + (kotlin.math.sin(progress.value * 6f + particle.initialAngle) * 30f)
            val currentRotation = particle.initialAngle + progress.value * particle.rotationSpeed
            val alpha = (1f - progress.value).coerceIn(0f, 1f)

            rotate(degrees = currentRotation, pivot = Offset(x, y)) {
                drawRect(
                    color = particle.color.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(particle.size, particle.size * 0.6f)
                )
            }
        }
    }
}
