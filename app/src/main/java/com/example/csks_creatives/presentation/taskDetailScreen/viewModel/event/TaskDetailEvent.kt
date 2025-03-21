package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event

import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskType

sealed class TaskDetailEvent {
    object CreateTask : TaskDetailEvent() // Only Admin should be able to Create a Task
    object SaveTask : TaskDetailEvent() // On Clicking Add comment button
    object AddComment : TaskDetailEvent() // On Clicking Add comment button
    data class TaskTitleTextFieldChanged(val taskTitle: String) : TaskDetailEvent()
    data class TaskDescriptionTextFieldChanged(val taskDescription: String) : TaskDetailEvent()
    data class TaskEstimateChanged(val taskEstimate: Int) : TaskDetailEvent()
    data class TaskCostChanged(val taskCost: Int) : TaskDetailEvent()
    data class TaskPaidStatusChanged(val paidStatus: TaskPaidStatus): TaskDetailEvent()
    data class TaskClientIdChanged(val clientId: String) : TaskDetailEvent()
    data class TaskAssignedToEmployeeChanged(val employeeId: String) : TaskDetailEvent()
    data class TaskTypeChanged(val taskType: TaskType) : TaskDetailEvent()
    data class TaskStatusTypeChanged(val taskStatusType: TaskStatusType) : TaskDetailEvent()
}