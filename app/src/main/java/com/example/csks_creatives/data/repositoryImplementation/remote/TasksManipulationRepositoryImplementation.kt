package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASKS_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASKS_COMPLETED_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASKS_IN_PROGRESS_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_ATTACHMENT
import com.example.csks_creatives.data.utils.Constants.TASK_CLIENT_ID
import com.example.csks_creatives.data.utils.Constants.TASK_COST
import com.example.csks_creatives.data.utils.Constants.TASK_CURRENT_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_DIRECTION_APP
import com.example.csks_creatives.data.utils.Constants.TASK_EMPLOYEE_ID
import com.example.csks_creatives.data.utils.Constants.TASK_ESTIMATE
import com.example.csks_creatives.data.utils.Constants.TASK_FULLY_PAID_DATE
import com.example.csks_creatives.data.utils.Constants.TASK_ID
import com.example.csks_creatives.data.utils.Constants.TASK_PAID_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_PAYMENTS_INFO_AMOUNT
import com.example.csks_creatives.data.utils.Constants.TASK_PAYMENTS_INFO_PAYMENT_DATE
import com.example.csks_creatives.data.utils.Constants.TASK_PAYMENTS_INFO_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_PRIORITY
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_ELAPSED_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_START_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_TASK_NAME
import com.example.csks_creatives.data.utils.Constants.TASK_TYPE
import com.example.csks_creatives.data.utils.Constants.TASK_UPLOAD_OUTPUT
import com.example.csks_creatives.data.utils.Utils.convertStatusTypeToString
import com.example.csks_creatives.domain.model.task.*
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.repository.remote.TasksManipulationRepository
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsLong
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksManipulationRepositoryImplementation @Inject constructor(
    private val firestore: FirebaseFirestore
) : TasksManipulationRepository {
    private val logTag = "TasksManipulationRepository"
    override suspend fun assignTaskToEmployee(taskId: String, employeeId: String) {
        try {
            getTaskPath(taskId).set(
                hashMapOf(TASK_EMPLOYEE_ID to employeeId),
                SetOptions.merge()
            )
            Log.d(logTag + "Assign", "Assigned taskId $taskId to employeeId $employeeId")
        } catch (exception: Exception) {
            Log.d(logTag + "Assign", "Error ${exception.message}  assigning task to employee")
        }
    }

    override suspend fun changeTaskStatus(taskId: String, status: TaskStatusType) {
        try {
            val statusCollectionRef =
                getTaskPath(taskId).collection(TASK_STATUS_HISTORY_SUB_COLLECTION)
            val lastUpdatedStatusDocument = statusCollectionRef.orderBy(
                TASK_STATUS_HISTORY_START_TIME,
                Query.Direction.DESCENDING
            ).limit(1).get().await().documents.firstOrNull()

            val currentTime = getCurrentTimeAsLong()
            if (lastUpdatedStatusDocument != null) {
                lastUpdatedStatusDocument.reference.set(
                    hashMapOf(
                        TASK_STATUS_HISTORY_ELAPSED_TIME to getElapsedTime(lastUpdatedStatusDocument),
                        TASK_STATUS_HISTORY_END_TIME to currentTime,
                        TASK_STATUS_HISTORY_START_TIME to 0L
                    ),
                    SetOptions.merge()
                ).await()
            } else {
                Log.d(logTag + "Status", "No Document found lastUpdatedStatusDocument is null")
            }
            val newStatusHashMap: HashMap<String, Long> =
                hashMapOf(
                    TASK_STATUS_HISTORY_START_TIME to currentTime,
                    TASK_STATUS_HISTORY_END_TIME to TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
                )
            statusCollectionRef.document(convertStatusTypeToString(status)).set(
                newStatusHashMap, SetOptions.merge()
            )
            Log.d(logTag + "Status", "Successfully changed TaskId $taskId to $status")
        } catch (exception: Exception) {
            Log.d(
                logTag + "Status",
                "Failed with ${exception.message}  to changed TaskId $taskId to $status"
            )
        }
    }

    private fun getElapsedTime(lastUpdatedStatusDocument: DocumentSnapshot): Long {
        val time = getStartAndEndTime(lastUpdatedStatusDocument)
        val alreadyElapsedTime = getAlreadyElapsedTime(lastUpdatedStatusDocument)
        return alreadyElapsedTime + (getCurrentTimeAsLong() - time.first)
    }

    private fun getAlreadyElapsedTime(lastUpdatedStatusDocument: DocumentSnapshot) =
        lastUpdatedStatusDocument.getLong(TASK_STATUS_HISTORY_ELAPSED_TIME) ?: 0L

    private fun getStartAndEndTime(document: DocumentSnapshot): Pair<Long, Long> {
        val startTime =
            document.getLong(TASK_STATUS_HISTORY_START_TIME)
                ?: 0L
        val endTime =
            document.getLong(TASK_STATUS_HISTORY_END_TIME)
                ?: 0L
        return Pair(startTime, endTime)
    }

    override suspend fun fetchTaskStatusHistory(taskId: String): Flow<List<TaskStatusHistory>> =
        callbackFlow {
            val historyCollectionRef = firestore.collection(TASKS_COLLECTION)
                .document(taskId)
                .collection(TASK_STATUS_HISTORY_SUB_COLLECTION)
                .orderBy(TASK_STATUS_HISTORY_START_TIME, Query.Direction.ASCENDING)
            val listener = historyCollectionRef.addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (documentSnapshot != null) {
                    val taskStatusHistoryList = documentSnapshot.documents.mapNotNull { document ->
                        val status = document.id.let { TaskStatusType.valueOf(it) }
                        val taskTimings = getStartAndEndTime(document)
                        val elapsedTime = getAlreadyElapsedTime(document)
                        TaskStatusHistory(
                            status,
                            taskTimings.first.toString(),
                            taskTimings.second.toString(),
                            elapsedTime
                        )
                    }
                    trySend(taskStatusHistoryList).isSuccess
                }
            }
            awaitClose { listener.remove() }
        }

    override suspend fun editTask(task: ClientTask) {
        try {
            val collectionRefToEdit = getTaskPath(taskId = task.taskId)
            collectionRefToEdit.set(
                hashMapOf(
                    TASK_ID to task.taskId,
                    TASK_CLIENT_ID to task.clientId,
                    TASK_EMPLOYEE_ID to task.employeeId,
                    TASK_TASK_NAME to task.taskName,
                    TASK_ATTACHMENT to task.taskAttachment,
                    TASK_ESTIMATE to task.taskEstimate,
                    TASK_COST to task.taskCost,
                    TASK_PAID_STATUS to task.taskPaidStatus,
                    TASK_FULLY_PAID_DATE to task.taskFullyPaidDate,
                    TASK_PRIORITY to task.taskPriority,
                    TASK_DIRECTION_APP to task.taskDirectionApp,
                    TASK_UPLOAD_OUTPUT to task.taskUploadOutput,
                    TASK_TYPE to task.taskType,
                    TASK_CURRENT_STATUS to task.currentStatus
                ), SetOptions.merge()
            )
            Log.d(logTag + "Edit", "Successfully Edited TaskId $task")
        } catch (exception: Exception) {
            Log.d(
                logTag + "Edit",
                "Failed with ${exception.message}  to Edit TaskId ${task.taskId}"
            )
        }
    }

    override suspend fun addPartialTaskAmount(taskId: String, paymentInfo: PaymentInfo) {
        try {
            getTaskPath(taskId).collection(TASK_PAYMENTS_INFO_SUB_COLLECTION)
                .document(paymentInfo.paymentDate).set(
                    hashMapOf(
                        TASK_PAYMENTS_INFO_AMOUNT to paymentInfo.amount,
                        TASK_PAYMENTS_INFO_PAYMENT_DATE to paymentInfo.paymentDate
                    ), SetOptions.merge()
                )
            Log.d(logTag + "PartialTaskAmount", "Successfully Edited TaskId $taskId")
        } catch (exception: Exception) {
            Log.d(
                logTag + "PartialTaskAmount",
                "Successfully Added partial Amount ${paymentInfo.amount} TaskId $taskId exception: ${exception.message}"
            )
        }
    }

    override suspend fun markTaskAsFullyPaid(taskId: String) {
        try {
            getTaskPath(taskId).set(
                hashMapOf(
                    TASK_FULLY_PAID_DATE to getCurrentTimeAsString(),
                    TASK_PAID_STATUS to TaskPaidStatus.FULLY_PAID
                ), SetOptions.merge()
            )
            Log.d(logTag + "PartialTaskAmount", "Successfully marked TaskId $taskId as Fully Paid")
        } catch (exception: Exception) {
            Log.d(
                logTag + "PartialTaskAmount",
                "Error in marking TaskId $taskId as Fully Paid, Exception: ${exception.message}"
            )
        }
    }

    override suspend fun removeTaskFromEmployeeDetails(employeeId: String, taskId: String) {
        try {
            val completedRef = getEmployeeTasksPathComplete(employeeId).document(taskId)
            val inProgressRef = getEmployeeTasksPathInProgress(employeeId).document(taskId)

            if (completedRef.get().await().exists()) {
                completedRef.delete().await()
                Log.d(
                    logTag + "removeTask",
                    "Deleted task $taskId from Completed for employee $employeeId"
                )
                return
            }

            if (inProgressRef.get().await().exists()) {
                inProgressRef.delete().await()
                Log.d(
                    logTag + "removeTask",
                    "Deleted task $taskId from In‑Progress for employee $employeeId"
                )
            } else {
                Log.d(
                    logTag + "removeTask",
                    "Task $taskId not found in either sub‑collection for employee $employeeId"
                )
            }
        } catch (e: Exception) {
            Log.e(
                logTag + "removeTask",
                "Error deleting task $taskId for employee $employeeId: ${e.message}"
            )
        }
    }

    override suspend fun deleteTaskFromTasksCollection(taskId: String, employeeId: String) {
        try {
            getTaskPath(taskId).delete().await()
            Log.d(logTag + "deleteTask", "TaskId: $taskId Deleted SuccessFully")
        } catch (exception: Exception) {
            Log.d(
                logTag + "deleteTask",
                "Exception in deleting TaskId: $taskId Exception: ${exception.message}"
            )
        }
    }

    private fun getEmployeeTasksPathComplete(employeeId: String) =
        firestore.collection(EMPLOYEE_COLLECTION).document(employeeId)
            .collection(TASKS_COMPLETED_SUB_COLLECTION)


    private fun getEmployeeTasksPathInProgress(employeeId: String) =
        firestore.collection(EMPLOYEE_COLLECTION).document(employeeId)
            .collection(TASKS_IN_PROGRESS_SUB_COLLECTION)

    private fun getTaskPath(taskId: String) =
        firestore.collection(TASKS_COLLECTION).document(taskId)
}