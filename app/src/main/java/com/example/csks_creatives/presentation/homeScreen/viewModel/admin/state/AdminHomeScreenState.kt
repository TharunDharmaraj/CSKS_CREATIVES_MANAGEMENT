package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state

import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.data.utils.Constants.DEFAULT_TASK_FETCH_LIMIT
import com.example.csks_creatives.presentation.components.sealed.DateOrder

data class AdminHomeScreenState(
    val employeeList: List<Employee> = emptyList(),
    val isEmployeesEndReached: Boolean = false,
    val employeesLimit: Long = DEFAULT_TASK_FETCH_LIMIT,

    val clientList: List<Client> = emptyList(),
    val isClientsEndReached: Boolean = false,

    val activeTaskList: List<ClientTask> = emptyList(),
    val isActiveTasksEndReached: Boolean = false,
    val activeTasksLimit: Long = DEFAULT_TASK_FETCH_LIMIT,

    val tasksOrder: DateOrder = DateOrder.Descending,

    val backlogTaskList: List<ClientTask> = emptyList(),
    val isBacklogTasksEndReached: Boolean = false,
    val backlogTasksLimit: Long = DEFAULT_TASK_FETCH_LIMIT,

    val completedTasksList: List<ClientTask> = emptyList(),
    val isCompletedTasksEndReached: Boolean = false,
    val completedTasksLimit: Long = DEFAULT_TASK_FETCH_LIMIT,

    val activeLeaveRequests: List<LeaveRequest> = emptyList(),

    val isPaginationLoading: Boolean = false
)
