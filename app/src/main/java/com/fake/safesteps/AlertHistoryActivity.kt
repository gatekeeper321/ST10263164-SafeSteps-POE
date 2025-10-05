package com.fake.safesteps



import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fake.safesteps.AlertHistoryAdapter
import com.fake.safesteps.databinding.ActivityAlertHistoryBinding
import com.fake.safesteps.viewmodels.AlertViewModel

class AlertHistoryActivity : AppCompatActivity() {
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
    }

    private fun setupRecyclerView() {
        adapter = AlertHistoryAdapter(emptyList())
        binding.alertHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AlertHistoryActivity)
            adapter = this@AlertHistoryActivity.adapter
        }
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