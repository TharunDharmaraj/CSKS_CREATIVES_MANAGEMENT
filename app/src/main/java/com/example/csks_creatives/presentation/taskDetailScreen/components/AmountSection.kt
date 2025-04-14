package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.domain.utils.Utils.getFormattedDateTimeFormat
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDetailEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.DropDownListState
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.TaskDetailState

@Composable
fun AmountSection(
    taskState: TaskDetailState,
    dropDownListState: DropDownListState,
    onEvent: (TaskDetailEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = taskState.taskCost.toString(),
            onValueChange = { value ->
                onEvent(TaskDetailEvent.TaskCostChanged(value.toIntOrNull() ?: 0))
            },
            label = { Text("Task Cost") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(12.dp))

        DropdownMenuWithSelection(
            label = "Paid Status",
            selectedItem = dropDownListState.taskPaidStatusList
                .find { it == taskState.taskPaidStatus }
                ?.name ?: "Select",
            items = dropDownListState.taskPaidStatusList.map { it.name },
            onItemSelected = { selected ->
                dropDownListState.taskPaidStatusList.find { it.name == selected }
                    ?.let { paidStatus ->
                        onEvent(TaskDetailEvent.TaskPaidStatusChanged(paidStatus))
                    }
            },
            enabled = true
        )

        if (taskState.taskPaidStatus == TaskPaidStatus.PARTIALLY_PAID) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Partial Payment Details", style = MaterialTheme.typography.titleMedium)
        }
        taskState.taskPaymentsHistory.forEach { info ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Amount Paid: ₹${info.amount}")
                    Text("Date: ${getFormattedDateTimeFormat(info.paymentDate)}")
                }
            }
        }

        val totalPaid = taskState.taskPaymentsHistory.sumOf { it.amount }
        val remaining = taskState.taskCost - totalPaid
        if (taskState.taskPaidStatus == TaskPaidStatus.PARTIALLY_PAID) {

            Spacer(modifier = Modifier.height(8.dp))
            Text("Remaining Amount: ₹$remaining", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = taskState.taskPartialPaymentsAmount.toString(),
                onValueChange = { onEvent(TaskDetailEvent.TaskPartialPaymentAmountChanged(it.toInt())) },
                label = { Text("Enter Partial Amount") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onEvent(TaskDetailEvent.AddTaskPartialPayment)
                }
            ) {
                Text("Submit Payment")
            }
        }
    }
}