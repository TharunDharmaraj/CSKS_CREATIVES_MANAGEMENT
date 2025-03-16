package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.ADMIN_COLLECTION
import com.example.csks_creatives.data.utils.Constants.ADMIN_PASSWORD
import com.example.csks_creatives.data.utils.Constants.ADMIN_USERNAME
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_COLLECTION
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_NAME
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_PASSWORD
import com.example.csks_creatives.domain.model.user.User
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.repository.remote.LoginRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepositoryImplementation @Inject constructor(
    private val firestore: FirebaseFirestore
) : LoginRepository {
    private val logTag = "LoginRepository"
    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val adminSnapshot = firestore.collection(ADMIN_COLLECTION)
                .whereEqualTo(ADMIN_USERNAME, username)
                .whereEqualTo(ADMIN_PASSWORD, password)
                .get()
                .await()

            if (adminSnapshot.isEmpty.not()) {
                val adminDoc = adminSnapshot.documents.first()
                Log.d(logTag, "UserRole is Admin")
                return Result.success(User(adminDoc.id, username, UserRole.Admin))
            }

            val employeeSnapshot = firestore.collection(EMPLOYEE_COLLECTION)
                .whereEqualTo(EMPLOYEE_EMPLOYEE_NAME, username)
                .whereEqualTo(EMPLOYEE_EMPLOYEE_PASSWORD, password)
                .get()
                .await()

            if (employeeSnapshot.isEmpty.not()) {
                val employeeDoc = employeeSnapshot.documents.first()
                Log.d(logTag, "UserRole is Employee")
                return Result.success(User(employeeDoc.id, username, UserRole.Employee))
            }

            Log.d(logTag, "Invalid Credentials")
            Result.failure(Exception("Invalid Credentials"))
        } catch (exception: Exception) {
            Log.d(logTag, "Exception $exception")
            Result.failure(exception)
        }
    }
}