package com.fake.safesteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.fake.safesteps.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        loadUserInfo()
        loadLanguagePreference()
        setupClickListeners()
    }

    private fun loadUserInfo() {
        auth.currentUser?.let { user ->
            binding.userNameText.text = user.displayName ?: "User"
            binding.userEmailText.text = user.email ?: "No email"
        }
    }

    private fun loadLanguagePreference() {
        val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        val language = sharedPref.getString("selected_language", "English")
        binding.selectedLanguageText.text = language
    }

    private fun setupClickListeners() {
        binding.profileContainer.setOnClickListener {
            // TODO: Navigate to profile edit screen
            Toast.makeText(this, "Profile editing coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.languageContainer.setOnClickListener {
            showLanguageDialog()
        }

        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Afrikaans", "isiZulu")
        val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        val currentLanguage = sharedPref.getString("selected_language", "English")
        val currentIndex = languages.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languages[which]

                with(sharedPref.edit()) {
                    putString("selected_language", selectedLanguage)
                    apply()
                }

                binding.selectedLanguageText.text = selectedLanguage
                Toast.makeText(
                    this,
                    "Language set to $selectedLanguage",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()

                // Clear preferences
                getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

                // Navigate to login screen
                // TODO: Replace with your actual LoginActivity
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}