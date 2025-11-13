package com.fake.safesteps.notifications

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Helper class to get and manage FCM tokens
 */
object FCMTokenHelper {
    private const val TAG = "FCMTokenHelper"

    /**
     * Get current FCM token and copy to clipboard
     * Call this from your activity to get the token for testing
     */
    fun getAndCopyToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                Toast.makeText(
                    context,
                    "Failed to get FCM token: ${task.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log the token
            Log.d(TAG, "FCM Token: $token")

            // Copy to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("FCM Token", token)
            clipboard.setPrimaryClip(clip)

            // Show success message
            Toast.makeText(
                context,
                "FCM Token copied to clipboard!\nToken: ${token.take(20)}...",
                Toast.LENGTH_LONG
            ).show()

            // Also save to SharedPreferences for later use
            saveFCMToken(context, token)
        }
    }

    /**
     * Save FCM token to SharedPreferences
     */
    private fun saveFCMToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        Log.d(TAG, "FCM token saved to SharedPreferences")
    }

    /**
     * Get saved FCM token from SharedPreferences
     */
    fun getSavedToken(context: Context): String? {
        val prefs = context.getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        return prefs.getString("fcm_token", null)
    }

    /**
     * Display saved token
     */
    fun showSavedToken(context: Context) {
        val token = getSavedToken(context)
        if (token != null) {
            Toast.makeText(
                context,
                "Saved FCM Token: ${token.take(30)}...",
                Toast.LENGTH_LONG
            ).show()
            Log.d(TAG, "Saved FCM Token: $token")
        } else {
            Toast.makeText(
                context,
                "No saved FCM token found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Subscribe to a topic for testing
     */
    fun subscribeToTopic(topic: String, context: Context) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed to $topic"
                if (!task.isSuccessful) {
                    msg = "Failed to subscribe to $topic"
                }
                Log.d(TAG, msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Unsubscribe from a topic
     */
    fun unsubscribeFromTopic(topic: String, context: Context) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Unsubscribed from $topic"
                if (!task.isSuccessful) {
                    msg = "Failed to unsubscribe from $topic"
                }
                Log.d(TAG, msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
    }
}