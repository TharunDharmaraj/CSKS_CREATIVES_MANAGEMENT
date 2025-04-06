package com.example.csks_creatives.presentation.employeeDetailsScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.utils.Utils.formatTimeStamp
import com.example.csks_creatives.presentation.components.helper.ColorHelper.getBorderColorBasedOnTaskPriority

@Composable
fun EmployeeTaskCard(task: ClientTaskOverview, onClick: () -> Unit, timeTaken: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 2.dp,
            color = getBorderColorBasedOnTaskPriority(task.taskPriority)
        ),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Task: ${task.taskName}", fontWeight = FontWeight.Bold)
            Text("Client ID: ${task.clientId}")
            Text("Task Points: ${task.taskEstimate}")
            Text("Task Cost: ${task.taskCost}")
            Text("Paid: ${task.taskPaidStatus}")
            Text("Task Type: ${task.taskType}")
            Text("Current Status: ${task.currentStatus}")
            Text("Created On: ${formatTimeStamp(task.taskCreationTime)}")
            Text("Time Taken: $timeTaken")
        }
    }
}
