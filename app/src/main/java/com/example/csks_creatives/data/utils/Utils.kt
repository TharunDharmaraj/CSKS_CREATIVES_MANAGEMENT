package com.example.csks_creatives.data.utils

import com.example.csks_creatives.data.database.ClientItem
import com.example.csks_creatives.data.database.EmployeeItem
import com.example.csks_creatives.data.utils.Constants.BACKLOG
import com.example.csks_creatives.data.utils.Constants.COMPLETED
import com.example.csks_creatives.data.utils.Constants.IN_PROGRESS
import com.example.csks_creatives.data.utils.Constants.IN_REVIEW
import com.example.csks_creatives.data.utils.Constants.IN_REVISION
import com.example.csks_creatives.data.utils.Constants.PAUSED
import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType

object Utils {
    fun convertStringStatusToStatusType(status: String): TaskStatusType {
        return when (status) {
            BACKLOG -> TaskStatusType.BACKLOG
            IN_PROGRESS -> TaskStatusType.IN_PROGRESS
            IN_REVIEW -> TaskStatusType.IN_REVIEW
            IN_REVISION -> TaskStatusType.IN_REVISION
            PAUSED -> TaskStatusType.PAUSED
            COMPLETED -> TaskStatusType.COMPLETED
            else -> TaskStatusType.BACKLOG
        }
    }

    fun convertStatusTypeToString(taskStatusType: TaskStatusType): String {
        return when (taskStatusType) {
            TaskStatusType.BACKLOG -> BACKLOG
            TaskStatusType.IN_PROGRESS -> IN_PROGRESS
            TaskStatusType.IN_REVIEW -> IN_REVIEW
            TaskStatusType.IN_REVISION -> IN_REVISION
            TaskStatusType.PAUSED -> PAUSED
            TaskStatusType.COMPLETED -> COMPLETED

            // Added for backward compatibility with version 1.0
            TaskStatusType.REVISION_1 -> TODO()
            TaskStatusType.REVISION_2 -> TODO()
            TaskStatusType.REVISION_3 -> TODO()
            TaskStatusType.REVISION_4 -> TODO()
            TaskStatusType.REVISION_5 -> TODO()
            TaskStatusType.REVISION_6 -> TODO()
            TaskStatusType.REVISION_7 -> TODO()
        }
    }

    fun Client.toClientItem() = ClientItem(
        clientName = this.clientName,
        clientId = this.clientId
    )

    fun List<ClientItem>.toClientList(): List<Client> {
        val clientsList = mutableListOf<Client>()
        this.forEach { clientItem ->
            clientsList.add(clientItem.toClient())
        }
        return clientsList
    }

    private fun ClientItem.toClient() = Client(
        clientName = this.clientName,
        clientId = this.clientId
    )

    fun List<EmployeeItem>.toEmployeeList(): List<Employee> {
        val employeesList = mutableListOf<Employee>()
        this.forEach { employeeItem ->
            employeesList.add(employeeItem.toEmployee())
        }
        return employeesList
    }

    private fun EmployeeItem.toEmployee() = Employee(
        employeeId = this.employeeId,
        employeeName = this.employeeName,
        employeePassword = this.employeePassword,
        joinedTime = this.joinedTime,
        tasksInProgress = this.tasksInProgress,
        tasksCompleted = this.tasksCompleted,
        numberOfTasksCompleted = this.numberOfTasksCompleted
    )

    fun Employee.toEmployeeItem() = EmployeeItem(
        employeeId = this.employeeId,
        employeeName = this.employeeName,
        employeePassword = this.employeePassword,
        joinedTime = this.joinedTime,
        tasksInProgress = this.tasksInProgress,
        tasksCompleted = this.tasksCompleted,
        numberOfTasksCompleted = this.numberOfTasksCompleted
    )
}