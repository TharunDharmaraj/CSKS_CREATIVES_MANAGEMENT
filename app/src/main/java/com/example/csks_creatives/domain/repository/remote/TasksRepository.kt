package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    suspend fun createTask(task: ClientTask)

    suspend fun getTask(taskId: String): Flow<ClientTask>

    suspend fun getTaskOverView(taskId: String): Flow<ClientTaskOverview>

    fun getTasksForClient(clientId: String, limit: Long? = null): Flow<List<ClientTask>>

    suspend fun getTasksForEmployee(employeeId: String, limit: Long? = null): Flow<List<ClientTask>>

    suspend fun getActiveTasksForEmployee(employeeId: String, limit: Long? = null): Flow<List<ClientTask>>

    suspend fun getCompletedTasksForEmployee(employeeId: String, limit: Long? = null): Flow<List<ClientTask>>

    suspend fun getCompletedTasksCountForEmployee(employeeId: String): Long

    suspend fun getBacklogTasksForEmployee(employeeId: String, limit: Long? = null): Flow<List<ClientTask>>

    suspend fun getActiveTasks(limit: Long? = null): Flow<List<ClientTask>>

    suspend fun getCompletedTasks(limit: Long? = null): Flow<List<ClientTask>>

    suspend fun getTasksInBackLog(limit: Long? = null): Flow<List<ClientTask>>
}