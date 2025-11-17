package com.fake.safesteps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.fake.safesteps.databinding.ActivityContactsBinding
import com.fake.safesteps.models.TrustedContact
import com.fake.safesteps.viewmodels.ContactViewModel

/**
 * Enhanced Contacts Activity with empty states and improved UX
 * Reference: Material Design - Empty States (https://material.io/design/communication/empty-states.html)
 */
class ContactsActivity : BaseActivity() {
    private lateinit var binding: ActivityContactsBinding
    private lateinit var viewModel: ContactViewModel
    private lateinit var adapter: ContactsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ContactViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.loadContacts()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        binding.bottomNavigation.selectedItemId = R.id.friends_item
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.alert_item -> {
                    startActivity(Intent(this, AlertActivity::class.java))
                    true
                }
                R.id.settings_item -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.friends_item -> true // Already here
                R.id.map_item -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.alert_history_item -> {
                    startActivity(Intent(this, AlertHistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ContactsAdapter(emptyList()) { contact ->
            showDeleteConfirmation(contact)
        }

        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ContactsActivity)
            adapter = this@ContactsActivity.adapter

            // Add item spacing
            addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: View,
                    parent: androidx.recyclerview.widget.RecyclerView,
                    state: androidx.recyclerview.widget.RecyclerView.State
                ) {
                    outRect.bottom = 8
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.contacts.observe(this) { contacts ->
            if (contacts.isEmpty()) {
                // Show empty state
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.contactsRecyclerView.visibility = View.GONE
            } else {
                // Show contacts list
                binding.emptyStateLayout.visibility = View.GONE
                binding.contactsRecyclerView.visibility = View.VISIBLE
                adapter.updateContacts(contacts)
            }
        }

        viewModel.contactAdded.observe(this) { added ->
            if (added) {
                Toast.makeText(this, "✓ Contact added successfully", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, "⚠ Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        // FAB click
        binding.addContactButton.setOnClickListener {
            showAddContactDialog()
        }

        // Empty state button click
        binding.emptyStateAddButton.setOnClickListener {
            showAddContactDialog()
        }
    }

    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.contactNameInput)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.contactEmailInput)
        val phoneInput = dialogView.findViewById<TextInputEditText>(R.id.contactPhoneInput)

        AlertDialog.Builder(this)
            .setTitle("Add Trusted Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()

                // Validation
                when {
                    name.isEmpty() -> {
                        Toast.makeText(this, "⚠ Name is required", Toast.LENGTH_SHORT).show()
                    }
                    email.isEmpty() && phone.isEmpty() -> {
                        Toast.makeText(this, "⚠ Please provide either email or phone number", Toast.LENGTH_SHORT).show()
                    }
                    email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        Toast.makeText(this, "⚠ Invalid email format", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        viewModel.addContactByEmail(name, email, phone)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(contact: TrustedContact) {
        AlertDialog.Builder(this)
            .setTitle("Remove Contact")
            .setMessage("Remove ${contact.contactName} from trusted contacts?")
            .setPositiveButton("Remove") { _, _ ->
                viewModel.removeContact(contact.id)
                Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}