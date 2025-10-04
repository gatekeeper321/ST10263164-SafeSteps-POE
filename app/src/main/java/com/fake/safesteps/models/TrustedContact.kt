package com.fake.safesteps.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TrustedContact(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val contactUserId: String = "",
    val contactName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null
)