package com.hydrotrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hydrotrack.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StreakCalendar(
    selectedMonth: YearMonth,
    monthRecords: Map<String, Boolean>,
    currentStreakDates: Set<String>,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + selectedMonth.year
    val today = LocalDate.now()
    val daysInMonth = selectedMonth.lengthOfMonth()
    val firstDayOfMonth = selectedMonth.atDay(1)
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value % 7) // 0 for Sunday, 1 for Monday etc.

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header with Prev/Next buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month"
                    )
                }

                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of week labels (S, M, T, W, T, F, S)
            val dayHeaders = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayHeaders.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            val totalCells = dayOfWeekOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (colIndex in 0 until 7) {
                        val cellIndex = rowIndex * 7 + colIndex
                        val dayNumber = cellIndex - dayOfWeekOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = selectedMonth.atDay(dayNumber)
                            val dateStr = DateUtils.formatDate(date)
                            val isGoalMet = monthRecords[dateStr] == true
                            val isInCurrentStreak = currentStreakDates.contains(dateStr)
                            val isCurrentDay = date == today

                            CalendarDayCell(
                                dayNumber = dayNumber,
                                isGoalMet = isGoalMet,
                                isInCurrentStreak = isInCurrentStreak,
                                isToday = isCurrentDay
                            )
                        } else {
                            Box(modifier = Modifier.size(36.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isGoalMet: Boolean,
    isInCurrentStreak: Boolean,
    isToday: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(36.dp)
            .then(
                if (isInCurrentStreak && isGoalMet) {
                    Modifier
                        .clip(CircleShape)
                        .background(primaryColor)
                } else if (isGoalMet) {
                    Modifier
                        .clip(CircleShape)
                        .background(primaryContainer)
                } else {
                    Modifier
                }
            )
            .then(
                if (isToday) {
                    Modifier.border(2.dp, primaryColor, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber.toString(),
            fontSize = 13.sp,
            fontWeight = if (isToday || isGoalMet) FontWeight.Bold else FontWeight.Normal,
            color = if (isInCurrentStreak && isGoalMet) {
                Color.White
            } else if (isGoalMet) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                onSurface
            }
        )
    }
}
