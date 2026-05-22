package com.example.csks_creatives.presentation.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.getFormattedDateTimeFormat
import com.example.csks_creatives.domain.utils.Utils.getTimeAgo
import com.example.csks_creatives.domain.utils.Utils.formatTimeStampToGetJustDate
import com.example.csks_creatives.presentation.components.*

@Composable
fun ModernDateView(
    timeStamp: String,
    modifier: Modifier = Modifier,
    useRelativeTime: Boolean = true,
    showTime: Boolean = true
) {
    val displayTime = when {
        useRelativeTime -> getTimeAgo(timeStamp)
        showTime -> getFormattedDateTimeFormat(timeStamp)
        else -> formatTimeStampToGetJustDate(timeStamp)
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = charCoal.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, white.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = vividCerulean.copy(alpha = 0.8f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = displayTime,
                style = MaterialTheme.typography.labelSmall,
                color = silverGrey,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TaskItem(task: ClientTask, onTaskClick: () -> Unit, taskElapsedTime: String) {
    val timeTaken = remember { taskElapsedTime }
    val statusColor = when (task.currentStatus) {
        TaskStatusType.BACKLOG -> Color.Red
        TaskStatusType.IN_PROGRESS -> vividCerulean
        TaskStatusType.COMPLETED -> limeGreen
        TaskStatusType.IN_REVIEW -> Color.Magenta
        TaskStatusType.PAUSED -> Color.Yellow
        else -> grey
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        onClick = onTaskClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.taskName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = white,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = task.currentStatus.name,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = silverGrey.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.employeeId.ifEmpty { "Not Assigned" },
                    fontSize = 14.sp,
                    color = silverGrey
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = task.taskType.icon,
                    contentDescription = null,
                    tint = silverGrey.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.taskType.displayName,
                    fontSize = 14.sp,
                    color = silverGrey
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Effort Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = vividCerulean.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, vividCerulean.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = vividCerulean,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${task.taskEstimate}h",
                            color = vividCerulean,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                ModernDateView(task.taskCreationTime)

                if (timeTaken.isNotEmpty()) {
                    Text(
                        text = "Time: $timeTaken",
                        color = vividCerulean,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}