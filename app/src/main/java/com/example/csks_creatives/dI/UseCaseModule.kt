package com.example.csks_creatives.dI

import com.example.csks_creatives.domain.repository.database.ClientsLocalRepository
import com.example.csks_creatives.domain.repository.database.EmployeesLocalRepository
import com.example.csks_creatives.domain.repository.remote.AdminRepository
import com.example.csks_creatives.domain.repository.remote.ClientsRepository
import com.example.csks_creatives.domain.repository.remote.CommentsRepository
import com.example.csks_creatives.domain.repository.remote.LoginRepository
import com.example.csks_creatives.domain.repository.remote.TasksManipulationRepository
import com.example.csks_creatives.domain.repository.remote.TasksRepository
import com.example.csks_creatives.domain.useCase.AdminUseCase
import com.example.csks_creatives.domain.useCase.AdminUseCaseFactory
import com.example.csks_creatives.domain.useCase.ClientsUseCase
import com.example.csks_creatives.domain.useCase.ClientsUseCaseFactory
import com.example.csks_creatives.domain.useCase.CommentsUseCase
import com.example.csks_creatives.domain.useCase.CommentsUseCaseFactory
import com.example.csks_creatives.domain.useCase.TasksManipulationUseCase
import com.example.csks_creatives.domain.useCase.TasksManipulationUseCaseFactory
import com.example.csks_creatives.domain.useCase.TasksUseCase
import com.example.csks_creatives.domain.useCase.TasksUseCaseFactory
import com.example.csks_creatives.domain.useCase.UserLoginUseCase
import com.example.csks_creatives.domain.useCase.UserPersistenceUseCase
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
}