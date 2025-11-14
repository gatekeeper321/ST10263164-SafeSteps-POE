package com.fake.safesteps.repository

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.fake.safesteps.models.EmergencyAlert
import com.fake.safesteps.models.TrustedContact
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AlertRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val alertsCollection = db.collection("emergencyAlerts")
    private val fcmTokenRepo = FCMTokenRepository()

    // Your Cloud Function URL
    private val CLOUD_FUNCTION_URL = "https://us-central1-safesteps-1cd09.cloudfunctions.net/sendNotification"

    /**
     * Create alert and send push notifications to all trusted contacts via Cloud Functions
     * NOW INCLUDES: Saving the geocoded address to Firestore
     */
    suspend fun createAlert(
        latitude: Double,
        longitude: Double,
        alertType: String = "EMERGENCY"
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val userName = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "A contact"

            // Get user's trusted contacts
            val contactsResult = getTrustedContacts()
            val contacts = contactsResult.getOrNull() ?: emptyList()
            val contactIds = contacts.map { it.contactUserId }

            // IMPORTANT: Geocode the address BEFORE saving to Firestore
            val address = getAddressFromCoordinates(latitude, longitude)
            Log.d(TAG, "Geocoded address: ${address ?: "Could not geocode"}")

            // Create the alert in Firestore WITH address
            val alert = hashMapOf(
                "userId" to userId,
                "latitude" to latitude,
                "longitude" to longitude,
                "address" to address, // NEW: Save the human-readable address
                "alertType" to alertType,
                "isActive" to true,
                "timestamp" to FieldValue.serverTimestamp(),
                "notifiedContacts" to contactIds
            )

            val documentRef = alertsCollection.add(alert).await()
            Log.d(TAG, "Alert created with address: ${documentRef.id}")

            // Send notifications to all contacts using Cloud Functions
            if (contacts.isNotEmpty()) {
                sendNotificationsViaCloudFunction(
                    contacts = contacts,
                    userName = userName,
                    latitude = latitude,
                    longitude = longitude,
                    alertType = alertType
                )
            } else {
                Log.w(TAG, "No contacts to notify")
            }

            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating alert", e)
            Result.failure(e)
        }
    }

    /**
     * Convert coordinates to human-readable address using Geocoder
     * Uses timeout to prevent blocking the alert sending
     */
    private suspend fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Add timeout to prevent hanging
                val geocoder = Geocoder(context, Locale.getDefault())

                // Quick check if Geocoder is available
                if (!Geocoder.isPresent()) {
                    Log.w(TAG, "Geocoder not available on this device")
                    return@withContext null
                }

                // Use withTimeoutOrNull to prevent blocking
                val addresses = kotlinx.coroutines.withTimeoutOrNull(3000L) {
                    geocoder.getFromLocation(latitude, longitude, 1)
                }

                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]

                    // Build a readable address string
                    val addressParts = mutableListOf<String>()

                    // Add street address (most detailed)
                    address.getAddressLine(0)?.let { addressParts.add(it) }

                    val fullAddress = addressParts.joinToString(", ")
                    Log.d(TAG, "Geocoded address: $fullAddress")
                    return@withContext fullAddress
                } else {
                    Log.w(TAG, "No address found for coordinates (timeout or no results)")
                    return@withContext null
                }
            } catch (e: java.io.IOException) {
                Log.w(TAG, "Geocoding network error (proceeding with coordinates)", e)
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "Geocoding failed", e)
                return@withContext null
            }
        }
    }

    /**
     * Send push notifications to trusted contacts via Cloud Functions
     * This is much more secure than hardcoding server keys!
     */
    private suspend fun sendNotificationsViaCloudFunction(
        contacts: List<TrustedContact>,
        userName: String,
        latitude: Double,
        longitude: Double,
        alertType: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Get FCM tokens for all contacts
                val contactUserIds = contacts.map { it.contactUserId }
                val tokensResult = fcmTokenRepo.getContactsFCMTokens(contactUserIds)
                val tokens = tokensResult.getOrNull() ?: emptyMap()

                Log.d(TAG, "Got ${tokens.size} FCM tokens for contacts")

                if (tokens.isEmpty()) {
                    Log.w(TAG, "No FCM tokens found for contacts")
                    return@withContext
                }

                // Geocode the coordinates to get readable address
                val address = getAddressFromCoordinates(latitude, longitude)
                Log.d(TAG, "Using address: ${address ?: "coordinates only"}")

                // Prepare data for Cloud Function
                val fcmTokensList = tokens.values.toList()

                // Call Cloud Function
                val success = callCloudFunction(
                    fcmTokens = fcmTokensList,
                    userName = userName,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    alertType = alertType
                )

                if (success) {
                    Log.d(TAG, "Notifications sent successfully via Cloud Functions to ${tokens.size} contacts")
                } else {
                    Log.e(TAG, "Failed to send notifications via Cloud Functions")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error sending notifications via Cloud Functions", e)
            }
        }
    }

    /**
     * Call the Cloud Function to send notifications
     * Returns true if successful
     */
    private suspend fun callCloudFunction(
        fcmTokens: List<String>,
        userName: String,
        latitude: Double,
        longitude: Double,
        address: String?,
        alertType: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(CLOUD_FUNCTION_URL)
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 10000 // 10 seconds
                    readTimeout = 10000
                }

                // Build JSON payload
                val payload = JSONObject().apply {
                    put("fcmTokens", JSONArray(fcmTokens))
                    put("userName", userName)
                    put("latitude", latitude)
                    put("longitude", longitude)
                    if (address != null) {
                        put("address", address)
                    }
                    put("alertType", alertType)
                }

                Log.d(TAG, "Calling Cloud Function with payload: $payload")

                // Write request body
                connection.outputStream.use { os ->
                    os.write(payload.toString().toByteArray())
                    os.flush()
                }

                // Read response
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    Log.d(TAG, "Cloud Function response: $response")

                    // Parse response
                    val jsonResponse = JSONObject(response)
                    val successful = jsonResponse.optInt("successful", 0)
                    val failed = jsonResponse.optInt("failed", 0)

                    Log.d(TAG, "Notifications sent: $successful successful, $failed failed")

                    connection.disconnect()
                    return@withContext successful > 0
                } else {
                    val errorStream = connection.errorStream?.bufferedReader()?.readText()
                    Log.e(TAG, "Cloud Function error: $responseCode - $errorStream")
                    connection.disconnect()
                    return@withContext false
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception calling Cloud Function", e)
                return@withContext false
            }
        }
    }

    // Keep all existing methods...
    suspend fun getTrustedContacts(): Result<List<TrustedContact>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = db.collection("trustedContacts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val contacts = snapshot.documents.mapNotNull {
                it.toObject(TrustedContact::class.java)
            }

            Result.success(contacts)
        } catch (e: Exception) {
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
            Log.e(TAG, "Error getting alerts", e)
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
            Log.e(TAG, "Error getting active alerts", e)
            Result.failure(e)
        }
    }

    suspend fun resolveAlert(alertId: String): Result<Unit> {
        return try {
            alertsCollection.document(alertId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "resolvedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving alert", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AlertRepository"
    }

    suspend fun getActiveAlertsFromContacts(contactUserIds: List<String>): Result<List<EmergencyAlert>> {
        return try {
            if (contactUserIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // Firestore 'in' query supports max 10 items, so we need to batch
            val alerts = mutableListOf<EmergencyAlert>()

            contactUserIds.chunked(10).forEach { batch ->
                val snapshot = alertsCollection
                    .whereIn("userId", batch)
                    .whereEqualTo("isActive", true)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val batchAlerts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(EmergencyAlert::class.java)?.copy(id = doc.id)
                }

                alerts.addAll(batchAlerts)
            }

            Log.d(TAG, "Found ${alerts.size} active alerts from contacts")
            Result.success(alerts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting contact alerts", e)
            Result.failure(e)
        }
    }
}