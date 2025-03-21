package com.example.csks_creatives.presentation.employeeDetailsScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.domain.model.task.ClientTaskOverview

@Composable
fun EmployeeTaskCard(task: ClientTaskOverview, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Task: ${task.taskName}", fontWeight = FontWeight.Bold)
            Text("Client ID: ${task.clientId}")
            Text("Task Points: ${task.estimate}")
            Text("Current Status: ${task.currentStatus}")
            Text("Created On: ${task.taskCreationTime}")
        }
    }
}
