package com.example.csks_creatives.data.repositoryImplementation.remote

import com.example.csks_creatives.data.utils.Constants.PARSED_FINANCE_COLLECTION
import com.example.csks_creatives.data.utils.Constants.PARSED_FINANCE_MONTH_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TOTAL_COST_FINANCE
import com.example.csks_creatives.data.utils.Constants.TOTAL_FULLY_PAID_COST_FINANCE
import com.example.csks_creatives.data.utils.Constants.TOTAL_PARTIALLY_PAID_COST_FINANCE
import com.example.csks_creatives.data.utils.Constants.TOTAL_UN_PAID_COST_FINANCE
import com.example.csks_creatives.domain.model.finance.FinanceMetric
import com.example.csks_creatives.domain.repository.remote.FinanceRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImplementation @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : FinanceRepository {
    private val logTag = "FinanceRepository"

    override suspend fun getFinancesForAdmin(): Flow<List<Map<FinanceMetric, List<FinanceMetric>>>> =
        callbackFlow {
            val rootRef = getParsedFinanceCollection()

            val listenerRegistration = rootRef.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    close(error ?: Exception("Unknown Firestore error"))
                    return@addSnapshotListener
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val yearTasks = snapshot.documents.map { yearDoc ->
                            async {
                                val year = yearDoc.id.toIntOrNull() ?: return@async emptyMap()

                                val yearMetric = buildFinanceMetric(
                                    year = year,
                                    month = 0,
                                    doc = yearDoc
                                )

                                val monthsSnapshot = yearDoc.reference
                                    .collection(PARSED_FINANCE_MONTH_SUB_COLLECTION)
                                    .get()
                                    .await()

                                val monthMetrics = monthsSnapshot.documents.mapNotNull { doc ->
                                    val month = doc.id.toIntOrNull() ?: return@mapNotNull null
                                    buildFinanceMetric(year, month, doc)
                                }

                                mapOf(yearMetric to monthMetrics)
                            }
                        }

                        val results = yearTasks.awaitAll()
                        trySend(results)
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }

            awaitClose { listenerRegistration.remove() }
        }

    private fun buildFinanceMetric(
        year: Int,
        month: Int,
        doc: DocumentSnapshot
    ) = FinanceMetric(
        year = year,
        month = month,
        totalCost = doc.getLong(TOTAL_COST_FINANCE)?.toInt() ?: 0,
        totalFullyPaidCost = doc.getLong(TOTAL_FULLY_PAID_COST_FINANCE)?.toInt() ?: 0,
        totalPartiallyPaidCost = doc.getLong(TOTAL_PARTIALLY_PAID_COST_FINANCE)?.toInt() ?: 0,
        totalUnpaidCost = doc.getLong(TOTAL_UN_PAID_COST_FINANCE)?.toInt() ?: 0
    )

    private fun getParsedFinanceCollection() =
        firebaseFirestore.collection(PARSED_FINANCE_COLLECTION)
}