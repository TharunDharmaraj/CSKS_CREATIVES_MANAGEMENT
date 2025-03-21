package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.data.utils.Utils.toClientItem
import com.example.csks_creatives.data.utils.Utils.toClientList
import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.repository.database.ClientsLocalRepository
import com.example.csks_creatives.domain.repository.remote.ClientsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClientsUseCase @Inject constructor(
    private val clientsRepository: ClientsRepository,
    private val clientsLocalRepository: ClientsLocalRepository
) :
    ClientsUseCaseFactory {
    override fun create(): ClientsUseCase {
        return ClientsUseCase(
            clientsRepository = clientsRepository,
            clientsLocalRepository = clientsLocalRepository
        )
    }

    override suspend fun addClient(client: Client): ResultState<String> {
        val buildClient = client.copy(
            clientId = client.clientName,
            clientTasks = listOf()
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

    override suspend fun getClients(isForceFetchFromServer: Boolean): ResultState<List<Client>> {
        return try {
            withContext(Dispatchers.IO) {
                if (isForceFetchFromServer.not()) {
                    val clientList = clientsLocalRepository.getClients().toClientList()
                    if (clientList.isNotEmpty()) {
                        return@withContext ResultState.Success(clientList)
                    }
                }
                val clientList = clientsRepository.getClientList()
                if (clientList.isEmpty()) ResultState.Error("No Clients Found")
                clientsLocalRepository.deleteAllClients()
                clientList.forEach { client ->
                    clientsLocalRepository.insert(client.toClientItem())
                }
                return@withContext ResultState.Success(clientList)
            }
        } catch (exception: Exception) {
            return ResultState.Error("Error Getting Clients List ${exception.message}")
        }
    }
}