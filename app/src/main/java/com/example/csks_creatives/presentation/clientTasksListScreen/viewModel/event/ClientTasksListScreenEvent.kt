package com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event

import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.presentation.components.DateOrder

sealed class ClientTasksListScreenEvent {
    object OnClientTaskClicked : ClientTasksListScreenEvent()
    object ToggleSearchBarClicked : ClientTasksListScreenEvent()
    data class OnSearchTextChanged(val searchText: String) : ClientTasksListScreenEvent()
    data class Order(val order: DateOrder) : ClientTasksListScreenEvent()
    data class ToggleStatusFilter(val status: TaskStatusType) : ClientTasksListScreenEvent()
}