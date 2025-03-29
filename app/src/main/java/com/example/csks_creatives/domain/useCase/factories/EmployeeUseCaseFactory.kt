package com.example.csks_creatives.domain.useCase.factories

import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.EmployeeUseCase
import java.util.Date

interface EmployeeUseCaseFactory {
    fun create(): EmployeeUseCase

    suspend fun addLeaveRequest(leaveDate: Date, leaveReason: String, postedBy: String): ResultState<String>

    suspend fun getAllLeavesTaken(employeeId: String): ResultState<List<LeaveRequest>>
}