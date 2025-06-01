package com.example.csks_creatives.domain.useCase.factories

import com.example.csks_creatives.domain.model.finance.FinanceMetric
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.FinanceUseCase
import kotlinx.coroutines.flow.Flow

interface FinanceUseCaseFactory {
    fun create(): FinanceUseCase

    suspend fun getFinancesForAdmin(): Flow<ResultState<List<Map<FinanceMetric, List<FinanceMetric>>>>>
}