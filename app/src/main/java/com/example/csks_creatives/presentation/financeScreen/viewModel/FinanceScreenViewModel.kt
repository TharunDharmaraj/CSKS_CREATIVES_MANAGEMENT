package com.example.csks_creatives.presentation.financeScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.finance.FinanceMetric
import com.example.csks_creatives.domain.model.utills.enums.finance.FinanceMonths
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.factories.FinanceUseCaseFactory
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.financeScreen.viewModel.event.FinanceScreenEvent
import com.example.csks_creatives.presentation.financeScreen.viewModel.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanceScreenViewModel @Inject constructor(
    private val financeUseCaseFactory: FinanceUseCaseFactory
) : ViewModel() {
    init {
        financeUseCaseFactory.create()
        getFinancials()
    }

    private val _financeScreenState = MutableStateFlow(FinanceScreenState())
    val financeScreenState = _financeScreenState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent: SharedFlow<ToastUiEvent> = _uiEvent.asSharedFlow()

    fun onEvent(financeScreenEvent: FinanceScreenEvent) {
        when (financeScreenEvent) {
            FinanceScreenEvent.ToggleLifeTimeFinanceSection -> {
                _financeScreenState.update {
                    it.copy(
                        isLifeTimeFinanceCardVisible = !_financeScreenState.value.isLifeTimeFinanceCardVisible
                    )
                }
            }

            is FinanceScreenEvent.ToggleYearlyFinanceSection -> {
                _financeScreenState.update { state ->
                    val updatedList = state.yearlyCardsList.map { yearCard ->
                        if (yearCard.year == financeScreenEvent.year) {
                            yearCard.copy(isYearCardVisible = !yearCard.isYearCardVisible)
                        } else yearCard
                    }
                    state.copy(yearlyCardsList = updatedList)
                }
            }

            is FinanceScreenEvent.ToggleMonthlyFinanceSection -> {
                _financeScreenState.update { state ->
                    val updatedYears = state.yearlyCardsList.map { yearCard ->
                        yearCard.copy(
                            monthlyFinanceCardList = yearCard.monthlyFinanceCardList.map { monthCard ->
                                if (monthCard.month == financeScreenEvent.month) {
                                    monthCard.copy(isMonthlyCardVisible = !monthCard.isMonthlyCardVisible)
                                } else monthCard
                            }
                        )
                    }
                    state.copy(yearlyCardsList = updatedYears)
                }
            }
        }
    }

    private fun getFinancials() {
        viewModelScope.launch(Dispatchers.IO) {
            financeUseCaseFactory.getFinancesForAdmin().collect { result ->
                when (result) {
                    is ResultState.Error -> {
                        _financeScreenState.update {
                            it.copy(isFinanceScreenLoading = false)
                        }
                        _uiEvent.emit(ToastUiEvent.ShowToast("Error retrieving data ${result.message}"))
                    }

                    ResultState.Loading -> {
                        _financeScreenState.update {
                            it.copy(
                                isFinanceScreenLoading = true
                            )
                        }
                    }

                    is ResultState.Success<List<Map<FinanceMetric, List<FinanceMetric>>>> -> {
                        val data = result.data

                        val lifeTimeCard = LifeTimeFinanceCard(
                            totalCost = data.sumOf { it.keys.first().totalCost },
                            totalCostPaid = data.sumOf { it.keys.first().totalFullyPaidCost },
                            totalCostPartiallyPaid = data.sumOf { it.keys.first().totalPartiallyPaidCost },
                            totalCostUnPaid = data.sumOf { it.keys.first().totalUnpaidCost }
                        )

                        val yearlyCardsList = data.map { mapEntry ->
                            val yearMetric = mapEntry.keys.first()
                            val monthlyMetrics = mapEntry.values.first()

                            val monthlyCardList = monthlyMetrics.map { monthlyMetric ->
                                MonthlyFinanceCard(
                                    month = FinanceMonths.fromInt(monthlyMetric.month),
                                    totalCost = monthlyMetric.totalCost,
                                    totalCostPaid = monthlyMetric.totalFullyPaidCost,
                                    totalCostPartiallyPaid = monthlyMetric.totalPartiallyPaidCost,
                                    totalCostUnPaid = monthlyMetric.totalUnpaidCost
                                )
                            }

                            YearlyFinanceCard(
                                year = yearMetric.year,
                                totalCost = yearMetric.totalCost,
                                totalCostPaid = yearMetric.totalFullyPaidCost,
                                totalCostPartiallyPaid = yearMetric.totalPartiallyPaidCost,
                                totalCostUnPaid = yearMetric.totalUnpaidCost,
                                monthlyFinanceCardList = monthlyCardList
                            )
                        }

                        _financeScreenState.update {
                            it.copy(
                                isFinanceScreenLoading = false,
                                lifeTimeCard = lifeTimeCard,
                                yearlyCardsList = yearlyCardsList
                            )
                        }
                    }
                }
            }
        }
    }
}