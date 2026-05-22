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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.csks_creatives.presentation.components.*
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
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lifetime Finance Summary",
                        style = MaterialTheme.typography.titleLarge,
                        color = white,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (state.isLifeTimeFinanceCardVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Lifetime",
                        tint = vividCerulean
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
                val yearlyData = state.yearlyCardsList[index]
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onEvent(
                                    FinanceScreenEvent.ToggleYearlyFinanceSection(
                                        yearlyData.year
                                    )
                                )
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Year: ${yearlyData.year}",
                            style = MaterialTheme.typography.titleMedium,
                            color = vividCerulean,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (yearlyData.isYearCardVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Year",
                            tint = vividCerulean
                        )
                    }

                    AnimatedVisibility(visible = yearlyData.isYearCardVisible) {
                        Column {
                            FinanceCard(
                                total = yearlyData.totalCost,
                                paid = yearlyData.totalCostPaid,
                                partiallyPaid = yearlyData.totalCostPartiallyPaid,
                                unpaid = yearlyData.totalCostUnPaid
                            )

                            /** MONTHLY CARDS **/
                            yearlyData.monthlyFinanceCardList.forEach { monthCard ->
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
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = silverGrey,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = if (monthCard.isMonthlyCardVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Month",
                                        tint = silverGrey
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
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = charCoalPurple
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, white.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Total Value", style = MaterialTheme.typography.labelSmall, color = silverGrey)
                    Text(text = "₹$total", style = MaterialTheme.typography.titleLarge, color = white, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Revenue", style = MaterialTheme.typography.labelSmall, color = silverGrey)
                    Text(text = "₹${paid + partiallyPaid}", style = MaterialTheme.typography.titleLarge, color = limeGreen, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = white.copy(alpha = 0.05f))

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
                color = limeGreen,
                startAngle = startAngle,
                sweepAngle = paidAngle,
                useCenter = true
            )
            startAngle += paidAngle

            drawArc(
                color = red,
                startAngle = startAngle,
                sweepAngle = unpaidAngle,
                useCenter = true
            )
            startAngle += unpaidAngle

            drawArc(
                color = goldenRod,
                startAngle = startAngle,
                sweepAngle = partiallyPaidAngle,
                useCenter = true
            )
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentLegend(color = limeGreen, label = "Fully Paid", amount = paid)
            PaymentLegend(color = goldenRod, label = "Partially Paid", amount = partiallyPaid)
            PaymentLegend(color = red, label = "Unpaid Balance", amount = unpaid)
        }
    }
}


@Composable
fun PaymentLegend(color: Color, label: String, amount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = silverGrey)
        }
        Text(text = "₹$amount", style = MaterialTheme.typography.bodyLarge, color = white, fontWeight = FontWeight.SemiBold)
    }
}
