package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.BACKLOG
import com.example.csks_creatives.data.utils.Constants.COMPLETED
import com.example.csks_creatives.data.utils.Constants.TASKS_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_ATTACHMENT
import com.example.csks_creatives.data.utils.Constants.TASK_CLIENT_ID
import com.example.csks_creatives.data.utils.Constants.TASK_COST
import com.example.csks_creatives.data.utils.Constants.TASK_CREATION_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_CURRENT_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_DIRECTION_APP
import com.example.csks_creatives.data.utils.Constants.TASK_EMPLOYEE_ID
import com.example.csks_creatives.data.utils.Constants.TASK_ESTIMATE
import com.example.csks_creatives.data.utils.Constants.TASK_ID
import com.example.csks_creatives.data.utils.Constants.TASK_PAID_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_PAYMENTS_INFO_AMOUNT
import com.example.csks_creatives.data.utils.Constants.TASK_PAYMENTS_INFO_PAYMENT_DATE
import com.example.csks_creatives.data.utils.Constants.TASK_PAYMENTS_INFO_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_PRIORITY
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_START_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_TASK_NAME
import com.example.csks_creatives.data.utils.Constants.TASK_TYPE
import com.example.csks_creatives.data.utils.Constants.TASK_UPLOAD_OUTPUT
import com.example.csks_creatives.data.utils.Utils.convertStringStatusToStatusType
import com.example.csks_creatives.domain.model.task.*
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.repository.remote.TasksRepository
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import com.google.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepositoryImplementation @Inject constructor(
    private val firestore: FirebaseFirestore
) : TasksRepository {
    private val logTag = "TasksRepository"
    private val tasksRepositoryCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override suspend fun createTask(task: ClientTask) {
        try {
            val clientId = task.clientId
            val employeeId = task.employeeId
            val taskId = task.taskId
            val taskCreationTime = task.taskCreationTime
            getTaskPath(taskId).set(
                hashMapOf(
                    TASK_ID to taskId,
                    TASK_CREATION_TIME to task.taskCreationTime,
                    TASK_CLIENT_ID to clientId,
                    TASK_EMPLOYEE_ID to employeeId,
                    TASK_TASK_NAME to task.taskName,
                    TASK_ATTACHMENT to task.taskAttachment,
                    TASK_ESTIMATE to task.taskEstimate,
                    TASK_COST to task.taskCost,
                    TASK_PRIORITY to task.taskPriority,
                    TASK_DIRECTION_APP to task.taskDirectionApp,
                    TASK_UPLOAD_OUTPUT to task.taskUploadOutput,
                    TASK_PAID_STATUS to task.taskPaidStatus,
                    TASK_TYPE to task.taskType,
                    TASK_CURRENT_STATUS to task.currentStatus
                ), SetOptions.merge()
            )
            setInitialTaskStatusOnTaskCreation(taskId, taskCreationTime)
            Log.d(logTag + "Create", "Successfully createdTask $task")
        } catch (exception: Exception) {
            Log.d(logTag + "Create", "Failed ${exception.message}  to create Task $task")
        }
    }

    override suspend fun getTask(taskId: String): Flow<ClientTask> = callbackFlow {
        val path = firestore.collection(TASKS_COLLECTION).document(taskId)
        val listener = path.addSnapshotListener { documentSnapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            var updatedTaskWithStatusHistory = ClientTask()
            documentSnapshot.let { documentSnapshot ->
                val getTaskStatusJob = tasksRepositoryCoroutineScope.launch {
                    val task = documentSnapshot?.toObject(ClientTask::class.java)
                        ?.copy(taskId = documentSnapshot.id)
                    task?.let {
                        val paymentHistory = getTaskPaymentsList(documentSnapshot.id)
                        val statusHistory = getTaskStatusList(documentSnapshot.id)
                        updatedTaskWithStatusHistory = it.copy(
                            paymentHistory = paymentHistory.map { pair ->
                                PaymentInfo(
                                    pair.first.toInt(),
                                    pair.second
                                )
                            },
                            statusHistory = statusHistory.map { (id, times) ->
                                TaskStatusHistory(
                                    convertStringStatusToStatusType(id),
                                    times[0].toString(),
                                    times[1].toString()
                                )
                            }
                        )
                    }
                }
                getTaskStatusJob.invokeOnCompletion {
                    trySend(updatedTaskWithStatusHistory).isSuccess
                }
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getTaskOverView(taskId: String): Flow<ClientTaskOverview> = callbackFlow {
        val path = firestore.collection(TASKS_COLLECTION).document(taskId)
        val listener = path.addSnapshotListener { documentSnapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            documentSnapshot?.let { docSnapshot ->
                val task = docSnapshot.toObject(ClientTask::class.java)
                    ?.copy(taskId = docSnapshot.id) ?: ClientTask()

                tasksRepositoryCoroutineScope.launch {
                    val statusHistory = firestore.collection(TASKS_COLLECTION)
                        .document(taskId)
                        .collection(TASK_STATUS_HISTORY_SUB_COLLECTION)
                        .get()
                        .await()

                    var inProgressStartTime: String? = null
                    var completedStartTime: String? = null

                    for (statusDoc in statusHistory.documents) {
                        val status = statusDoc.id
                        val startTime =
                            statusDoc.getLong(TASK_STATUS_HISTORY_START_TIME)?.toString()

                        when (status) {
                            "IN_PROGRESS" -> inProgressStartTime = startTime
                            "COMPLETED" -> completedStartTime = startTime
                        }
                    }

                    val taskOverView = ClientTaskOverview(
                        taskId = task.taskId,
                        taskName = task.taskName,
                        taskCreationTime = task.taskCreationTime,
                        clientId = task.clientId,
                        taskEstimate = task.taskEstimate,
                        taskPaidStatus = task.taskPaidStatus,
                        taskCost = task.taskCost,
                        taskType = task.taskType,
                        taskPriority = task.taskPriority,
                        taskDirectionApp = task.taskDirectionApp,
                        taskUploadOutput = task.taskUploadOutput,
                        currentStatus = task.currentStatus,
                        taskInProgressTime = inProgressStartTime ?: "133",
                        taskCompletedTime = completedStartTime ?: "153"
                    )

                    trySend(taskOverView)
                }
            }
        }

        awaitClose { listener.remove() }
    }

    override fun getTasksForClient(clientId: String): Flow<List<ClientTask>> =
        getTasksBasedOnCondition(TASK_CLIENT_ID, clientId)

    override suspend fun getTasksForEmployee(employeeId: String): Flow<List<ClientTask>> =
        getTasksBasedOnCondition(TASK_EMPLOYEE_ID, employeeId)

    override suspend fun getActiveTasksForEmployee(employeeId: String): Flow<List<ClientTask>> {
        val query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_EMPLOYEE_ID, employeeId)
            .whereNotEqualTo(TASK_CURRENT_STATUS, COMPLETED)
        return getTasks(query)
    }

    override suspend fun getCompletedTasksForEmployee(employeeId: String): Flow<List<ClientTask>> {
        val query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_EMPLOYEE_ID, employeeId)
            .whereEqualTo(TASK_CURRENT_STATUS, COMPLETED)
        return getTasks(query)
    }

    override suspend fun getActiveTasks(): Flow<List<ClientTask>> {
        val query = firestore.collection(TASKS_COLLECTION)
            .whereNotIn(
                TASK_CURRENT_STATUS,
                listOf(TaskStatusType.BACKLOG.name, TaskStatusType.COMPLETED.name)
            )
        return getTasks(query)
    }

    override suspend fun getTasksInBackLog(): Flow<List<ClientTask>> {
        val query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_CURRENT_STATUS, TaskStatusType.BACKLOG.name)
        return getTasks(query)
    }

    override suspend fun getCompletedTasks(): Flow<List<ClientTask>> {
        val query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_CURRENT_STATUS, TaskStatusType.COMPLETED.name)
        return getTasks(query)
    }

    private fun getTasksBasedOnCondition(searchQuery: String, condition: String) =
        getTasks(firestore.collection(TASKS_COLLECTION).whereEqualTo(searchQuery, condition))

    private fun getTasks(query: Query): Flow<List<ClientTask>> = callbackFlow {
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            snapshot?.let { querySnapshot ->
                val tasksList = mutableListOf<ClientTask>()
                val getTasksWithStatusJob = tasksRepositoryCoroutineScope.launch {
                    for (document in querySnapshot.documents) {
                        val task = document.toObject(ClientTask::class.java)
                            ?.copy(taskId = document.id)
                        task?.let {
                            val statusHistory = getTaskStatusList(document.id)
                            val updatedTask = it.copy(
                                statusHistory = statusHistory.map { (id, times) ->
                                    TaskStatusHistory(
                                        convertStringStatusToStatusType(id),
                                        times[0].toString(),
                                        times[1].toString()
                                    )
                                }
                            )
                            tasksList.add(updatedTask)
                        }
                    }
                }
                getTasksWithStatusJob.invokeOnCompletion {
                    trySend(tasksList).isSuccess
                }
            }
        }
        awaitClose { listener.remove() }
    }

    private fun setInitialTaskStatusOnTaskCreation(
        taskId: String, taskCreationTime: String
    ) {
        try {
            getTaskPath(taskId).collection(TASK_STATUS_HISTORY_SUB_COLLECTION)
                .document(BACKLOG).set(
                    hashMapOf(
                        TASK_STATUS_HISTORY_START_TIME to taskCreationTime.toLong(),
                        TASK_STATUS_HISTORY_END_TIME to TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
                    ), SetOptions.merge()
                )
        } catch (exception: Exception) {
            Log.d(
                logTag + "Create",
                "Failed to create dummy status on Task Creation exception: ${exception.message}"
            )
        }
    }

    // To get the list of task statuses
    private suspend fun getTaskStatusList(taskId: String): Map<String, List<Long>> {
        val statusHistoryMap = mutableMapOf<String, List<Long>>()
        try {
            val statusSubCollectionRef =
                getTaskPath(taskId).collection(TASK_STATUS_HISTORY_SUB_COLLECTION).get().await()
            statusSubCollectionRef.documents.forEach { documentSnapshot ->
                val startTime =
                    documentSnapshot.getLong(TASK_STATUS_HISTORY_START_TIME) ?: 0
                val endTime =
                    documentSnapshot.getLong(TASK_STATUS_HISTORY_END_TIME)
                        ?: TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
                statusHistoryMap[documentSnapshot.id] = listOf(startTime, endTime)
            }
        } catch (exception: Exception) {
            Log.d(logTag + "StatusList", "Error building statusMap ${exception.message} ")
        }
        return statusHistoryMap
    }

    // To get the list of payment statuses
    private suspend fun getTaskPaymentsList(taskId: String): List<Pair<Long, String>> {
        val paymentHistoryList = arrayListOf<Pair<Long, String>>()
        try {
            val paymentsSubCollectionRef =
                getTaskPath(taskId).collection(TASK_PAYMENTS_INFO_SUB_COLLECTION).get().await()
            paymentsSubCollectionRef.documents.forEach { documentSnapshot ->
                val paymentAmount =
                    documentSnapshot.getLong(TASK_PAYMENTS_INFO_AMOUNT) ?: 0
                val paymentDate =
                    documentSnapshot.getString(TASK_PAYMENTS_INFO_PAYMENT_DATE)
                        ?: getCurrentTimeAsString()
                paymentHistoryList.add(Pair(paymentAmount, paymentDate))
            }
        } catch (exception: Exception) {
            Log.d(logTag + "StatusList", "Error building statusMap ${exception.message} ")
        }
        return paymentHistoryList.toList()
    }

    private fun getTaskPath(taskId: String) =
        firestore.collection(TASKS_COLLECTION).document(taskId)
}