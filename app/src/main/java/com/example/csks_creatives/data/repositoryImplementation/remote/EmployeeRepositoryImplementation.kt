package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_COLLECTION
import com.example.csks_creatives.data.utils.Constants.LEAVES_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_APPROVAL_STATUS
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_DATE
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_ID
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_POSTED_BY
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_REASON
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.repository.remote.EmployeeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepositoryImplementation @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : EmployeeRepository {
    private val logTag = "EmployeeRepository"
    override suspend fun postLeaveRequest(leaveRequest: LeaveRequest) {
        try {
            getLeaveCollectionReference(leaveRequest.postedBy).document(leaveRequest.leaveRequestId)
                .set(
                    hashMapOf(
                        LEAVE_REQUEST_ID to leaveRequest.leaveRequestId,
                        LEAVE_REQUEST_DATE to leaveRequest.leaveDate,
                        LEAVE_REQUEST_REASON to leaveRequest.leaveReason,
                        LEAVE_REQUEST_POSTED_BY to leaveRequest.postedBy,
                        LEAVE_REQUEST_APPROVAL_STATUS to leaveRequest.approvedStatus
                    ),
                    SetOptions.merge()
                ).await()
            Log.d(
                logTag + "Post",
                "Successfully posted LeaveRequest $leaveRequest"
            )
        } catch (exception: Exception) {
            Log.d(
                logTag + "Post",
                "Error $exception posting Leave for EmployeeId: ${leaveRequest.postedBy}"
            )
        }
    }

    override suspend fun getAllLeaveRequestsForEmployee(employeeId: String): List<LeaveRequest> {
        try {
            val path = getLeaveCollectionReference(employeeId).get().await()
            val leaveList = mutableListOf(LeaveRequest())
            path.documents.forEach { leave ->
                leaveList.add(leave.toObject(LeaveRequest::class.java) ?: LeaveRequest())
            }
            Log.d(
                logTag + "GetAll",
                "All ${leaveList.size} Leaves for employeeId : $employeeId - $leaveList"
            )
            return leaveList
        } catch (exception: Exception) {
            Log.d(
                logTag + "GetAll",
                "Error $exception getting Leaves for employeeId : $employeeId"
            )
            return emptyList()
        }
    }

    fun getLeaveCollectionReference(employeeId: String) =
        firebaseFirestore.collection(EMPLOYEE_COLLECTION).document(employeeId)
            .collection(LEAVES_SUB_COLLECTION)
}