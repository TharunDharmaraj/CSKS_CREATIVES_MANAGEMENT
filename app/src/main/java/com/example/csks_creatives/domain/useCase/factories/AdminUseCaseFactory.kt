package com.example.csks_creatives.domain.useCase.factories

import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.AdminUseCase
import kotlinx.coroutines.flow.Flow

interface AdminUseCaseFactory {
    fun create(): AdminUseCase

    suspend fun createEmployee(employee: Employee): ResultState<String>

    suspend fun deleteEmployee(employeeId: String): ResultState<String>

    suspend fun getEmployeeDetails(employeeId: String, isForceFetch: Boolean = false, limit: Long? = null): Flow<ResultState<Employee>>

    suspend fun getEmployeesList(isForceFetch: Boolean = false, limit: Long? = null): ResultState<List<Employee>>

    suspend fun getAllActiveLeaveRequests(isForceFetch: Boolean = false): Flow<ResultState<List<LeaveRequest>>>

    suspend fun markLeaveRequestAsApproved(leaveRequest: LeaveRequest): ResultState<String>

    suspend fun markLeaveRequestAsRejected(leaveRequest: LeaveRequest): ResultState<String>

    suspend fun getLocalEmployeeCompletedTasksCount(employeeId: String): String

    suspend fun forceUpdateEmployeeCompletedTasksCount(employeeId: String): ResultState<Long>
}