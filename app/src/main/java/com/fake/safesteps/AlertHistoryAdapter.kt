package com.fake.safesteps



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fake.safesteps.databinding.AlertHistoryItemBinding
import com.fake.safesteps.models.EmergencyAlert
import java.text.SimpleDateFormat
import java.util.Locale

class AlertHistoryAdapter(
    private var alerts: List<EmergencyAlert>
) : RecyclerView.Adapter<AlertHistoryAdapter.AlertViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    inner class AlertViewHolder(private val binding: AlertHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: EmergencyAlert) {
            binding.alertTypeText.text = alert.alertType
            binding.alertDateText.text = alert.timestamp?.let { dateFormat.format(it) } ?: "Unknown"
            binding.alertLocationText.text = "Lat: ${alert.latitude}, Lng: ${alert.longitude}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = AlertHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount() = alerts.size

    fun updateAlerts(newAlerts: List<EmergencyAlert>) {
        alerts = newAlerts
        notifyDataSetChanged()
    }
}