package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.TaskStatusHistory
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.remote.AdminRepository
import com.example.csks_creatives.domain.repository.remote.TasksManipulationRepository
import com.example.csks_creatives.domain.useCase.factories.TasksManipulationUseCaseFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class TasksManipulationUseCase @Inject constructor(
    private val tasksManipulationRepository: TasksManipulationRepository,
    private val adminRepository: AdminRepository
) : TasksManipulationUseCaseFactory {
    override fun create(): TasksManipulationUseCase {
        return TasksManipulationUseCase(tasksManipulationRepository, adminRepository)
    }

    override suspend fun assignTaskToEmployee(
        taskId: String,
        employeeId: String
    ): ResultState<String> {
        return try {
            tasksManipulationRepository.assignTaskToEmployee(taskId, employeeId)
            ResultState.Success("Task assigned successfully to employee $employeeId")
        } catch (exception: Exception) {
            ResultState.Error("Failed to assign task to employee ${exception.message}")
        }
    }

    override suspend fun changeTaskStatus(
        taskId: String,
        status: TaskStatusType
    ): ResultState<String> {
        return try {
            tasksManipulationRepository.changeTaskStatus(taskId, status)
            ResultState.Success("Task status updated to ${status.name}")
        } catch (exception: Exception) {
            ResultState.Error("Failed to change task status ${exception.message}")
        }
    }

    override suspend fun getTaskStatusHistory(taskId: String): Flow<ResultState<List<TaskStatusHistory>>> =
        flow {
            emit(ResultState.Loading)
            try {
                tasksManipulationRepository.fetchTaskStatusHistory(taskId)
                    .collect { statusHistory ->
                        emit(ResultState.Success(statusHistory))
                    }
            } catch (exception: Exception) {
                emit(ResultState.Error("Failed to get statusHistory for $taskId Tasks $exception"))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun editTask(
        currentTask: ClientTask,
        initialTask: ClientTask
    ): ResultState<String> {
        return try {
            if (currentTask == ClientTask()) return ResultState.Error("No Changes done")
            if (currentTask == initialTask) return ResultState.Error("No changes done")
            if (currentTask.employeeId != initialTask.employeeId) {
                adminRepository.removeActiveTaskFromEmployeeDetails(
                    initialTask.employeeId,
                    initialTask.taskId
                )
                assignTaskToEmployee(
                    taskId = currentTask.taskId,
                    employeeId = currentTask.employeeId
                )
                adminRepository.addActiveTaskIntoEmployeeDetails(
                    currentTask.employeeId,
                    currentTask.taskId
                )
            }
            if (currentTask.currentStatus != initialTask.currentStatus) {
                changeTaskStatus(currentTask.taskId, currentTask.currentStatus)
                // (Only possible by ADMIN))Moving a task from Completed to Backlog/In Progress -> Delete from CompletedTasksList and Add into Active tasks list
                if (initialTask.currentStatus == TaskStatusType.COMPLETED) {
                    adminRepository.removeCompletedTaskFromEmployeeDetails(
                        currentTask.employeeId,
                        currentTask.taskId
                    )
                    adminRepository.addActiveTaskIntoEmployeeDetails(
                        currentTask.employeeId,
                        currentTask.taskId
                    )
                }
                // Moving a task from backlog to completed -> Delete from ActiveTasksList and Add into Completed tasks list
                if (currentTask.currentStatus == TaskStatusType.COMPLETED) {
                    adminRepository.removeActiveTaskFromEmployeeDetails(
                        currentTask.employeeId,
                        currentTask.taskId
                    )
                    adminRepository.addCompletedTaskIntoEmployeeDetails(
                        currentTask.employeeId,
                        currentTask.taskId
                    )
                }
            }
            tasksManipulationRepository.editTask(currentTask)
            ResultState.Success("Task '${currentTask.taskName}' updated successfully")
        } catch (exception: Exception) {
            ResultState.Error("Failed to edit task ${exception.message}")
        }
    }
}