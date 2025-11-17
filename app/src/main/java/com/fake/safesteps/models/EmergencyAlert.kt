package com.fake.safesteps.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Emergency Alert data model
 * Reference: Firebase Firestore Documentation (https://firebase.google.com/docs/firestore)
 *
 * @property address Human-readable address geocoded from coordinates (added for POE final)
 */
data class EmergencyAlert(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String? = null, // NEW: Human-readable address
    val alertType: String = "EMERGENCY",
    val isActive: Boolean = true,
    @ServerTimestamp
    val timestamp: Date? = null,
    val resolvedAt: Date? = null,
    val notifiedContacts: List<String> = emptyList()
)