package com.example.csks_creatives.domain.useCase.factories

import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.employee.LeaveRequestsGrouped
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveDuration
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.EmployeeUseCase
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface EmployeeUseCaseFactory {
    fun create(): EmployeeUseCase

    suspend fun addLeaveRequest(leaveDate: Date, leaveReason: String, postedBy: String, leaveDuration: LeaveDuration): ResultState<String>

    suspend fun getAllLeavesTaken(employeeId: String, isForceFetch: Boolean = false): Flow<ResultState<List<LeaveRequest>>>

    suspend fun getAllLeaveRequestsGrouped(employeeId: String, isForceFetch: Boolean = false): Flow<ResultState<LeaveRequestsGrouped>>

    suspend fun withDrawLeaveRequest(leaveRequest: LeaveRequest)

    suspend fun reRequestLeaveRequest(leaveRequest: LeaveRequest)
}