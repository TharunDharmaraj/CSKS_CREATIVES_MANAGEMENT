package com.example.csks_creatives.presentation.taskDetailScreen.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csks_creatives.domain.model.task.TaskStatusHistory
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.presentation.components.*
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskStatusHistoryComposable(
    statusHistory: List<TaskStatusHistory>,
    isVisible: Boolean
) {
    if (!isVisible) return

    val orderedHistory =
        remember(statusHistory) { statusHistory.sortedBy { it.taskStatusType.order } }

    val currentStatusType = orderedHistory.firstOrNull { it.endTime == "0" }?.taskStatusType

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Status Timeline",
            style = MaterialTheme.typography.titleMedium,
            color = vividCerulean,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        orderedHistory.forEachIndexed { index, entry ->
            val isCurrentStatus = entry.taskStatusType == currentStatusType
            val statusColor = when (entry.taskStatusType) {
                TaskStatusType.BACKLOG -> red
                TaskStatusType.IN_PROGRESS -> vividCerulean
                TaskStatusType.IN_REVIEW -> Color.Magenta
                TaskStatusType.PAUSED -> goldenRod
                TaskStatusType.COMPLETED -> limeGreen
                else -> silverGrey
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Timeline indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (isCurrentStatus) statusColor else statusColor.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (isCurrentStatus) white else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                    if (index < orderedHistory.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(60.dp)
                                .background(silverGrey.copy(alpha = 0.1f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentStatus) charCoalPurple else charCoalPurple.copy(alpha = 0.6f)
                    ),
                    border = if (isCurrentStatus) BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentStatus) 4.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.taskStatusType.name.replace("_", " "),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isCurrentStatus) statusColor else white,
                                fontWeight = if (isCurrentStatus) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "Time Spent: ${entry.getDurationString()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = silverGrey
                            )
                        }

                        if (isCurrentStatus) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = statusColor.copy(alpha = 0.1f),
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = statusColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        val isTaskCompleted =
            (statusHistory.lastOrNull()?.taskStatusType == TaskStatusType.COMPLETED) && (statusHistory.lastOrNull()?.endTime == "0" || statusHistory.lastOrNull()?.endTime == statusHistory.lastOrNull()?.startTime)
        if (isTaskCompleted) {
            val intermediateStatuses = statusHistory.filter {
                it.taskStatusType != TaskStatusType.BACKLOG &&
                        it.taskStatusType != TaskStatusType.COMPLETED
            }
            // Added for backward compatibility
            val revisions =
                statusHistory.filter { it.taskStatusType.order > 99 && it.elapsedTime == 0L }
            val pausedStatus = statusHistory.find { it.taskStatusType == TaskStatusType.PAUSED }
            if (intermediateStatuses.isNotEmpty()) {
                val totalElapsedTime =
                    intermediateStatuses.sumOf { it.elapsedTime } + revisions.sumOf { it.endTime.toLong() - it.startTime.toLong() } - (pausedStatus?.elapsedTime
                        ?: 0L)
                val timeSpent = formatDuration(totalElapsedTime)
                CompletedSummary(timeSpent)
            } else {
                // Empty, No Intermediate Status
                CompletedSummary("Completed Time Could not be Displayed")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CompletedSummary(summaryText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = limeGreen.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, limeGreen.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = limeGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Total Completion Time",
                style = MaterialTheme.typography.labelMedium,
                color = silverGrey
            )
            Text(
                text = summaryText,
                style = MaterialTheme.typography.titleLarge,
                color = limeGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDuration(totalElapsedTime: Long): String {
    if (totalElapsedTime <= 0) return "Less than a minute"

    val duration = Duration.ofMillis(totalElapsedTime)
    val days = duration.toDays()
    val hours = (duration.toHours() % 24)
    val minutes = (duration.toMinutes() % 60)

    return buildString {
        if (days > 0) append("$days d ")
        if (hours > 0) append("$hours h ")
        if (minutes > 0) append("$minutes m")
    }.trim().ifEmpty { "Less than a minute" }
}
