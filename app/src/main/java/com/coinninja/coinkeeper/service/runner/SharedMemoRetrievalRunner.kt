package com.coinninja.coinkeeper.service.runner

import android.util.Log
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.transaction.notification.TransactionNotificationMapper
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.TransactionHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor
import retrofit2.Response
import javax.inject.Inject

@Mockable
class SharedMemoRetrievalRunner @Inject constructor(
        internal val transactionHelper: TransactionHelper,
        internal val signedCoinKeeperApiClient: SignedCoinKeeperApiClient,
        internal val messageEncryptor: MessageEncryptor,
        internal val transactionNotificationMapper: TransactionNotificationMapper,
        internal val daoSessionManager: DaoSessionManager,
        internal val dropbitAccountHelper: DropbitAccountHelper
) : Runnable {

    override fun run() {
        Log.d(TAG, "|--------- Retrieving Shared Memos for Transactions --")
        if (!dropbitAccountHelper.hasVerifiedAccount) {
            return
        }

        val transactionSummaries = transactionHelper.requiringNotificationCheck
        for (transaction in transactionSummaries) {
            val response = signedCoinKeeperApiClient.getTransactionNotification(transaction.txid)
            if (response.isSuccessful && (response.body() as List<CNSharedMemo>).size > 0) {
                handleMemoResponse(transaction, response)
            }

            transaction.soughtNotification = true
            transaction.update()
        }
    }

    private fun handleMemoResponse(transaction: TransactionSummary, response: Response<*>) {
        try {
            val memo = (response.body() as List<CNSharedMemo>)[0]
            val decrypted = messageEncryptor.decrypt(memo.address, memo.encrypted_payload)
            if (decrypted.isNullOrEmpty()) return
            transaction.transactionNotification = transactionNotificationMapper.toNotification(decrypted)
        } catch (ex: Exception) {
        }
    }

    companion object {
        private val TAG = SharedMemoRetrievalRunner::class.java.simpleName
    }
}
