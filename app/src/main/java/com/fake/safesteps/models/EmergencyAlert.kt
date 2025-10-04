package com.fake.safesteps.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class EmergencyAlert(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val alertType: String = "EMERGENCY",
    val isActive: Boolean = true,
    @ServerTimestamp
    val timestamp: Date? = null,
    val resolvedAt: Date? = null,
    val notifiedContacts: List<String> = emptyList()
)