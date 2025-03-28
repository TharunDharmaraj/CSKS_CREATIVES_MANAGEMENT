package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.remote.AdminRepository
import com.example.csks_creatives.domain.repository.remote.TasksRepository
import com.example.csks_creatives.domain.utils.Utils.calculateFormattedTaskTakenTime
import com.example.csks_creatives.domain.utils.Utils.getActiveTasks
import com.example.csks_creatives.domain.utils.Utils.getCompletedTasks
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import com.example.csks_creatives.domain.utils.Utils.getFormattedTaskTakenTime
import com.example.csks_creatives.presentation.components.DateOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class TasksUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val adminRepository: AdminRepository
) : TasksUseCaseFactory {
    override fun create(): TasksUseCase {
        return TasksUseCase(tasksRepository, adminRepository)
    }

    override fun getTasksForClient(
        order: DateOrder,
        clientId: String
    ): Flow<ResultState<List<ClientTask>>> = flow {
        emit(ResultState.Loading)
        try {
            tasksRepository.getTasksForClient(clientId)
                .collect { tasks ->
                    val orderedTasks = if (order == DateOrder.Ascending) {
                        tasks.sortedBy { it.taskCreationTime }
                    } else {
                        tasks.sortedByDescending { it.taskCreationTime }
                    }
                    emit(ResultState.Success(orderedTasks))
                }
        } catch (exception: Exception) {
            emit(ResultState.Error("Failed to fetch tasks for client ${exception.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTasksForEmployee(
        employeeId: String,
        order: DateOrder
    ): Flow<ResultState<Pair<List<ClientTask>, List<ClientTask>>>> = flow {
        emit(ResultState.Loading)
        try {
            tasksRepository.getTasksForEmployee(employeeId)
                .collect { tasks ->
                    var activeTasks = tasks.getActiveTasks()
                    var completedTasks = tasks.getCompletedTasks()
                    when (order) {
                        DateOrder.Ascending -> {
                            activeTasks = activeTasks.sortedBy { it.taskCreationTime }
                            completedTasks = completedTasks.sortedBy { it.taskCreationTime }
                        }

                        DateOrder.Descending -> {
                            activeTasks = activeTasks.sortedByDescending { it.taskCreationTime }
                            completedTasks =
                                completedTasks.sortedByDescending { it.taskCreationTime }
                        }
                    }
                    emit(ResultState.Success(activeTasks to completedTasks))
                }
        } catch (exception: Exception) {
            emit(ResultState.Error("Failed to fetch tasks for employee ${exception.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createTask(task: ClientTask): ResultState<String> {
        return try {
            if (task.taskAttachment.isEmpty()) return ResultState.Error("Task Description Cannot Be Empty")
            if (task.taskName.isEmpty()) return ResultState.Error("Task Name Cannot Be Empty")
            if (task.clientId.isEmpty()) return ResultState.Error("Task Client Cannot be Empty")
            val currentTime = getCurrentTimeAsString()
            val taskToCreate = task.copy(
                taskId = currentTime,
                taskCreationTime = currentTime,
                currentStatus = TaskStatusType.BACKLOG,
                statusHistory = emptyList()
            )
            tasksRepository.createTask(taskToCreate)
            if (task.employeeId.isNotEmpty()) {
                // Write into employee collection, under inProgress
                adminRepository.addActiveTaskIntoEmployeeDetails(
                    employeeId = task.employeeId,
                    taskId = currentTime
                )
            }
            ResultState.Success("Task Created Successfully")
        } catch (exception: Exception) {
            ResultState.Error("Failed to create task ${exception.message}")
        }
    }

    override suspend fun getTask(taskId: String): Flow<ResultState<ClientTask>> = flow {
        emit(ResultState.Loading)
        try {
            tasksRepository.getTask(taskId).collect { task ->
                emit(ResultState.Success(task))
            }
        } catch (exception: Exception) {
            emit(ResultState.Error("Failed to get Task $taskId Tasks $exception"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getTaskOverView(taskId: String): Flow<ResultState<ClientTaskOverview>> =
        flow {
            emit(ResultState.Loading)
            try {
                tasksRepository.getTaskOverView(taskId).collect { taskOverView ->
                    emit(
                        ResultState.Success(taskOverView)
                    )
                }
            } catch (exception: Exception) {
                emit(ResultState.Error("Failed to get taskOverView $taskId Tasks $exception"))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun getAllActiveTasks(): Flow<ResultState<List<ClientTask>>> = flow {
        emit(ResultState.Loading)
        try {
            tasksRepository.getActiveTasks().collect { tasks ->
                emit(ResultState.Success(tasks))
            }
        } catch (exception: Exception) {
            emit(ResultState.Error("Failed to get All active Tasks $exception"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllBacklogTasks(): Flow<ResultState<List<ClientTask>>> = flow {
        emit(ResultState.Loading)
        try {
            tasksRepository.getTasksInBackLog().collect { tasks ->
                emit(ResultState.Success(tasks))
            }
        } catch (exception: Exception) {
            emit(ResultState.Error("Failed to get All Backlog Tasks"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllCompletedTasks(): Flow<ResultState<List<ClientTask>>> = flow {
        emit(ResultState.Loading)
        try {
            tasksRepository.getCompletedTasks().collect { tasks ->
                emit(ResultState.Success(tasks))
            }
        } catch (exception: Exception) {
            emit(ResultState.Error("Failed to get All Completed Tasks"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getUniqueTaskOverViewList(
        taskOverViewData: ClientTaskOverview,
        tasksList: List<ClientTaskOverview>
    ): List<ClientTaskOverview> {
        return tasksList.filter { it.taskId != taskOverViewData.taskId } + taskOverViewData
    }

    override fun removeCompletedTaskFromActiveList(
        completedTaskId: ClientTaskOverview,
        tasksInProgressList: List<ClientTaskOverview>
    ) = tasksInProgressList.filter { it.taskId != completedTaskId.taskId }

    override fun getTimeTakenForActiveTask(
        taskId: String,
        tasksInProgress: List<ClientTaskOverview>
    ): String {
        tasksInProgress.forEach { task ->
            return calculateFormattedTaskTakenTime(
                task.taskCreationTime,
                getCurrentTimeAsString()
            )
        }
        return "123456"
    }

    override fun getTimeTakenForCompletedTask(
        taskId: String,
        tasksCompleted: List<ClientTaskOverview>
    ): String {
        tasksCompleted.forEach { task ->
            if (task.taskId == taskId) {
                return calculateFormattedTaskTakenTime(
                    task.taskInProgressTime,
                    task.taskCompletedTime
                )
            }
        }
        return "123456"
    }

    override fun getTimeTakenForCompletedTask(clientTask: ClientTask): String {
        val inProgressEntry =
            clientTask.statusHistory.find { it.taskStatusType == TaskStatusType.IN_PROGRESS }
        val completedEntry =
            clientTask.statusHistory.find { it.taskStatusType == TaskStatusType.COMPLETED }

        if (inProgressEntry == null || completedEntry == null ||
            inProgressEntry.startTime.isEmpty() || completedEntry.startTime.isEmpty()
        ) {
            return "No In-Progress Time Available"
        }

        return try {
            val diffMillis = completedEntry.startTime.toLong() - inProgressEntry.startTime.toLong()
            getFormattedTaskTakenTime(diffMillis)
        } catch (e: Exception) {
            "0"
        }
    }
}