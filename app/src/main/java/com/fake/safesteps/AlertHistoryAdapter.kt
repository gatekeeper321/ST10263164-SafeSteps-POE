package com.fake.safesteps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fake.safesteps.databinding.AlertHistoryItemBinding
import com.fake.safesteps.models.EmergencyAlert
import com.fake.safesteps.utils.DateFormatter

/**
 * Enhanced adapter with timeline design and better date formatting
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

            // Set location coordinates
            binding.alertCoordinatesText.text =
                "Lat: ${String.format("%.4f", alert.latitude)}, Lng: ${String.format("%.4f", alert.longitude)}"

            // Set location (for now show coordinates, could be geocoded address)
            binding.alertLocationText.text = "Emergency Location"

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