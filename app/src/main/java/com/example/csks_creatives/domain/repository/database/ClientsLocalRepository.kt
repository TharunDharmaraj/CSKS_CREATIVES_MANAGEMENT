package com.example.csks_creatives.domain.repository.database

import com.example.csks_creatives.data.database.ClientItem

interface ClientsLocalRepository {
    suspend fun insert(clientItem: ClientItem)

    suspend fun getClients() : List<ClientItem>

    suspend fun deleteAllClients()
}