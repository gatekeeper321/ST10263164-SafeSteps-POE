package com.fake.safesteps.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserSearchRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Find user by email and return their Firebase UID
     */
    suspend fun findUserByEmail(email: String): Result<String?> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", email.trim().lowercase())
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val userId = snapshot.documents[0].getString("firebaseUid")
                Log.d("UserSearch", "Found user: $userId for email: $email")
                Result.success(userId)
            } else {
                Log.d("UserSearch", "No user found for email: $email")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("UserSearch", "Error searching user", e)
            Result.failure(e)
        }
    }
}
