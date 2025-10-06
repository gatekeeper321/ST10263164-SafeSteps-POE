package com.fake.safesteps.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fake.safesteps.models.EmergencyAlert
import com.fake.safesteps.repository.AlertRepository
import kotlinx.coroutines.launch

class AlertViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AlertRepository()

    private val _alertCreated = MutableLiveData<Boolean>()
    val alertCreated: LiveData<Boolean> = _alertCreated

    private val _activeAlerts = MutableLiveData<List<EmergencyAlert>>()
    val activeAlerts: LiveData<List<EmergencyAlert>> = _activeAlerts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun createEmergencyAlert(latitude: Double, longitude: Double) {
        _loading.value = true
        viewModelScope.launch {
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
        }
    }

    fun loadActiveAlerts() {
        viewModelScope.launch {
            repository.getActiveAlerts()
                .onSuccess { _activeAlerts.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
                .onSuccess { loadActiveAlerts() }
                .onFailure { _error.value = it.message }
        }
    }
    private val _userAlerts = MutableLiveData<List<EmergencyAlert>>()
    val userAlerts: LiveData<List<EmergencyAlert>> = _userAlerts

    fun loadAllAlerts() {
        viewModelScope.launch {
            repository.getUserAlerts()
                .onSuccess { _userAlerts.value = it }
                .onFailure { _error.value = it.message }
        }
    }
}