package com.example.csks_creatives.presentation.homeScreen.viewModel.admin

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveApprovalStatus
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.factories.*
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event.*
import com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminHomeScreenViewModel @Inject constructor(
    private val adminUseCaseFactory: AdminUseCaseFactory,
    private val clientsUseCaseFactory: ClientsUseCaseFactory,
    private val tasksUseCaseFactory: TasksUseCaseFactory
) : ViewModel() {
    init {
        adminUseCaseFactory.create()
        clientsUseCaseFactory.create()
        tasksUseCaseFactory.create()
        fetchActiveLeaveRequests()
    }

    private val _homeScreenTitleState = MutableStateFlow("Welcome, Admin")
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
                _adminHomeScreenVisibilityState.value = _adminHomeScreenVisibilityState.value.copy(
                    isClientSectionVisible = !_adminHomeScreenVisibilityState.value.isClientSectionVisible
                )
                if (_adminHomeScreenVisibilityState.value.isClientSectionVisible and isClientsFetched.not()) {
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isClientsLoading = !_adminHomeScreenLoadingState.value.isClientsLoading
                    )
                    fetchClients()
                }
            }

            AdminHomeScreenEvent.ToggleEmployeeSection -> {
                _adminHomeScreenVisibilityState.value = _adminHomeScreenVisibilityState.value.copy(
                    isEmployeeSectionVisible = !_adminHomeScreenVisibilityState.value.isEmployeeSectionVisible
                )
                if (_adminHomeScreenVisibilityState.value.isEmployeeSectionVisible and isEmployeesFetched.not()) {
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isEmployeesLoading = !_adminHomeScreenLoadingState.value.isEmployeesLoading
                    )
                    fetchEmployees()
                }
            }

            AdminHomeScreenEvent.ToggleActiveTaskSection -> {
                _adminHomeScreenVisibilityState.value = _adminHomeScreenVisibilityState.value.copy(
                    isActiveTaskSectionVisible = !_adminHomeScreenVisibilityState.value.isActiveTaskSectionVisible
                )
                if (_adminHomeScreenVisibilityState.value.isActiveTaskSectionVisible and isActiveTasksFetched.not()) {
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isActiveTasksLoading = !_adminHomeScreenLoadingState.value.isActiveTasksLoading
                    )
                    fetchActiveTasks()
                }
            }

            AdminHomeScreenEvent.ToggleBacklogTaskSection -> {
                _adminHomeScreenVisibilityState.value = _adminHomeScreenVisibilityState.value.copy(
                    isBacklogTaskSectionVisible = !_adminHomeScreenVisibilityState.value.isBacklogTaskSectionVisible
                )
                if (_adminHomeScreenVisibilityState.value.isBacklogTaskSectionVisible and isBacklogTasksFetched.not()) {
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isBacklogTasksLoading = !_adminHomeScreenLoadingState.value.isBacklogTasksLoading
                    )
                    fetchBacklogTasks()
                }
            }

            AdminHomeScreenEvent.ToggleCompletedTaskSection -> {
                _adminHomeScreenVisibilityState.value = _adminHomeScreenVisibilityState.value.copy(
                    isCompletedTaskSectionVisible = !_adminHomeScreenVisibilityState.value.isCompletedTaskSectionVisible
                )
                if (_adminHomeScreenVisibilityState.value.isCompletedTaskSectionVisible and isCompletedTasksFetched.not()) {
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isCompletedTasksLoading = !_adminHomeScreenLoadingState.value.isCompletedTasksLoading
                    )
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
                            _adminHomeScreenVisibilityState.value.copy(isAddEmployeeDialogVisible = false)
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

    private fun fetchEmployees() {
        viewModelScope.launch {
            _adminHomeScreenLoadingState.update {
                it.copy(
                    isEmployeesLoading = true
                )
            }
            val result = adminUseCaseFactory.getEmployeesList(isForceFetchFromServer = true)
            if (result is ResultState.Success) {
                _adminHomeScreenState.value =
                    _adminHomeScreenState.value.copy(employeeList = result.data)
                _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                    isEmployeesLoading = false
                )
                isEmployeesFetched = true
            }
        }
    }

    private fun fetchClients() {
        viewModelScope.launch {
            _adminHomeScreenLoadingState.update {
                it.copy(
                    isClientsLoading = true
                )
            }
            val result = clientsUseCaseFactory.getClients(isForceFetchFromServer = true)
            when (result) {
                is ResultState.Error -> TODO()
                ResultState.Loading -> {
                    _adminHomeScreenLoadingState
                }
                is ResultState.Success<List<Client>> -> {
                    _adminHomeScreenState.value =
                        _adminHomeScreenState.value.copy(clientList = result.data)
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isClientsLoading = false
                    )
                    isClientsFetched = true
                }
            }
        }
    }

    private fun fetchActiveTasks() {
        viewModelScope.launch {
            _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                isActiveTasksLoading = true
            )
            tasksUseCaseFactory.getAllActiveTasks().collect { result ->
                if (result is ResultState.Success) {
                    _adminHomeScreenState.value =
                        _adminHomeScreenState.value.copy(activeTaskList = result.data)
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isActiveTasksLoading = false
                    )
                    isActiveTasksFetched = true
                }
            }
        }
    }

    private fun fetchActiveLeaveRequests() {
        viewModelScope.launch {
            adminUseCaseFactory.getAllActiveLeaveRequests().collect { result ->
                if (result is ResultState.Success) {
                    hasUnapprovedLeaves.value =
                        result.data.any { it.approvedStatus == LeaveApprovalStatus.UN_APPROVED }
                    _adminHomeScreenState.update {
                        it.copy(
                            activeLeaveRequests = result.data
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

    private fun fetchCompletedTasks() {
        viewModelScope.launch {
            _adminHomeScreenLoadingState.update {
                it.copy(
                    isCompletedTasksLoading = true
                )
            }
            tasksUseCaseFactory.getAllCompletedTasks().collect { result ->
                if (result is ResultState.Success) {
                    _adminHomeScreenState.value =
                        _adminHomeScreenState.value.copy(completedTasksList = result.data)
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isCompletedTasksLoading = false
                    )
                    isCompletedTasksFetched = true
                }
            }
        }
    }

    private fun fetchBacklogTasks() {
        viewModelScope.launch {
            _adminHomeScreenLoadingState.update {
                it.copy(
                    isBacklogTasksLoading = true
                )
            }
            tasksUseCaseFactory.getAllBacklogTasks().collect { result ->
                if (result is ResultState.Success) {
                    _adminHomeScreenState.value =
                        _adminHomeScreenState.value.copy(backlogTaskList = result.data)
                    _adminHomeScreenLoadingState.value = _adminHomeScreenLoadingState.value.copy(
                        isBacklogTasksLoading = false
                    )
                    isBacklogTasksFetched = true
                }
            }
        }
    }

    fun emitLogoutEvent(isUserLoggedOut: Boolean) {
        viewModelScope.launch {
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
}