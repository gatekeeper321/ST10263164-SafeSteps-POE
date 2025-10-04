package com.fake.safesteps



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fake.safesteps.databinding.ContactItemBinding
import com.fake.safesteps.models.TrustedContact

class ContactsAdapter(
    private var contacts: List<TrustedContact>,
    private val onDeleteClick: (TrustedContact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(private val binding: ContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: TrustedContact) {
            binding.contactNameText.text = contact.contactName
            binding.contactEmailText.text = contact.contactEmail
            binding.contactPhoneText.text = contact.contactPhone

            binding.deleteButton.setOnClickListener {
                onDeleteClick(contact)
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