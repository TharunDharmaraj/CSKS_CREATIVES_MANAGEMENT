package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    }
}