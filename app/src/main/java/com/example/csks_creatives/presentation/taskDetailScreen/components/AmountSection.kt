package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.presentation.components.charCoalPurple
import com.example.csks_creatives.presentation.components.goldenRod
import com.example.csks_creatives.presentation.components.limeGreen
import com.example.csks_creatives.presentation.components.silverGrey
import com.example.csks_creatives.presentation.components.ui.ModernDateView
import com.example.csks_creatives.presentation.components.vividCerulean
import com.example.csks_creatives.presentation.components.white
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Financial Summary",
                    style = MaterialTheme.typography.titleMedium,
                    color = vividCerulean,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = taskState.taskCost.toString().ifEmpty { "0" },
                    onValueChange = { value ->
                        onEvent(TaskDetailEvent.TaskCostChanged(value.toIntOrNull() ?: 0))
                    },
                    label = { Text("Total Task Cost") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = white,
                        unfocusedTextColor = white,
                        focusedBorderColor = vividCerulean,
                        unfocusedBorderColor = silverGrey.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            item {
                GenericDropdownMenu(
                    label = "Payment Status",
                    selectedItem = dropDownListState.taskPaidStatusList
                        .find { it == taskState.taskPaidStatus }
                        ?.name ?: "Select",
                    items = dropDownListState.taskPaidStatusList.map { it.name },
                    onItemSelected = { selected ->
                        dropDownListState.taskPaidStatusList.find { it.name == selected }
                            ?.let { paidStatus ->
                                onEvent(TaskDetailEvent.TaskPaidStatusChanged(paidStatus))
                            }
                    }
                )
            }

            if (taskState.taskPaidStatus == TaskPaidStatus.PARTIALLY_PAID) {
                item {
                    val totalPaid = taskState.taskPaymentsHistory.sumOf { it.amount }
                    val remaining = taskState.taskCost - totalPaid

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = charCoalPurple.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, vividCerulean.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Remaining to Pay", style = MaterialTheme.typography.labelSmall, color = silverGrey)
                                Text("₹$remaining", style = MaterialTheme.typography.headlineSmall, color = goldenRod, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = silverGrey)
                                Text("₹$totalPaid", style = MaterialTheme.typography.titleMedium, color = limeGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                item {
                    Text("Payment History", style = MaterialTheme.typography.titleSmall, color = white)
                }

                items(taskState.taskPaymentsHistory.size) { index ->
                    val info = taskState.taskPaymentsHistory[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = charCoalPurple),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(limeGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = limeGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Amount: ₹${info.amount}", style = MaterialTheme.typography.bodyLarge, color = white, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                ModernDateView(info.paymentDate)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = taskState.taskPartialPaymentsAmount.toString(),
                        onValueChange = { onEvent(TaskDetailEvent.TaskPartialPaymentAmountChanged(it)) },
                        label = { Text("New Payment Amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = white,
                            unfocusedTextColor = white,
                            focusedBorderColor = limeGreen,
                            unfocusedBorderColor = silverGrey.copy(alpha = 0.3f)
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = { onEvent(TaskDetailEvent.AddTaskPartialPayment) }
                        ),
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        onClick = { onEvent(TaskDetailEvent.AddTaskPartialPayment) },
                        colors = ButtonDefaults.buttonColors(containerColor = limeGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Record Payment", color = white, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
