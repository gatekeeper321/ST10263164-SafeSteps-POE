package com.fake.safesteps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.fake.safesteps.databinding.ActivityPhotoSharingBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class PhotoSharing : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoSharingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoSharingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        executor = ContextCompat.getMainExecutor(this)

        binding.textView.text = "Welcome ${auth.currentUser?.email ?: "User"}"

        val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        val isBiometricEnabled = sharedPref.getBoolean("biometric_enabled", false)

        if (isBiometricEnabled) {
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> showBiometricPrompt()
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    notifyUser("Biometric unavailable. Logging in without authentication.")
                }
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    notifyUser("Biometric authentication not available due to security restrictions.")
                }
                else -> notifyUser("Cannot use biometrics. Logging in without authentication.")
            }
        } else {
            notifyUser("Biometric login disabled")
        }

        binding.startAuthentication.setOnClickListener {
            val intent = Intent(this, AlertActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showBiometricPrompt() {
        try {
            val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        notifyUser("Biometric authentication cancelled or failed")
                        goBackToLogin()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        notifyUser("Authentication succeeded")

                        val intent = Intent(this@PhotoSharing, AlertActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        notifyUser("Authentication failed")
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock App")
                .setSubtitle("Confirm your identity")
                .setDescription("Use fingerprint to continue")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)

        } catch (e: Exception) {
            notifyUser("Biometric service unavailable. Logging in without authentication.")
        }
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun goBackToLogin() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}