package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state

import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.task.ClientTask

data class AdminHomeScreenState(
    val employeeList: List<Employee> = emptyList(),
    val clientList: List<Client> = emptyList(),
    val activeTaskList: List<ClientTask> = emptyList(),
    val backlogTaskList: List<ClientTask> = emptyList(),
    val completedTasksList: List<ClientTask> = emptyList()
)
