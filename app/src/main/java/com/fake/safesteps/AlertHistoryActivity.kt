package com.fake.safesteps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fake.safesteps.databinding.ActivityAlertHistoryBinding
import com.fake.safesteps.viewmodels.AlertViewModel

class AlertHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityAlertHistoryBinding
    private lateinit var viewModel: AlertViewModel
    private lateinit var adapter: AlertHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AlertViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupPullToRefresh()
        setupBottomNavigation()

        // Load initial data
        viewModel.loadAllAlerts()

        // Hide system navigation
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        // Set selected nav item
        binding.bottomNavigation.selectedItemId = R.id.alert_history_item
    }

    private fun setupRecyclerView() {
        adapter = AlertHistoryAdapter(emptyList()) { alert ->
            // Handle alert click - could open map or details
            Toast.makeText(
                this,
                "Alert from ${com.fake.safesteps.utils.DateFormatter.getRelativeTimeString(alert.timestamp)}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.alertHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AlertHistoryActivity)
            adapter = this@AlertHistoryActivity.adapter

            // Add item spacing
            val spacing = resources.getDimensionPixelSize(R.dimen.alert_item_spacing)
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    context,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                ).apply {
                    // Optional: Add custom divider drawable if needed
                }
            )
        }
    }

    private fun setupPullToRefresh() {
        binding.swipeRefreshLayout.setColorSchemeColors(
            getColor(R.color.safe_button),
            getColor(R.color.safe_nav),
            getColor(R.color.emergency_red)
        )

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshAlerts()
        }
    }

    private fun refreshAlerts() {
        viewModel.loadAllAlerts()

        // Stop refresh animation after a delay (will be stopped in observer too)
        binding.swipeRefreshLayout.postDelayed({
            binding.swipeRefreshLayout.isRefreshing = false
        }, 1500)
    }

    private fun setupObservers() {
        viewModel.userAlerts.observe(this) { alerts ->
            // Stop refreshing
            binding.swipeRefreshLayout.isRefreshing = false

            if (alerts.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.alertHistoryRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.alertHistoryRecyclerView.visibility = View.VISIBLE
                adapter.updateAlerts(alerts)
            }
        }

        viewModel.error.observe(this) { error ->
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        }
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
                R.id.map_item -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.alert_history_item -> true // Already here
                else -> false
            }
        }
    }
}