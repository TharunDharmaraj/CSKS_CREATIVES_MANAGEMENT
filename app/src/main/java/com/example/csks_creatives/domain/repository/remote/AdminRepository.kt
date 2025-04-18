package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    suspend fun createEmployee(employee: Employee)

    suspend fun deleteEmployee(employeeId: String)

    suspend fun getEmployeeDetails(employeeId: String): Flow<Employee>

    suspend fun getEmployees(): List<Employee>

    suspend fun checkEmployeeIdExists(employeeId: String): Boolean

    suspend fun addActiveTaskIntoEmployeeDetails(employeeId: String, taskId: String)

    suspend fun removeActiveTaskFromEmployeeDetails(employeeId: String, taskId: String)

    suspend fun addCompletedTaskIntoEmployeeDetails(employeeId: String, taskId: String)

    suspend fun removeCompletedTaskFromEmployeeDetails(employeeId: String, taskId: String)

    suspend fun getAllActiveLeaveRequests(): Flow<List<LeaveRequest>>

    suspend fun markLeaveRequestAsApproved(leaveRequestId: String, employeeId: String)

    suspend fun markLeaveRequestAsRejected(leaveRequestId: String, employeeId: String)
}