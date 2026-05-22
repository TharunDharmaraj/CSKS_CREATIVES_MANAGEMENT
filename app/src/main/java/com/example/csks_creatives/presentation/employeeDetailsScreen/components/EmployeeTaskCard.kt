package com.example.csks_creatives.presentation.employeeDetailsScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.presentation.components.*
import com.example.csks_creatives.presentation.components.helper.ColorHelper.getBorderColorBasedOnTaskPriority
import com.example.csks_creatives.presentation.components.ui.ModernDateView

@Composable
fun EmployeeTaskCard(task: ClientTaskOverview, onClick: () -> Unit, timeTaken: String) {
    val priorityColor = getBorderColorBasedOnTaskPriority(task.taskPriority)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(2.dp, priorityColor.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.taskName,
                    style = MaterialTheme.typography.titleMedium,
                    color = white,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = priorityColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = task.taskPriority.name,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = task.taskType.icon,
                    contentDescription = null,
                    tint = silverGrey.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${task.taskType.displayName} • ${task.taskEstimate}h",
                    fontSize = 14.sp,
                    color = silverGrey
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = silverGrey.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.taskPaidStatus.name,
                    fontSize = 14.sp,
                    color = if (task.taskPaidStatus.name.contains("PAID")) limeGreen else silverGrey
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Status: ${task.currentStatus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = vividCerulean
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ModernDateView(task.taskCreationTime)
                }

                if (timeTaken.isNotEmpty()) {
                    Text(
                        text = "Time: $timeTaken",
                        color = goldenRod,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
