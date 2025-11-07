package com.fake.safesteps.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fake.safesteps.AlertActivity
import com.fake.safesteps.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service for push notifications
 * Reference: Firebase Cloud Messaging (https://firebase.google.com/docs/cloud-messaging/android/client)
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")

        // Send token to your server if needed
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")
            showNotification(it.title ?: "SafeSteps Alert", it.body ?: "")
        }

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val alertType = data["alertType"]
        val userName = data["userName"]
        val latitude = data["latitude"]
        val longitude = data["longitude"]

        when (alertType) {
            "EMERGENCY" -> {
                showEmergencyNotification(
                    userName ?: "A contact",
                    latitude ?: "Unknown",
                    longitude ?: "Unknown"
                )
            }
            "GEOFENCE_ENTRY" -> {
                showGeofenceNotification(userName ?: "A contact", "entered")
            }
            "GEOFENCE_EXIT" -> {
                showGeofenceNotification(userName ?: "A contact", "exited")
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "safesteps_alerts"

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SafeSteps Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency alerts from SafeSteps"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent for notification tap
        val intent = Intent(this, AlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.alert_circle)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showEmergencyNotification(userName: String, lat: String, lng: String) {
        val title = "🚨 EMERGENCY ALERT"
        val body = "$userName needs help!\nLocation: $lat, $lng"
        showNotification(title, body)
    }

    private fun showGeofenceNotification(userName: String, action: String) {
        val title = "📍 Location Update"
        val body = "$userName has $action a geofence"
        showNotification(title, body)
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Send token to your backend server
        // Store in Firebase Firestore under user's document
        Log.d(TAG, "Token should be sent to server: $token")
    }

    companion object {
        private const val TAG = "FCMService"
        private const val NOTIFICATION_ID = 1001
    }
}

/**
 * Helper class to send notifications to specific users
 */
object NotificationSender {
    private const val TAG = "NotificationSender"

    /**
     * Send emergency alert notification to trusted contacts
     * In production, this would call your backend API which uses FCM Admin SDK
     */
    fun sendEmergencyAlert(
        contactTokens: List<String>,
        userName: String,
        latitude: Double,
        longitude: Double
    ) {
        // This is a placeholder - actual implementation requires backend server
        Log.d(TAG, "Would send emergency alert to ${contactTokens.size} contacts")
        Log.d(TAG, "Alert from: $userName at $latitude, $longitude")

        // In production, you would call your backend API:
        // POST /api/send-notification
        // Body: {
        //   "tokens": contactTokens,
        //   "data": {
        //     "alertType": "EMERGENCY",
        //     "userName": userName,
        //     "latitude": latitude.toString(),
        //     "longitude": longitude.toString()
        //   }
        // }
    }
}