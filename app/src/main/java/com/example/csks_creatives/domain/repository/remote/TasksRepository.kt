package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    suspend fun createTask(task: ClientTask)

    suspend fun getTask(taskId: String): Flow<ClientTask>

    suspend fun getTaskOverView(taskId: String): Flow<ClientTaskOverview>

    fun getTasksForClient(clientId: String): Flow<List<ClientTask>>

    suspend fun getTasksForEmployee(employeeId: String): Flow<List<ClientTask>>

    suspend fun getActiveTasks(): Flow<List<ClientTask>>

    suspend fun getCompletedTasks(): Flow<List<ClientTask>>

    suspend fun getTasksInBackLog(): Flow<List<ClientTask>>
}