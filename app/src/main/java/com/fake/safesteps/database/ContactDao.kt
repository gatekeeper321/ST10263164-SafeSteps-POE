package com.fake.safesteps.database

import androidx.room.*

@Dao
interface ContactDao {
    @Query("SELECT * FROM cached_contacts WHERE userId = :userId")
    suspend fun getAllContacts(userId: String): List<CachedContact>

    @Query("SELECT * FROM cached_contacts WHERE isSynced = 0")
    suspend fun getUnsyncedContacts(): List<CachedContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: CachedContact)

    @Query("DELETE FROM cached_contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)

    @Query("UPDATE cached_contacts SET isSynced = 1 WHERE id = :contactId")
    suspend fun markAsSynced(contactId: String)
}