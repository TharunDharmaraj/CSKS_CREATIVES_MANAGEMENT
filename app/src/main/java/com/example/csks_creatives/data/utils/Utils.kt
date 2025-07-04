package com.example.csks_creatives.data.utils

import com.example.csks_creatives.data.database.ClientItem
import com.example.csks_creatives.data.database.EmployeeItem
import com.example.csks_creatives.data.utils.Constants.BACKLOG
import com.example.csks_creatives.data.utils.Constants.COMPLETED
import com.example.csks_creatives.data.utils.Constants.IN_PROGRESS
import com.example.csks_creatives.data.utils.Constants.IN_REVIEW
import com.example.csks_creatives.data.utils.Constants.REVISION_1
import com.example.csks_creatives.data.utils.Constants.REVISION_3
import com.example.csks_creatives.data.utils.Constants.REVISION_2
import com.example.csks_creatives.data.utils.Constants.REVISION_4
import com.example.csks_creatives.data.utils.Constants.REVISION_5
import com.example.csks_creatives.data.utils.Constants.REVISION_6
import com.example.csks_creatives.data.utils.Constants.REVISION_7
import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType

object Utils {
    fun convertStringStatusToStatusType(status: String): TaskStatusType {
        return when (status) {
            BACKLOG -> TaskStatusType.BACKLOG
            IN_PROGRESS -> TaskStatusType.IN_PROGRESS
            IN_REVIEW -> TaskStatusType.IN_REVIEW
            REVISION_1 -> TaskStatusType.REVISION_1
            REVISION_2 -> TaskStatusType.REVISION_2
            REVISION_3 -> TaskStatusType.REVISION_3
            REVISION_4 -> TaskStatusType.REVISION_4
            REVISION_5 -> TaskStatusType.REVISION_5
            REVISION_6 -> TaskStatusType.REVISION_6
            REVISION_7 -> TaskStatusType.REVISION_7
            COMPLETED -> TaskStatusType.COMPLETED
            else -> TaskStatusType.BACKLOG
        }
    }

    fun convertStatusTypeToString(taskStatusType: TaskStatusType): String {
        return when (taskStatusType) {
            TaskStatusType.BACKLOG -> BACKLOG
            TaskStatusType.IN_PROGRESS -> IN_PROGRESS
            TaskStatusType.IN_REVIEW -> IN_REVIEW
            TaskStatusType.REVISION_1 -> REVISION_1
            TaskStatusType.REVISION_2 -> REVISION_2
            TaskStatusType.REVISION_3 -> REVISION_3
            TaskStatusType.REVISION_4 -> REVISION_4
            TaskStatusType.REVISION_5 -> REVISION_5
            TaskStatusType.REVISION_6 -> REVISION_6
            TaskStatusType.REVISION_7 -> REVISION_7
            TaskStatusType.COMPLETED -> COMPLETED
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