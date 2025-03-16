package com.example.csks_creatives.domain.repository.database

import com.example.csks_creatives.data.database.EmployeeItem

interface EmployeesLocalRepository {
    suspend fun insert(employeeItem: EmployeeItem)

    suspend fun getEmployees(): List<EmployeeItem>

    suspend fun deleteAllEmployees()
}