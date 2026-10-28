package com.hydrotrack.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AnimatedCounter(
    value: Number,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    textStyle: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prefix.isNotEmpty()) {
            Text(text = prefix, style = textStyle, color = color)
        }
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                if (targetState.toDouble() > initialState.toDouble()) {
                    slideInVertically { height -> height } togetherWith
                            slideOutVertically { height -> -height }
                } else {
                    slideInVertically { height -> -height } togetherWith
                            slideOutVertically { height -> height }
                }
            },
            label = "animatedCounter"
        ) { targetVal ->
            Text(
                text = targetVal.toString(),
                style = textStyle,
                color = color
            )
        }
        if (suffix.isNotEmpty()) {
            Text(text = suffix, style = textStyle, color = color)
        }
    }
}
