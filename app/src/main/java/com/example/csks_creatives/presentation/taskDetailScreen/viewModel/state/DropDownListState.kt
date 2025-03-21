package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state

import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskType

data class DropDownListState(
    var clientsList: List<Client> = emptyList(),
    var employeeList: List<Employee> = emptyList(),
    var taskPaidStatusList: List<TaskPaidStatus> = TaskPaidStatus.entries,
    var taskTypeList: List<TaskType> = TaskType.entries
)