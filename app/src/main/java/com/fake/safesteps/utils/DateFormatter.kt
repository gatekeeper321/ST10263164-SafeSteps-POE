package com.fake.safesteps.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utility class for formatting dates in user-friendly ways
 * Reference: Android DateUtils (https://developer.android.com/reference/android/text/format/DateUtils)
 */
object DateFormatter {

    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    /**
     * Format date relative to current time
     * Examples:
     * - "Just now" (< 1 min)
     * - "5 minutes ago" (< 1 hour)
     * - "2 hours ago" (< 24 hours)
     * - "Yesterday at 14:23" (yesterday)
     * - "Mar 15, 2024 at 10:30" (older)
     */
    fun getRelativeTimeString(date: Date?): String {
        if (date == null) return "Unknown time"

        val now = Date()
        val diffMs = now.time - date.time

        // Negative time difference (future date) - shouldn't happen but handle it
        if (diffMs < 0) {
            return fullDateFormat.format(date)
        }

        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)

        return when {
            diffMinutes < 1 -> "Just now"
            diffMinutes < 60 -> "$diffMinutes minute${if (diffMinutes > 1) "s" else ""} ago"
            diffHours < 24 -> "$diffHours hour${if (diffHours > 1) "s" else ""} ago"
            diffDays == 1L -> "Yesterday at ${timeFormat.format(date)}"
            diffDays < 7 -> "$diffDays days ago"
            else -> fullDateFormat.format(date)
        }
    }

    /**
     * Format date for display
     * Example: "Mar 15, 2024"
     */
    fun getFormattedDate(date: Date?): String {
        return if (date != null) {
            dateFormat.format(date)
        } else {
            "Unknown date"
        }
    }

    /**
     * Format time for display
     * Example: "14:23"
     */
    fun getFormattedTime(date: Date?): String {
        return if (date != null) {
            timeFormat.format(date)
        } else {
            "Unknown time"
        }
    }

    /**
     * Get day of week for grouping alerts
     * Returns: "Today", "Yesterday", or "Mar 15, 2024"
     */
    fun getDayHeader(date: Date?): String {
        if (date == null) return "Unknown"

        val now = Calendar.getInstance()
        val alertDate = Calendar.getInstance().apply { time = date }

        return when {
            isSameDay(now, alertDate) -> "Today"
            isYesterday(now, alertDate) -> "Yesterday"
            else -> dateFormat.format(date)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, date)
    }
}