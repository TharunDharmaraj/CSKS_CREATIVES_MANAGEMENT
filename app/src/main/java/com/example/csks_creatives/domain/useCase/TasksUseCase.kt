package com.example.csks_creatives.domain.useCase

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.remote.*
import com.example.csks_creatives.domain.useCase.factories.TasksUseCaseFactory
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.domain.utils.Utils.getActiveTasks
import com.example.csks_creatives.domain.utils.Utils.getCompletedTasks
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.Duration
import javax.inject.Inject

class TasksUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val adminRepository: AdminRepository,
    private val clientsRepository: ClientsRepository
) : TasksUseCaseFactory {
    override fun create(): TasksUseCase {
        return TasksUseCase(tasksRepository, adminRepository, clientsRepository)
    }

    override suspend fun getClientName(clientId: String) = clientsRepository.getClientName(clientId)

    override suspend fun editClientName(
        clientId: String,
        clientName: String
    ): ResultState<String> {
        return try {
            clientsRepository.editClientName(clientId, clientName)
            ResultState.Success("Client Name Updated Successfully")
        } catch (exception: Exception) {
            ResultState.Error("Exception in Updating ClientName: $exception")
        }
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

    override fun getActiveTasksForEmployee(
        employeeId: String,
        order: DateOrder
    ): Flow<ResultState<List<ClientTask>>> = flow {
        emit(ResultState.Loading)
        try {
            tasksRepository.getActiveTasksForEmployee(employeeId)
                .collect { tasks ->
                    val sorted = when (order) {
                        DateOrder.Ascending -> tasks.sortedBy { it.taskCreationTime }
                        DateOrder.Descending -> tasks.sortedByDescending { it.taskCreationTime }
                    }
                    emit(ResultState.Success(sorted))
                }
        } catch (exception: Exception) {
            emit(ResultState.Error("Failed to fetch active tasks: ${exception.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getCompletedTasksForEmployee(
        employeeId: String,
        order: DateOrder
    ): Flow<ResultState<List<ClientTask>>> = flow {
        emit(ResultState.Loading)
        try {
            tasksRepository.getCompletedTasksForEmployee(employeeId)
                .collect { tasks ->
                    val sorted = when (order) {
                        DateOrder.Ascending -> tasks.sortedBy { it.taskCreationTime }
                        DateOrder.Descending -> tasks.sortedByDescending { it.taskCreationTime }
                    }
                    emit(ResultState.Success(sorted))
                }
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to fetch completed tasks: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun createTask(task: ClientTask): ResultState<String> {
        return try {
            if (task.taskAttachment.isEmpty()) return ResultState.Error("Task Description Cannot Be Empty")
            if (task.taskName.isEmpty()) return ResultState.Error("Task Name Cannot Be Empty")
            if (task.clientId.isEmpty()) return ResultState.Error("Task Client Cannot be Empty")
            if (task.taskEstimate == 0) return ResultState.Error("Task Estimate Cannot be 0")
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
            emit(ResultState.Error("Failed to get Task $taskId Tasks ${exception.message} "))
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
                emit(ResultState.Error("Failed to get taskOverView $taskId Tasks ${exception.message} "))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun getAllActiveTasks(tasksOrder: DateOrder): Flow<ResultState<List<ClientTask>>> {
        return getTasksWithOrder({ tasksRepository.getActiveTasks() }, tasksOrder, "active")
    }

    override suspend fun getAllBacklogTasks(tasksOrder: DateOrder): Flow<ResultState<List<ClientTask>>> {
        return getTasksWithOrder({ tasksRepository.getTasksInBackLog() }, tasksOrder, "backlog")
    }

    override suspend fun getAllCompletedTasks(tasksOrder: DateOrder): Flow<ResultState<List<ClientTask>>> {
        return getTasksWithOrder({ tasksRepository.getCompletedTasks() }, tasksOrder, "completed")
    }

    private fun getTasksWithOrder(
        fetchTasks: suspend () -> Flow<List<ClientTask>>,
        order: DateOrder,
        tag: String
    ): Flow<ResultState<List<ClientTask>>> = flow {
        emit(ResultState.Loading)
        try {
            fetchTasks().collect { tasks ->
                val sorted = when (order) {
                    DateOrder.Descending -> tasks.sortedByDescending { it.taskCreationTime }
                    DateOrder.Ascending -> tasks.sortedBy { it.taskCreationTime }
                }
                emit(ResultState.Success(sorted))
            }
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to get $tag tasks: ${e.message}"))
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

    @SuppressLint("NewApi")
    override fun getTimeTakenForActiveTask(
        taskId: String,
        tasksInProgress: List<ClientTaskOverview>
    ): String {
        if (tasksInProgress.isNotEmpty()) {
            tasksInProgress.forEach { task ->
                if (task.taskId == taskId && task.taskCreationTime.isNotEmpty()) {
                    return formatDuration(task.taskElapsedTime)
                }
            }
        }
        return "123456"
    }

    @SuppressLint("NewApi")
    override fun getTimeTakenForCompletedTask(
        taskId: String,
        tasksCompleted: List<ClientTaskOverview>
    ): String {
        tasksCompleted.forEach { task ->
            if (task.taskId == taskId) {
                return formatDuration(task.taskElapsedTime)
            }
        }
        return "123456789"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getTimeTakenForCompletedTask(clientTask: ClientTask): String {
        if (clientTask.currentStatus == TaskStatusType.COMPLETED) {
            val intermediateStatuses = clientTask.statusHistory.filter {
                it.taskStatusType != TaskStatusType.BACKLOG &&
                        it.taskStatusType != TaskStatusType.COMPLETED
            }
            // Added for backward compatibility
            val revisions =
                clientTask.statusHistory.filter { it.taskStatusType.order > 99 && it.elapsedTime == 0L }
            val pausedStatus =
                clientTask.statusHistory.find { it.taskStatusType == TaskStatusType.PAUSED }

            if (intermediateStatuses.isNotEmpty()) {
                val totalElapsedTime =
                    intermediateStatuses.sumOf { it.elapsedTime } + revisions.sumOf { it.endTime.toLong() - it.startTime.toLong() } - (pausedStatus?.elapsedTime
                        ?: 0L)
                return formatDuration(totalElapsedTime)
            } else {
                // Empty, No Intermediate Status
                return "No In-Progress Time Available"
            }
        } else return EMPTY_STRING
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDuration(totalElapsedTime: Long): String {
        if (totalElapsedTime <= 0) return "Less than a minute"

        val duration = Duration.ofMillis(totalElapsedTime)
        val days = duration.toDays()
        val hours = (duration.toHours() % 24)
        val minutes = (duration.toMinutes() % 60)

        return buildString {
            if (days > 0) append("$days day${if (days > 1) "s" else ""} ")
            if (hours > 0) append("$hours hr${if (hours > 1) "s" else ""} ")
            if (minutes > 0) append("$minutes min${if (minutes > 1) "s" else ""}")
        }.trim().ifEmpty { "Less than a minute" }
    }
}