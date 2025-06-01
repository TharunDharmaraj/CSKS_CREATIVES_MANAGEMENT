package com.example.csks_creatives.domain.model.finance

import com.example.csks_creatives.domain.model.utills.enums.finance.FinanceMonths

data class FinanceMetric(
    val totalCost: Int = 0,
    val totalFullyPaidCost: Int = 0,
    val totalPartiallyPaidCost: Int = 0,
    val totalUnpaidCost: Int = 0,
    val year: Int = 2025,
    val month: Int = FinanceMonths.JANUARY.monthId
)