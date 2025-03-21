package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.BACKLOG
import com.example.csks_creatives.data.utils.Constants.TASKS_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_ATTACHMENT
import com.example.csks_creatives.data.utils.Constants.TASK_CLIENT_ID
import com.example.csks_creatives.data.utils.Constants.TASK_COST
import com.example.csks_creatives.data.utils.Constants.TASK_CREATION_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_CURRENT_STATUS
import com.example.csks_creatives.data.utils.Constants.TASK_EMPLOYEE_ID
import com.example.csks_creatives.data.utils.Constants.TASK_ESTIMATE
import com.example.csks_creatives.data.utils.Constants.TASK_ID
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_START_TIME
import com.example.csks_creatives.data.utils.Constants.TASK_STATUS_HISTORY_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASK_TASK_NAME
import com.example.csks_creatives.data.utils.Constants.TASK_TYPE
import com.example.csks_creatives.data.utils.Utils.convertStringStatusToStatusType
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.model.task.TaskStatusHistory
import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import com.example.csks_creatives.domain.repository.remote.TasksRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
                    TASK_TYPE to task.taskType,
                    TASK_CURRENT_STATUS to task.currentStatus
                ), SetOptions.merge()
            )
            setInitialTaskStatusOnTaskCreation(taskId, taskCreationTime)
            Log.d(logTag + "Create", "Successfully createdTask $task")
        } catch (exception: Exception) {
            Log.d(logTag + "Create", "Failed $exception to create Task $task")
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
            documentSnapshot.let { documentSnapshot1 ->
                val getTaskStatusJob = tasksRepositoryCoroutineScope.launch {
                    val task = documentSnapshot1?.toObject(ClientTask::class.java)
                        ?.copy(taskId = documentSnapshot1.id)
                    task?.let {
                        val statusHistory = getTaskStatusList(documentSnapshot1.id)
                        updatedTaskWithStatusHistory = it.copy(
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
            documentSnapshot.let { documentSnapshot1 ->
                val task = documentSnapshot1?.toObject(ClientTask::class.java)
                    ?.copy(taskId = documentSnapshot1.id) ?: ClientTask()
                val taskOverView = ClientTaskOverview(
                    taskId = task.taskId,
                    taskName = task.taskName,
                    taskCreationTime = task.taskCreationTime,
                    clientId = task.clientId,
                    taskEstimate = task.taskEstimate,
                    taskCost = task.taskCost,
                    taskType = task.taskType,
                    currentStatus = task.currentStatus
                )
                trySend(taskOverView)
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getTasksForClient(clientId: String): Flow<List<ClientTask>> =
        getTasksBasedOnCondition(TASK_CLIENT_ID, clientId)

    override suspend fun getTasksForEmployee(employeeId: String): Flow<List<ClientTask>> =
        getTasksBasedOnCondition(TASK_EMPLOYEE_ID, employeeId)

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
            Log.d(logTag + "Create", "Successfully created dummy status on Task Creation")
        } catch (exception: Exception) {
            Log.d(logTag + "Create", "Failed to create dummy status on Task Creation")
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
            Log.d(logTag + "StatusList", "HistoryMap is $statusHistoryMap")
        } catch (exception: Exception) {
            Log.d(logTag + "StatusList", "Error building statusMap $exception")
        }
        return statusHistoryMap
    }

    private fun getTaskPath(taskId: String) =
        firestore.collection(TASKS_COLLECTION).document(taskId)
}