package com.fake.safesteps.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fake.safesteps.models.EmergencyAlert
import com.fake.safesteps.repository.AlertRepository
import com.fake.safesteps.repository.ContactRepository
import kotlinx.coroutines.launch

data class AlertWithContact(
    val alert: EmergencyAlert,
    val contactName: String,
    val contactEmail: String
)

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val alertRepository = AlertRepository(application.applicationContext)
    private val contactRepository = ContactRepository()

    private val _contactAlerts = MutableLiveData<List<AlertWithContact>>()
    val contactAlerts: LiveData<List<AlertWithContact>> = _contactAlerts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Load active alerts from trusted contacts
     */
    fun loadContactAlerts() {
        _loading.value = true
        viewModelScope.launch {
            try {
                // Get trusted contacts
                val contactsResult = contactRepository.getTrustedContacts()
                val contacts = contactsResult.getOrNull() ?: emptyList()

                if (contacts.isEmpty()) {
                    _contactAlerts.value = emptyList()
                    _loading.value = false
                    return@launch
                }

                // Get active alerts from these contacts
                val contactUserIds = contacts.map { it.contactUserId }
                val alertsResult = alertRepository.getActiveAlertsFromContacts(contactUserIds)

                alertsResult.onSuccess { alerts ->
                    // Match alerts with contact info
                    val alertsWithContacts = alerts.mapNotNull { alert ->
                        val contact = contacts.find { it.contactUserId == alert.userId }
                        if (contact != null) {
                            AlertWithContact(
                                alert = alert,
                                contactName = contact.contactName,
                                contactEmail = contact.contactEmail
                            )
                        } else null
                    }

                    _contactAlerts.value = alertsWithContacts
                    _loading.value = false
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load alerts"
                    _loading.value = false
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _loading.value = false
            }
        }
    }

    /**
     * Refresh alerts
     */
    fun refreshAlerts() {
        loadContactAlerts()
    }
}