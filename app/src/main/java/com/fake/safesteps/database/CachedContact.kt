package com.fake.safesteps.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_contacts")
data class CachedContact(
    @PrimaryKey val id: String,
    val userId: String,
    val contactName: String,
    val contactEmail: String,
    val contactPhone: String,
    val isSynced: Boolean = false
)