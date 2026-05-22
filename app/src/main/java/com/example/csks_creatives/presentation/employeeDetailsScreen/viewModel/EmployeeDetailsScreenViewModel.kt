package com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.factories.*
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.domain.utils.SecurityUtils.decrypt
import com.example.csks_creatives.data.utils.Constants.DEFAULT_TASK_FETCH_LIMIT
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.event.EmployeeDetailsScreenEvent
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.state.EmployeeDetailsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeDetailsScreenViewModel @Inject constructor(
    private val adminUseCaseFactory: AdminUseCaseFactory,
    private val tasksUseCaseFactory: TasksUseCaseFactory,
    private val employeeUseCaseFactory: EmployeeUseCaseFactory
) : ViewModel() {

    private val _activeTasksIds = MutableStateFlow<List<String>>(emptyList())
    private val _completedTasksIds = MutableStateFlow<List<String>>(emptyList())

    private val _employeeDetailsScreenState = MutableStateFlow(EmployeeDetailsScreenState())
    val employeeDetailsScreenState = _employeeDetailsScreenState.asStateFlow()

    private val tasksCompletedFetchFromFirestore =
        MutableStateFlow<List<ClientTaskOverview>>(emptyList())
    private val activeTasksFetchFromFirestore =
        MutableStateFlow<List<ClientTaskOverview>>(emptyList())

    private val _employeeDetailScreenTitle = MutableStateFlow(EMPTY_STRING)
    val employeeDetailsScreenTitle = _employeeDetailScreenTitle.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent: SharedFlow<ToastUiEvent> = _uiEvent.asSharedFlow()

    private var employeeId: String = EMPTY_STRING
    private var hasInitialized = false

    private var employeeDetailsJob: Job? = null
    private var leavesJob: Job? = null

    fun onEvent(employeeDetailsScreenEvent: EmployeeDetailsScreenEvent) {
        when (employeeDetailsScreenEvent) {
            EmployeeDetailsScreenEvent.OnEmployeeTaskItemClicked -> {
                // TODO Go to Task Details Page
            }

            is EmployeeDetailsScreenEvent.OnSearchTextChangedForCompleted -> {
                _employeeDetailsScreenState.update {
                    it.copy(searchTextForCompleted = employeeDetailsScreenEvent.text)
                }
                filterCompletedTasks()
            }

            is EmployeeDetailsScreenEvent.OnSearchTextChangedForActive -> {
                _employeeDetailsScreenState.update {
                    it.copy(searchTextForActive = employeeDetailsScreenEvent.text)
                }
                filterActiveTasks()
            }

            is EmployeeDetailsScreenEvent.Order -> {
                val sortedCompletedTasks =
                    if (employeeDetailsScreenEvent.order == DateOrder.Ascending) {
                        _employeeDetailsScreenState.value.tasksCompleted.sortedBy { it.taskCreationTime }
                    } else {
                        _employeeDetailsScreenState.value.tasksCompleted.sortedByDescending { it.taskCreationTime }
                    }
                val sortedInProgressTasks =
                    if (employeeDetailsScreenEvent.order == DateOrder.Ascending) {
                        _employeeDetailsScreenState.value.tasksInProgress.sortedBy { it.taskCreationTime }
                    } else {
                        _employeeDetailsScreenState.value.tasksInProgress.sortedByDescending { it.taskCreationTime }
                    }
                _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                    completedTasksOrder = employeeDetailsScreenEvent.order,
                    tasksInProgress = sortedInProgressTasks,
                    tasksCompleted = sortedCompletedTasks
                )
            }

            EmployeeDetailsScreenEvent.ToggleCompletedTasksSectionVisibility -> {
                _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                    isCompletedTasksSectionVisible = !_employeeDetailsScreenState.value.isCompletedTasksSectionVisible
                )
            }

            EmployeeDetailsScreenEvent.ToggleActiveTasksSectionVisibility -> {
                _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                    isActiveTasksSectionVisible = !_employeeDetailsScreenState.value.isActiveTasksSectionVisible
                )
            }

            EmployeeDetailsScreenEvent.ToggleSearchBarVisibility -> {
                _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                    isSearchBarVisible = !_employeeDetailsScreenState.value.isSearchBarVisible
                )
            }

            EmployeeDetailsScreenEvent.ForceFetchTasks -> {
                  if (_employeeDetailsScreenState.value.isActiveTasksLoading || _employeeDetailsScreenState.value.isCompletedTasksLoading) return
                
                // Clear observing sets to allow re-fetching
                _activeTasksObserving.clear()
                _completedTasksObserving.clear()
                
                _employeeDetailsScreenState.update { 
                    it.copy(
                        tasksInProgress = emptyList(), 
                        tasksCompleted = emptyList(), 
                        approvedLeavesList = emptyList(),
                        unApprovedLeavesList = emptyList(),
                        rejectedLeavesList = emptyList(),
                        tasksLimit = DEFAULT_TASK_FETCH_LIMIT
                    ) 
                }
                getEmployeeDetails(employeeId, true)
                getAllLeavesTaken(employeeId, true)
            }

            EmployeeDetailsScreenEvent.LoadMoreTasks -> {
                if (_employeeDetailsScreenState.value.isEndReached || _employeeDetailsScreenState.value.isPaginationLoading) return
                _employeeDetailsScreenState.update { it.copy(tasksLimit = it.tasksLimit + DEFAULT_TASK_FETCH_LIMIT) }
                getEmployeeDetails(employeeId)
            }

            EmployeeDetailsScreenEvent.FetchCompletedTasksCount -> {
                viewModelScope.launch {
                    _employeeDetailsScreenState.update { it.copy(isCompletedCountLoading = true) }
                    val result = adminUseCaseFactory.forceUpdateEmployeeCompletedTasksCount(employeeId)
                    when (result) {
                        is ResultState.Success -> {
                            _employeeDetailsScreenState.update {
                                it.copy(
                                    totalNumberOfTasksCompleted = result.data.toString(),
                                    isCompletedCountLoading = false
                                )
                            }
                        }
                        is ResultState.Error -> {
                            _employeeDetailsScreenState.update { it.copy(isCompletedCountLoading = false) }
                            _uiEvent.emit(ToastUiEvent.ShowToast(result.message))
                        }
                        ResultState.Loading -> {}
                    }
                }
            }
        }
    }

    private fun filterCompletedTasks() {
        val searchText = _employeeDetailsScreenState.value.searchTextForCompleted
        val filtered = if (searchText.isBlank()) {
            tasksCompletedFetchFromFirestore.value
        } else {
            tasksCompletedFetchFromFirestore.value.filter { task ->
                task.taskName.contains(searchText, ignoreCase = true) ||
                        task.clientId.contains(searchText, ignoreCase = true) ||
                        task.currentStatus.name.contains(searchText, ignoreCase = true)
            }
        }

        _employeeDetailsScreenState.update {
            it.copy(tasksCompleted = filtered)
        }
    }

    private fun filterActiveTasks() {
        val searchText = _employeeDetailsScreenState.value.searchTextForActive
        val filtered = if (searchText.isBlank()) {
            activeTasksFetchFromFirestore.value
        } else {
            activeTasksFetchFromFirestore.value.filter { task ->
                task.taskName.contains(searchText, ignoreCase = true) ||
                        task.clientId.contains(searchText, ignoreCase = true) ||
                        task.currentStatus.name.contains(searchText, ignoreCase = true)
            }
        }

        _employeeDetailsScreenState.update {
            it.copy(tasksInProgress = filtered)
        }
    }


    private fun getEmployeeDetails(employeeId: String, isForceFetch: Boolean = false) {
        employeeDetailsJob?.cancel()
        val limit = if (isForceFetch) null else _employeeDetailsScreenState.value.tasksLimit
        if (!isForceFetch && _employeeDetailsScreenState.value.tasksLimit > DEFAULT_TASK_FETCH_LIMIT) {
            _employeeDetailsScreenState.update { it.copy(isPaginationLoading = true) }
        }

        employeeDetailsJob = viewModelScope.launch {
            adminUseCaseFactory.getEmployeeDetails(employeeId, isForceFetch, limit).collect { result ->
                if (result is ResultState.Success) {
                    val employeeDetails = result.data
                    _activeTasksIds.value = employeeDetails.tasksInProgress
                    _completedTasksIds.value = employeeDetails.tasksCompleted
                    val isEndReached = if (limit != null) (employeeDetails.tasksInProgress.size + employeeDetails.tasksCompleted.size) < limit else true
                    _employeeDetailsScreenState.update {
                        it.copy(
                            employeeName = employeeDetails.employeeName,
                            employeePassword = decrypt(employeeDetails.employeePassword),
                            employeeJoinedTime = employeeDetails.joinedTime,
                            isPaginationLoading = false,
                            isEndReached = isEndReached
                        )
                    }
                }
            }
        }
    }

    private val _activeTasksObserving = mutableSetOf<String>()
    private val _completedTasksObserving = mutableSetOf<String>()

    private fun observeTaskIds() {
        viewModelScope.launch {
            _activeTasksIds.collect { taskIds ->
                if (taskIds.isNotEmpty()) {
                    getActiveTasksList(taskIds)
                } else {
                    _employeeDetailsScreenState.update {
                        it.copy(
                            isActiveTasksLoading = false,
                            tasksInProgress = emptyList()
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            _completedTasksIds.collect { taskIds ->
                if (taskIds.isNotEmpty()) {
                    getCompletedTasksList(taskIds)
                } else {
                    _employeeDetailsScreenState.update {
                        it.copy(
                            isCompletedTasksLoading = false,
                            tasksCompleted = emptyList()
                        )
                    }
                }
            }
        }
    }

    private fun getCompletedTasksList(tasksCompletedIds: List<String>) {
        tasksCompletedIds.forEach { taskId ->
            if (_completedTasksObserving.contains(taskId)) return@forEach
            _completedTasksObserving.add(taskId)
            
            viewModelScope.launch {
                tasksUseCaseFactory.getTaskOverView(taskId).collect { taskOverViewResult ->
                    when (taskOverViewResult) {
                        is ResultState.Error -> {
                            _employeeDetailsScreenState.update { it.copy(isCompletedTasksLoading = false) }
                            _uiEvent.emit(ToastUiEvent.ShowToast("Error retrieving data ${taskOverViewResult.message}"))
                        }
                        ResultState.Loading -> {
                            _employeeDetailsScreenState.update { it.copy(isCompletedTasksLoading = true) }
                        }
                        is ResultState.Success<ClientTaskOverview> -> {
                            val taskOverViewData = taskOverViewResult.data
                            _employeeDetailsScreenState.update { state ->
                                val completedTasksList = tasksUseCaseFactory.getUniqueTaskOverViewList(
                                    taskOverViewData,
                                    state.tasksCompleted
                                )
                                state.copy(
                                    isCompletedTasksLoading = false,
                                    tasksCompleted = completedTasksList.sortedByDescending { it.taskCreationTime }
                                )
                            }
                            tasksCompletedFetchFromFirestore.value = _employeeDetailsScreenState.value.tasksCompleted
                        }
                    }
                }
            }
        }
    }

    private fun getActiveTasksList(activeTasksIds: List<String>) {
        activeTasksIds.forEach { taskId ->
            if (_activeTasksObserving.contains(taskId)) return@forEach
            _activeTasksObserving.add(taskId)

            viewModelScope.launch {
                tasksUseCaseFactory.getTaskOverView(taskId).collect { taskOverViewResult ->
                    when (taskOverViewResult) {
                        is ResultState.Error -> {
                            _employeeDetailsScreenState.update { it.copy(isActiveTasksLoading = false) }
                            _uiEvent.emit(ToastUiEvent.ShowToast("Error retrieving data ${taskOverViewResult.message}"))
                        }
                        ResultState.Loading -> {
                            _employeeDetailsScreenState.update { it.copy(isActiveTasksLoading = true) }
                        }
                        is ResultState.Success<ClientTaskOverview> -> {
                            val taskOverViewData = taskOverViewResult.data
                            _employeeDetailsScreenState.update { state ->
                                var activeTasksList = tasksUseCaseFactory.getUniqueTaskOverViewList(
                                    taskOverViewData,
                                    state.tasksInProgress
                                )
                                var completedTasks = state.tasksCompleted
                                
                                if (taskOverViewData.currentStatus == TaskStatusType.COMPLETED) {
                                    completedTasks = (completedTasks + taskOverViewData).sortedByDescending { it.taskCreationTime }
                                    activeTasksList = tasksUseCaseFactory.removeCompletedTaskFromActiveList(
                                        taskOverViewData,
                                        activeTasksList
                                    )
                                }
                                
                                state.copy(
                                    isActiveTasksLoading = false,
                                    tasksInProgress = activeTasksList.sortedByDescending { it.taskCreationTime },
                                    tasksCompleted = completedTasks
                                )
                            }
                            activeTasksFetchFromFirestore.value = _employeeDetailsScreenState.value.tasksInProgress
                        }
                    }
                }
            }
        }
    }

    fun initialize(employeeId: String) {
        if (hasInitialized) return
        hasInitialized = true
        this.employeeId = employeeId
        _employeeDetailScreenTitle.value = employeeId
        
        // Initially set count from local DB
        viewModelScope.launch {
            val localCount = adminUseCaseFactory.getLocalEmployeeCompletedTasksCount(employeeId)
            _employeeDetailsScreenState.update { it.copy(totalNumberOfTasksCompleted = localCount) }
        }

        getEmployeeDetails(employeeId)
        getAllLeavesTaken(employeeId)
    }

    private fun getAllLeavesTaken(employeeId: String, isForceFetch: Boolean = false) {
        leavesJob?.cancel()
        leavesJob = viewModelScope.launch {
            employeeUseCaseFactory.getAllLeaveRequestsGrouped(employeeId, isForceFetch).collect { result ->
                if (result is ResultState.Success) {
                    val groupedLeavesList = result.data
                    _employeeDetailsScreenState.update {
                        it.copy(
                            approvedLeavesList = groupedLeavesList.approved,
                            unApprovedLeavesList = groupedLeavesList.unapproved,
                            rejectedLeavesList = groupedLeavesList.rejected
                        )
                    }
                }
            }
        }
    }

    fun emitLogoutEvent(isUserLoggedOut: Boolean) {
        viewModelScope.launch {
            LogoutEvent.emitLogoutEvent(isUserLoggedOut)
        }
    }

    fun getTimeTakenForCompletion(taskId: String) =
        tasksUseCaseFactory.getTimeTakenForCompletedTask(
            taskId,
            _employeeDetailsScreenState.value.tasksCompleted
        )

    fun getTimeTakenForActiveTask(taskId: String) =
        tasksUseCaseFactory.getTimeTakenForActiveTask(
            taskId,
            _employeeDetailsScreenState.value.tasksInProgress
        )

    fun setEmployeeDetailsScreenToolbarTitle(title: String) {
        _employeeDetailScreenTitle.value = title
    }

    fun approveEmployeeLeave(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            adminUseCaseFactory.markLeaveRequestAsApproved(leaveRequest)
        }
    }

    fun rejectEmployeeLeave(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            adminUseCaseFactory.markLeaveRequestAsRejected(leaveRequest)
        }
    }

    init {
        adminUseCaseFactory.create()
        tasksUseCaseFactory.create()
        observeTaskIds()
    }
}