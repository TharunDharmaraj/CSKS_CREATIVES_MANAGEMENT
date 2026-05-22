package com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state

import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.data.utils.Constants.DEFAULT_TASK_FETCH_LIMIT
import com.example.csks_creatives.presentation.components.sealed.DateOrder

data class EmployeeHomeScreenState(
    val activeTasks: List<ClientTask> = emptyList(),
    val isActiveTasksEndReached: Boolean = false,
    val activeTasksLimit: Long = DEFAULT_TASK_FETCH_LIMIT,

    val completedTasks: List<ClientTask> = emptyList(),
    val isCompletedTasksEndReached: Boolean = false,
    val completedTasksLimit: Long = DEFAULT_TASK_FETCH_LIMIT,

    val backlogTasks: List<ClientTask> = emptyList(),
    val isBacklogTasksEndReached: Boolean = false,
    val backlogTasksLimit: Long = DEFAULT_TASK_FETCH_LIMIT,

    val tasksOrder: DateOrder = DateOrder.Descending,
    val isOrderByToggleVisible: Boolean = true,
    val isAddLeaveDialogVisible: Boolean = false,
    val isCompletedTasksLoading: Boolean = false,
    val isActiveTasksLoading: Boolean = true, // Default to true to show initial loader
    val isBacklogTasksLoading: Boolean = false,

    val approvedLeaves: List<LeaveRequest> = emptyList(),
    val unApprovedLeaves: List<LeaveRequest> = emptyList(),
    val rejectedLeaves: List<LeaveRequest> = emptyList(),

    val isPaginationLoading: Boolean = false,
    
    val employeeName: String = "",
    val employeeJoinedTime: String = "",
    val employeePassword: String = "",
    val totalNumberOfTasksCompleted: String = "0",
    val isCompletedCountLoading: Boolean = false
)