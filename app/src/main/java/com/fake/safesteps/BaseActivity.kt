package com.fake.safesteps

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * Base activity that handles locale changes for all activities
 * All activities should extend this instead of AppCompatActivity
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("SafeStepsPrefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString("language_code", "en") ?: "en"

        val locale = java.util.Locale(langCode)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val context = newBase.createConfigurationContext(config)

        super.attachBaseContext(context)
    }
}