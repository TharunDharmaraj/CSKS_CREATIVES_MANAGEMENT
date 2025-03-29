package com.example.csks_creatives.domain.useCase.factories

import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.ClientsUseCase

interface ClientsUseCaseFactory {
    fun create(): ClientsUseCase

    suspend fun addClient(client: Client): ResultState<String>

    suspend fun getClients(isForceFetchFromServer: Boolean = true): ResultState<List<Client>>
}