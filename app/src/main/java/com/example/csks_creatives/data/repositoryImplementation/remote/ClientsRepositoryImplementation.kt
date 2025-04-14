package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.CLIENT_COLLECTION
import com.example.csks_creatives.data.utils.Constants.CLIENT_ID
import com.example.csks_creatives.data.utils.Constants.CLIENT_NAME
import com.example.csks_creatives.domain.model.client.Client
import com.example.csks_creatives.domain.repository.remote.ClientsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientsRepositoryImplementation @Inject constructor(
    private val firestore: FirebaseFirestore
) : ClientsRepository {
    private val logTag = "ClientsRepository"

    override suspend fun getClientList(): List<Client> {
        return try {
            val snapshot = firestore.collection(CLIENT_COLLECTION).get().await()
            Log.d(
                logTag + "Get",
                "Successfully fetched client list size: ${snapshot.documents}"
            )
            return snapshot.documents.mapNotNull { it.toObject(Client::class.java) }
        } catch (exception: Exception) {
            Log.d(logTag + "Get", "Error ${exception.message}  in fetching clients")
            emptyList()
        }
    }

    override suspend fun addClient(client: Client) {
        try {
            val clientName = client.clientName
            firestore.collection(CLIENT_COLLECTION).document(clientName).set(
                hashMapOf(
                    CLIENT_ID to client.clientId,
                    CLIENT_NAME to clientName
                ),
                SetOptions.merge()
            ).await()
            Log.d(logTag + "Add", "Client Added into firestore $client")
        } catch (exception: Exception) {
            Log.d(logTag + "Add", "Failed ${exception.message}  Adding client $client to firestore")
        }
    }

    override suspend fun checkClientNameExists(clientName: String): Boolean {
        try {
            val clientSnapShot =
                firestore.collection(CLIENT_COLLECTION).document(clientName).get().await()
            if (clientSnapShot.exists()) {
                Log.d(logTag + "Check", "Client $clientName Already Exists")
                return true
            }
            Log.d(logTag + "Check", "Client $clientName Not Exists")
            return false
        } catch (exception: Exception) {
            Log.d(logTag + "Check", "Error ${exception.message}  checking employee Existence")
            return true
        }
    }
}