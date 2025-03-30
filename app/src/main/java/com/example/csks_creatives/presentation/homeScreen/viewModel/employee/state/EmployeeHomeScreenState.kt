package com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state

import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.presentation.components.sealed.DateOrder

data class EmployeeHomeScreenState(
    val activeTasks: List<ClientTask> = emptyList(),
    val completedTasks: List<ClientTask> = emptyList(),
    val isActiveTasksSectionVisible: Boolean = true,
    val isCompletedTasksSectionVisible: Boolean = false,
    val tasksOrder: DateOrder = DateOrder.Descending,
    val isOrderByToggleVisible: Boolean = true,
    val isAddLeaveDialogVisible: Boolean = false,
    val isLoading: Boolean = false,
    val approvedLeaves: List<LeaveRequest> = emptyList(),
    val rejectedLeaves: List<LeaveRequest> = emptyList(),
)