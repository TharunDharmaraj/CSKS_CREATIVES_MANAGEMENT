package com.example.csks_creatives.presentation.components.helper

import androidx.compose.ui.graphics.Color
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPriority

object ColorHelper {
    fun getBorderColorBasedOnTaskPriority(taskPriority: TaskPriority, isTaskCompleted: Boolean = false): Color {
        if(isTaskCompleted) return Color.Green.copy(alpha = 0.5f)
        return when (taskPriority) {
            TaskPriority.CRITICAL -> Color.Magenta
            TaskPriority.HIGH -> Color.Red
            TaskPriority.MEDIUM -> Color.Yellow
            TaskPriority.LOW -> Color.LightGray
        }
    }
}