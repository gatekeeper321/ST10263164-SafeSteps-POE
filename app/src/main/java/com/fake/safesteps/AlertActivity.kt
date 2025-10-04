package com.fake.safesteps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.fake.safesteps.databinding.ActivityAlertBinding
import com.fake.safesteps.viewmodels.AlertViewModel
import com.google.android.gms.location.*

class AlertActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlertBinding
    private lateinit var viewModel: AlertViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
    }

    private fun setupObservers() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.emergencyButton.isEnabled = !isLoading
        }

        viewModel.alertCreated.observe(this) { created ->
            if (created) {
                binding.statusText.text = "Emergency alert sent successfully!"
                Toast.makeText(
                    this,
                    "Emergency alert sent to your trusted contacts",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.error.observe(this) { error ->
            binding.statusText.text = "Error: $error"
            Toast.makeText(this, "Failed to send alert: $error", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        binding.emergencyButton.setOnClickListener {
            checkLocationPermissionAndSendAlert()
        }
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
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Use last known location
                    viewModel.createEmergencyAlert(location.latitude, location.longitude)
                } else {
                    // Request a fresh location fix if last known is null
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY, 1000
                    ).setMaxUpdates(1).build()

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                val freshLocation = result.lastLocation
                                if (freshLocation != null) {
                                    viewModel.createEmergencyAlert(
                                        freshLocation.latitude,
                                        freshLocation.longitude
                                    )
                                } else {
                                    Toast.makeText(
                                        this@AlertActivity,
                                        "Still unable to get location. Try again outdoors with GPS on.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        },
                        mainLooper
                    )
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error getting location: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
