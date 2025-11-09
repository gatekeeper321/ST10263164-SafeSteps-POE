package com.fake.safesteps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fake.safesteps.databinding.ActivityMainBinding
import com.fake.safesteps.repository.FCMTokenRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val fcmTokenRepo = FCMTokenRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        applySavedLocale()
        setContentView(binding.root)


        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Already logged in → save FCM token and go to PhotoSharing
            saveFCMTokenAndProceed()
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Welcome: ${auth.currentUser?.email}",
                            Toast.LENGTH_LONG
                        ).show()

                        // Save FCM token after successful login
                        saveFCMTokenAndProceed()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.btnSkip.setOnClickListener {
            startActivity(Intent(this, AlertActivity::class.java))
            finish()
        }
    }

    /**
     * Get FCM token and save it to Firestore
     * This allows the user to receive notifications from their contacts
     */
    private fun saveFCMTokenAndProceed() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("MainActivity", "FCM Token: $fcmToken")

                // Save token to Firestore
                lifecycleScope.launch {
                    fcmTokenRepo.saveUserFCMToken(fcmToken)
                        .onSuccess {
                            Log.d("MainActivity", "FCM token saved to Firestore")
                        }
                        .onFailure { e ->
                            Log.e("MainActivity", "Failed to save FCM token", e)
                        }
                }

                // Proceed to next screen
                val intent = Intent(this, PhotoSharing::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.e("MainActivity", "Failed to get FCM token", task.exception)
                // Still proceed even if token fails
                val intent = Intent(this, PhotoSharing::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun applySavedLocale() {
        val sharedPref = getSharedPreferences("SafeStepsPrefs", MODE_PRIVATE)
        val selectedLanguage = sharedPref.getString("selected_language", "English")

        val localeCode = when (selectedLanguage) {
            "Afrikaans" -> "af"
            "isiZulu" -> "zu"
            else -> "en"
        }

        val config = resources.configuration
        val localeObj = java.util.Locale(localeCode)
        java.util.Locale.setDefault(localeObj)
        config.setLocale(localeObj)
        config.setLayoutDirection(localeObj)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

}