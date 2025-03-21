package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.TaskStatusHistory
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import kotlinx.coroutines.flow.Flow

interface TasksManipulationUseCaseFactory {
    fun create(): TasksManipulationUseCase

    suspend fun assignTaskToEmployee(taskId: String, employeeId: String): ResultState<String>

    suspend fun changeTaskStatus(taskId: String, status: TaskStatusType): ResultState<String>

    suspend fun getTaskStatusHistory(taskId: String): Flow<ResultState<List<TaskStatusHistory>>>

    suspend fun editTask(currentTask: ClientTask, initialTask: ClientTask): ResultState<String>
}