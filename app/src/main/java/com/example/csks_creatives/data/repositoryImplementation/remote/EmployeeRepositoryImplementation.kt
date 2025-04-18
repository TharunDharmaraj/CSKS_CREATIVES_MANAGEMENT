package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_COLLECTION
import com.example.csks_creatives.data.utils.Constants.LEAVES_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUESTS_COLLECTION
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_APPROVAL_STATUS
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_DATE
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_ID
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_POSTED_BY
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_REASON
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.employee.LeaveRequestsGrouped
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveApprovalStatus
import com.example.csks_creatives.domain.repository.remote.EmployeeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
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
            // We are storing a copy of the active leave requests in a separate firestore collection
            // to help admin fetch the active leave requests from that collection,
            // once the leave requests are resolved, they will be deleted from this temporary collection.
            getLeaveRequestsCollection().document(leaveRequest.leaveRequestId)
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
                "Error ${exception.message}  posting Leave for EmployeeId: ${leaveRequest.postedBy}"
            )
        }
    }

    override suspend fun getAllLeaveRequestsForEmployee(employeeId: String) = callbackFlow {
        val listenerRegistration = getLeaveCollectionReference(employeeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(logTag, "Error fetching leaves: ", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val leaveList = snapshot.documents.mapNotNull { leave ->
                        leave.toObject(LeaveRequest::class.java)
                    }

                    trySend(leaveList).isSuccess
                }
            }

        awaitClose {
            listenerRegistration.remove()
            Log.d(logTag, "Firestore listener removed for employeeId: $employeeId")
        }
    }

    override suspend fun getAllApprovedAndUnApprovedRequestsForEmployee(employeeId: String) =
        callbackFlow {
            val listenerRegistration = getLeaveCollectionReference(employeeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(logTag, "Error fetching leaves: ", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val leaveList = snapshot.documents.mapNotNull { leave ->
                            leave.toObject(LeaveRequest::class.java)
                        }

                        val approved =
                            leaveList.filter { it.approvedStatus == LeaveApprovalStatus.APPROVED }
                        val unapproved =
                            leaveList.filter { it.approvedStatus == LeaveApprovalStatus.UN_APPROVED }
                        val rejected =
                            leaveList.filter { it.approvedStatus == LeaveApprovalStatus.REJECTED}

                        trySend(LeaveRequestsGrouped(approved, unapproved, rejected)).isSuccess
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }

    override suspend fun widthDrawLeaveRequest(employeeId: String, leaveRequest: LeaveRequest) {
        try {
            getLeaveCollectionReference(employeeId).document(leaveRequest.leaveRequestId).delete()
                .await()
            Log.d(
                logTag + "Withdraw",
                "Successfully deleted employeeId:$employeeId leaveRequest $leaveRequest"
            )
            deleteLeaveRequest(leaveRequest)
        } catch (exception: Exception) {
            Log.d(
                logTag + "Withdraw",
                "Error ${exception.message}  in withdrawing employeeId:$employeeId leaveRequest $leaveRequest"
            )
        }
    }

    override suspend fun reRequestDrawLeaveRequest(
        leaveRequest: LeaveRequest
    ) {
        postLeaveRequest(leaveRequest)
    }

    private suspend fun deleteLeaveRequest(leaveRequest: LeaveRequest) {
        try {
            getLeaveRequestsCollection().document(leaveRequest.leaveRequestId).delete().await()
            Log.d(
                logTag + "Delete",
                "Successfully deleted leaveRequest $leaveRequest from active eave requests collection"
            )
        } catch (exception: Exception) {
            Log.e(
                logTag + "Delete",
                "Error in deleting leaveRequest $leaveRequest from active eave requests collection exception: $exception"
            )
        }
    }

    private fun getLeaveCollectionReference(employeeId: String) =
        firebaseFirestore.collection(EMPLOYEE_COLLECTION).document(employeeId)
            .collection(LEAVES_SUB_COLLECTION)

    private fun getLeaveRequestsCollection() =
        firebaseFirestore.collection(LEAVE_REQUESTS_COLLECTION)
}