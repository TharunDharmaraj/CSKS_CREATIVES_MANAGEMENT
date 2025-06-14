package com.example.csks_creatives.presentation.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.csks_creatives.domain.utils.Utils.calculateFormattedTaskTakenTime
import com.example.csks_creatives.domain.utils.Utils.getFormattedDateTimeFormat

@Composable
fun TaskItem(task: ClientTask, onTaskClick: () -> Unit) {
    val timeTaken = remember(task) {
        if (task.currentStatus == TaskStatusType.COMPLETED) {
            val backlogEnd =
                task.statusHistory.find { it.taskStatusType == TaskStatusType.BACKLOG }!!.endTime
            val completedStart =
                task.statusHistory.find { it.taskStatusType == TaskStatusType.COMPLETED }!!.startTime
            calculateFormattedTaskTakenTime(backlogEnd, completedStart)
        } else ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onTaskClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.taskName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Assigned to: ${task.employeeId}", fontSize = 14.sp)
                Text(text = "Estimate: ${task.taskEstimate}", fontSize = 14.sp)
                Text(
                    text = "Created Date: ${getFormattedDateTimeFormat(task.taskCreationTime)}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Status: ${task.currentStatus}",
                    fontSize = 14.sp,
                    color = when (task.currentStatus) {
                        TaskStatusType.BACKLOG -> Color.Red
                        TaskStatusType.IN_PROGRESS -> Color.Yellow
                        TaskStatusType.COMPLETED -> Color.Green
                        else -> Color.Gray
                    }
                )
            }

            if (timeTaken.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = timeTaken,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}