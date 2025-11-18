package com.fake.safesteps.logic

import com.fake.safesteps.repository.AlertRepository
import com.fake.safesteps.sync.SyncManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class AlertLogicTest {

    private val repository = mockk<AlertRepository>()
    private val syncManager = mockk<SyncManager>()

    private val logic = AlertLogic(repository, syncManager)

    @Test
    fun `createAlert returns success when online`() = runBlocking {
        coEvery { syncManager.isNetworkAvailable() } returns true
        coEvery { repository.createAlert(1.0, 2.0) } returns Result.success("alert123")

        val result = logic.createAlert(1.0, 2.0)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals("alert123", result.getOrNull())
    }

    @Test
    fun `createAlert returns success when offline`() = runBlocking {
        coEvery { syncManager.isNetworkAvailable() } returns false
        coEvery { syncManager.saveAlertLocally(1.0, 2.0, "EMERGENCY") } returns "offlineAlert1"

        val result = logic.createAlert(1.0, 2.0)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals("offlineAlert1", result.getOrNull())
    }
}