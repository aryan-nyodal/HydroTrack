package com.hydrotrack.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun WaveProgressIndicator(
    progress: Float, // 0.0f to 1.0f+
    currentMl: Int,
    goalMl: Int,
    unitLabel: String = "ml",
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1.5f),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val radius = canvasWidth / 2f
            val center = Offset(radius, radius)

            // Draw background circle
            drawCircle(
                color = surfaceColor.copy(alpha = 0.5f),
                radius = radius,
                center = center
            )

            // Circular clipping path
            val circlePath = Path().apply {
                addOval(Rect(0f, 0f, canvasWidth, canvasHeight))
            }

            clipPath(circlePath) {
                val clampedProg = animatedProgress.value.coerceIn(0f, 1.0f)
                val waterLevel = canvasHeight * (1f - clampedProg)
                val waveAmplitude = if (clampedProg <= 0.01f || clampedProg >= 0.99f) 0f else 16f
                val waveFrequency = 1.5f

                // Back wave
                val backWavePath = Path().apply {
                    moveTo(0f, canvasHeight)
                    lineTo(0f, waterLevel)
                    var x = 0f
                    while (x <= canvasWidth) {
                        val y = waterLevel + waveAmplitude * sin(
                            (x / canvasWidth * 2f * Math.PI.toFloat() * waveFrequency) + waveOffset + Math.PI.toFloat()
                        )
                        lineTo(x, y)
                        x += 4f
                    }
                    lineTo(canvasWidth, canvasHeight)
                    close()
                }

                drawPath(
                    path = backWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryContainer.copy(alpha = 0.6f),
                            primaryColor.copy(alpha = 0.4f)
                        ),
                        startY = waterLevel - waveAmplitude,
                        endY = canvasHeight
                    )
                )

                // Front wave
                val frontWavePath = Path().apply {
                    moveTo(0f, canvasHeight)
                    lineTo(0f, waterLevel)
                    var x = 0f
                    while (x <= canvasWidth) {
                        val y = waterLevel + waveAmplitude * sin(
                            (x / canvasWidth * 2f * Math.PI.toFloat() * waveFrequency) + waveOffset
                        )
                        lineTo(x, y)
                        x += 4f
                    }
                    lineTo(canvasWidth, canvasHeight)
                    close()
                }

                drawPath(
                    path = frontWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.85f),
                            primaryColor
                        ),
                        startY = waterLevel - waveAmplitude,
                        endY = canvasHeight
                    )
                )
            }
        }

        // Center Content Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val percentage = (progress * 100).toInt()
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$currentMl / $goalMl $unitLabel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
