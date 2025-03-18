package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.TASKS_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_ATTACHMENT
import com.example.csks_creatives.data.utils.Constants.TASK_CLIENT_ID
import com.example.csks_creatives.data.utils.Constants.TASK_CURRENT_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_EMPLOYEE_ID
import com.example.csks_creatives.data.utils.Constants.TASK_ID
import com.example.csks_creatives.data.utils.Constants.TASK_POINT
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_START_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_TASK_NAME
import com.example.csks_creatives.data.utils.Utils.convertStatusTypeToString
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.TaskStatusHistory
import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import com.example.csks_creatives.domain.repository.remote.TasksManipulationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
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
            Log.d(logTag + "Assign", "Error $exception assigning task to employee")
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

            val currentTime = System.currentTimeMillis()
            if (lastUpdatedStatusDocument != null) {
                lastUpdatedStatusDocument.reference.set(
                    hashMapOf(TASK_STATUS_HISTORY_END_TIME to currentTime),
                    SetOptions.merge()
                ).await()
            } else {
                Log.d(logTag + "Status", "No Document found lastUpdatedStatusDocument is null")
            }
            val newStatusHashMap: HashMap<String, Long> = if (status == TaskStatusType.COMPLETED) {
                hashMapOf(
                    TASK_STATUS_HISTORY_START_TIME to currentTime,
                    TASK_STATUS_HISTORY_END_TIME to currentTime
                )
            } else {
                hashMapOf(
                    TASK_STATUS_HISTORY_START_TIME to currentTime,
                    TASK_STATUS_HISTORY_END_TIME to TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
                )
            }
            statusCollectionRef.document(convertStatusTypeToString(status)).set(
                newStatusHashMap, SetOptions.merge()
            )
            Log.d(logTag + "Status", "Successfully changed TaskId $taskId to $status")
        } catch (exception: Exception) {
            Log.d(logTag + "Status", "Failed to changed TaskId $taskId to $status")
        }
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
                        val startTime =
                            document.getLong(TASK_STATUS_HISTORY_START_TIME)
                                ?: return@mapNotNull null
                        val endTime =
                            document.getLong(TASK_STATUS_HISTORY_END_TIME)
                                ?: System.currentTimeMillis()
                        TaskStatusHistory(status, startTime.toString(), endTime.toString())
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
                    TASK_POINT to task.taskPoint,
                    TASK_CURRENT_STATUS to task.currentStatus
                ), SetOptions.merge()
            )
            Log.d(logTag + "Edit", "Successfully Edited TaskId $task")
        } catch (exception: Exception) {
            Log.d(logTag + "Edit", "Failed to Edit TaskId ${task.taskId}")
        }
    }

    private fun getTaskPath(taskId: String) =
        firestore.collection(TASKS_COLLECTION).document(taskId)
}