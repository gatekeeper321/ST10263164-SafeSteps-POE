package com.fake.safesteps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fake.safesteps.databinding.ContactItemBinding
import com.fake.safesteps.models.TrustedContact

/**
 * Enhanced Contacts Adapter with avatar initials
 * Reference: Material Design - Lists (https://material.io/components/lists)
 */
class ContactsAdapter(
    private var contacts: List<TrustedContact>,
    private val onDeleteClick: (TrustedContact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(private val binding: ContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: TrustedContact) {
            // Set contact info
            binding.contactNameText.text = contact.contactName
            binding.contactEmailText.text = contact.contactEmail
            binding.contactPhoneText.text = contact.contactPhone

            // Generate and set avatar initials
            binding.contactInitials.text = getInitials(contact.contactName)

            // Delete button click
            binding.deleteButton.setOnClickListener {
                onDeleteClick(contact)
            }
        }

        /**
         * Extract initials from contact name
         * Examples:
         * "John Doe" -> "JD"
         * "Alice" -> "A"
         * "Bob Smith Jr" -> "BS"
         */
        private fun getInitials(name: String): String {
            val parts = name.trim().split(" ")
            return when {
                parts.isEmpty() -> "?"
                parts.size == 1 -> parts[0].take(1).uppercase()
                else -> {
                    val first = parts[0].take(1)
                    val last = parts[parts.size - 1].take(1)
                    (first + last).uppercase()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ContactItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount() = contacts.size

    fun updateContacts(newContacts: List<TrustedContact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}