package com.fake.safesteps.database

import androidx.room.*

@Dao
interface AlertDao {
    @Query("SELECT * FROM cached_alerts WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllAlerts(userId: String): List<CachedAlert>

    @Query("SELECT * FROM cached_alerts WHERE isSynced = 0")
    suspend fun getUnsyncedAlerts(): List<CachedAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: CachedAlert)

    @Query("UPDATE cached_alerts SET isSynced = 1 WHERE id = :alertId")
    suspend fun markAsSynced(alertId: String)
}