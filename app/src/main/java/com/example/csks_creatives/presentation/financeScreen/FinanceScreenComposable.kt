package com.example.csks_creatives.presentation.financeScreen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.financeScreen.viewModel.FinanceScreenViewModel
import com.example.csks_creatives.presentation.financeScreen.viewModel.event.FinanceScreenEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FinanceScreenComposable(
    viewModel: FinanceScreenViewModel = hiltViewModel()
) {
    val state by viewModel.financeScreenState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ToastUiEvent.ShowToast -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    if (state.isFinanceScreenLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingProgress()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            /** LIFETIME CARD **/
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onEvent(FinanceScreenEvent.ToggleLifeTimeFinanceSection) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lifetime Finance Summary",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Icon(
                        imageVector = if (state.isLifeTimeFinanceCardVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Lifetime"
                    )
                }

                AnimatedVisibility(visible = state.isLifeTimeFinanceCardVisible) {
                    FinanceCard(
                        total = state.lifeTimeCard.totalCost,
                        paid = state.lifeTimeCard.totalCostPaid,
                        partiallyPaid = state.lifeTimeCard.totalCostPartiallyPaid,
                        unpaid = state.lifeTimeCard.totalCostUnPaid
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            /** YEARLY CARDS **/
            items(state.yearlyCardsList.size) { index ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onEvent(
                                    FinanceScreenEvent.ToggleYearlyFinanceSection(
                                        state.yearlyCardsList[index].year
                                    )
                                )
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Year: ${state.yearlyCardsList[index].year}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = if (state.yearlyCardsList[index].isYearCardVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Year"
                        )
                    }

                    AnimatedVisibility(visible = state.yearlyCardsList[index].isYearCardVisible) {
                        Column {
                            FinanceCard(
                                total = state.yearlyCardsList[index].totalCost,
                                paid = state.yearlyCardsList[index].totalCostPaid,
                                partiallyPaid = state.yearlyCardsList[index].totalCostPartiallyPaid,
                                unpaid = state.yearlyCardsList[index].totalCostUnPaid
                            )

                            /** MONTHLY CARDS **/
                            state.yearlyCardsList[index].monthlyFinanceCardList.forEach { monthCard ->
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onEvent(
                                                FinanceScreenEvent.ToggleMonthlyFinanceSection(
                                                    monthCard.month
                                                )
                                            )
                                        }
                                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Month: ${monthCard.month.name}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Icon(
                                        imageVector = if (monthCard.isMonthlyCardVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Month"
                                    )
                                }


                                AnimatedVisibility(visible = monthCard.isMonthlyCardVisible) {
                                    FinanceCard(
                                        total = monthCard.totalCost,
                                        paid = monthCard.totalCostPaid,
                                        partiallyPaid = monthCard.totalCostPartiallyPaid,
                                        unpaid = monthCard.totalCostUnPaid,
                                        modifier = Modifier.padding(start = 32.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun FinanceCard(
    total: Int,
    paid: Int,
    partiallyPaid: Int,
    unpaid: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tasks Total Cost: ₹$total")
            Text("Received Cost: ₹${paid + partiallyPaid}")
            Spacer(Modifier.height(8.dp))

            FinancePieChart(
                paid = paid,
                unpaid = unpaid,
                partiallyPaid = partiallyPaid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FinancePieChart(
    paid: Int,
    unpaid: Int,
    partiallyPaid: Int,
    modifier: Modifier = Modifier
) {
    val total = paid + unpaid + partiallyPaid
    if (total == 0) return // Avoid drawing empty chart

    val paidAngle = (paid.toFloat() / total) * 360f
    val unpaidAngle = (unpaid.toFloat() / total) * 360f
    val partiallyPaidAngle = 360f - paidAngle - unpaidAngle

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Canvas(modifier = Modifier.size(180.dp)) {
            var startAngle = -90f

            drawArc(
                color = Color(0xFF2E7D32), // Green for Paid
                startAngle = startAngle,
                sweepAngle = paidAngle,
                useCenter = true
            )
            startAngle += paidAngle

            drawArc(
                color = Color(0xFFC62828), // Red for Unpaid
                startAngle = startAngle,
                sweepAngle = unpaidAngle,
                useCenter = true
            )
            startAngle += unpaidAngle

            drawArc(
                color = Color(0xFFFFA000), // Amber for Partially Paid
                startAngle = startAngle,
                sweepAngle = partiallyPaidAngle,
                useCenter = true
            )
        }

        Spacer(Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PaymentLegend(color = Color(0xFF2E7D32), label = "Fully Paid: ₹$paid")
            PaymentLegend(color = Color(0xFFC62828), label = "Unpaid Tasks Cost: ₹$unpaid")
            PaymentLegend(color = Color(0xFFFFA000), label = "Partially Paid: ₹$partiallyPaid")
        }
    }
}


@Composable
fun PaymentLegend(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
