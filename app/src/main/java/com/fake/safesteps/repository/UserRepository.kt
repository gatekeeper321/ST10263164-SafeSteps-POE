package com.fake.safesteps.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fake.safesteps.models.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun createOrUpdateUser(
        email: String,
        displayName: String,
        phoneNumber: String
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val userData = hashMapOf(
                "firebaseUid" to userId,
                "email" to email,
                "displayName" to displayName,
                "phoneNumber" to phoneNumber,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            usersCollection.document(userId)
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user", e)
            Result.failure(e)
        }
    }

    suspend fun getUser(): Result<User?> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toObject(User::class.java)

            Result.success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user", e)
            Result.failure(e)
        }
    }
}