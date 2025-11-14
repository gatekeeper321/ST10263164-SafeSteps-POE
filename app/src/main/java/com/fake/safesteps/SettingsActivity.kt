package com.fake.safesteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.fake.safesteps.databinding.ActivitySettingsBinding
import com.fake.safesteps.notifications.FCMTokenHelper

class SettingsActivity : BaseActivity() {
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

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        binding.bottomNavigation.selectedItemId = R.id.settings_item
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.alert_item -> {
                    startActivity(Intent(this, AlertActivity::class.java))
                    true
                }
                R.id.settings_item -> true // Already here
                R.id.friends_item -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    true
                }
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

        // Load biometric preference
        val isBiometricEnabled = sharedPref.getBoolean("biometric_enabled", false)
        binding.biometricSwitch.isChecked = isBiometricEnabled
    }

    private fun setupClickListeners() {
        // Edit Profile Button
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Language Container
        binding.languageContainer.setOnClickListener {
            showLanguageDialog()
        }

        // Biometric Switch
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("biometric_enabled", isChecked)
                apply()
            }

            val message = if (isChecked) {
                getString(R.string.biometric_enabled)
            } else {
                getString(R.string.biometric_disabled)
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // FCM Token Container
        binding.fcmTokenContainer.setOnClickListener {
            showFCMTokenOptions()
        }

        // Logout Button
        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showFCMTokenOptions() {
        val options = arrayOf(
            "Get & Copy FCM Token",
            "Show Saved Token",
            "Subscribe to 'test' Topic",
            "Unsubscribe from 'test' Topic"
        )

        AlertDialog.Builder(this)
            .setTitle("FCM Token Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> FCMTokenHelper.getAndCopyToken(this)
                    1 -> FCMTokenHelper.showSavedToken(this)
                    2 -> FCMTokenHelper.subscribeToTopic("test", this)
                    3 -> FCMTokenHelper.unsubscribeFromTopic("test", this)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Afrikaans", "isiZulu")
        val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        val currentLanguage = sharedPref.getString("selected_language", "English")
        val currentIndex = languages.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languages[which]

                with(sharedPref.edit()) {
                    putString("selected_language", selectedLanguage)
                    apply()
                }

                binding.selectedLanguageText.text = selectedLanguage
                setLocale(selectedLanguage)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                auth.signOut()

                getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setLocale(languageName: String) {
        val localeCode = when (languageName) {
            "Afrikaans" -> "af"
            "isiZulu" -> "zu"
            else -> "en"
        }

        val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("language_code", localeCode)
            putString("selected_language", languageName)
            apply()
        }

        // Restart app to apply locale cleanly
        val intent = Intent(this, SettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}