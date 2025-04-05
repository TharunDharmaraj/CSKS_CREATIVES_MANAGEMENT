package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.employee.LeaveRequestsGrouped
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {
    suspend fun postLeaveRequest(leaveRequest: LeaveRequest)

    suspend fun getAllLeaveRequestsForEmployee(employeeId: String): Flow<List<LeaveRequest>>

    suspend fun getAllApprovedAndUnApprovedRequestsForEmployee(employeeId: String): Flow<LeaveRequestsGrouped>

    suspend fun widthDrawLeaveRequest(employeeId: String, leaveRequest: LeaveRequest)
}