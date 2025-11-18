// AlertLogic.kt
package com.fake.safesteps.logic

import com.fake.safesteps.models.EmergencyAlert
import com.fake.safesteps.repository.AlertRepository
import com.fake.safesteps.sync.SyncManager
import java.util.Date

class AlertLogic(
    private val repository: AlertRepository,
    private val syncManager: SyncManager
) {
    suspend fun createAlert(latitude: Double, longitude: Double): Result<String> {
        return try {
            if (syncManager.isNetworkAvailable()) {
                repository.createAlert(latitude, longitude)
            } else {
                val id = syncManager.saveAlertLocally(latitude, longitude, "EMERGENCY")
                if (id.isNotEmpty()) Result.success(id) else Result.failure(Exception("Failed to save offline"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveAlerts(): Result<List<EmergencyAlert>> {
        return try {
            if (syncManager.isNetworkAvailable()) repository.getActiveAlerts()
            else {
                val cached = syncManager.getCachedAlerts()
                val mapped = cached.map { cached ->
                    EmergencyAlert(
                        id = cached.id,
                        userId = cached.userId,
                        latitude = cached.latitude,
                        longitude = cached.longitude,
                        alertType = cached.alertType,
                        timestamp = Date(cached.timestamp),
                        isActive = true
                    )
                }
                Result.success(mapped)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}