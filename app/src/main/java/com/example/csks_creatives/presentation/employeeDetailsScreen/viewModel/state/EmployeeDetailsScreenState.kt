package com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.state

import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.presentation.components.sealed.DateOrder

data class EmployeeDetailsScreenState(
    val searchTextForCompleted: String = EMPTY_STRING,
    val searchTextForActive: String = EMPTY_STRING,
    val employeeName: String = EMPTY_STRING,
    val employeePassword: String = EMPTY_STRING,
    val employeeJoinedTime: String = EMPTY_STRING,
    val totalNumberOfTasksCompleted: String = EMPTY_STRING,
    val completedTasksOrder: DateOrder = DateOrder.Ascending,
    val isSearchBarVisible: Boolean = true,
    val isCompletedTasksSectionVisible: Boolean = true,
    val isActiveTasksSectionVisible: Boolean = true,
    val tasksCompleted: List<ClientTaskOverview> = emptyList(),
    val tasksInProgress: List<ClientTaskOverview> = emptyList(),
    val approvedLeavesList: List<LeaveRequest> = emptyList(),
    val unApprovedLeavesList: List<LeaveRequest> = emptyList(),
    val rejectedLeavesList: List<LeaveRequest> = emptyList()
)
