package com.coinninja.coinkeeper.service.tasks

import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import retrofit2.Response

class CNHealthCheckTaskTest {

    private val task: CNHealthCheckTask = CNHealthCheckTask(mock(), mock())

    @Before
    fun setUp() {
        whenever(task.apiClient.checkHealth()).thenReturn(Response.success(JsonObject()))
    }

    @Test
    fun conducts_health_check() {
        val isSuccessful = task.doInBackground()

        verify<CoinKeeperApiClient>(task.apiClient).checkHealth()

        assertTrue(isSuccessful!!)
    }

    @Test
    fun successful_checks_notify_of_success() {
        task.onPostExecute(true)

        verify<CNHealthCheckTask.HealthCheckCallback>(task.callback).onHealthSuccess()
    }

    @Test
    fun failed_checks_inform_callback() {
        task.onPostExecute(false)

        verify<CNHealthCheckTask.HealthCheckCallback>(task.callback).onHealthFail()
    }
}
