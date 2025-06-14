package com.example.csks_creatives.presentation.clientTasksListScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.factories.TasksUseCaseFactory
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event.ClientTasksListScreenEvent
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event.EditClientNameDialogEvent
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.state.ClientNameDialogState
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.state.ClientTasksListState
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ClientTasksListViewModel @Inject constructor(
    private val tasksUseCaseFactory: TasksUseCaseFactory
) : ViewModel() {
    init {
        tasksUseCaseFactory.create()
    }

    private val _allTasksForClientFromFireStore = MutableStateFlow<List<ClientTask>>(emptyList())

    private var hasInitialized = false

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent: SharedFlow<ToastUiEvent> = _uiEvent.asSharedFlow()

    private val _clientName = MutableStateFlow("")
    var clientName = _clientName.asStateFlow()

    private val _editClientNameDialogState = MutableStateFlow(ClientNameDialogState())
    var editClientNameDialogState = _editClientNameDialogState.asStateFlow()

    private val _clientTasksListState = MutableStateFlow(ClientTasksListState())
    val clientsTasksListState = _clientTasksListState.asStateFlow()

    fun onEvent(clientTaskListScreenEvent: ClientTasksListScreenEvent) {
        when (clientTaskListScreenEvent) {
            is ClientTasksListScreenEvent.OnSearchTextChanged -> {
                _clientTasksListState.value = _clientTasksListState.value.copy(
                    searchText = clientTaskListScreenEvent.searchText
                )
                filterTasks()
            }

            is ClientTasksListScreenEvent.Order -> {
                val newOrder = clientTaskListScreenEvent.order
                // Thread Safer Way. TODO Update everything  that uses _state.value = _state.copy() into a thread safer way
                _clientTasksListState.update {
                    it.copy(tasksOrder = newOrder)
                }
                sortTasks(newOrder)
            }

            is ClientTasksListScreenEvent.ToggleStatusFilter -> {
                val currentSet = _clientTasksListState.value.selectedStatuses.toMutableSet()
                if (currentSet.contains(clientTaskListScreenEvent.status)) {
                    currentSet.remove(clientTaskListScreenEvent.status)
                } else {
                    currentSet.add(clientTaskListScreenEvent.status)
                }
                _clientTasksListState.value =
                    _clientTasksListState.value.copy(selectedStatuses = currentSet)
                filterTasks()
            }

            ClientTasksListScreenEvent.ToggleSearchBarClicked -> {
                _clientTasksListState.update {
                    it.copy(
                        isSearchBarVisible = !_clientTasksListState.value.isSearchBarVisible
                    )
                }
            }

            is ClientTasksListScreenEvent.ShowOnlyUnPaidTasksFilter -> {
                _clientTasksListState.update {
                    it.copy(
                        isUnpaidTasksVisible = !it.isUnpaidTasksVisible,
                        isPartiallyPaidTasksVisible = false,
                        isPaidTasksVisible = false,
                        isAllTasksVisible = false
                    )
                }
                filterTasks()
            }

            ClientTasksListScreenEvent.ShowOnlyPaidTasksFilter -> {
                _clientTasksListState.update {
                    it.copy(
                        isPaidTasksVisible = !it.isPaidTasksVisible,
                        isPartiallyPaidTasksVisible = false,
                        isUnpaidTasksVisible = false,
                        isAllTasksVisible = false
                    )
                }
                filterTasks()
            }

            ClientTasksListScreenEvent.ShowOnlyPartiallyPaidTasksFilter -> {
                _clientTasksListState.update {
                    it.copy(
                        isPartiallyPaidTasksVisible = !it.isPartiallyPaidTasksVisible,
                        isPaidTasksVisible = false,
                        isUnpaidTasksVisible = false,
                        isAllTasksVisible = false
                    )
                }
                filterTasks()
            }

            ClientTasksListScreenEvent.ToggleFilterTasksClicked -> {
                _clientTasksListState.update {
                    it.copy(
                        isSearchBarVisible = false,
                        canShowSearchIcon = !_clientTasksListState.value.canShowSearchIcon,
                        isFilterSectionVisible = !_clientTasksListState.value.isFilterSectionVisible
                    )
                }
            }
        }
    }

    fun onEditDialogEvent(editDialogEvent: EditClientNameDialogEvent) {
        when (editDialogEvent) {
            EditClientNameDialogEvent.CancelClicked -> {
                _clientTasksListState.update { it.copy(isEditClientNameDialogVisible = false) }
            }

            is EditClientNameDialogEvent.OnClientNameTextEdit -> {
                _editClientNameDialogState.update {
                    it.copy(
                        clientName = editDialogEvent.clientName
                    )
                }
            }

            EditClientNameDialogEvent.SaveClicked -> {
                editClientName(
                    clientId = _clientTasksListState.value.clientId,
                    clientName = _editClientNameDialogState.value.clientName
                )
            }
        }
    }

    private fun getClientTasks(order: DateOrder, clientId: String) {
        viewModelScope.launch {
            _clientTasksListState.update { it.copy(isLoading = true) }
            tasksUseCaseFactory.getTasksForClient(order, clientId).collect { result ->
                if (result is ResultState.Success) {
                    val clientTasksList = result.data
                    _allTasksForClientFromFireStore.value = clientTasksList
                    _clientTasksListState.value = _clientTasksListState.value.copy(
                        tasksList = clientTasksList,
                        isAllTasksVisible = true,
                        isUnpaidTasksVisible = false,
                        isLoading = false
                    )
                    if (clientTasksList.isNotEmpty()) {
                        _clientTasksListState.value = _clientTasksListState.value.copy(
                            isFilterTasksIconVisible = true
                        )
                        filterTasks()
                    } else {
                        _clientTasksListState.value = _clientTasksListState.value.copy(
                            isFilterTasksIconVisible = false
                        )
                    }
                }
            }
        }
    }

    private fun filterTasks() {
        val searchText = _clientTasksListState.value.searchText.lowercase()
        val selectedStatuses = _clientTasksListState.value.selectedStatuses

        var filteredList = _allTasksForClientFromFireStore.value

        if (searchText.isNotBlank()) {
            filteredList = filteredList.filter { task ->
                task.taskName.lowercase().contains(searchText) ||
                        task.currentStatus.name.lowercase().contains(searchText) ||
                        task.taskAttachment.lowercase().contains(searchText)
            }
        }

        if (selectedStatuses.isNotEmpty()) {
            filteredList = filteredList.filter { task -> task.currentStatus in selectedStatuses }
        }

        when {
            _clientTasksListState.value.isUnpaidTasksVisible -> {
                filteredList = filteredList.filter { it.taskPaidStatus == TaskPaidStatus.NOT_PAID }
            }

            _clientTasksListState.value.isPaidTasksVisible -> {
                filteredList =
                    filteredList.filter { it.taskPaidStatus == TaskPaidStatus.FULLY_PAID }
            }

            _clientTasksListState.value.isPartiallyPaidTasksVisible -> {
                filteredList =
                    filteredList.filter { it.taskPaidStatus == TaskPaidStatus.PARTIALLY_PAID }
            }
        }

        _clientTasksListState.value = _clientTasksListState.value.copy(tasksList = filteredList)
    }

    private fun sortTasks(order: DateOrder) {
        val sortedList = if (order is DateOrder.Ascending) {
            _clientTasksListState.value.tasksList.sortedBy { it.taskCreationTime }
        } else {
            _clientTasksListState.value.tasksList.sortedByDescending { it.taskCreationTime }
        }

        _clientTasksListState.value = _clientTasksListState.value.copy(tasksList = sortedList)
    }

    fun initialize(clientId: String) {
        if (hasInitialized) return
        hasInitialized = true
        _clientTasksListState.value = _clientTasksListState.value.copy(clientId)
        getClientName(clientId)
        getClientTasks(DateOrder.Descending, clientId)
    }

    private fun getClientName(clientId: String) {
        viewModelScope.launch {
            _clientName.value = tasksUseCaseFactory.getClientName(clientId)
        }
    }

    fun emitLogoutEvent(isUserLoggedOut: Boolean) {
        viewModelScope.launch {
            LogoutEvent.emitLogoutEvent(isUserLoggedOut)
        }
    }

    fun getTotalUnPaidCostForClient(): Triple<Int, Int, Int> {
        var totalCostPaid = 0
        var totalCostUnPaid = 0
        var totalPartialPaid = 0
        _allTasksForClientFromFireStore.value.forEach { task ->
            when (task.taskPaidStatus) {
                TaskPaidStatus.NOT_PAID -> {
                    totalCostUnPaid += task.taskCost
                }

                TaskPaidStatus.PARTIALLY_PAID -> {
                    var totalPartialAmountForTask = 0
                    task.paymentHistory.forEach { taskPaymentHistory ->
                        totalPartialAmountForTask += taskPaymentHistory.amount
                    }
                    totalPartialPaid += totalPartialAmountForTask
                    totalCostUnPaid += task.taskCost - totalPartialAmountForTask
                }

                else -> {
                    totalCostPaid += task.taskCost
                }
            }
        }
        return Triple(totalCostPaid, totalCostUnPaid, totalPartialPaid)
    }

    fun getYearlyAndMonthlyCostBreakdown(): Map<Int, Map<Int, Triple<Int, Int, Int>>> {
        val costBreakdown = mutableMapOf<Int, MutableMap<Int, Triple<Int, Int, Int>>>()

        _allTasksForClientFromFireStore.value.forEach { task ->
            // We are using taskFullyPaidDate for fully paid tasks, otherwise taskCreationTime
            val relevantTimestamp = when (task.taskPaidStatus) {
                TaskPaidStatus.FULLY_PAID -> task.taskFullyPaidDate.toLongOrNull()
                else -> task.taskCreationTime.toLongOrNull()
            }
            val taskCost = task.taskCost

            if (relevantTimestamp == null) return@forEach

            val calendar = Calendar.getInstance().apply { timeInMillis = relevantTimestamp }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1

            val currentYearData = costBreakdown.getOrPut(year) { mutableMapOf() }
            val currentMonthData = currentYearData.getOrPut(month) { Triple(0, 0, 0) }

            val updatedMonthData = when (task.taskPaidStatus) {
                TaskPaidStatus.FULLY_PAID -> {
                    currentMonthData.copy(first = currentMonthData.first + taskCost)
                }

                TaskPaidStatus.NOT_PAID -> {
                    currentMonthData.copy(second = currentMonthData.second + taskCost)
                }

                TaskPaidStatus.PARTIALLY_PAID -> {
                    val totalPartialAmountForTask = task.paymentHistory.sumOf { it.amount }
                    val unPaidCost = taskCost - totalPartialAmountForTask
                    currentMonthData.copy(
                        second = currentMonthData.second + unPaidCost,
                        third = currentMonthData.third + totalPartialAmountForTask
                    )
                }
            }
            currentYearData[month] = updatedMonthData
        }
        return costBreakdown
    }

    fun setFilterAndSearchIconVisibility(isVisible: Boolean) {
        _clientTasksListState.update {
            it.copy(
                isFilterTasksIconVisible = isVisible,
                isSearchBarVisible = isVisible
            )
        }
    }

    fun makeEmployeeEditDialogVisible() {
        _clientTasksListState.update { it.copy(isEditClientNameDialogVisible = true) }
        _editClientNameDialogState.update {
            it.copy(clientName = _clientName.value)
        }
    }

    fun editClientName(clientName: String, clientId: String) {
        viewModelScope.launch {
            val result = tasksUseCaseFactory.editClientName(clientId, clientName)
            when (result) {
                is ResultState.Error -> {
                    _uiEvent.emit(ToastUiEvent.ShowToast(result.message))
                }

                ResultState.Loading -> {

                }

                is ResultState.Success<String> -> {
                    _uiEvent.emit(ToastUiEvent.ShowToast(result.data))
                    _editClientNameDialogState.update {
                        it.copy(
                            clientName = ""
                        )
                    }
                    _clientName.value = clientName
                }
            }
            _clientTasksListState.update { it.copy(isEditClientNameDialogVisible = false) }
        }
    }
}