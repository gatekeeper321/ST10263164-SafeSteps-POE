package com.fake.safesteps.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fake.safesteps.models.EmergencyAlert
import com.fake.safesteps.repository.AlertRepository
import com.fake.safesteps.sync.SyncManager
import kotlinx.coroutines.launch

class AlertViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AlertRepository(application.applicationContext)
    private val syncManager = SyncManager(application)

    private val _alertCreated = MutableLiveData<Boolean>()
    val alertCreated: LiveData<Boolean> = _alertCreated

    private val _activeAlerts = MutableLiveData<List<EmergencyAlert>>()
    val activeAlerts: LiveData<List<EmergencyAlert>> = _activeAlerts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _offlineMode = MutableLiveData<Boolean>()
    val offlineMode: LiveData<Boolean> = _offlineMode

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    /**
     * Create emergency alert with offline support
     * If online: saves to Firebase
     * If offline: saves locally and syncs when online
     */
    fun createEmergencyAlert(latitude: Double, longitude: Double) {
        _loading.value = true
        viewModelScope.launch {
            try {
                if (syncManager.isNetworkAvailable()) {
                    // Online - save to Firebase
                    _offlineMode.value = false
                    repository.createAlert(latitude, longitude)
                        .onSuccess {
                            _alertCreated.value = true
                            _loading.value = false
                            loadActiveAlerts()
                        }
                        .onFailure {
                            _error.value = it.message
                            _alertCreated.value = false
                            _loading.value = false
                        }
                } else {
                    // Offline - save locally
                    _offlineMode.value = true
                    val alertId = syncManager.saveAlertLocally(
                        latitude,
                        longitude,
                        "EMERGENCY"
                    )

                    if (alertId.isNotEmpty()) {
                        _alertCreated.value = true
                        _syncStatus.value = "Alert saved offline. Will sync when online."
                        loadCachedAlerts()
                    } else {
                        _error.value = "Failed to save alert locally"
                        _alertCreated.value = false
                    }
                    _loading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _alertCreated.value = false
                _loading.value = false
            }
        }
    }

    /**
     * Load active alerts from Firebase
     */
    fun loadActiveAlerts() {
        viewModelScope.launch {
            repository.getActiveAlerts()
                .onSuccess { _activeAlerts.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    /**
     * Load cached alerts from local database
     */
    fun loadCachedAlerts() {
        viewModelScope.launch {
            try {
                val cachedAlerts = syncManager.getCachedAlerts()
                // Convert CachedAlert to EmergencyAlert for display
                val emergencyAlerts = cachedAlerts.map { cached ->
                    EmergencyAlert(
                        id = cached.id,
                        userId = cached.userId,
                        latitude = cached.latitude,
                        longitude = cached.longitude,
                        alertType = cached.alertType,
                        timestamp = java.util.Date(cached.timestamp),
                        isActive = true
                    )
                }
                _activeAlerts.value = emergencyAlerts
            } catch (e: Exception) {
                _error.value = "Failed to load cached alerts: ${e.message}"
            }
        }
    }

    /**
     * Resolve alert (mark as inactive)
     */
    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
                .onSuccess { loadActiveAlerts() }
                .onFailure { _error.value = it.message }
        }
    }

    private val _userAlerts = MutableLiveData<List<EmergencyAlert>>()
    val userAlerts: LiveData<List<EmergencyAlert>> = _userAlerts

    /**
     * Load all alerts (active and resolved)
     */
    fun loadAllAlerts() {
        viewModelScope.launch {
            if (syncManager.isNetworkAvailable()) {
                // Load from Firebase
                repository.getUserAlerts()
                    .onSuccess { _userAlerts.value = it }
                    .onFailure { _error.value = it.message }
            } else {
                // Load from cache
                loadCachedAlerts()
            }
        }
    }

    /**
     * Sync all offline data with Firebase
     * Call this when network becomes available
     */
    fun syncOfflineData() {
        viewModelScope.launch {
            try {
                _syncStatus.value = "Syncing..."
                syncManager.syncAllData()
                _syncStatus.value = "Sync complete"

                // Reload alerts after sync
                loadAllAlerts()
            } catch (e: Exception) {
                _syncStatus.value = "Sync failed: ${e.message}"
            }
        }
    }

    /**
     * Check network status and update offline mode
     */
    fun checkNetworkStatus() {
        _offlineMode.value = !syncManager.isNetworkAvailable()
    }
}