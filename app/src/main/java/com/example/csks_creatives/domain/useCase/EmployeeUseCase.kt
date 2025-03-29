package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.remote.EmployeeRepository
import com.example.csks_creatives.domain.useCase.factories.EmployeeUseCaseFactory
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class EmployeeUseCase @Inject constructor(
    private val employeeRepository: EmployeeRepository
) : EmployeeUseCaseFactory {
    override fun create(): EmployeeUseCase {
        return EmployeeUseCase(employeeRepository)
    }

    override suspend fun addLeaveRequest(
        leaveDate: Date,
        leaveReason: String,
        postedBy: String
    ): ResultState<String> {
        return try {
            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            if (leaveDate.after(currentDate).not())
                return ResultState.Error("Cannot request leave for today or past dates")
            if (leaveReason.isEmpty()) return ResultState.Error("Type leave reason")
            if (postedBy.isEmpty()) return ResultState.Error("EmployeeId Empty")
            val leaveRequest = LeaveRequest(
                leaveRequestId = getCurrentTimeAsString(),
                leaveReason = leaveReason,
                leaveDate = leaveDate,
                postedBy = postedBy
            )
            employeeRepository.postLeaveRequest(leaveRequest)
            ResultState.Success("Leave request submitted successfully!")
        } catch (exception: Exception) {
            ResultState.Error(exception.localizedMessage ?: "Error posting leave request")
        }
    }

    override suspend fun getAllLeavesTaken(employeeId: String): ResultState<List<LeaveRequest>> {
        return try {
            val leaveRequests = employeeRepository.getAllLeaveRequestsForEmployee(employeeId)
            ResultState.Success(leaveRequests)
        } catch (exception: Exception) {
            ResultState.Error(exception.localizedMessage ?: "Error fetching leave requests")
        }
    }
}