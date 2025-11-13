package com.fake.safesteps.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fake.safesteps.models.TrustedContact
import com.fake.safesteps.repository.ContactRepository
import com.fake.safesteps.repository.UserSearchRepository
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ContactRepository()
    private val userSearch = UserSearchRepository() // new repository for email lookup

    private val _contacts = MutableLiveData<List<TrustedContact>>()
    val contacts: LiveData<List<TrustedContact>> = _contacts

    private val _contactAdded = MutableLiveData<Boolean>()
    val contactAdded: LiveData<Boolean> = _contactAdded

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _userFound = MutableLiveData<String?>()
    val userFound: LiveData<String?> = _userFound

    fun loadContacts() {
        viewModelScope.launch {
            repository.getTrustedContacts()
                .onSuccess { _contacts.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    /**
     * New method: Search by email first, then add if found
     */
    fun addContactByEmail(
        contactName: String,
        contactEmail: String,
        contactPhone: String
    ) {
        viewModelScope.launch {
            userSearch.findUserByEmail(contactEmail)
                .onSuccess { firebaseUid ->
                    if (firebaseUid != null) {
                        // User found - call existing addContact
                        addContact(firebaseUid, contactName, contactEmail, contactPhone)
                    } else {
                        // User not registered
                        _error.value = "User with email $contactEmail not registered in SafeSteps"
                        _contactAdded.value = false
                    }
                }
                .onFailure {
                    _error.value = "Error searching for user: ${it.message}"
                    _contactAdded.value = false
                }
        }
    }

    /**
     * Existing method: Actually adds the contact to Firestore
     */
    fun addContact(
        contactUserId: String,
        contactName: String,
        contactEmail: String,
        contactPhone: String
    ) {
        viewModelScope.launch {
            repository.addContact(contactUserId, contactName, contactEmail, contactPhone)
                .onSuccess {
                    _contactAdded.value = true
                    loadContacts()
                }
                .onFailure {
                    _error.value = it.message
                    _contactAdded.value = false
                }
        }
    }

    fun removeContact(contactId: String) {
        viewModelScope.launch {
            repository.removeContact(contactId)
                .onSuccess { loadContacts() }
                .onFailure { _error.value = it.message }
        }
    }
}
