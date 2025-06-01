package com.example.csks_creatives.presentation.components.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.getFormattedDateTimeFormat


@Composable
fun TaskItem(task: ClientTask, onTaskClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onTaskClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.taskName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Assigned to: ${task.employeeId}", fontSize = 14.sp)
            Text(text = "Estimate: ${task.taskEstimate}", fontSize = 14.sp)
            Text(text = "Created Date: ${getFormattedDateTimeFormat(task.taskCreationTime)}", fontSize = 14.sp)
            Text(
                text = "Status: ${task.currentStatus}",
                fontSize = 14.sp,
                color = when (task.currentStatus) {
                    TaskStatusType.BACKLOG -> Color.Red
                    TaskStatusType.IN_PROGRESS -> Color.Yellow
                    TaskStatusType.COMPLETED -> Color.Green
//                    TaskStatusType.BLOCKED -> Color.Black
                    else -> Color.Gray
                }
            )
        }
    }
}