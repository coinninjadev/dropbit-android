package com.coinninja.coinkeeper.service.runner

import app.coinninja.cn.libbitcoin.model.TransactionData
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.bitcoin.BroadcastResult
import com.coinninja.coinkeeper.service.client.CurrentState
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONObject
import org.junit.Test
import retrofit2.Response


class BroadcastTransactionRunnerTest {

    private var broadcastResult = BroadcastResult(isSuccess = true)

    private fun createTask(): BroadcastTransactionRunner {
        val task = BroadcastTransactionRunner(mock(), mock(), mock(), mock())
        whenever(task.apiClient.currentState).thenReturn(Response.success<CurrentState>(null))
        whenever(task.application.getString(R.string.transaction_checksum_error)).thenReturn("checksum failed")
        task.broadcastListener = mock()
        whenever(task.broadcastTransactionHelper.broadcast(any())).thenReturn(broadcastResult)
        return task
    }

    @Test
    fun clone_retains_listener() {
        val task = createTask().clone()

        assertThat(task.broadcastListener).isNotNull()
    }

    @Test
    fun notifies_of_broadcast_started() {
        val task = createTask()

        task.doInBackground(mock())

        verify(task.analytics).trackEvent(eq(Analytics.EVENT_BROADCAST_STARTED))
    }

    @Test
    fun flushes_before_ending() {
        val task = createTask()

        task.onPostExecute(broadcastResult)

        verify(task.analytics).flush()
    }

    @Test
    fun logs_broadcast_failure_when_checking_fails() {
        val task = createTask()
        whenever(task.apiClient.currentState).thenReturn(Response.error<CurrentState>(500,
                ResponseBody.create(MediaType.parse("plain/text"), "")))


        task.doInBackground(mock())


        val argumentCaptor = argumentCaptor<JSONObject>()
        verify(task.analytics).trackEvent(eq(Analytics.EVENT_BROADCAST_FAILED), argumentCaptor.capture())

        val properties = argumentCaptor.firstValue
        assertThat(properties.getString(Analytics.EVENT_BROADCAST_JSON_KEY_CHECK_IN_FAIL)).isEqualTo("CheckInFail")
    }

    @Test
    fun progress_stops_when_failure_occurs() {
        val task = createTask()
        whenever(task.apiClient.currentState).thenReturn(Response.error<CurrentState>(500,
                ResponseBody.create(MediaType.parse("plain/text"), "")))

        task.doInBackground(mock())

        verify(task.apiClient).currentState
        assertThat(task.progress).isEqualTo(.33333334f)
    }

    @Test
    fun checks_in_with_coinninja() {
        val task = createTask()

        task.doInBackground(mock())

        verify(task.apiClient).currentState
        assertThat(task.progress).isEqualTo(1f)
    }

    @Test
    fun doInBackground() {
        val task = createTask()
        val transactionData: TransactionData = mock()

        val result = task.doInBackground(transactionData)

        verify(task.broadcastTransactionHelper).broadcast(transactionData)
        assertThat(result).isEqualTo(result)
    }

    @Test
    fun onPostExecute_broadcast_successful() {
        val task = createTask()
        val result: BroadcastResult = BroadcastResult(isSuccess = true)

        task.onPostExecute(result)

        verify(task.broadcastListener)!!.onBroadcastSuccessful(result)
        verify(task.analytics).setUserProperty(Analytics.PROPERTY_HAS_SENT_ADDRESS, true)
    }

    @Test
    fun onPostExecute_broadcastFail() {
        val task = createTask()
        val result: BroadcastResult = BroadcastResult()

        task.onPostExecute(result)

        verify(task.broadcastListener)!!.onBroadcastError(result)
    }

}