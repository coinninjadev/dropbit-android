package com.coinninja.coinkeeper.service.runner

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.TransactionData
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.bitcoin.BroadcastListener
import com.coinninja.coinkeeper.bitcoin.BroadcastResult
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.util.analytics.Analytics
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.roundToInt

@Mockable
class BroadcastTransactionRunner @Inject constructor(
        internal val application: CoinKeeperApplication,
        internal val broadcastTransactionHelper: BroadcastTransactionHelper,
        internal val apiClient: SignedCoinKeeperApiClient,
        internal val analytics: Analytics
) : SteppedAsyncTask<TransactionData, Int, BroadcastResult>() {

    var broadcastListener: BroadcastListener? = null

    private constructor(application: CoinKeeperApplication,
                broadcastTransactionHelper: BroadcastTransactionHelper,
                apiClient: SignedCoinKeeperApiClient,
                analytics: Analytics,
                broadcastListener: BroadcastListener?): this(application, broadcastTransactionHelper, apiClient, analytics) {

        @Suppress("LeakingThis")
        this.broadcastListener = broadcastListener
    }

    fun clone(): BroadcastTransactionRunner {
        return BroadcastTransactionRunner(application, broadcastTransactionHelper,
                apiClient, analytics, broadcastListener)
    }

    internal override fun getNumberOfPrimarySteps(): Int {
        return NUMBER_OF_STEPS
    }

    override fun reportPrimaryStepCompleted() {
        super.reportPrimaryStepCompleted()
        publishProgress(progress.roundToInt() * 100)
    }

    override fun reportSubStepCompleted(numSubSteps: Int) {
        super.reportSubStepCompleted(numSubSteps)
        publishProgress(progress.roundToInt() * 100)
    }

    public override fun doInBackground(vararg transactions: TransactionData): BroadcastResult? {
        analytics.trackEvent(Analytics.EVENT_BROADCAST_STARTED)
        if (transactions.isEmpty()) return null
        val transactionData = transactions[0]

        return if (checkIn()) {
            broadcastTransaction(transactionData)
        } else {
            reportCheckingFailed()
            BroadcastResult(message = application.getString(R.string.transaction_checksum_error))
        }
    }

    public override fun onPostExecute(broadcastResult: BroadcastResult) {
        if (broadcastResult.isSuccess) {
            broadcastListener?.onBroadcastSuccessful(broadcastResult)
            analytics.setUserProperty(Analytics.PROPERTY_HAS_SENT_ADDRESS, true)
        } else {
            broadcastListener?.onBroadcastError(broadcastResult)
        }
        analytics.flush()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        values[0]?.let { broadcastListener?.onBroadcastProgress(it) }
    }

    private fun checkIn(): Boolean {
        reportSubStepCompleted(3)
        val currentState = apiClient.currentState
        reportSubStepCompleted(3)
        val isSuccess = currentState.isSuccessful
        if (isSuccess) reportPrimaryStepCompleted()
        return isSuccess
    }

    private fun broadcastTransaction(transactionData: TransactionData): BroadcastResult {
        reportSubStepCompleted(2)
        val result = broadcastTransactionHelper.broadcast(transactionData)
        reportPrimaryStepCompleted()
        return result
    }

    private fun reportCheckingFailed() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put(Analytics.EVENT_BROADCAST_JSON_KEY_CHECK_IN_FAIL, Analytics.EVENT_BROADCAST_CHECK_IN_FAIL)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        analytics.trackEvent(Analytics.EVENT_BROADCAST_FAILED, jsonObject)
    }

    companion object {
        private const val NUMBER_OF_STEPS = 2
    }
}
