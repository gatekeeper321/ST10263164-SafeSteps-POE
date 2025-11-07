package com.fake.safesteps.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_alerts")
data class CachedAlert(
    @PrimaryKey val id: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val alertType: String,
    val timestamp: Long,
    val isSynced: Boolean = false
)