package com.example.csks_creatives.presentation.financeScreen.viewModel.state

data class LifeTimeFinanceCard(
    val totalCost: Int = 0,
    val totalCostPaid: Int = 0,
    val totalCostPartiallyPaid: Int = 0,
    val totalCostUnPaid: Int = 0,
)