package com.fake.safesteps.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.fake.safesteps.database.AlertDatabase
import com.fake.safesteps.database.CachedAlert
import com.fake.safesteps.database.CachedContact
import com.fake.safesteps.repository.AlertRepository
import com.fake.safesteps.repository.ContactRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages offline data synchronization with Firebase
 * Reference: Android Developers - Room Database (https://developer.android.com/training/data-storage/room)
 */
class SyncManager(private val context: Context) {
    private val database = AlertDatabase.getDatabase(context)
    private val alertRepository = AlertRepository()
    private val contactRepository = ContactRepository()
    private val auth = FirebaseAuth.getInstance()

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Save alert to local database when offline
     */
    suspend fun saveAlertLocally(
        latitude: Double,
        longitude: Double,
        alertType: String = "EMERGENCY"
    ): String {
        val userId = auth.currentUser?.uid ?: return ""
        val alertId = "local_${System.currentTimeMillis()}"

        val cachedAlert = CachedAlert(
            id = alertId,
            userId = userId,
            latitude = latitude,
            longitude = longitude,
            alertType = alertType,
            timestamp = System.currentTimeMillis(),
            isSynced = false
        )

        database.alertDao().insertAlert(cachedAlert)
        Log.d("SyncManager", "Alert saved locally: $alertId")
        return alertId
    }

    /**
     * Save contact to local database when offline
     */
    suspend fun saveContactLocally(
        contactUserId: String,
        contactName: String,
        contactEmail: String,
        contactPhone: String
    ): String {
        val userId = auth.currentUser?.uid ?: return ""
        val contactId = "local_${System.currentTimeMillis()}"

        val cachedContact = CachedContact(
            id = contactId,
            userId = userId,
            contactName = contactName,
            contactEmail = contactEmail,
            contactPhone = contactPhone,
            isSynced = false
        )

        database.contactDao().insertContact(cachedContact)
        Log.d("SyncManager", "Contact saved locally: $contactId")
        return contactId
    }

    /**
     * Sync all unsynced data when network becomes available
     */
    suspend fun syncAllData() {
        if (!isNetworkAvailable()) {
            Log.d("SyncManager", "No network available, skipping sync")
            return
        }

        val userId = auth.currentUser?.uid ?: return

        // Sync alerts
        syncAlerts(userId)

        // Sync contacts
        syncContacts(userId)
    }

    private suspend fun syncAlerts(userId: String) {
        try {
            val unsyncedAlerts = database.alertDao().getUnsyncedAlerts()

            unsyncedAlerts.forEach { cachedAlert ->
                val result = alertRepository.createAlert(
                    cachedAlert.latitude,
                    cachedAlert.longitude,
                    cachedAlert.alertType
                )

                result.onSuccess { firebaseId ->
                    database.alertDao().markAsSynced(cachedAlert.id)
                    Log.d("SyncManager", "Alert synced: ${cachedAlert.id} -> $firebaseId")
                }.onFailure { exception ->
                    Log.e("SyncManager", "Failed to sync alert: ${cachedAlert.id}", exception)
                }
            }

            Log.d("SyncManager", "Synced ${unsyncedAlerts.size} alerts")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error syncing alerts", e)
        }
    }

    private suspend fun syncContacts(userId: String) {
        try {
            val unsyncedContacts = database.contactDao().getUnsyncedContacts()

            unsyncedContacts.forEach { cachedContact ->
                val result = contactRepository.addContact(
                    cachedContact.id,
                    cachedContact.contactName,
                    cachedContact.contactEmail,
                    cachedContact.contactPhone
                )

                result.onSuccess { firebaseId ->
                    database.contactDao().markAsSynced(cachedContact.id)
                    Log.d("SyncManager", "Contact synced: ${cachedContact.id} -> $firebaseId")
                }.onFailure { exception ->
                    Log.e("SyncManager", "Failed to sync contact: ${cachedContact.id}", exception)
                }
            }

            Log.d("SyncManager", "Synced ${unsyncedContacts.size} contacts")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error syncing contacts", e)
        }
    }

    /**
     * Get cached alerts from local database
     */
    suspend fun getCachedAlerts(): List<CachedAlert> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return database.alertDao().getAllAlerts(userId)
    }

    /**
     * Get cached contacts from local database
     */
    suspend fun getCachedContacts(): List<CachedContact> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return database.contactDao().getAllContacts(userId)
    }

    companion object {
        fun startBackgroundSync(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val syncManager = SyncManager(context)
                syncManager.syncAllData()
            }
        }
    }
}