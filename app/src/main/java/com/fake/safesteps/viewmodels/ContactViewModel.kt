package com.fake.safesteps.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fake.safesteps.models.TrustedContact
import com.fake.safesteps.repository.ContactRepository
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ContactRepository()

    private val _contacts = MutableLiveData<List<TrustedContact>>()
    val contacts: LiveData<List<TrustedContact>> = _contacts

    private val _contactAdded = MutableLiveData<Boolean>()
    val contactAdded: LiveData<Boolean> = _contactAdded

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadContacts() {
        viewModelScope.launch {
            repository.getTrustedContacts()
                .onSuccess { _contacts.value = it }
                .onFailure { _error.value = it.message }
        }
    }

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