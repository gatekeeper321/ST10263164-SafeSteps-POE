package com.fake.safesteps



import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fake.safesteps.databinding.ActivityProfileBinding
import com.fake.safesteps.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        loadUserProfile()

        binding.saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            val result = userRepository.getUser()
            result.onSuccess { user ->
                user?.let {
                    binding.nameInput.setText(it.displayName)
                    binding.phoneInput.setText(it.phoneNumber)
                    binding.emailText.text = it.email
                }
            }.onFailure {
                Toast.makeText(this@ProfileActivity, "Error loading profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val name = binding.nameInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameInput.error = "Name required"
            return
        }

        lifecycleScope.launch {
            val email = auth.currentUser?.email ?: ""

            val result = userRepository.createOrUpdateUser(email, name, phone)
            result.onSuccess {
                Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this@ProfileActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
