package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.data.utils.Utils.toEmployeeItem
import com.example.csks_creatives.data.utils.Utils.toEmployeeList
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.data.utils.Constants.DEFAULT_TASK_FETCH_LIMIT
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.database.EmployeesLocalRepository
import com.example.csks_creatives.domain.repository.remote.AdminRepository
import com.example.csks_creatives.domain.repository.remote.TasksRepository
import com.example.csks_creatives.domain.useCase.factories.AdminUseCaseFactory
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import com.example.csks_creatives.domain.utils.SecurityUtils.encrypt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AdminUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
    private val employeesLocalRepository: EmployeesLocalRepository,
    private val tasksRepository: TasksRepository
) :
    AdminUseCaseFactory {
    private var lastForceFetchTime = 0L
    private val forceFetchCooldown = 10000L // 10 seconds

    private fun canPerformForceFetch(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastForceFetchTime < forceFetchCooldown) {
            return false
        }
        lastForceFetchTime = currentTime
        return true
    }

    override fun create(): AdminUseCase {
        return AdminUseCase(
            adminRepository = adminRepository,
            employeesLocalRepository = employeesLocalRepository,
            tasksRepository = tasksRepository
        )
    }

    override suspend fun createEmployee(employee: Employee): ResultState<String> {
        val buildEmployee = employee.copy(
            employeeId = employee.employeeName,
            joinedTime = getCurrentTimeAsString(),
            employeePassword = encrypt(employee.employeePassword),
            tasksInProgress = listOf(),
            tasksCompleted = listOf(),
            numberOfTasksCompleted = "0"
        )
        if (buildEmployee.employeeId.isBlank())
            return ResultState.Error("Employee ID cannot be empty")
        if (buildEmployee.employeeName.isBlank())
            return ResultState.Error("Employee Name cannot be empty")
        if (buildEmployee.employeePassword.isBlank())
            return ResultState.Error("Employee Password cannot be empty")

        return try {
            val canAddEmployee = adminRepository.checkEmployeeIdExists(buildEmployee.employeeName)
            if (canAddEmployee.not()) {
                adminRepository.createEmployee(
                    buildEmployee
                )
                ResultState.Success("Employee ${buildEmployee.employeeName} Created Successfully")
            } else {
                ResultState.Error("Employee ID ${buildEmployee.employeeId} already exists")
            }
        } catch (exception: Exception) {
            ResultState.Error("Failed to create employee: ${exception.message}")
        }
    }

    override suspend fun deleteEmployee(employeeId: String): ResultState<String> {
        if (employeeId.isBlank()) return ResultState.Error("Employee ID not found")
        return try {
            val isEmployeeExists = adminRepository.checkEmployeeIdExists(employeeId)
            if (isEmployeeExists) {
                adminRepository.deleteEmployee(employeeId)
                ResultState.Success("Successfully Deleted Employee")
            } else {
                ResultState.Error("Employee already deleted")
            }
        } catch (exception: Exception) {
            ResultState.Error("Failed to delete employee: ${exception.message}")
        }
    }

    override suspend fun getEmployeeDetails(employeeId: String, isForceFetch: Boolean, limit: Long?): Flow<ResultState<Employee>> =
        flow {
            if (isForceFetch && !canPerformForceFetch()) return@flow
            if (employeeId.isBlank()) emit(ResultState.Error("Could not get Employee Details"))
            try {
                val isEmployeeExists = adminRepository.checkEmployeeIdExists(employeeId)
                if (isEmployeeExists) {
                    val finalLimit = if (isForceFetch) null else (limit ?: DEFAULT_TASK_FETCH_LIMIT)
                    adminRepository.getEmployeeDetails(employeeId, finalLimit).collect { employeeDetails ->
                        emit(ResultState.Success(employeeDetails))
                    }
                } else {
                    emit(ResultState.Error("Employee Not Exists"))
                }
            } catch (exception: Exception) {
                emit(ResultState.Error("Failed to fetch employee details: ${exception.message}"))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun getEmployeesList(isForceFetch: Boolean, limit: Long?): ResultState<List<Employee>> {
        if (isForceFetch && limit != null && !canPerformForceFetch()) {
            return ResultState.Error("Please wait 10 seconds between force fetches")
        }
        return try {
            withContext(Dispatchers.IO) {
                // If we are not force fetching, and we have a limit, we can try local first.
                // But if limit is null (requesting ALL), the local cache might be incomplete,
                // so we should fetch from remote to ensure we have the full list.
                if (isForceFetch.not() && limit != null) {
                    val employeesList = employeesLocalRepository.getEmployees().toEmployeeList()
                    if (employeesList.isNotEmpty() && employeesList.size >= limit) {
                        return@withContext ResultState.Success(employeesList.take(limit.toInt()))
                    }
                }
                
                // If limit is null, fetch everything.
                val finalLimit = if (isForceFetch) null else limit
                val employeesList = adminRepository.getEmployees(finalLimit)
                if (employeesList.isEmpty()) ResultState.Error("No Employees Found")
                
                // If we fetched ALL (limit == null) or it's a force fetch, sync the local cache.
                if (isForceFetch || limit == null) {
                    // 1. Get all local employees to build a map of existing counts
                    val localEmployees = employeesLocalRepository.getEmployees()
                    val localCountMap = localEmployees.associate { it.employeeId to it.numberOfTasksCompleted }
                    
                    // 2. Map remote employees, preserving local counts if Firestore count is empty
                    val employeesWithLocalCounts = employeesList.map { remoteEmployee ->
                        val remoteCount = remoteEmployee.numberOfTasksCompleted
                        val localCount = localCountMap[remoteEmployee.employeeId]
                        
                        // Priority: 1. Remote count (if not empty), 2. Local count, 3. Default "0"
                        val finalCount = when {
                            remoteCount.isNotEmpty() && remoteCount != "0" -> remoteCount
                            else -> localCount ?: "0"
                        }
                        
                        remoteEmployee.copy(numberOfTasksCompleted = finalCount)
                    }
                    
                    // 3. Update Room (Insert with REPLACE strategy)
                    employeesWithLocalCounts.forEach { employee ->
                        employeesLocalRepository.insert(employee.toEmployeeItem())
                    }

                    // 4. Return the list with local counts merged in
                    return@withContext ResultState.Success(employeesWithLocalCounts)
                }
                return@withContext ResultState.Success(employeesList)
            }
        } catch (e: Exception) {
            return ResultState.Error("Failed to fetch employees: ${e.message}")
        }
    }

    override suspend fun getAllActiveLeaveRequests(isForceFetch: Boolean): Flow<ResultState<List<LeaveRequest>>> =
        flow {
            if (isForceFetch && !canPerformForceFetch()) return@flow
            try {
                adminRepository.getAllActiveLeaveRequests(limit = null).collect { leaveRequestList ->
                    emit(ResultState.Success(leaveRequestList))
                }
            } catch (exception: Exception) {
                emit(ResultState.Error("Error ${exception.message}  fetching Active Leave Requests"))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun markLeaveRequestAsApproved(leaveRequest: LeaveRequest): ResultState<String> {
        try {
            adminRepository.markLeaveRequestAsApproved(
                employeeId = leaveRequest.postedBy,
                leaveRequestId = leaveRequest.leaveRequestId
            )
            return ResultState.Success("Leave Approved")
        } catch (exception: Exception) {
            return ResultState.Error("Error ${exception.message}  in Approval")
        }
    }

    override suspend fun markLeaveRequestAsRejected(leaveRequest: LeaveRequest): ResultState<String> {
        try {
            adminRepository.markLeaveRequestAsRejected(
                employeeId = leaveRequest.postedBy,
                leaveRequestId = leaveRequest.leaveRequestId
            )
            return ResultState.Success("Leave Rejected")
        } catch (exception: Exception) {
            return ResultState.Error("Error ${exception.message}  in Rejection")
        }
    }

    override suspend fun getLocalEmployeeCompletedTasksCount(employeeId: String): String {
        return employeesLocalRepository.getEmployeeById(employeeId)?.numberOfTasksCompleted ?: "0"
    }

    override suspend fun forceUpdateEmployeeCompletedTasksCount(employeeId: String): ResultState<Long> {
        return try {
            val count = tasksRepository.getCompletedTasksCountForEmployee(employeeId)
            employeesLocalRepository.updateCompletedTasksCount(employeeId, count.toString())
            ResultState.Success(count)
        } catch (e: Exception) {
            ResultState.Error("Failed to update completed tasks count: ${e.message}")
        }
    }
}