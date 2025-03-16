package com.example.csks_creatives.dI

import com.example.csks_creatives.data.database.ClientsDao
import com.example.csks_creatives.data.database.EmployeesDao
import com.example.csks_creatives.data.repositoryImplementation.database.ClientsLocalRepositoryImplementation
import com.example.csks_creatives.data.repositoryImplementation.database.EmployeesLocalRepositoryImplementation
import com.example.csks_creatives.data.repositoryImplementation.remote.AdminRepositoryImplementation
import com.example.csks_creatives.data.repositoryImplementation.remote.ClientsRepositoryImplementation
import com.example.csks_creatives.data.repositoryImplementation.remote.CommentsRepositoryImplementation
import com.example.csks_creatives.data.repositoryImplementation.remote.LoginRepositoryImplementation
import com.example.csks_creatives.data.repositoryImplementation.remote.TasksManipulationRepositoryImplementation
import com.example.csks_creatives.data.repositoryImplementation.remote.TasksRepositoryImplementation
import com.example.csks_creatives.domain.repository.database.ClientsLocalRepository
import com.example.csks_creatives.domain.repository.database.EmployeesLocalRepository
import com.example.csks_creatives.domain.repository.remote.AdminRepository
import com.example.csks_creatives.domain.repository.remote.ClientsRepository
import com.example.csks_creatives.domain.repository.remote.CommentsRepository
import com.example.csks_creatives.domain.repository.remote.LoginRepository
import com.example.csks_creatives.domain.repository.remote.TasksManipulationRepository
import com.example.csks_creatives.domain.repository.remote.TasksRepository
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideLoginRepository(firestore: FirebaseFirestore): LoginRepository {
        return LoginRepositoryImplementation(firestore)
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
}
