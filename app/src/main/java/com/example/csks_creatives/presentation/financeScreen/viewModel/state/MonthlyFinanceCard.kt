package com.example.csks_creatives.presentation.financeScreen.viewModel.state

import com.example.csks_creatives.domain.model.utills.enums.finance.FinanceMonths

data class MonthlyFinanceCard(
    val month: FinanceMonths = FinanceMonths.JANUARY,
    val isMonthlyCardVisible: Boolean = false,
    val totalCost: Int = 0,
    val totalCostPaid: Int = 0,
    val totalCostPartiallyPaid: Int = 0,
    val totalCostUnPaid: Int = 0,
)
