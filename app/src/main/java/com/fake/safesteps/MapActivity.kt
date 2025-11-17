package com.fake.safesteps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.fake.safesteps.databinding.ActivityMapBinding
import com.fake.safesteps.viewmodels.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.text.SimpleDateFormat
import java.util.*

class MapActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var viewModel: MapViewModel
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val markers = mutableMapOf<String, Marker>()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableMyLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupObservers()
        setupClickListeners()
        setupBottomNavigation()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        binding.bottomNavigation.selectedItemId = R.id.map_item
    }

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
                R.id.map_item -> true // Already here
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
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.contactAlerts.observe(this) { alerts ->
            if (::map.isInitialized) {
                updateMapMarkers(alerts)
            }

            binding.emptyStateText.visibility =
                if (alerts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.refreshButton.setOnClickListener {
            viewModel.refreshAlerts()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Customize map
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
        }

        // Enable my location if permission granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Setup marker click listener
        map.setOnInfoWindowClickListener { marker ->
            val alert = marker.tag as? com.fake.safesteps.viewmodels.AlertWithContact
            if (alert != null) {
                openNavigationToAlert(alert)
            }
        }

        // Load alerts
        viewModel.loadContactAlerts()
    }

    private fun enableMyLocation() {
        try {
            map.isMyLocationEnabled = true

            // Move camera to user's location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f))
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMapMarkers(alerts: List<com.fake.safesteps.viewmodels.AlertWithContact>) {
        // Clear existing markers
        markers.values.forEach { it.remove() }
        markers.clear()

        if (alerts.isEmpty()) {
            return
        }

        val boundsBuilder = LatLngBounds.Builder()
        var hasMarkers = false

        // Add marker for each alert
        alerts.forEach { alertWithContact ->
            val alert = alertWithContact.alert
            val position = LatLng(alert.latitude, alert.longitude)

            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("🚨 ${alertWithContact.contactName}")
                    .snippet(formatAlertInfo(alertWithContact))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )

            marker?.tag = alertWithContact
            if (marker != null) {
                markers[alert.id] = marker
                boundsBuilder.include(position)
                hasMarkers = true
            }
        }

        // Zoom to show all markers
        if (hasMarkers) {
            try {
                val bounds = boundsBuilder.build()
                val padding = 150 // pixels
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            } catch (e: Exception) {
                // Ignore if bounds are invalid
            }
        }
    }

    private fun formatAlertInfo(alertWithContact: com.fake.safesteps.viewmodels.AlertWithContact): String {
        val alert = alertWithContact.alert
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val timeStr = alert.timestamp?.let { dateFormat.format(it) } ?: "Unknown time"

        return """
            Type: ${alert.alertType}
            Time: $timeStr
            Tap to navigate
        """.trimIndent()
    }

    private fun openNavigationToAlert(alertWithContact: com.fake.safesteps.viewmodels.AlertWithContact) {
        val alert = alertWithContact.alert
        val uri = Uri.parse("google.navigation:q=${alert.latitude},${alert.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Fallback to web maps
            val webUri = Uri.parse("https://maps.google.com/?q=${alert.latitude},${alert.longitude}")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) {
            viewModel.refreshAlerts()
        }
    }
}