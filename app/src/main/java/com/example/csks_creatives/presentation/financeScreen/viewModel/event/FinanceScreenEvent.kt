package com.example.csks_creatives.presentation.financeScreen.viewModel.event

import com.example.csks_creatives.domain.model.utills.enums.finance.FinanceMonths

sealed class FinanceScreenEvent(
    open val year: Int = 0,
    open val month: FinanceMonths = FinanceMonths.JANUARY
) {
    object ToggleLifeTimeFinanceSection : FinanceScreenEvent()
    data class ToggleYearlyFinanceSection(override val year: Int) : FinanceScreenEvent()
    data class ToggleMonthlyFinanceSection(override val month: FinanceMonths) : FinanceScreenEvent()
}