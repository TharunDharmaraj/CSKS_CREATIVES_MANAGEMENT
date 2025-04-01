package com.example.csks_creatives.dI

import com.example.csks_creatives.data.database.*
import com.example.csks_creatives.data.repositoryImplementation.database.*
import com.example.csks_creatives.data.repositoryImplementation.remote.*
import com.example.csks_creatives.domain.repository.database.*
import com.example.csks_creatives.domain.repository.remote.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // Remote Repository
    @Provides
    @Singleton
    fun provideAdminRepository(firestore: FirebaseFirestore): AdminRepository {
        return AdminRepositoryImplementation(firestore)
    }

    @Provides
    @Singleton
    fun provideClientsRepository(firestore: FirebaseFirestore): ClientsRepository {
        return ClientsRepositoryImplementation(firestore)
    }

    @Provides
    @Singleton
    fun provideCommentsRepository(firestore: FirebaseFirestore): CommentsRepository {
        return CommentsRepositoryImplementation(firestore)
    }

    @Provides
    @Singleton
    fun provideTasksRepository(firestore: FirebaseFirestore): TasksRepository {
        return TasksRepositoryImplementation(firestore)
    }

    @Provides
    @Singleton
    fun provideTasksManipulationRepository(firestore: FirebaseFirestore): TasksManipulationRepository {
        return TasksManipulationRepositoryImplementation(firestore)
    }

    @Provides
    @Singleton
    fun provideLoginRepository(
        firestore: FirebaseFirestore,
        messaging: FirebaseMessaging
    ): LoginRepository {
        return LoginRepositoryImplementation(firestore = firestore, messaging = messaging)
    }

    @Provides
    @Singleton
    fun provideEmployeeRepository(
        firestore: FirebaseFirestore
    ): EmployeeRepository {
        return EmployeeRepositoryImplementation(firestore)
    }

    // local Repository
    @Provides
    @Singleton
    fun provideLocalClientsRepository(clientsDao: ClientsDao): ClientsLocalRepository {
        return ClientsLocalRepositoryImplementation(clientsDao)
    }

    @Provides
    @Singleton
    fun provideLocalEmployeesRepository(employeesDao: EmployeesDao): EmployeesLocalRepository {
        return EmployeesLocalRepositoryImplementation(employeesDao)
    }

    @Provides
    @Singleton
    fun provideCurrentUserRepository(currentUserDao: CurrentUserDao): CurrentUserRepository {
        return CurrentUserRepositoryImplementation(currentUserDao)
    }
}
