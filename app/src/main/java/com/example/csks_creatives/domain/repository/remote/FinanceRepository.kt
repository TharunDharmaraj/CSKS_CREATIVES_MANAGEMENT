package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.finance.FinanceMetric
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    // Returns List of Map of Year to Months financial performance
    suspend fun getFinancesForAdmin(): Flow<List<Map<FinanceMetric, List<FinanceMetric>>>>
}