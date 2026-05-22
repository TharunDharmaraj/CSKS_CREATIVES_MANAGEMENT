package com.example.csks_creatives.presentation.homeScreen.viewModel.admin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.data.utils.Constants.DEFAULT_TASK_FETCH_LIMIT
import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveApprovalStatus
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.UserPersistenceUseCase
import com.example.csks_creatives.domain.useCase.factories.*
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.*
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminHomeScreenViewModel @Inject constructor(
    private val adminUseCaseFactory: AdminUseCaseFactory,
    private val clientsUseCaseFactory: ClientsUseCaseFactory,
    private val tasksUseCaseFactory: TasksUseCaseFactory,
    private val userPersistenceUseCase: UserPersistenceUseCase
) : ViewModel() {

    private val _homeScreenTitleState = MutableStateFlow("Welcome, Kishor!")
    val homeScreenTitle = _homeScreenTitleState.asStateFlow()

    private val _adminHomeScreenState = MutableStateFlow(AdminHomeScreenState())
    val adminHomeScreenState: StateFlow<AdminHomeScreenState> = _adminHomeScreenState.asStateFlow()

    private val _adminHomeScreenVisibilityState = MutableStateFlow(AdminHomeScreenVisibilityState())
    val adminHomeScreenVisibilityState: StateFlow<AdminHomeScreenVisibilityState> =
        _adminHomeScreenVisibilityState.asStateFlow()

    private val _adminHomeScreenLoadingState = MutableStateFlow(AdminHomeScreenLoadingState())
    val adminHomeScreenLoadingState: StateFlow<AdminHomeScreenLoadingState> =
        _adminHomeScreenLoadingState.asStateFlow()

    private val _addClientDialogState = MutableStateFlow(AddClientDialogState())
    val addClientDialogState: StateFlow<AddClientDialogState> = _addClientDialogState.asStateFlow()

    private val _addEmployeeDialogState = MutableStateFlow(AddEmployeeDialogState())
    val addEmployeeDialogState: StateFlow<AddEmployeeDialogState> =
        _addEmployeeDialogState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent: SharedFlow<ToastUiEvent> = _uiEvent.asSharedFlow()

    val hasUnapprovedLeaves = mutableStateOf(false)

    private var isEmployeesFetched = false
    private var isClientsFetched = false
    private var isActiveTasksFetched = false
    private var isCompletedTasksFetched = false
    private var isBacklogTasksFetched = false

    private var activeTasksJob: Job? = null
    private var backlogTasksJob: Job? = null
    private var completedTasksJob: Job? = null

    private var employeesJob: Job? = null
    private var clientsJob: Job? = null
    private var leaveRequestsJob: Job? = null

    fun onHomeScreenEvent(adminHomeScreenEvent: AdminHomeScreenEvent) {
        when (adminHomeScreenEvent) {
            AdminHomeScreenEvent.CreateEmployeeButtonClick -> {
                _adminHomeScreenVisibilityState.value =
                    _adminHomeScreenVisibilityState.value.copy(isAddEmployeeDialogVisible = true)
            }

            AdminHomeScreenEvent.CreateClientButtonClick -> {
                _adminHomeScreenVisibilityState.value =
                    _adminHomeScreenVisibilityState.value.copy(isAddClientDialogVisible = true)
            }

            AdminHomeScreenEvent.ToggleClientSection -> {
                _adminHomeScreenVisibilityState.update { it.copy(isClientSectionVisible = true) }
                fetchClients(isForceFetch = true)
            }

            AdminHomeScreenEvent.ToggleEmployeeSection -> {
                _adminHomeScreenVisibilityState.update { it.copy(isEmployeeSectionVisible = true) }
                fetchEmployees(isForceFetch = true)
            }

            AdminHomeScreenEvent.ToggleActiveTaskSection -> {
                _adminHomeScreenVisibilityState.update { it.copy(isActiveTaskSectionVisible = true) }
                if (isActiveTasksFetched.not()) {
                    fetchActiveTasks()
                }
            }

            AdminHomeScreenEvent.ToggleBacklogTaskSection -> {
                _adminHomeScreenVisibilityState.update { it.copy(isBacklogTaskSectionVisible = true) }
                if (isBacklogTasksFetched.not()) {
                    fetchBacklogTasks()
                }
            }

            AdminHomeScreenEvent.ToggleCompletedTaskSection -> {
                _adminHomeScreenVisibilityState.update { it.copy(isCompletedTaskSectionVisible = true) }
                if (isCompletedTasksFetched.not()) {
                    fetchCompletedTasks()
                }
            }

            AdminHomeScreenEvent.ToggleActiveLeavesSection -> {
                _adminHomeScreenVisibilityState.update {
                    it.copy(
                        isLeaveRequestsSectionVisible = !_adminHomeScreenVisibilityState.value.isLeaveRequestsSectionVisible
                    )
                }
            }

            AdminHomeScreenEvent.ForceFetchTasks -> {
                if (_adminHomeScreenLoadingState.value.isActiveTasksLoading || 
                    _adminHomeScreenLoadingState.value.isBacklogTasksLoading || 
                    _adminHomeScreenLoadingState.value.isCompletedTasksLoading) return
                
                _adminHomeScreenState.update { 
                    it.copy(
                        activeTaskList = emptyList(),
                        backlogTaskList = emptyList(),
                        completedTasksList = emptyList(),
                        activeTasksLimit = DEFAULT_TASK_FETCH_LIMIT,
                        backlogTasksLimit = DEFAULT_TASK_FETCH_LIMIT,
                        completedTasksLimit = DEFAULT_TASK_FETCH_LIMIT
                    ) 
                }
                fetchActiveTasks(isForceFetch = true)
                fetchBacklogTasks(isForceFetch = true)
                fetchCompletedTasks(isForceFetch = true)
            }

            AdminHomeScreenEvent.ForceFetchEmployees -> {
                if (_adminHomeScreenLoadingState.value.isEmployeesLoading) return
                _adminHomeScreenState.update { it.copy(employeeList = emptyList()) }
                fetchEmployees(isForceFetch = true)
            }

            AdminHomeScreenEvent.ForceFetchClients -> {
                if (_adminHomeScreenLoadingState.value.isClientsLoading) return
                _adminHomeScreenState.update { it.copy(clientList = emptyList()) }
                fetchClients(isForceFetch = true)
            }

            AdminHomeScreenEvent.ForceFetchLeaveRequests -> {
                if (_adminHomeScreenLoadingState.value.isLeaveRequestsLoading) return
                _adminHomeScreenState.update { it.copy(activeLeaveRequests = emptyList()) }
                fetchActiveLeaveRequests(isForceFetch = true)
            }

            AdminHomeScreenEvent.LoadMoreEmployees -> {
                if (_adminHomeScreenState.value.isEmployeesEndReached || _adminHomeScreenState.value.isPaginationLoading) return
                _adminHomeScreenState.update { it.copy(employeesLimit = it.employeesLimit + DEFAULT_TASK_FETCH_LIMIT) }
                fetchEmployees()
            }

            is AdminHomeScreenEvent.LoadMoreActiveTasks -> {
                if (_adminHomeScreenState.value.isActiveTasksEndReached || _adminHomeScreenState.value.isPaginationLoading) return
                _adminHomeScreenState.update { it.copy(activeTasksLimit = it.activeTasksLimit + DEFAULT_TASK_FETCH_LIMIT) }
                fetchActiveTasks()
            }

            is AdminHomeScreenEvent.LoadMoreBacklogTasks -> {
                if (_adminHomeScreenState.value.isBacklogTasksEndReached || _adminHomeScreenState.value.isPaginationLoading) return
                _adminHomeScreenState.update { it.copy(backlogTasksLimit = it.backlogTasksLimit + DEFAULT_TASK_FETCH_LIMIT) }
                fetchBacklogTasks()
            }

            is AdminHomeScreenEvent.LoadMoreCompletedTasks -> {
                if (_adminHomeScreenState.value.isCompletedTasksEndReached || _adminHomeScreenState.value.isPaginationLoading) return
                _adminHomeScreenState.update { it.copy(completedTasksLimit = it.completedTasksLimit + DEFAULT_TASK_FETCH_LIMIT) }
                fetchCompletedTasks()
            }

            is AdminHomeScreenEvent.ToggleOrderDate -> {
                _adminHomeScreenState.update {
                    it.copy(
                        tasksOrder = adminHomeScreenEvent.order
                    )
                }
                updateTasksOrder(adminHomeScreenEvent.order)
            }
        }
    }

    private fun updateTasksOrder(order: DateOrder) {
        if (order == DateOrder.Descending) {
            _adminHomeScreenState.update {
                it.copy(
                    activeTaskList = _adminHomeScreenState.value.activeTaskList.sortedByDescending { task -> task.taskCreationTime },
                    backlogTaskList = _adminHomeScreenState.value.backlogTaskList.sortedByDescending { task -> task.taskCreationTime },
                    completedTasksList = _adminHomeScreenState.value.completedTasksList.sortedByDescending { task -> task.taskCreationTime }
                )
            }
        } else {
            _adminHomeScreenState.update {
                it.copy(
                    activeTaskList = _adminHomeScreenState.value.activeTaskList.sortedBy { task -> task.taskCreationTime },
                    backlogTaskList = _adminHomeScreenState.value.backlogTaskList.sortedBy { task -> task.taskCreationTime },
                    completedTasksList = _adminHomeScreenState.value.completedTasksList.sortedBy { task -> task.taskCreationTime }
                )
            }
        }
    }

    fun onClientDialogEvent(addClientDialogEvent: AddClientDialogEvent) {
        when (addClientDialogEvent) {
            AddClientDialogEvent.AddClientButtonClicked -> {
                viewModelScope.launch {
                    val result =
                        clientsUseCaseFactory.addClient(
                            Client(clientName = _addClientDialogState.value.clientName)
                        )
                    if (result is ResultState.Success) {
                        fetchClients()
                        _uiEvent.emit(ToastUiEvent.ShowToast("Client Added Successfully"))
                        _adminHomeScreenVisibilityState.value =
                            _adminHomeScreenVisibilityState.value.copy(isAddClientDialogVisible = false)
                    }
                    if (result is ResultState.Error) {
                        _uiEvent.emit(ToastUiEvent.ShowToast("Error Adding Client ${result.message}"))
                    }
                }
            }

            is AddClientDialogEvent.ClientNameTextFieldChanged -> {
                _addClientDialogState.value =
                    _addClientDialogState.value.copy(clientName = addClientDialogEvent.clientName)
            }

            AddClientDialogEvent.CloseDialogButtonClicked -> {
                _adminHomeScreenVisibilityState.value =
                    _adminHomeScreenVisibilityState.value.copy(isAddClientDialogVisible = false)
            }
        }
    }

    fun onEmployeeDialogEvent(addEmployeeDialogEvent: AddEmployeeDialogEvent) {
        when (addEmployeeDialogEvent) {
            AddEmployeeDialogEvent.AddEmployeeButtonClicked -> {
                viewModelScope.launch {
                    val result = adminUseCaseFactory.createEmployee(
                        Employee(
                            employeeName = _addEmployeeDialogState.value.employeeName.lowercase(),
                            employeePassword = _addEmployeeDialogState.value.employeePassword.lowercase(),
                        )
                    )
                    if (result is ResultState.Success) {
                        fetchEmployees()
                        _uiEvent.emit(ToastUiEvent.ShowToast("Employee Added Successfully"))
                        _adminHomeScreenVisibilityState.value =
                            _adminHomeScreenVisibilityState.value.copy(
                                isAddEmployeeDialogVisible = false
                            )
                    }
                    if (result is ResultState.Error) {
                        _uiEvent.emit(ToastUiEvent.ShowToast("Error Adding Employee ${result.message}"))
                    }
                }
            }

            is AddEmployeeDialogEvent.EmployeeNameTextFieldChanged -> {
                _addEmployeeDialogState.value =
                    _addEmployeeDialogState.value.copy(employeeName = addEmployeeDialogEvent.employeeName)
            }

            is AddEmployeeDialogEvent.EmployeeNamePasswordFieldChanged -> {
                _addEmployeeDialogState.value =
                    _addEmployeeDialogState.value.copy(employeePassword = addEmployeeDialogEvent.employeePassword)
            }

            AddEmployeeDialogEvent.CloseDialogButtonClicked -> {
                _adminHomeScreenVisibilityState.value =
                    _adminHomeScreenVisibilityState.value.copy(isAddEmployeeDialogVisible = false)
            }
        }
    }

    private fun fetchEmployees(isForceFetch: Boolean = false) {
        employeesJob?.cancel()
        employeesJob = viewModelScope.launch {
            _adminHomeScreenLoadingState.update { it.copy(isEmployeesLoading = true) }

            val result = adminUseCaseFactory.getEmployeesList(isForceFetch = isForceFetch, limit = null)
            if (result is ResultState.Success) {
                _adminHomeScreenState.update {
                    it.copy(
                        employeeList = result.data,
                        isEmployeesEndReached = true,
                        isPaginationLoading = false
                    )
                }
                _adminHomeScreenLoadingState.update { it.copy(isEmployeesLoading = false) }
                isEmployeesFetched = true
            }
        }
    }

    private fun fetchClients(isForceFetch: Boolean = false) {
        clientsJob?.cancel()
        clientsJob = viewModelScope.launch {
            _adminHomeScreenLoadingState.update { it.copy(isClientsLoading = true) }

            val result = if (isForceFetch) clientsUseCaseFactory.getAllClients() 
                         else clientsUseCaseFactory.getClients(isForceFetch = false, limit = null)
            
            when (result) {
                is ResultState.Error -> {
                     _adminHomeScreenState.update { it.copy(isPaginationLoading = false) }
                     _adminHomeScreenLoadingState.update { it.copy(isClientsLoading = false) }
                }
                ResultState.Loading -> {}

                is ResultState.Success<List<Client>> -> {
                    _adminHomeScreenState.update {
                        it.copy(
                            clientList = result.data,
                            isClientsEndReached = true,
                            isPaginationLoading = false
                        )
                    }
                    _adminHomeScreenLoadingState.update { it.copy(isClientsLoading = false) }
                    isClientsFetched = true
                }
            }
        }
    }

    private fun fetchActiveTasks(isForceFetch: Boolean = false) {
        activeTasksJob?.cancel()
        activeTasksJob = viewModelScope.launch {
            val limit = if (isForceFetch) null else _adminHomeScreenState.value.activeTasksLimit
            if (!isForceFetch && _adminHomeScreenState.value.activeTasksLimit > DEFAULT_TASK_FETCH_LIMIT) {
                _adminHomeScreenState.update { it.copy(isPaginationLoading = true) }
            } else {
                _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                    isActiveTasksLoading = true
                )
            }
            tasksUseCaseFactory.getAllActiveTasks(_adminHomeScreenState.value.tasksOrder, isForceFetch, limit)
                .collect { result ->
                    if (result is ResultState.Success) {
                        val isEndReached = if (limit != null) result.data.size < limit else true
                        _adminHomeScreenState.update {
                            it.copy(
                                activeTaskList = result.data,
                                isActiveTasksEndReached = isEndReached,
                                isPaginationLoading = false
                            )
                        }
                        _adminHomeScreenLoadingState.value =
                            _adminHomeScreenLoadingState.value.copy(
                                isActiveTasksLoading = false
                            )
                        isActiveTasksFetched = true
                    }
                }
        }
    }

    private fun fetchActiveLeaveRequests(isForceFetch: Boolean = false) {
        leaveRequestsJob?.cancel()
        leaveRequestsJob = viewModelScope.launch {
            _adminHomeScreenLoadingState.update { it.copy(isLeaveRequestsLoading = true) }

            adminUseCaseFactory.getAllActiveLeaveRequests(isForceFetch).collect { result ->
                if (result is ResultState.Success) {
                    hasUnapprovedLeaves.value =
                        result.data.any { it.approvedStatus == LeaveApprovalStatus.UN_APPROVED }
                    _adminHomeScreenState.update {
                        it.copy(
                            activeLeaveRequests = result.data,
                            isPaginationLoading = false
                        )
                    }
                    _adminHomeScreenLoadingState.update {
                        it.copy(
                            isLeaveRequestsLoading = false
                        )
                    }
                }

            }
        }
    }

    private fun fetchCompletedTasks(isForceFetch: Boolean = false) {
        completedTasksJob?.cancel()
        completedTasksJob = viewModelScope.launch {
            val limit = if (isForceFetch) null else _adminHomeScreenState.value.completedTasksLimit
            if (!isForceFetch && _adminHomeScreenState.value.completedTasksLimit > DEFAULT_TASK_FETCH_LIMIT) {
                _adminHomeScreenState.update { it.copy(isPaginationLoading = true) }
            } else {
                _adminHomeScreenLoadingState.update { it.copy(isCompletedTasksLoading = true) }
            }
            tasksUseCaseFactory.getAllCompletedTasks(_adminHomeScreenState.value.tasksOrder, isForceFetch, limit)
                .collect { result ->
                    if (result is ResultState.Success) {
                        val isEndReached = if (limit != null) result.data.size < limit else true
                        _adminHomeScreenState.update {
                            it.copy(
                                completedTasksList = result.data,
                                isCompletedTasksEndReached = isEndReached,
                                isPaginationLoading = false
                            )
                        }
                        _adminHomeScreenLoadingState.value =
                            _adminHomeScreenLoadingState.value.copy(
                                isCompletedTasksLoading = false
                            )
                        isCompletedTasksFetched = true
                    }
                }
        }
    }

    private fun fetchBacklogTasks(isForceFetch: Boolean = false) {
        backlogTasksJob?.cancel()
        backlogTasksJob = viewModelScope.launch {
            val limit = if (isForceFetch) null else _adminHomeScreenState.value.backlogTasksLimit
            if (!isForceFetch && _adminHomeScreenState.value.backlogTasksLimit > DEFAULT_TASK_FETCH_LIMIT) {
                _adminHomeScreenState.update { it.copy(isPaginationLoading = true) }
            } else {
                _adminHomeScreenLoadingState.update { it.copy(isBacklogTasksLoading = true) }
            }
            tasksUseCaseFactory.getAllBacklogTasks(_adminHomeScreenState.value.tasksOrder, isForceFetch, limit)
                .collect { result ->
                    if (result is ResultState.Success) {
                        val isEndReached = if (limit != null) result.data.size < limit else true
                        _adminHomeScreenState.update {
                            it.copy(
                                backlogTaskList = result.data,
                                isBacklogTasksEndReached = isEndReached,
                                isPaginationLoading = false
                            )
                        }
                        _adminHomeScreenLoadingState.value =
                            _adminHomeScreenLoadingState.value.copy(
                                isBacklogTasksLoading = false
                            )
                        isBacklogTasksFetched = true
                    }
                }
        }
    }

    fun emitLogoutEvent(isUserLoggedOut: Boolean) {
        viewModelScope.launch {
            if (isUserLoggedOut) {
                userPersistenceUseCase.deleteCurrentUser()
            }
            LogoutEvent.emitLogoutEvent(isUserLoggedOut)
        }
    }

    fun onLeaveRequestApproved(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            val result = adminUseCaseFactory.markLeaveRequestAsApproved(leaveRequest)
            if (result is ResultState.Success) {
                _uiEvent.emit(ToastUiEvent.ShowToast("Leave Approved Successfully"))
            } else if (result is ResultState.Error) {
                _uiEvent.emit(ToastUiEvent.ShowToast("Error Approving Leave: ${result.message}"))
            }
        }
    }

    fun onLeaveRequestRejected(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            val result = adminUseCaseFactory.markLeaveRequestAsRejected(leaveRequest)
            if (result is ResultState.Success) {
                _uiEvent.emit(ToastUiEvent.ShowToast("Leave Rejected Successfully"))
            } else if (result is ResultState.Error) {
                _uiEvent.emit(ToastUiEvent.ShowToast("Error Rejected Leave: ${result.message}"))
            }
        }
    }

    fun setHomeScreenTitle(title: String) {
        _homeScreenTitleState.value = title
    }

    init {
        adminUseCaseFactory.create()
        clientsUseCaseFactory.create()
        tasksUseCaseFactory.create()
        fetchActiveLeaveRequests()
    }
}