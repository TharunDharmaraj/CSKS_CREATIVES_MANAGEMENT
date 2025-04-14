package com.example.csks_creatives.data.repositoryImplementation.local

import com.example.csks_creatives.data.database.ClientItem
import com.example.csks_creatives.data.database.ClientsDao
import com.example.csks_creatives.domain.repository.database.ClientsLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientsLocalRepositoryImplementation @Inject constructor(
    private val clientsDao: ClientsDao
) : ClientsLocalRepository {
    override suspend fun insert(clientItem: ClientItem) {
        clientsDao.insert(clientItem)
    }

    override suspend fun getClients(): List<ClientItem> = clientsDao.getAllClients()

    override suspend fun deleteAllClients() {
        clientsDao.deleteAllClients()
    }
}