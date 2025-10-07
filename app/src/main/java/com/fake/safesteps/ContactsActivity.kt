package com.fake.safesteps



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.fake.safesteps.R
import com.fake.safesteps.ContactsAdapter
import com.fake.safesteps.databinding.ActivityContactsBinding
import com.fake.safesteps.models.TrustedContact
import com.fake.safesteps.viewmodels.ContactViewModel

class ContactsActivity : AppCompatActivity() {
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

        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        binding.bottomNavigation.selectedItemId = R.id.friends_item
        setupBottomNavigation()
    }

    //bottom nav (copy paste to every activity)
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
                R.id.friends_item -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    true
                }

                R.id.map_item -> {
                    notifyUser("Map coming soon")
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
        }
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
        viewModel.contacts.observe(this) { contacts ->
            if (contacts.isEmpty()) {
                binding.emptyText.visibility = View.VISIBLE
                binding.contactsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyText.visibility = View.GONE
                binding.contactsRecyclerView.visibility = View.VISIBLE
                adapter.updateContacts(contacts)
            }
        }

        viewModel.contactAdded.observe(this) { added ->
            if (added) {
                Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        binding.addContactButton.setOnClickListener {
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

                // ADD VALIDATION HERE
                when {
                    name.isEmpty() -> {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                    }
                    email.isEmpty() && phone.isEmpty() -> {
                        Toast.makeText(this, "Please provide either email or phone number", Toast.LENGTH_SHORT).show()
                    }
                    email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val contactUserId = "contact${System.currentTimeMillis()}"
                        viewModel.addContact(contactUserId, name, email, phone)
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
}