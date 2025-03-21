package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state

import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.utills.enums.TaskType

data class DropDownListState(
    var clientsList: List<Client> = emptyList(),
    var employeeList: List<Employee> = emptyList(),
    var taskTypeList: List<TaskType> = listOf(TaskType.SHORTS_VIDEO, TaskType.LONG_VIDEO)
)