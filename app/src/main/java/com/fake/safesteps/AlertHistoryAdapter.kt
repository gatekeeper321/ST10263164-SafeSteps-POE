package com.fake.safesteps

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fake.safesteps.databinding.AlertHistoryItemBinding
import com.fake.safesteps.models.EmergencyAlert
import com.fake.safesteps.utils.DateFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Enhanced adapter with timeline design, geocoding, and better date formatting
 */
class AlertHistoryAdapter(
    private var alerts: List<EmergencyAlert>,
    private val onAlertClick: ((EmergencyAlert) -> Unit)? = null
) : RecyclerView.Adapter<AlertHistoryAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(private val binding: AlertHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: EmergencyAlert, position: Int) {
            // Set relative time
            binding.alertTimeText.text = DateFormatter.getRelativeTimeString(alert.timestamp)

            // Set coordinates (always show as fallback)
            binding.alertCoordinatesText.text =
                "Lat: ${String.format("%.4f", alert.latitude)}, Lng: ${String.format("%.4f", alert.longitude)}"

            // Try to geocode the address
            geocodeLocation(alert.latitude, alert.longitude)

            // Configure alert type badge and dot color
            when (alert.alertType) {
                "EMERGENCY" -> {
                    binding.alertTypeBadge.text = "🚨 EMERGENCY"
                    binding.alertTypeBadge.setBackgroundResource(R.drawable.badge_emergency)
                    binding.alertTypeBadge.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.emergency_red_dark)
                    )
                    setTimelineDotColor(R.color.timeline_dot_emergency)
                }
                "GEOFENCE_ENTRY" -> {
                    binding.alertTypeBadge.text = "📍 GEOFENCE ENTRY"
                    binding.alertTypeBadge.setBackgroundResource(R.drawable.badge_geofence)
                    binding.alertTypeBadge.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.warning_amber)
                    )
                    setTimelineDotColor(R.color.timeline_dot_geofence)
                }
                "GEOFENCE_EXIT" -> {
                    binding.alertTypeBadge.text = "📍 GEOFENCE EXIT"
                    binding.alertTypeBadge.setBackgroundResource(R.drawable.badge_geofence)
                    binding.alertTypeBadge.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.warning_amber)
                    )
                    setTimelineDotColor(R.color.timeline_dot_geofence)
                }
                else -> {
                    binding.alertTypeBadge.text = alert.alertType
                    binding.alertTypeBadge.setBackgroundResource(R.drawable.badge_emergency)
                    setTimelineDotColor(R.color.timeline_dot_emergency)
                }
            }

            // Show resolved status if not active
            if (!alert.isActive) {
                binding.alertStatusText.visibility = View.VISIBLE
                binding.alertStatusText.text = "✓ Resolved"
                setTimelineDotColor(R.color.timeline_dot_resolved)
            } else {
                binding.alertStatusText.visibility = View.GONE
            }

            // Handle timeline line visibility (hide for last item)
            if (position == alerts.size - 1) {
                binding.timelineLine.visibility = View.INVISIBLE
            } else {
                binding.timelineLine.visibility = View.VISIBLE
            }

            // Click listener
            binding.alertCard.setOnClickListener {
                onAlertClick?.invoke(alert)
            }
        }

        /**
         * Geocode coordinates to readable address
         * Reference: Android Geocoder (https://developer.android.com/reference/android/location/Geocoder)
         */
        private fun geocodeLocation(latitude: Double, longitude: Double) {
            // Show loading state
            binding.alertLocationText.text = "Loading address..."

            // Run geocoding in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val geocoder = Geocoder(binding.root.context, Locale.getDefault())

                    // Check if Geocoder is available
                    if (!Geocoder.isPresent()) {
                        withContext(Dispatchers.Main) {
                            binding.alertLocationText.text = "Emergency Location"
                        }
                        return@launch
                    }

                    // Get address from coordinates
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                    withContext(Dispatchers.Main) {
                        if (addresses != null && addresses.isNotEmpty()) {
                            val address = addresses[0]

                            // Build readable address
                            val addressLine = buildString {
                                // Street address
                                if (!address.thoroughfare.isNullOrEmpty()) {
                                    append(address.thoroughfare)
                                    if (!address.subThoroughfare.isNullOrEmpty()) {
                                        append(" ${address.subThoroughfare}")
                                    }
                                    append(", ")
                                }

                                // City
                                if (!address.locality.isNullOrEmpty()) {
                                    append(address.locality)
                                } else if (!address.subAdminArea.isNullOrEmpty()) {
                                    append(address.subAdminArea)
                                }

                                // Country (only if not South Africa, to save space)
                                if (!address.countryCode.isNullOrEmpty() && address.countryCode != "ZA") {
                                    append(", ${address.countryCode}")
                                }
                            }

                            // Set the address or fallback
                            binding.alertLocationText.text = if (addressLine.isNotEmpty()) {
                                addressLine
                            } else {
                                "Emergency Location"
                            }
                        } else {
                            // No address found
                            binding.alertLocationText.text = "Emergency Location"
                        }
                    }
                } catch (e: Exception) {
                    // Geocoding failed
                    withContext(Dispatchers.Main) {
                        binding.alertLocationText.text = "Emergency Location"
                    }
                }
            }
        }

        private fun setTimelineDotColor(colorRes: Int) {
            binding.timelineDot.setBackgroundResource(R.drawable.timeline_dot)
            binding.timelineDot.backgroundTintList =
                ContextCompat.getColorStateList(binding.root.context, colorRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = AlertHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position], position)
    }

    override fun getItemCount() = alerts.size

    fun updateAlerts(newAlerts: List<EmergencyAlert>) {
        alerts = newAlerts
        notifyDataSetChanged()
    }
}