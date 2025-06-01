package com.example.csks_creatives.dI

import com.example.csks_creatives.domain.repository.database.ClientsLocalRepository
import com.example.csks_creatives.domain.repository.database.EmployeesLocalRepository
import com.example.csks_creatives.domain.repository.remote.*
import com.example.csks_creatives.domain.useCase.*
import com.example.csks_creatives.domain.useCase.factories.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideAdminUseCaseFactory(
        adminRepository: AdminRepository,
        employeesLocalRepository: EmployeesLocalRepository
    ): AdminUseCaseFactory {
        return AdminUseCase(
            adminRepository = adminRepository,
            employeesLocalRepository = employeesLocalRepository
        )
    }

    @Provides
    fun provideClientsUseCaseFactory(
        clientsRepository: ClientsRepository,
        clientsLocalRepository: ClientsLocalRepository
    ): ClientsUseCaseFactory {
        return ClientsUseCase(clientsRepository, clientsLocalRepository)
    }

    @Provides
    fun provideCommentsUseCaseFactory(commentsRepository: CommentsRepository): CommentsUseCaseFactory {
        return CommentsUseCase(commentsRepository)
    }

    @Provides
    fun provideTasksManipulationsUseCaseFactory(
        tasksManipulationRepository: TasksManipulationRepository,
        adminRepository: AdminRepository
    ): TasksManipulationUseCaseFactory {
        return TasksManipulationUseCase(tasksManipulationRepository, adminRepository)
    }

    @Provides
    fun provideTasksUseCaseFactory(
        tasksRepository: TasksRepository,
        adminRepository: AdminRepository
    ): TasksUseCaseFactory {
        return TasksUseCase(
            tasksRepository = tasksRepository,
            adminRepository = adminRepository
        )
    }

    @Provides
    fun provideUserLoginUseCase(
        loginRepository: LoginRepository,
        userPersistenceUseCase: UserPersistenceUseCase
    ): UserLoginUseCase {
        return UserLoginUseCase(
            loginRepository = loginRepository,
            userPersistenceUseCase = userPersistenceUseCase
        )
    }

    @Provides
    fun provideEmployeeUseCase(
        employeeRepository: EmployeeRepository
    ): EmployeeUseCaseFactory {
        return EmployeeUseCase(
            employeeRepository = employeeRepository
        )
    }

    @Provides
    fun provideFinanceUseCase(
        financeRepository: FinanceRepository
    ): FinanceUseCaseFactory {
        return FinanceUseCase(
            financeRepository = financeRepository
        )
    }
}