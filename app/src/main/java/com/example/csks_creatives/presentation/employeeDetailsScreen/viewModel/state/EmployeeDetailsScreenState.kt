package com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.state

import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.presentation.components.DateOrder

data class EmployeeDetailsScreenState(
    val searchText: String = EMPTY_STRING,
    val employeeName: String = EMPTY_STRING,
    val employeePassword: String = EMPTY_STRING,
    val employeeJoinedTime: String = EMPTY_STRING,
    val totalNumberOfTasksCompleted: String = EMPTY_STRING,
    val completedTasksOrder: DateOrder = DateOrder.Ascending,
    val isSearchBarVisible: Boolean = false,
    val isCompletedTasksSectionVisible: Boolean = true,
    val isActiveTasksSectionVisible: Boolean = true,
    val tasksCompleted: List<ClientTaskOverview> = emptyList(),
    val tasksInProgress: List<ClientTaskOverview> = emptyList(),
)
