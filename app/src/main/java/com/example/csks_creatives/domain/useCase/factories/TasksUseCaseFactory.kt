package com.example.csks_creatives.domain.useCase.factories

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.TasksUseCase
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import kotlinx.coroutines.flow.Flow

interface TasksUseCaseFactory {
    fun create(): TasksUseCase

    suspend fun getClientName(clientId: String): String

    suspend fun editClientName(clientId: String, clientName: String): ResultState<String>

    fun getTasksForClient(order: DateOrder, clientId: String): Flow<ResultState<List<ClientTask>>>

    fun getTasksForEmployee(
        employeeId: String,
        order: DateOrder
    ): Flow<ResultState<Pair<List<ClientTask>, List<ClientTask>>>>

    fun getActiveTasksForEmployee(employeeId: String, order: DateOrder) : Flow<ResultState<List<ClientTask>>>

    fun getCompletedTasksForEmployee(employeeId: String, order: DateOrder) : Flow<ResultState<List<ClientTask>>>

    suspend fun createTask(task: ClientTask): ResultState<String>

    suspend fun getTask(taskId: String): Flow<ResultState<ClientTask>>

    suspend fun getTaskOverView(taskId: String): Flow<ResultState<ClientTaskOverview>>

    suspend fun getAllActiveTasks(): Flow<ResultState<List<ClientTask>>>

    suspend fun getAllBacklogTasks(): Flow<ResultState<List<ClientTask>>>

    suspend fun getAllCompletedTasks(): Flow<ResultState<List<ClientTask>>>

    fun getUniqueTaskOverViewList(
        taskOverViewData: ClientTaskOverview,
        tasksList: List<ClientTaskOverview>
    ): List<ClientTaskOverview>

    fun removeCompletedTaskFromActiveList(
        completedTaskId: ClientTaskOverview,
        tasksInProgressList: List<ClientTaskOverview>
    ): List<ClientTaskOverview>

    fun getTimeTakenForActiveTask(taskId: String, tasksInProgress: List<ClientTaskOverview>): String

    fun getTimeTakenForCompletedTask(
        taskId: String,
        tasksCompleted: List<ClientTaskOverview>
    ): String

    fun getTimeTakenForCompletedTask(clientTask: ClientTask): String
}