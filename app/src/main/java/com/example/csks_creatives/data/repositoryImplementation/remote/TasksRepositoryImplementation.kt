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
import com.example.csks_creatives.data.utils.Utils.convertStringStatusToStatusType
import com.example.csks_creatives.domain.model.task.*
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.repository.remote.TasksRepository
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsLong
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
        withContext(Dispatchers.IO + NonCancellable) {
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
                        TASK_FULLY_PAID_DATE to task.taskFullyPaidDate,
                        TASK_TYPE to task.taskType,
                        TASK_CURRENT_STATUS to task.currentStatus
                    ), SetOptions.merge()
                ).await()
                setInitialTaskStatusOnTaskCreation(taskId, taskCreationTime)
                Log.d(logTag + "Create", "Successfully createdTask $task")
            } catch (exception: Exception) {
                Log.e(logTag + "Create", "Failed ${exception.message}  to create Task $task", exception)
            }
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
                                    times[1].toString(),
                                    times[2]
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

                    val totalElapsedTime = getTotalElapsedTime(statusHistory)

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
                        taskElapsedTime = totalElapsedTime
                    )

                    trySend(taskOverView)
                }
            }
        }

        awaitClose { listener.remove() }
    }

    private fun getTotalElapsedTime(statusHistory: QuerySnapshot): Long {
        val history = statusHistory.documents.mapNotNull { doc ->
            val type =
                runCatching { TaskStatusType.valueOf(doc.id) }.getOrNull() ?: return@mapNotNull null
            TaskStatusHistory(
                taskStatusType = type,
                startTime = doc.getLong(TASK_STATUS_HISTORY_START_TIME)?.toString() ?: "0",
                endTime = doc.getLong(TASK_STATUS_HISTORY_END_TIME)?.toString() ?: "0",
                elapsedTime = doc.getLong(TASK_STATUS_HISTORY_ELAPSED_TIME) ?: 0L
            )
        }

        val intermediateStatuses = history.filter {
            it.taskStatusType != TaskStatusType.BACKLOG && it.taskStatusType != TaskStatusType.COMPLETED
        }

        val intermediateStatusWithElapsedTimeAsZero =
            intermediateStatuses.filter { it.elapsedTime == 0L }

        val revisions = history.filter {
            it.taskStatusType.order > 99 && it.elapsedTime == 0L
        }

        val pausedStatus = history.find { it.taskStatusType == TaskStatusType.PAUSED }

        return if (intermediateStatuses.isNotEmpty()) {
            intermediateStatuses.sumOf { it.elapsedTime } +
                    revisions.sumOf { it.endTime.toLong() - it.startTime.toLong() } -
                    (pausedStatus?.elapsedTime ?: 0L) +
                    // We need to accommodate the case where a status is just selected and not changed (Current State), so we calculate based on StartTime, to correctly display the elapsed time
                    intermediateStatusWithElapsedTimeAsZero.sumOf { getCurrentTimeAsLong() - it.startTime.toLong() }
        } else 0L
    }

    override fun getTasksForClient(clientId: String, limit: Long?): Flow<List<ClientTask>> =
        getTasksBasedOnCondition(TASK_CLIENT_ID, clientId, limit)

    override suspend fun getTasksForEmployee(employeeId: String, limit: Long?): Flow<List<ClientTask>> =
        getTasksBasedOnCondition(TASK_EMPLOYEE_ID, employeeId, limit)

    override suspend fun getActiveTasksForEmployee(employeeId: String, limit: Long?): Flow<List<ClientTask>> {
        val activeStatuses = TaskStatusType.entries
            .filter { it != TaskStatusType.BACKLOG && it != TaskStatusType.COMPLETED }
            .map { it.name }

        var query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_EMPLOYEE_ID, employeeId)
            .whereIn(TASK_CURRENT_STATUS, activeStatuses)
            .orderBy(TASK_CREATION_TIME, Query.Direction.DESCENDING)
        
        if (limit != null) {
            query = query.limit(limit)
        }
        return getTasks(query)
    }

    override suspend fun getCompletedTasksForEmployee(employeeId: String, limit: Long?): Flow<List<ClientTask>> {
        var query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_EMPLOYEE_ID, employeeId)
            .whereEqualTo(TASK_CURRENT_STATUS, COMPLETED)
            .orderBy(TASK_CREATION_TIME, Query.Direction.DESCENDING)
        
        if (limit != null) {
            query = query.limit(limit)
        }
        return getTasks(query)
    }

    override suspend fun getCompletedTasksCountForEmployee(employeeId: String): Long {
        return try {
            firestore.collection(TASKS_COLLECTION)
                .whereEqualTo(TASK_EMPLOYEE_ID, employeeId)
                .whereEqualTo(TASK_CURRENT_STATUS, COMPLETED)
                .count()
                .get(AggregateSource.SERVER)
                .await()
                .count
        } catch (e: Exception) {
            Log.e(logTag, "Error getting completed tasks count for $employeeId: ${e.message}")
            0L
        }
    }

    override suspend fun getBacklogTasksForEmployee(employeeId: String, limit: Long?): Flow<List<ClientTask>> {
        var query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_EMPLOYEE_ID, employeeId)
            .whereEqualTo(TASK_CURRENT_STATUS, BACKLOG)
            .orderBy(TASK_CREATION_TIME, Query.Direction.DESCENDING)
        
        if (limit != null) {
            query = query.limit(limit)
        }
        return getTasks(query)
    }

    override suspend fun getActiveTasks(limit: Long?): Flow<List<ClientTask>> {
        val activeStatuses = TaskStatusType.entries
            .filter { it != TaskStatusType.BACKLOG && it != TaskStatusType.COMPLETED }
            .map { it.name }

        var query = firestore.collection(TASKS_COLLECTION)
            .whereIn(TASK_CURRENT_STATUS, activeStatuses)
            .orderBy(TASK_CREATION_TIME, Query.Direction.DESCENDING)
        
        if (limit != null) {
            query = query.limit(limit)
        }
        return getTasks(query)
    }

    override suspend fun getTasksInBackLog(limit: Long?): Flow<List<ClientTask>> {
        var query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_CURRENT_STATUS, TaskStatusType.BACKLOG.name)
            .orderBy(TASK_CREATION_TIME, Query.Direction.DESCENDING)
        
        if (limit != null) {
            query = query.limit(limit)
        }
        return getTasks(query)
    }

    override suspend fun getCompletedTasks(limit: Long?): Flow<List<ClientTask>> {
        var query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(TASK_CURRENT_STATUS, TaskStatusType.COMPLETED.name)
            .orderBy(TASK_CREATION_TIME, Query.Direction.DESCENDING)
        
        if (limit != null) {
            query = query.limit(limit)
        }
        return getTasks(query)
    }

    private fun getTasksBasedOnCondition(searchQuery: String, condition: String, limit: Long?) : Flow<List<ClientTask>> {
        var query = firestore.collection(TASKS_COLLECTION)
            .whereEqualTo(searchQuery, condition)
            .orderBy(TASK_CREATION_TIME, Query.Direction.DESCENDING)
        
        if (limit != null) {
            query = query.limit(limit)
        }
        return getTasks(query)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTasks(query: Query): Flow<List<ClientTask>> = callbackFlow {
        // Use a limited dispatcher for fetching tasks to prevent saturating the IO pool
        // and ensure that write operations (like createTask) can still execute promptly.
        val fetchDispatcher = Dispatchers.IO.limitedParallelism(15)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(logTag, "Firestore error for query $query: ${error.message}")
                close(error)
                return@addSnapshotListener
            }

            snapshot?.let { querySnapshot ->
                // Use the callbackFlow's scope so coroutines are cancelled when the flow is closed
                launch(fetchDispatcher) {
                    try {
                        val tasksList = querySnapshot.documents.map { document ->
                            async {
                                val task = document.toObject(ClientTask::class.java)
                                    ?.copy(taskId = document.id)
                                task?.let {
                                    val statusHistoryDeferred = async { getTaskStatusList(document.id) }
                                    val paymentHistoryDeferred = async { getTaskPaymentsList(document.id) }
                                    
                                    it.copy(
                                        statusHistory = statusHistoryDeferred.await().map { (id, times) ->
                                            TaskStatusHistory(
                                                convertStringStatusToStatusType(id),
                                                times[0].toString(),
                                                times[1].toString(),
                                                times[2]
                                            )
                                        },
                                        paymentHistory = paymentHistoryDeferred.await().map { pair ->
                                            PaymentInfo(
                                                pair.first.toInt(),
                                                pair.second
                                            )
                                        },
                                    )
                                }
                            }
                        }.awaitAll().filterNotNull()
                        
                        trySend(tasksList).isSuccess
                    } catch (e: Exception) {
                        Log.e(logTag, "Error processing task documents: ${e.message}")
                        trySend(emptyList()).isSuccess
                    }
                }
            }
        }
        awaitClose { listener.remove() }
    }

    private suspend fun setInitialTaskStatusOnTaskCreation(
        taskId: String, taskCreationTime: String
    ) {
        withContext(Dispatchers.IO + NonCancellable) {
            try {
                getTaskPath(taskId).collection(TASK_STATUS_HISTORY_SUB_COLLECTION)
                    .document(BACKLOG).set(
                        hashMapOf(
                            TASK_STATUS_HISTORY_START_TIME to taskCreationTime.toLong(),
                            TASK_STATUS_HISTORY_END_TIME to TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE,
                            TASK_STATUS_HISTORY_ELAPSED_TIME to 0L
                        ), SetOptions.merge()
                    ).await()
            } catch (exception: Exception) {
                Log.e(
                    logTag + "Create",
                    "Failed to create dummy status on Task Creation exception: ${exception.message}",
                    exception
                )
            }
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
                val elapsedTime = documentSnapshot.getLong(TASK_STATUS_HISTORY_ELAPSED_TIME) ?: 0
                statusHistoryMap[documentSnapshot.id] = listOf(startTime, endTime, elapsedTime)
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