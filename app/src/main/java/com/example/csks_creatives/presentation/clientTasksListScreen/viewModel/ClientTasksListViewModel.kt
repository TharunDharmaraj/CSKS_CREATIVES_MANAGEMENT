package com.example.csks_creatives.presentation.clientTasksListScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.TasksUseCaseFactory
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event.ClientTasksListScreenEvent
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.state.ClientTasksListState
import com.example.csks_creatives.presentation.components.DateOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientTasksListViewModel @Inject constructor(
    private val tasksUseCaseFactory: TasksUseCaseFactory
) : ViewModel() {
    init {
        tasksUseCaseFactory.create()
    }

    private val _allTasksForClientFromFireStore = MutableStateFlow<List<ClientTask>>(emptyList())

    private val _clientTasksListState = MutableStateFlow(ClientTasksListState())
    val clientsTasksListState = _clientTasksListState.asStateFlow()

    private var hasInitialized = false

    fun onEvent(clientTaskListScreenEvent: ClientTasksListScreenEvent) {
        when (clientTaskListScreenEvent) {
            ClientTasksListScreenEvent.OnClientTaskClicked -> {
                // TODO Navigate
            }

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
        }
    }

    private fun getClientTasks(order: DateOrder, clientId: String) {
        viewModelScope.launch {
            tasksUseCaseFactory.getTasksForClient(order, clientId).collect { result ->
                if (result is ResultState.Success) {
                    val clientTasksList = result.data
                    _allTasksForClientFromFireStore.value = clientTasksList
                    _clientTasksListState.value = _clientTasksListState.value.copy(
                        tasksList = clientTasksList
                    )
                    filterTasks()
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

        getClientTasks(DateOrder.Descending, clientId)
    }

    fun emitLogoutEvent(isUserLoggedOut: Boolean){
        viewModelScope.launch {
            LogoutEvent.emitLogoutEvent(isUserLoggedOut)
        }
    }
}