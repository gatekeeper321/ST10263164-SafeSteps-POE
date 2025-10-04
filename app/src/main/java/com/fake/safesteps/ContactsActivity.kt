package com.fake.safesteps



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

                if (name.isNotEmpty()) {
                    // For now, use a generated ID. In production, this would be another user's Firebase UID
                    val contactUserId = "contact_${System.currentTimeMillis()}"
                    viewModel.addContact(contactUserId, name, email, phone)
                } else {
                    Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
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