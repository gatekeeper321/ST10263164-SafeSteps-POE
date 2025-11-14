package com.fake.safesteps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.fake.safesteps.databinding.ActivityAlertBinding
import com.fake.safesteps.repository.ContactRepository
import com.fake.safesteps.viewmodels.AlertViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

/**
 * Enhanced Alert Activity with animations and better UX
 * Reference: Material Design Motion (https://material.io/design/motion)
 */
class AlertActivity : BaseActivity() {
    private lateinit var binding: ActivityAlertBinding
    private lateinit var viewModel: AlertViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val contactRepository = ContactRepository()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (isLocationEnabled()) {
                sendEmergencyAlert()
            } else {
                promptEnableLocation()
            }
        } else {
            Toast.makeText(
                this,
                "Location permission is required for emergency alerts",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AlertViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupObservers()
        setupClickListeners()
        loadContactCount()
        startPulseAnimation()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        binding.bottomNavigation.selectedItemId = R.id.alert_item
        setupBottomNavigation()
    }

    /**
     * Start pulsing animation on emergency button and glow ring
     */
    private fun startPulseAnimation() {
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        binding.emergencyButton.startAnimation(pulseAnimation)
        binding.glowRing.startAnimation(pulseAnimation)
    }

    /**
     * Stop pulsing animation
     */
    private fun stopPulseAnimation() {
        binding.emergencyButton.clearAnimation()
        binding.glowRing.clearAnimation()
    }

    /**
     * Load contact count and display in card
     */
    private fun loadContactCount() {
        lifecycleScope.launch {
            try {
                val result = contactRepository.getTrustedContacts()
                result.onSuccess { contacts ->
                    val count = contacts.size
                    if (count > 0) {
                        binding.contactCountCard.visibility = View.VISIBLE
                        binding.contactCountText.text = "$count contact${if (count == 1) "" else "s"} will be notified"
                    } else {
                        binding.contactCountCard.visibility = View.GONE
                    }
                }.onFailure {
                    binding.contactCountCard.visibility = View.GONE
                }
            } catch (e: Exception) {
                binding.contactCountCard.visibility = View.GONE
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.alert_item -> true // Already here
                R.id.settings_item -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
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

    private fun setupObservers() {
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                // Show loading state
                stopPulseAnimation()
                binding.statusCard.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.successIcon.visibility = View.GONE
                binding.statusText.text = "Sending emergency alert..."
                binding.emergencyButton.isEnabled = false
                binding.emergencyButton.alpha = 0.5f
            } else {
                // Hide loading state
                binding.progressBar.visibility = View.GONE
                binding.emergencyButton.isEnabled = true
                binding.emergencyButton.alpha = 1.0f
                startPulseAnimation()
            }
        }

        viewModel.alertCreated.observe(this) { created ->
            if (created) {
                // Show success state
                binding.statusCard.visibility = View.VISIBLE
                binding.successIcon.visibility = View.VISIBLE
                binding.statusText.text = "✓ Emergency alert sent successfully!"
                binding.statusText.setTextColor(getColor(R.color.safe_green))

                Toast.makeText(
                    this,
                    "Emergency alert sent to your trusted contacts",
                    Toast.LENGTH_LONG
                ).show()

                // Hide success message after 3 seconds
                binding.statusCard.postDelayed({
                    binding.statusCard.visibility = View.GONE
                    binding.statusText.setTextColor(getColor(R.color.text_primary))
                }, 3000)
            }
        }

        viewModel.error.observe(this) { error ->
            binding.statusCard.visibility = View.VISIBLE
            binding.successIcon.visibility = View.GONE
            binding.statusText.text = "⚠ Error: $error"
            binding.statusText.setTextColor(getColor(R.color.emergency_red))
            Toast.makeText(this, "Failed to send alert: $error", Toast.LENGTH_LONG).show()

            // Hide error message after 3 seconds
            binding.statusCard.postDelayed({
                binding.statusCard.visibility = View.GONE
                binding.statusText.setTextColor(getColor(R.color.text_primary))
            }, 3000)
        }
    }

    private fun setupClickListeners() {
        binding.emergencyButton.setOnClickListener {
            // Haptic feedback
            it.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)

            checkLocationPermissionAndSendAlert()
        }
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkLocationPermissionAndSendAlert() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (isLocationEnabled()) {
                    sendEmergencyAlert()
                } else {
                    promptEnableLocation()
                }
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptEnableLocation() {
        Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show()
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun sendEmergencyAlert() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        binding.statusCard.visibility = View.VISIBLE
        binding.statusText.text = "Getting your location..."

        // Always request a fresh location for emergency alerts
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000 // 5 second interval for better accuracy
        )
            .setMaxUpdates(1) // Only need one update
            .setMinUpdateIntervalMillis(1000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    if (location != null) {
                        android.util.Log.d("AlertActivity",
                            "Fresh location received: lat=${location.latitude}, lng=${location.longitude}")

                        binding.statusText.text = "Sending alert from your location..."
                        viewModel.createEmergencyAlert(
                            location.latitude,
                            location.longitude
                        )
                    } else {
                        android.util.Log.w("AlertActivity", "Fresh location is null, trying last known")

                        // Fallback to last known location if fresh location fails
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastKnown ->
                                if (lastKnown != null) {
                                    android.util.Log.d("AlertActivity",
                                        "Last known location: lat=${lastKnown.latitude}, lng=${lastKnown.longitude}")

                                    binding.statusText.text = "Sending alert (using last known location)..."
                                    viewModel.createEmergencyAlert(
                                        lastKnown.latitude,
                                        lastKnown.longitude
                                    )
                                } else {
                                    android.util.Log.e("AlertActivity", "Both fresh and last known location are null")
                                    binding.statusText.text = "Unable to get location"
                                    Toast.makeText(
                                        this@AlertActivity,
                                        "Unable to get location. Please ensure GPS is enabled and try again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } catch (e: SecurityException) {
                            android.util.Log.e("AlertActivity", "Security exception getting location", e)
                        }
                    }
                    // Clean up location updates
                    fusedLocationClient.removeLocationUpdates(this)
                }
            },
            mainLooper
        )
    }

    override fun onResume() {
        super.onResume()

        // Reload contact count
        loadContactCount()

        // Check network status
        viewModel.checkNetworkStatus()

        // Observe offline mode
        viewModel.offlineMode.observe(this) { isOffline ->
            if (isOffline) {
                Toast.makeText(
                    this,
                    "You are offline. Data will sync when online.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Try to sync offline data
                viewModel.syncOfflineData()
            }
        }

        // Observe sync status
        viewModel.syncStatus.observe(this) { status ->
            if (status.isNotEmpty()) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop animations to save battery
        stopPulseAnimation()
    }
}