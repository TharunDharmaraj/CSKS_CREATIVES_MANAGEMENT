package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.PaymentInfo
import com.example.csks_creatives.domain.model.task.TaskStatusHistory
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import kotlinx.coroutines.flow.Flow

interface TasksManipulationRepository {
    suspend fun assignTaskToEmployee(taskId: String, employeeId: String)

    suspend fun changeTaskStatus(taskId: String, status: TaskStatusType)

    suspend fun fetchTaskStatusHistory(taskId: String): Flow<List<TaskStatusHistory>>

    suspend fun editTask(task: ClientTask)

    suspend fun addPartialTaskAmount(taskId: String, paymentInfo: PaymentInfo)

    suspend fun markTaskAsFullyPaid(taskId: String)

    suspend fun removeTaskFromEmployeeDetails(employeeId: String, taskId: String)

    suspend fun deleteTaskFromTasksCollection(taskId: String, employeeId: String)
}