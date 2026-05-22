package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.ADMIN_COLLECTION
import com.example.csks_creatives.data.utils.Constants.ADMIN_DOCUMENT_NAME
import com.example.csks_creatives.data.utils.Constants.ADMIN_PASSWORD
import com.example.csks_creatives.data.utils.Constants.ADMIN_USERNAME
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_COLLECTION
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_NAME
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_PASSWORD
import com.example.csks_creatives.data.utils.Constants.FCM_TOKEN_FIELD
import com.example.csks_creatives.data.utils.Constants.FCM_TOKEN_LAST_UPDATED_FIELD
import com.example.csks_creatives.domain.model.login.CurrentLoginUser
import com.example.csks_creatives.domain.model.user.User
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.repository.remote.LoginRepository
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.domain.utils.Utils.formatTimeStamp
import com.example.csks_creatives.domain.utils.Utils.getCurrentTimeAsString
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepositoryImplementation @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) : LoginRepository {
    private val logTag = "LoginRepository"
    private var currentLoggedInUser: CurrentLoginUser? = null

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            // 1. Try Admin Login (Encrypted)
            val adminSnapshot = firestore.collection(ADMIN_COLLECTION)
                .whereEqualTo(ADMIN_USERNAME, username)
                .whereEqualTo(ADMIN_PASSWORD, password)
                .get()
                .await()

            if (adminSnapshot.isEmpty.not()) {
                return handleAdminSuccess(adminSnapshot, username)
            }

            // 2. Try Employee Login (Encrypted)
            val employeeSnapshot = firestore.collection(EMPLOYEE_COLLECTION)
                .whereEqualTo(EMPLOYEE_EMPLOYEE_NAME, username)
                .whereEqualTo(EMPLOYEE_EMPLOYEE_PASSWORD, password)
                .get()
                .await()

            if (employeeSnapshot.isEmpty.not()) {
                return handleEmployeeSuccess(employeeSnapshot, username)
            }

            // 3. Fallback: Try Plain Text (for legacy support during transition)
            // We need the raw password here. However, the UseCase already encrypted it.
            // Wait, the UseCase calls encrypt(password).
            // To support fallback, the repository needs the plain password OR the UseCase needs to pass both.
            // But the requirement is "if hashed doesn't match, use unhashed".
            // Since we use AES, it's reversible. I can decrypt it here to try the fallback.
            
            val plainPassword = com.example.csks_creatives.domain.utils.SecurityUtils.decrypt(password)
            if (plainPassword != password) { // If it was actually encrypted

                // Try Admin Plain Text
                val adminLegacySnapshot = firestore.collection(ADMIN_COLLECTION)
                    .whereEqualTo(ADMIN_USERNAME, username)
                    .whereEqualTo(ADMIN_PASSWORD, plainPassword)
                    .get()
                    .await()
                
                if (adminLegacySnapshot.isEmpty.not()) {
                    return handleAdminSuccess(adminLegacySnapshot, username)
                }
                
                // Try Employee Plain Text
                val employeeLegacySnapshot = firestore.collection(EMPLOYEE_COLLECTION)
                    .whereEqualTo(EMPLOYEE_EMPLOYEE_NAME, username)
                    .whereEqualTo(EMPLOYEE_EMPLOYEE_PASSWORD, plainPassword)
                    .get()
                    .await()
                
                if (employeeLegacySnapshot.isEmpty.not()) {
                    return handleEmployeeSuccess(employeeLegacySnapshot, username)
                }
            }

            Log.d(logTag, "Invalid Credentials")
            Result.failure(Exception("Invalid Credentials"))
        } catch (exception: Exception) {
            Log.d(logTag, "Exception ${exception.message} ")
            Result.failure(exception)
        }
    }

    private fun handleAdminSuccess(snapshot: com.google.firebase.firestore.QuerySnapshot, username: String): Result<User> {
        val adminDoc = snapshot.documents.first()
        setCurrentUserAsAdmin(
            CurrentLoginUser(
                userRole = UserRole.Admin,
                adminName = adminDoc.id,
                employeeId = EMPTY_STRING
            )
        )
        Log.d(logTag, "UserRole is Admin")
        return Result.success(User(adminDoc.id, username, UserRole.Admin))
    }

    private fun handleEmployeeSuccess(snapshot: com.google.firebase.firestore.QuerySnapshot, username: String): Result<User> {
        val employeeDoc = snapshot.documents.first()
        setCurrentUserAsEmployee(
            CurrentLoginUser(
                userRole = UserRole.Employee,
                adminName = EMPTY_STRING,
                employeeId = employeeDoc.id
            )
        )
        Log.d(logTag, "UserRole is Employee")
        return Result.success(User(employeeDoc.id, username, UserRole.Employee))
    }

    override suspend fun saveFCMToken(employeeId: String) {
        messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                firestore.collection(EMPLOYEE_COLLECTION).document(employeeId)
                    .set(
                        hashMapOf(
                            FCM_TOKEN_FIELD to token,
                            FCM_TOKEN_LAST_UPDATED_FIELD to formatTimeStamp(
                                getCurrentTimeAsString()
                            )
                        ), SetOptions.merge()
                    )
                    .addOnSuccessListener { Log.d(logTag + "FCM", "New Token $token") }
                    .addOnFailureListener { Log.e(logTag + "FCM", "Error saving new token", it) }
            }
        }
    }

    override suspend fun saveAdminFMToken() {
        messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                firestore.collection(ADMIN_COLLECTION).document(ADMIN_DOCUMENT_NAME)
                    .set(
                        hashMapOf(
                            FCM_TOKEN_FIELD to token,
                            FCM_TOKEN_LAST_UPDATED_FIELD to formatTimeStamp(
                                getCurrentTimeAsString()
                            )
                        ), SetOptions.merge()
                    )
                    .addOnSuccessListener { Log.d(logTag + "FCM", "New Token set to Admin $token") }
                    .addOnFailureListener {
                        Log.e(
                            logTag + "FCM",
                            "Error saving new token to Admin",
                            it
                        )
                    }
            }
        }
    }

    override suspend fun saveNewFcmToken(employeeId: String, newToken: String) {
        firestore.collection(EMPLOYEE_COLLECTION).document(employeeId)
            .set(
                hashMapOf(
                    FCM_TOKEN_FIELD to newToken,
                    FCM_TOKEN_LAST_UPDATED_FIELD to formatTimeStamp(
                        getCurrentTimeAsString()
                    )
                ), SetOptions.merge()
            )
            .addOnSuccessListener { Log.d(logTag + "NewFCM", "Token Updated $newToken") }
            .addOnFailureListener { Log.e(logTag + "NewFCM", "Error saving token", it) }
    }

    override fun getCurrentUser() = currentLoggedInUser

    override fun deleteCurrentUser() {
        // To delete the FCM Tokens for a device on Logout
        messaging.deleteToken()
        currentLoggedInUser = null
    }

    private fun setCurrentUserAsAdmin(user: CurrentLoginUser) {
        currentLoggedInUser = user
    }

    private fun setCurrentUserAsEmployee(user: CurrentLoginUser) {
        currentLoggedInUser = user
    }
}