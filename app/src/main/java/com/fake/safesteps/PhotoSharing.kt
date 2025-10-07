package com.fake.safesteps

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fake.safesteps.databinding.ActivityMainBinding
import com.fake.safesteps.databinding.ActivityPhotoSharingBinding
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.CancellationSignal
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.util.Log

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import androidx.biometric.BiometricPrompt.PromptInfo

class PhotoSharing : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoSharingBinding
    private lateinit var aut : FirebaseAuth
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPhotoSharingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aut = FirebaseAuth.getInstance()
        binding.textView.text = "Welcome ${aut.currentUser!!.email}"
        executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Biometric authentication cancelled or failed")
                    goBackToLogin()  // ⬅️ Send them back to login instead of bypassing
                }



                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser("Authentication succeeded")


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

        val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        val isBiometricEnabled = sharedPref.getBoolean("biometric_enabled", false)

        if (isBiometricEnabled) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            notifyUser("Biometric login disabled")
        }

        binding.startAuthentication.setOnClickListener {
            val sharedPref = getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
            val isBiometricEnabled = sharedPref.getBoolean("biometric_enabled", false)

            if (isBiometricEnabled) {
                Log.d("Biometric", "Starting authentication")
                biometricPrompt.authenticate(promptInfo)
            } else {
                notifyUser("Biometric login disabled")
                // Proceed normally
                val intent = Intent(this, AlertActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun goBackToLogin() {
        aut.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.option,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.SignOut){
            aut.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}