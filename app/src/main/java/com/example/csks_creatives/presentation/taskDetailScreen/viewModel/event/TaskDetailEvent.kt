package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event

import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType

sealed class TaskDetailEvent {
    object CreateTask : TaskDetailEvent() // Only Admin should be able to Create a Task
    object SaveTask : TaskDetailEvent() // On Clicking Add comment button
    object AddComment : TaskDetailEvent() // On Clicking Add comment button
    data class TaskTitleTextFieldChanged(val taskTitle: String) : TaskDetailEvent()
    data class TaskDescriptionTextFieldChanged(val taskDescription: String) : TaskDetailEvent()
    data class TaskClientIdChanged(val clientId: String) : TaskDetailEvent()
    data class TaskAssignedToEmployeeChanged(val employeeId: String) : TaskDetailEvent()
    data class TaskStoryPointsChanged(val storyPoints: Int) : TaskDetailEvent()
    data class TaskStatusTypeChanged(val taskStatusType: TaskStatusType) : TaskDetailEvent()
}