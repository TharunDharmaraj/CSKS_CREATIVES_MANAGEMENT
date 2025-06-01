package com.example.csks_creatives.presentation.financeScreen.viewModel.state

data class FinanceScreenState(
    val isFinanceScreenLoading: Boolean = false,
    val isLifeTimeFinanceCardVisible: Boolean = true,
    val lifeTimeCard: LifeTimeFinanceCard = LifeTimeFinanceCard(),
    val yearlyCardsList: List<YearlyFinanceCard> = emptyList(),
)