package com.fake.safesteps



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fake.safesteps.AlertHistoryAdapter
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

        viewModel.loadAllAlerts()

        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        binding.bottomNavigation.selectedItemId = R.id.alert_history_item
        setupBottomNavigation()
    }

    //bottom nav (copy paste to every activity)
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
                    notifyUser("Map coming soon")
                    true
                }

                R.id.alert_history_item -> {
                    startActivity(Intent(this, AlertHistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AlertHistoryAdapter(emptyList())
        binding.alertHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AlertHistoryActivity)
            adapter = this@AlertHistoryActivity.adapter
        }
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
        viewModel.userAlerts.observe(this) { alerts ->
            if (alerts.isEmpty()) {
                binding.emptyText.visibility = View.VISIBLE
                binding.alertHistoryRecyclerView.visibility = View.GONE
            } else {
                binding.emptyText.visibility = View.GONE
                binding.alertHistoryRecyclerView.visibility = View.VISIBLE
                adapter.updateAlerts(alerts)
            }
        }
    }
}