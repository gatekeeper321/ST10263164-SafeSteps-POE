package com.fake.safesteps.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Geofence(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val placeName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusMeters: Double = 0.0,
    val notifyOnEntry: Boolean = true,
    val notifyOnExit: Boolean = true,
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null
)