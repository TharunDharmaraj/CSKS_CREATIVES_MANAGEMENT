package com.example.csks_creatives.presentation.taskDetailScreen.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.domain.model.task.TaskStatusHistory
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskStatusHistoryComposable(
    statusHistory: List<TaskStatusHistory>,
    isVisible: Boolean
) {
    if (!isVisible) return

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        statusHistory.forEachIndexed { index, entry ->
            val isCurrentStatus = index == statusHistory.lastIndex

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(
                        width = if (isCurrentStatus) 2.dp else 0.dp,
                        color = if (isCurrentStatus) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isCurrentStatus) 6.dp else 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentStatus)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        Color(0xFF1E1E1E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (entry.taskStatusType) {
                            TaskStatusType.BACKLOG -> Icons.Default.Lock
                            TaskStatusType.IN_PROGRESS -> Icons.Default.PlayArrow
                            TaskStatusType.IN_REVIEW -> Icons.Default.Build
                            TaskStatusType.COMPLETED -> Icons.Default.CheckCircle
                            else -> Icons.Default.MailOutline
                        },
                        contentDescription = null,
                        tint = if (isCurrentStatus)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.taskStatusType.name.replace("_", " ")
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrentStatus) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Time spent: ${entry.getDurationString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        val isTaskCompleted = statusHistory.lastOrNull()?.taskStatusType == TaskStatusType.COMPLETED
        if (isTaskCompleted) {
            val intermediateStatuses = statusHistory.filter {
                it.taskStatusType != TaskStatusType.BACKLOG &&
                        it.taskStatusType != TaskStatusType.COMPLETED
            }
            if (intermediateStatuses.isNotEmpty()) {
                val startTime = intermediateStatuses.first().startTime.toLong()
                val endTime = intermediateStatuses.last().endTime.toLong()
                val timeSpent = formatDuration(startTime, endTime)
                CompletedSummary(timeSpent)
            } else {
                // Empty, No Intermediate Status
                CompletedSummary("Completed Time Could not be Displayed")
            }
        }
    }
}

@Composable
fun CompletedSummary(summaryText: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)) // dark green
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Total Task Duration",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDuration(startMillis: Long, endMillis: Long): String {
    val durationMillis = endMillis - startMillis
    if (durationMillis <= 0) return "Less than a minute"

    val duration = Duration.ofMillis(durationMillis)
    val days = duration.toDays()
    val hours = (duration.toHours() % 24)
    val minutes = (duration.toMinutes() % 60)

    return buildString {
        if (days > 0) append("$days day${if (days > 1) "s" else ""} ")
        if (hours > 0) append("$hours hr${if (hours > 1) "s" else ""} ")
        if (minutes > 0) append("$minutes min${if (minutes > 1) "s" else ""}")
    }.trim().ifEmpty { "Less than a minute" }
}