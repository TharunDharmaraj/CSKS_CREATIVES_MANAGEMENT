package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.data.utils.Utils.toEmployeeItem
import com.example.csks_creatives.data.utils.Utils.toEmployeeList
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.database.EmployeesLocalRepository
import com.example.csks_creatives.domain.repository.remote.AdminRepository
import com.example.csks_creatives.domain.useCase.factories.AdminUseCaseFactory
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AdminUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
    private val employeesLocalRepository: EmployeesLocalRepository
) :
    AdminUseCaseFactory {
    override fun create(): AdminUseCase {
        return AdminUseCase(
            adminRepository = adminRepository,
            employeesLocalRepository = employeesLocalRepository
        )
    }

    override suspend fun createEmployee(employee: Employee): ResultState<String> {
        val buildEmployee = employee.copy(
            employeeId = employee.employeeName,
            joinedTime = getCurrentTimeAsString(),
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

    override suspend fun getEmployeeDetails(employeeId: String): Flow<ResultState<Employee>> =
        flow {
            if (employeeId.isBlank()) emit(ResultState.Error("Could not get Employee Details"))
            try {
                val isEmployeeExists = adminRepository.checkEmployeeIdExists(employeeId)
                if (isEmployeeExists) {
                    adminRepository.getEmployeeDetails(employeeId).collect { employeeDetails ->
                        emit(ResultState.Success(employeeDetails))
                    }
                } else {
                    emit(ResultState.Error("Employee Not Exists"))
                }
            } catch (exception: Exception) {
                emit(ResultState.Error("Failed to fetch employee details: ${exception.message}"))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun getEmployeesList(isForceFetchFromServer: Boolean): ResultState<List<Employee>> {
        return try {
            withContext(Dispatchers.IO) {
                if (isForceFetchFromServer.not()) {
                    val employeesList = employeesLocalRepository.getEmployees().toEmployeeList()
                    if (employeesList.isNotEmpty()) {
                        return@withContext ResultState.Success(employeesList)
                    }
                }
                val employeesList = adminRepository.getEmployees()
                if (employeesList.isEmpty()) ResultState.Error("No Employees Found")
                employeesLocalRepository.deleteAllEmployees()
                employeesList.forEach { employee ->
                    employeesLocalRepository.insert(employee.toEmployeeItem())
                }
                return@withContext ResultState.Success(employeesList)
            }
        } catch (e: Exception) {
            return ResultState.Error("Failed to fetch employees: ${e.message}")
        }
    }

    override suspend fun getAllActiveLeaveRequests(): Flow<ResultState<List<LeaveRequest>>> =
        flow {
            try {
                adminRepository.getAllActiveLeaveRequests().collect { leaveRequestList ->
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
}