package com.fake.safesteps.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.fake.safesteps.models.Geofence
import kotlinx.coroutines.tasks.await

class GeofenceRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val geofencesCollection = db.collection("geofences")

    suspend fun createGeofence(
        placeName: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val geofence = hashMapOf(
                "userId" to userId,
                "placeName" to placeName,
                "latitude" to latitude,
                "longitude" to longitude,
                "radiusMeters" to radiusMeters,
                "notifyOnEntry" to true,
                "notifyOnExit" to true,
                "isActive" to true,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            val documentRef = geofencesCollection.add(geofence).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e("GeofenceRepository", "Error creating geofence", e)
            Result.failure(e)
        }
    }

    suspend fun getUserGeofences(): Result<List<Geofence>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = geofencesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val geofences = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Geofence::class.java)
            }

            Result.success(geofences)
        } catch (e: Exception) {
            Log.e("GeofenceRepository", "Error getting geofences", e)
            Result.failure(e)
        }
    }

    suspend fun deleteGeofence(geofenceId: String): Result<Unit> {
        return try {
            geofencesCollection.document(geofenceId)
                .update("isActive", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GeofenceRepository", "Error deleting geofence", e)
            Result.failure(e)
        }
    }
}