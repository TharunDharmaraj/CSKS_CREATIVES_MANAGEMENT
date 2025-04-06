package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state

import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.utills.enums.tasks.*

data class DropDownListState(
    var clientsList: List<Client> = emptyList(),
    var employeeList: List<Employee> = emptyList(),
    var taskPaidStatusList: List<TaskPaidStatus> = TaskPaidStatus.entries,
    var taskTypeList: List<TaskType> = TaskType.entries,
    var taskPriority: List<TaskPriority> = TaskPriority.entries,
    var taskUploadOutput: List<TaskUploadOutput> = TaskUploadOutput.entries,
    var taskDirectionApp: List<TaskDirectionApp> = TaskDirectionApp.entries
)