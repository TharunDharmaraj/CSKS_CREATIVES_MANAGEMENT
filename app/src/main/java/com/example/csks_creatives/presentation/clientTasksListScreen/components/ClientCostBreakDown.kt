package com.example.csks_creatives.presentation.clientTasksListScreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csks_creatives.domain.utils.Utils.getMonthName

@Composable
fun ClientCostBreakDown(
    getYearlyAndMonthlyCostBreakdown: () -> Map<Int, Map<Int, Triple<Int, Int, Int>>>,
    getTotalUnPaidCostForClient: () -> Triple<Int, Int, Int>
) {
    val yearlyBreakdown = getYearlyAndMonthlyCostBreakdown()
    val totalCost = getTotalUnPaidCostForClient()

    val yearExpandedStates = rememberSaveable(
        saver = mapSaver(
            save = { map -> map.mapKeys { it.key.toString() } },
            restore = { map ->
                mutableStateMapOf(
                    *map.entries.map { it.key.toInt() to (it.value as Boolean) }.toTypedArray()
                )
            }
        )
    ) { mutableStateMapOf<Int, Boolean>() }

    val monthExpandedStates = rememberSaveable(
        saver = mapSaver(
            save = { map -> map.mapKeys { "${it.key.first}-${it.key.second}" } },
            restore = { map ->
                mutableStateMapOf(
                    *map.entries.map {
                        val (year, month) = it.key.split("-").map(String::toInt)
                        (year to month) to (it.value as Boolean)
                    }.toTypedArray()
                )
            }
        )
    ) { mutableStateMapOf<Pair<Int, Int>, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        item {
            ToggleablePaymentSummaryCard(
                paid = totalCost.first,
                unpaid = totalCost.second,
                partiallyPaid = totalCost.third
            )
        }

        yearlyBreakdown.forEach { (year, monthlyData) ->
            item {
                val isYearExpanded = yearExpandedStates[year] == true

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            yearExpandedStates[year] = !isYearExpanded
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(
                            0xFF2E2E2E
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Year: $year",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Cyan
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(
                                imageVector = if (isYearExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Toggle Year"
                            )
                        }

                        AnimatedVisibility(visible = isYearExpanded) {
                            Column {
                                monthlyData.forEach { (month, cost) ->
                                    val monthKey = year to month
                                    val isMonthExpanded =
                                        monthExpandedStates[monthKey] == true

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                monthExpandedStates[monthKey] =
                                                    !isMonthExpanded
                                            },
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 2.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF1E1E1E)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    "Month: ${
                                                        getMonthName(
                                                            month
                                                        )
                                                    }",
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )
                                                Spacer(Modifier.weight(1f))
                                                Icon(
                                                    imageVector = if (isMonthExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Toggle Month"
                                                )
                                            }

                                            AnimatedVisibility(visible = isMonthExpanded) {
                                                Column(
                                                    modifier = Modifier.padding(
                                                        top = 8.dp
                                                    )
                                                ) {
                                                    Text(
                                                        "Paid: ${cost.first}",
                                                        color = Color.Green
                                                    )
                                                    Text(
                                                        "Unpaid: ${cost.second}",
                                                        color = Color.Red
                                                    )
                                                    Text(
                                                        "Partially Paid: ${cost.third}",
                                                        color = Color.Yellow
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleablePaymentSummaryCard(
    paid: Int,
    unpaid: Int,
    partiallyPaid: Int,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Client Payment Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))

                    val total = paid + unpaid + partiallyPaid
                    val paidAngle = if (total == 0) 0f else (paid.toFloat() / total) * 360f
                    val unpaidAngle = if (total == 0) 0f else (unpaid.toFloat() / total) * 360f
                    val partiallyPaidAngle = 360f - paidAngle - unpaidAngle

                    Canvas(modifier = Modifier.size(180.dp)) {
                        var startAngle = -90f

                        drawArc(
                            color = Color.Green,
                            startAngle = startAngle,
                            sweepAngle = paidAngle,
                            useCenter = true
                        )
                        startAngle += paidAngle

                        drawArc(
                            color = Color.Red,
                            startAngle = startAngle,
                            sweepAngle = unpaidAngle,
                            useCenter = true
                        )
                        startAngle += unpaidAngle

                        drawArc(
                            color = Color.Yellow,
                            startAngle = startAngle,
                            sweepAngle = partiallyPaidAngle,
                            useCenter = true
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PaymentLegend(color = Color.Green, label = "Paid: ₹$paid")
                        PaymentLegend(color = Color.Red, label = "Unpaid: ₹$unpaid")
                        PaymentLegend(
                            color = Color.Yellow,
                            label = "Partially Paid: ₹$partiallyPaid"
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PaymentLegend(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White)
    }
}