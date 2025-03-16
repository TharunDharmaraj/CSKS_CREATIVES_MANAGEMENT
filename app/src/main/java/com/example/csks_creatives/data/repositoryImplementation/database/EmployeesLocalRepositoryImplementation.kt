package com.example.csks_creatives.data.repositoryImplementation.database

import com.example.csks_creatives.data.database.EmployeeItem
import com.example.csks_creatives.data.database.EmployeesDao
import com.example.csks_creatives.domain.repository.database.EmployeesLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeesLocalRepositoryImplementation @Inject constructor(
    private val employeesDao: EmployeesDao
) : EmployeesLocalRepository {
    override suspend fun insert(employeeItem: EmployeeItem) = employeesDao.insert(employeeItem)

    override suspend fun getEmployees(): List<EmployeeItem> = employeesDao.getAllEmployees()

    override suspend fun deleteAllEmployees() = employeesDao.deleteAllClients()
}