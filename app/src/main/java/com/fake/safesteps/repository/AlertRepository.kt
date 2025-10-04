package com.fake.safesteps.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.fake.safesteps.models.EmergencyAlert
import kotlinx.coroutines.tasks.await

class AlertRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val alertsCollection = db.collection("emergencyAlerts")

    suspend fun createAlert(
        latitude: Double,
        longitude: Double,
        alertType: String = "EMERGENCY"
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val alert = hashMapOf(
                "userId" to userId,
                "latitude" to latitude,
                "longitude" to longitude,
                "alertType" to alertType,
                "isActive" to true,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "notifiedContacts" to emptyList<String>()
            )

            val documentRef = alertsCollection.add(alert).await()
            Log.d("AlertRepository", "Alert created: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e("AlertRepository", "Error creating alert", e)
            Result.failure(e)
        }
    }

    suspend fun getUserAlerts(): Result<List<EmergencyAlert>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = alertsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val alerts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(EmergencyAlert::class.java)
            }

            Result.success(alerts)
        } catch (e: Exception) {
            Log.e("AlertRepository", "Error getting alerts", e)
            Result.failure(e)
        }
    }

    suspend fun getActiveAlerts(): Result<List<EmergencyAlert>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = alertsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val alerts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(EmergencyAlert::class.java)
            }

            Result.success(alerts)
        } catch (e: Exception) {
            Log.e("AlertRepository", "Error getting active alerts", e)
            Result.failure(e)
        }
    }

    suspend fun resolveAlert(alertId: String): Result<Unit> {
        return try {
            alertsCollection.document(alertId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "resolvedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AlertRepository", "Error resolving alert", e)
            Result.failure(e)
        }
    }
}