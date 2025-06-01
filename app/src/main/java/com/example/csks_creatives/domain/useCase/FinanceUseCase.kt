package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.finance.FinanceMetric
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.remote.FinanceRepository
import com.example.csks_creatives.domain.useCase.factories.FinanceUseCaseFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FinanceUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) : FinanceUseCaseFactory {
    override fun create(): FinanceUseCase {
        return FinanceUseCase(financeRepository)
    }

            override suspend fun getFinancesForAdmin(): Flow<ResultState<List<Map<FinanceMetric, List<FinanceMetric>>>>> =
        flow {
            emit(ResultState.Loading)
            try {
                financeRepository.getFinancesForAdmin().collect { financeData ->
                    emit(ResultState.Success(financeData))
                }
            } catch (exception: Exception) {
                emit(
                    ResultState.Error(
                        exception.localizedMessage ?: "Error fetching finance data"
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
}