package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state

import com.example.csks_creatives.domain.model.task.*
import com.example.csks_creatives.domain.model.utills.enums.tasks.*
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class TaskDetailState(
    val taskId: String = EMPTY_STRING, // Id to the task to be Modified
    val taskTitle: String = EMPTY_STRING, // Edited only by Admin - Read only for Employee
    val taskDescription: String = EMPTY_STRING,  // Editable only for Admin - Read only for Employee
    val taskClientId: String = EMPTY_STRING, // Client ID of the task,  Editable only for Admin - Read only for Employee
    val taskAssignedTo: String = EMPTY_STRING, // EmployeeId of the task,  Editable only for Admin - Read only for Employee
    val taskEstimate: Int = 0, // Editable only for Admin - Read only for Employee
    val taskCost: Int = 0, // Editable and viewable only for admin
    val taskPaidStatus: TaskPaidStatus = TaskPaidStatus.NOT_PAID,
    val taskType: TaskType = TaskType.SHORTS_VIDEO, // Editable for admin, Viewable for employee
    val taskCurrentStatus: TaskStatusType = TaskStatusType.BACKLOG, // Read Only for Both Admin and Employee
    val taskStatusHistory: List<TaskStatusHistory> = emptyList(), // Status History - Read Only by both Admin and Employee
    val taskComments: List<Comment> = emptyList(), // List of Comments
)

fun TaskDetailState.toClientTask(): ClientTask {
    return ClientTask(
        taskId = this.taskId,
        clientId = this.taskClientId,
        employeeId = this.taskAssignedTo,
        taskName = this.taskTitle,
        taskAttachment = this.taskDescription,
        taskEstimate = this.taskEstimate,
        taskCost = this.taskCost,
        taskPaidStatus = this.taskPaidStatus,
        taskType = this.taskType,
        currentStatus = this.taskCurrentStatus,
        statusHistory = this.taskStatusHistory
    )
}
