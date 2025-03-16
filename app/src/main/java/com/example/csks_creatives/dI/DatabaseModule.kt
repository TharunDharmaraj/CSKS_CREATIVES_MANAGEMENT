package com.example.csks_creatives.dI

import android.content.Context
import androidx.room.Room
import com.example.csks_creatives.data.database.ClientsDao
import com.example.csks_creatives.data.database.ClientsDataBase
import com.example.csks_creatives.data.database.EmployeesDao
import com.example.csks_creatives.data.database.EmployeesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Volatile
    private var clientsDatabaseInstance: ClientsDataBase? = null
    private fun getClientsDatabase(context: Context): ClientsDataBase {
        return clientsDatabaseInstance ?: synchronized(this) {
            Room.databaseBuilder(context, ClientsDataBase::class.java, "ClientsDataBase")
                .fallbackToDestructiveMigration().build().also { clientsDatabaseInstance = it }
        }
    }

    @Volatile
    private var employeesDatabaseInstance: EmployeesDatabase? = null
    private fun getEmployeesDatabase(context: Context): EmployeesDatabase {
        return employeesDatabaseInstance ?: synchronized(this) {
            Room.databaseBuilder(context, EmployeesDatabase::class.java, "EmployeesDataBase")
                .fallbackToDestructiveMigration().build().also { employeesDatabaseInstance = it }
        }
    }

    @Provides
    @Singleton
    fun provideClientsDatabase(context: Context): ClientsDataBase {
        return getClientsDatabase(context)
    }

    @Provides
    @Singleton
    fun provideClientsDao(clientsDataBase: ClientsDataBase): ClientsDao {
        return clientsDataBase.clientsDao
    }

    @Provides
    @Singleton
    fun provideEmployeesDatabase(context: Context): EmployeesDatabase {
        return getEmployeesDatabase(context)
    }

    @Provides
    @Singleton
    fun provideEmployeesDao(employeesDatabase: EmployeesDatabase): EmployeesDao {
        return employeesDatabase.employeesDao
    }
}