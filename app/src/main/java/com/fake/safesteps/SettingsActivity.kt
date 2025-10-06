package com.fake.safesteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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

        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        binding.bottomNavigation.selectedItemId = R.id.settings_item
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

                R.id.alert_history_item -> {
                    startActivity(Intent(this, AlertHistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }
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
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.languageContainer.setOnClickListener {
            showLanguageDialog()
        }

        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        val isBiometricEnabled = sharedPref.getBoolean("biometric_enabled", false)
        binding.biometricSwitch.isChecked = isBiometricEnabled

        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("biometric_enabled", isChecked)
                apply()
            }
            Toast.makeText(
                this,
                if (isChecked) "Biometric enabled" else "Biometric disabled",
                Toast.LENGTH_SHORT
            ).show()
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