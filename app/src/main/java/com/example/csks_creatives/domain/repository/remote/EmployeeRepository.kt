package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.employee.LeaveRequest

interface EmployeeRepository {
    suspend fun postLeaveRequest(leaveRequest: LeaveRequest)

    suspend fun getAllLeaveRequestsForEmployee(employeeId: String): List<LeaveRequest>
}