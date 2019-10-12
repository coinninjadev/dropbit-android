package com.coinninja.coinkeeper.bitcoin

import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.util.ErrorLoggingUtil
import com.coinninja.coinkeeper.util.analytics.Analytics
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@Mockable
class BroadcastTransactionHelper @Inject internal constructor(
        internal val hdWallet: HDWalletWrapper,
        internal val transactionBroadcaster: TransactionBroadcaster,
        internal val analytics: Analytics,
        internal val errorLoggingUtil: ErrorLoggingUtil
) {

    fun broadcast(transactionData: TransactionData): BroadcastResult {
        val transaction = hdWallet.transactionFrom(transactionData)

        val broadcastResult = if (transactionData.isValid()) {
            transactionBroadcaster.broadcast(transaction)
        } else {
            BroadcastResult(transaction = transaction)

        }
        reportBroadcastResult(broadcastResult)
        return broadcastResult
    }


    private fun reportBroadcastResult(broadcastResult: BroadcastResult) {
        val properties = JSONObject()
        try {
            properties.put(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE, broadcastResult.responseCode)
            properties.put(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG, broadcastResult.message)
        } catch (e: JSONException) {
            errorLoggingUtil.logError(e)
        }

        if (broadcastResult.isSuccess) {
            analytics.trackEvent(Analytics.EVENT_BROADCAST_COMPLETE, properties)
        } else {
            analytics.trackEvent(Analytics.EVENT_BROADCAST_FAILED, properties)
        }
    }
}

