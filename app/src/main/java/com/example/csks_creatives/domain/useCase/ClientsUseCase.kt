package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.data.utils.Utils.toClientItem
import com.example.csks_creatives.data.utils.Utils.toClientList
import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.database.ClientsLocalRepository
import com.example.csks_creatives.domain.repository.remote.ClientsRepository
import com.example.csks_creatives.domain.useCase.factories.ClientsUseCaseFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClientsUseCase @Inject constructor(
    private val clientsRepository: ClientsRepository,
    private val clientsLocalRepository: ClientsLocalRepository
) :
    ClientsUseCaseFactory {
    private var lastForceFetchTime = 0L
    private val forceFetchCooldown = 10000L // 10 seconds

    private fun canPerformForceFetch(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastForceFetchTime < forceFetchCooldown) {
            return false
        }
        lastForceFetchTime = currentTime
        return true
    }

    override fun create(): ClientsUseCase {
        return ClientsUseCase(
            clientsRepository = clientsRepository,
            clientsLocalRepository = clientsLocalRepository
        )
    }

    override suspend fun addClient(client: Client): ResultState<String> {
        val buildClient = client.copy(
            clientId = client.clientName,
        )

        if (buildClient.clientId.isBlank()) return ResultState.Error("Client ID Cannot be EMPTY")
        if (buildClient.clientName.isBlank()) return ResultState.Error("Client Name Cannot be EMPTY")

        return try {
            val clientNameAlreadyExists =
                clientsRepository.checkClientNameExists(buildClient.clientName)
            if (clientNameAlreadyExists) ResultState.Error("Client Name Already Exists")
            clientsRepository.addClient(buildClient)
            ResultState.Success("Client ${buildClient.clientName} Created Successfully")
        } catch (exception: Exception) {
            ResultState.Error("Failed to create Client: ${exception.message}")
        }
    }

    override suspend fun getClients(isForceFetch: Boolean, limit: Long?): ResultState<List<Client>> {
        if (isForceFetch && limit != null && !canPerformForceFetch()) {
            return ResultState.Error("Please wait 10 seconds between force fetches")
        }
        return try {
            withContext(Dispatchers.IO) {
                // If we are not force fetching, and we have a limit, we can try local first.
                // But if limit is null (requesting ALL), the local cache might be incomplete 
                // (e.g., from a previous paginated fetch), so we should fetch from remote 
                // to ensure we have the full list.
                if (isForceFetch.not() && limit != null) {
                    val clientList = clientsLocalRepository.getClients().toClientList()
                    if (clientList.isNotEmpty() && clientList.size >= limit) {
                        return@withContext ResultState.Success(clientList.take(limit.toInt()))
                    }
                }
                
                val finalLimit = if (isForceFetch) null else limit
                val clientList = clientsRepository.getClientList(finalLimit)
                if (clientList.isEmpty()) ResultState.Error("No Clients Found")
                
                // If we fetched ALL (limit == null) or it's a force fetch, sync the local cache.
                if (isForceFetch || limit == null) {
                    clientsLocalRepository.deleteAllClients()
                    clientList.forEach { client ->
                        clientsLocalRepository.insert(client.toClientItem())
                    }
                }
                return@withContext ResultState.Success(clientList)
            }
        } catch (exception: Exception) {
            return ResultState.Error("Error Getting Clients List ${exception.message}")
        }
    }

    override suspend fun getAllClients(): ResultState<List<Client>> {
        return getClients(isForceFetch = true, limit = null)
    }
}