package com.fake.safesteps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fake.safesteps.databinding.ActivityRegisterBinding
import com.fake.safesteps.repository.FCMTokenRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val fcmTokenRepo = FCMTokenRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        auth = FirebaseAuth.getInstance()

        binding.btnSignIn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Registration successful! Welcome!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Save FCM token after successful registration
                        saveFCMTokenAndProceed()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }

        binding.btnSkip.setOnClickListener {
            val intent = Intent(this, AlertActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Get FCM token and save it to Firestore
     * This allows the new user to receive notifications from their contacts
     */
    private fun saveFCMTokenAndProceed() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("RegisterActivity", "FCM Token: $fcmToken")

                // Save token to Firestore
                lifecycleScope.launch {
                    fcmTokenRepo.saveUserFCMToken(fcmToken)
                        .onSuccess {
                            Log.d("RegisterActivity", "FCM token saved to Firestore")
                        }
                        .onFailure { e ->
                            Log.e("RegisterActivity", "Failed to save FCM token", e)
                        }
                }

                // Proceed to login
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.e("RegisterActivity", "Failed to get FCM token", task.exception)
                // Still proceed even if token fails
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}