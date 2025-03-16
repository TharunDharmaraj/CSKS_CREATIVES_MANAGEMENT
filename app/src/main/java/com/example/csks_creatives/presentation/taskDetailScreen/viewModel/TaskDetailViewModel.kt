package com.example.csks_creatives.presentation.taskDetailScreen.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.data.utils.Constants.ADMIN_COMMENT_OWNER
import com.example.csks_creatives.domain.model.task.Comment
import com.example.csks_creatives.domain.model.utills.enums.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.useCase.AdminUseCaseFactory
import com.example.csks_creatives.domain.useCase.ClientsUseCaseFactory
import com.example.csks_creatives.domain.useCase.CommentsUseCaseFactory
import com.example.csks_creatives.domain.useCase.TasksManipulationUseCaseFactory
import com.example.csks_creatives.domain.useCase.TasksUseCaseFactory
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.domain.utils.Utils.getAvailableStatusOptions
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskCommentsEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskCreationUiEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDetailEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.DropDownListState
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.TaskCommentState
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.TaskDetailState
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.TaskDetailsSectionVisibilityState
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.toClientTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val tasksUseCaseFactory: TasksUseCaseFactory,
    private val tasksManipulationUseCaseFactory: TasksManipulationUseCaseFactory,
    private val clientsUseCaseFactory: ClientsUseCaseFactory,
    private val adminUseCaseFactory: AdminUseCaseFactory,
    private val commentsUseCaseFactory: CommentsUseCaseFactory
) : ViewModel() {
    init {
        tasksUseCaseFactory.create()
        tasksManipulationUseCaseFactory.create()
        clientsUseCaseFactory.create()
        adminUseCaseFactory.create()
        commentsUseCaseFactory.create()
    }

    private val _uiEvent = MutableSharedFlow<TaskCreationUiEvent>()
    val uiEvent: SharedFlow<TaskCreationUiEvent> = _uiEvent.asSharedFlow()

    private val _taskDetailState = MutableStateFlow(TaskDetailState())
    val taskDetailState = _taskDetailState.asStateFlow()

    private val initialTaskDetailState = MutableStateFlow(TaskDetailState())

    private val _dropDownListState = MutableStateFlow(DropDownListState())
    val dropDownListState = _dropDownListState.asStateFlow()

    private val _taskCommentState = MutableStateFlow(TaskCommentState())
    val taskCommentState = _taskCommentState.asStateFlow()

    private val _visibilityState = MutableStateFlow(TaskDetailsSectionVisibilityState())
    val visibilityState = _visibilityState.asStateFlow()

    private var initialTaskStatus: TaskStatusType? = null

    private var hasInitialized = false
    private var isTaskSaved = false
    private var commentOwner = EMPTY_STRING
    private val _taskName = MutableStateFlow("Task Name")
    var taskName = _taskName.asStateFlow()

    fun onEvent(event: TaskDetailEvent) {
        when (event) {
            TaskDetailEvent.CreateTask -> {
                viewModelScope.launch {
                    val task = _taskDetailState.value.toClientTask()
                    when (val result = tasksUseCaseFactory.createTask(task)) {
                        is ResultState.Success -> {
                            _uiEvent.emit(TaskCreationUiEvent.ShowToast("Task Created Successfully"))
                            _uiEvent.emit(TaskCreationUiEvent.NavigateBack)
                        }

                        is ResultState.Error -> {
                            _uiEvent.emit(TaskCreationUiEvent.ShowToast("Failed Create task ${result.message}"))
                        }

                        is ResultState.Loading -> {

                        }

                        else -> {
                            // Ignore
                        }
                    }
                }
            }

            TaskDetailEvent.SaveTask -> {
                viewModelScope.launch {
                    val initialTask = initialTaskDetailState.value.toClientTask()
                    val currentTask = _taskDetailState.value.toClientTask()
                    when (tasksManipulationUseCaseFactory.editTask(
                        currentTask = currentTask,
                        initialTask = initialTask
                    )) {
                        is ResultState.Success -> {
                            updateTaskStatus(_taskDetailState.value.taskCurrentStatus)
                            Log.d("TaskDetailViewModel", "Task Updated Successfully")
                            _uiEvent.emit(TaskCreationUiEvent.ShowToast("Task Saved Successfully"))
                            _uiEvent.emit(TaskCreationUiEvent.NavigateBack)
                        }

                        is ResultState.Error -> {
                            Log.d("TaskDetailViewModel", "Error in updating task")
                        }

                        else -> {}
                    }
                }
            }

            TaskDetailEvent.AddComment -> {
                viewModelScope.launch {
                    val comment = Comment(
                        commentString = _taskCommentState.value.commentString,
                        commentedBy = _taskCommentState.value.commentedBy
                    )
                    val taskId = _taskDetailState.value.taskId
                    when (commentsUseCaseFactory.postComment(
                        taskId,
                        commentOwner,
                        comment
                    )) {
                        is ResultState.Success -> {
                            _taskCommentState.value = _taskCommentState.value.copy(
                                commentString = EMPTY_STRING,
                                commentedBy = EMPTY_STRING
                            )
                        }

                        is ResultState.Error -> {

                        }

                        else -> {}
                    }
                }
            }

            is TaskDetailEvent.TaskTitleTextFieldChanged -> {
                _taskDetailState.value = _taskDetailState.value.copy(taskTitle = event.taskTitle)
            }

            is TaskDetailEvent.TaskDescriptionTextFieldChanged -> {
                _taskDetailState.value =
                    _taskDetailState.value.copy(taskDescription = event.taskDescription)
            }

            is TaskDetailEvent.TaskClientIdChanged -> {
                _taskDetailState.value = _taskDetailState.value.copy(taskClientId = event.clientId)
            }

            is TaskDetailEvent.TaskAssignedToEmployeeChanged -> {
                _taskDetailState.value =
                    _taskDetailState.value.copy(taskAssignedTo = event.employeeId)
            }

            is TaskDetailEvent.TaskStoryPointsChanged -> {
                _taskDetailState.value =
                    _taskDetailState.value.copy(taskStoryPoints = event.storyPoints)
            }

            is TaskDetailEvent.TaskStatusTypeChanged -> {
                _taskDetailState.value =
                    _taskDetailState.value.copy(taskCurrentStatus = event.taskStatusType)
            }

            TaskDetailEvent.ToggleCommentsSection -> {
                _visibilityState.value =
                    _visibilityState.value.copy(isCommentsSectionVisible = !_visibilityState.value.isCommentsSectionVisible)
            }
        }
    }

    fun onCommentEvent(event: TaskCommentsEvent) {
        when (event) {
            is TaskCommentsEvent.commentStringChanged -> {
                _taskCommentState.value =
                    _taskCommentState.value.copy(commentString = event.commentDescription)
            }

            TaskCommentsEvent.CreateComment -> onEvent(TaskDetailEvent.AddComment)

            TaskCommentsEvent.CancelComment -> {
                _taskCommentState.value = _taskCommentState.value.copy(commentString = "")
            }
        }
    }

    private fun fetchClients() {
        viewModelScope.launch {
            when (val result = clientsUseCaseFactory.getClients(isForceFetchFromServer = false)) {
                is ResultState.Success -> _dropDownListState.value.clientsList = result.data
                is ResultState.Error -> Log.e("TaskDetailViewModel", "Error fetching clients")
                else -> Unit
            }
        }
    }

    private fun fetchEmployees() {
        viewModelScope.launch {
            when (val result =
                adminUseCaseFactory.getEmployeesList(isForceFetchFromServer = false)) {
                is ResultState.Success -> _dropDownListState.value.employeeList = result.data
                is ResultState.Error -> Log.e("TaskDetailViewModel", "Error fetching employees")
                else -> Unit
            }
        }
    }

    private fun fetchTaskStatusHistory(taskId: String) {
        viewModelScope.launch {
            tasksManipulationUseCaseFactory.getTaskStatusHistory(taskId).collect { result ->
                if (result is ResultState.Success) {
                    val taskStatusHistoryList = result.data
                    _taskDetailState.value =
                        _taskDetailState.value.copy(taskStatusHistory = taskStatusHistoryList)
                }
            }
        }
    }

    private fun fetchTaskDetails(taskId: String) {
        viewModelScope.launch {
            tasksUseCaseFactory.getTask(taskId).collect { result ->
                if (result is ResultState.Success) {
                    val task = result.data
                    initialTaskDetailState.value = _taskDetailState.value.copy(
                        taskTitle = task.taskName,
                        taskDescription = task.taskAttachment,
                        taskClientId = task.clientId,
                        taskAssignedTo = task.employeeId,
                        taskStoryPoints = task.taskPoint,
                        taskCurrentStatus = task.currentStatus
                    )
                    _taskDetailState.value = _taskDetailState.value.copy(
                        taskTitle = task.taskName,
                        taskDescription = task.taskAttachment,
                        taskClientId = task.clientId,
                        taskAssignedTo = task.employeeId,
                        taskStoryPoints = task.taskPoint,
                        taskCurrentStatus = task.currentStatus
                    )
                    _taskName.value = task.taskName
                    saveTaskStatus()
                }
            }
        }
    }

    private fun fetchCommentsForTask(taskId: String) {
        viewModelScope.launch {
            commentsUseCaseFactory.getComments(taskId).collect { result ->
                if (result is ResultState.Success) {
                    val commentsList = result.data
                    _taskDetailState.value = _taskDetailState.value.copy(
                        taskComments = commentsList
                    )
                }
            }
        }
    }

    private fun updateTaskStatus(newStatus: TaskStatusType) {
        if (isTaskSaved) {
            _taskDetailState.value = _taskDetailState.value.copy(taskCurrentStatus = newStatus)
            initialTaskStatus = _taskDetailState.value.taskCurrentStatus
        } else {
            val allowedNextStatuses = getAvailableStatusOptions(initialTaskStatus!!)
            if (newStatus.name in allowedNextStatuses) {
                _taskDetailState.value = _taskDetailState.value.copy(taskCurrentStatus = newStatus)
            }
        }
    }

    private fun saveTaskStatus() {
        initialTaskStatus = _taskDetailState.value.taskCurrentStatus
        isTaskSaved = true
    }

    private fun getIsTaskSavedStatus(): Boolean = isTaskSaved

    private fun getInitialTaskStatus(): TaskStatusType = initialTaskStatus ?: TaskStatusType.BACKLOG

    fun getAvailableStatusOptions(): List<String> {
        return if (getIsTaskSavedStatus().not()) {
            getAvailableStatusOptions(_taskDetailState.value.taskCurrentStatus)
        } else {
            getAvailableStatusOptions(getInitialTaskStatus())
        }
    }

    fun initialize(
        userRole: UserRole,
        isTaskCreation: Boolean,
        taskId: String,
        employeeId: String
    ) {
        if (hasInitialized) return
        hasInitialized = true
        // To copy taskId to state, so that it can be used in UC/Repo call
        _taskDetailState.update { it.copy(taskId = taskId) }

        // Todo Move business logics into a UseCase
        if (userRole == UserRole.Admin) {
            commentOwner = ADMIN_COMMENT_OWNER
            fetchClients()
            fetchEmployees()
        }
        if (userRole == UserRole.Employee) {
            commentOwner = employeeId
        }
        if (userRole == UserRole.Employee || (userRole == UserRole.Admin && !isTaskCreation && taskId.isNotEmpty())) {
            fetchTaskDetails(taskId)
            fetchCommentsForTask(taskId)
            fetchTaskStatusHistory(taskId)
        }
    }

}