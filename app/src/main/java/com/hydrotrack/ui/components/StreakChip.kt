package com.hydrotrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StreakChip(
    streakCount: Int,
    isAtRisk: Boolean,
    modifier: Modifier = Modifier
) {
    val isActive = streakCount > 0
    val containerColor = if (isAtRisk) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
    } else if (isActive) {
        Color(0xFFFFECB3) // warm amber container
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isAtRisk) {
        MaterialTheme.colorScheme.onErrorContainer
    } else if (isActive) {
        Color(0xFFE65100) // warm dark orange
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (isAtRisk) MaterialTheme.colorScheme.error else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FlameAnimation(isActive = isActive, size = 20.dp)
        Text(
            text = "$streakCount days",
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        if (isAtRisk) {
            Text(
                text = "• At Risk",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}
