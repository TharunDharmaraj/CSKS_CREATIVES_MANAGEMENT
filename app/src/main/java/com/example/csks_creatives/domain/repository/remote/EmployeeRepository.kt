package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.employee.LeaveRequest
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    suspend fun postLeaveRequest(leaveRequest: LeaveRequest)

    suspend fun getAllLeaveRequestsForEmployee(employeeId: String): Flow<List<LeaveRequest>>
}