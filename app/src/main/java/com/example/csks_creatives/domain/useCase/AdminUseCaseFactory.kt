package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import kotlinx.coroutines.flow.Flow

interface AdminUseCaseFactory {
    fun create(): AdminUseCase

    suspend fun createEmployee(employee: Employee): ResultState<String>

    suspend fun deleteEmployee(employeeId: String): ResultState<String>

    suspend fun getEmployeeDetails(employeeId: String): Flow<ResultState<Employee>>

    suspend fun getEmployeesList(isForceFetchFromServer : Boolean = true): ResultState<List<Employee>>
}