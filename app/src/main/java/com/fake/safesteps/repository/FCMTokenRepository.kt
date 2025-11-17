package com.fake.safesteps.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

/**
 * Repository to manage FCM tokens for push notifications
 * Each user stores their FCM token so contacts can send them alerts
 */
class FCMTokenRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * Save current user's FCM token to Firestore
     * This allows their contacts to send them notifications
     */
    suspend fun saveUserFCMToken(fcmToken: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // Update user document with FCM token
            usersCollection.document(userId)
                .set(
                    mapOf(
                        "fcmToken" to fcmToken,
                        "tokenUpdatedAt" to FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()

            Log.d(TAG, "FCM token saved for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token", e)
            Result.failure(e)
        }
    }

    /**
     * Get FCM token for a specific user (contact)
     * Used to send notifications to trusted contacts
     */
    suspend fun getUserFCMToken(userId: String): Result<String?> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val token = snapshot.getString("fcmToken")

            Log.d(TAG, "Retrieved FCM token for user: $userId")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            Result.failure(e)
        }
    }

    /**
     * Get FCM tokens for all trusted contacts
     * Returns map of contactUserId to FCM token
     */
    suspend fun getContactsFCMTokens(contactUserIds: List<String>): Result<Map<String, String>> {
        return try {
            val tokens = mutableMapOf<String, String>()

            for (userId in contactUserIds) {
                val snapshot = usersCollection.document(userId).get().await()
                val token = snapshot.getString("fcmToken")

                if (token != null) {
                    tokens[userId] = token
                    Log.d(TAG, "Got token for contact: $userId")
                }
            }

            Log.d(TAG, "Retrieved ${tokens.size} FCM tokens for contacts")
            Result.success(tokens)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting contacts' FCM tokens", e)
            Result.failure(e)
        }
    }

    /**
     * Delete user's FCM token (on logout)
     */
    suspend fun deleteUserFCMToken(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            usersCollection.document(userId)
                .update("fcmToken", FieldValue.delete())
                .await()

            Log.d(TAG, "FCM token deleted for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting FCM token", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "FCMTokenRepository"
    }
}