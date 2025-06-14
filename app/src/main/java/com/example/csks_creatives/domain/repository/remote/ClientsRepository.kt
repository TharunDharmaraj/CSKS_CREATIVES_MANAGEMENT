package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.client.Client

interface ClientsRepository {
    suspend fun getClientList(): List<Client>

    suspend fun addClient(client: Client)

    suspend fun checkClientNameExists(clientName: String): Boolean

    suspend fun getClientName(clientId: String): String

    suspend fun editClientName(clientId: String, newClientName: String)

    // TODO Add Tasks Immediately after Clients Gets Created (Add Tasks to client when creating a Client)

    // TODO Add Delete Client functionality
}