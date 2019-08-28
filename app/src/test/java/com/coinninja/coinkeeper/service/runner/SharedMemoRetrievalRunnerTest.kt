package com.coinninja.coinkeeper.service.runner

import com.coinninja.coinkeeper.cn.transaction.notification.TransactionNotificationMapper
import com.coinninja.coinkeeper.model.db.TransactionNotification
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.TransactionHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Response
import java.util.*

class SharedMemoRetrievalRunnerTest {

    companion object {
        private const val T2_ID: String = "2"
        private const val T1_ID: String = "1"
    }

    var transactions = mutableListOf<TransactionSummary>()

    @After
    fun tearDown() {
        transactions.clear()
    }

    fun setUp(): SharedMemoRetrievalRunner {
        val t1 = mock(TransactionSummary::class.java)
        val t2 = mock(TransactionSummary::class.java)
        transactions.add(t1)
        transactions.add(t2)
        val transactionHelper = mock(TransactionHelper::class.java)
        whenever(transactionHelper.requiringNotificationCheck).thenReturn(transactions)
        whenever(t1.txid).thenReturn(T1_ID)
        whenever(t2.txid).thenReturn(T2_ID)
        val dropbitAccountHelper = mock(DropbitAccountHelper::class.java)
        whenever(dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)

        return SharedMemoRetrievalRunner(
                transactionHelper,
                mock(SignedCoinKeeperApiClient::class.java),
                mock(MessageEncryptor::class.java),
                mock(TransactionNotificationMapper::class.java),
                mock(DaoSessionManager::class.java),
                dropbitAccountHelper
        )
    }

    @Test
    fun handles_no_memo_for_a_successful_transaction_notification_request() {
        val runner = setUp()
        val response: Response<List<CNSharedMemo>> = Response.success(emptyList())
        whenever(runner.signedCoinKeeperApiClient.getTransactionNotification(T1_ID)).thenReturn(response)
        whenever(runner.signedCoinKeeperApiClient.getTransactionNotification(T2_ID)).thenReturn(response)

        runner.run()
    }

    @Test
    fun noop_when_not_verified() {
        val runner = setUp()
        whenever(runner.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)

        runner.run()

        verify(transactions.get(0), times(0)).soughtNotification = true
        verify(transactions.get(1), times(0)).soughtNotification = true

    }

    @Test
    fun updates_transactions_with_memos() {
        val runner = setUp()
        val cnSharedMemo = CNSharedMemo()
        val response = Response.success(Arrays.asList(cnSharedMemo))
        whenever(runner.signedCoinKeeperApiClient.getTransactionNotification(T1_ID)).thenReturn(response)

        val json = "{}"
        val decrypted = "{   \n" +
                "     \"meta\": {\n" +
                "       \"version\": 1\n" +
                "     },  \n" +
                "     \"txid\": \"....\",\n" +
                "     \"info\": {\n" +
                "       \"memo\": \"Here's your 5 dollars \uD83D\uDCB8\",\n" +
                "       \"amount\": 500,\n" +
                "       \"currency\": \"USD\"\n" +
                "     },  \n" +
                "     \"profile\": {\n" +
                "       \"display_name\": \"\", \n" +
                "       \"country_code\": 1,\n" +
                "       \"phone_number\": \"3305551122\",\n" +
                "       \"dropbit_me\": \"\", \n" +
                "       \"avatar\": \"aW5zZXJ0IGF2YXRhciBoZXJlCg==\"\n" +
                "     }   \n" +
                "   }   "

        val response2 = Response.error<List<CNSharedMemo>>(404, ResponseBody.create(MediaType.parse("application/json"),
                json))
        whenever(runner.signedCoinKeeperApiClient.getTransactionNotification(T2_ID)).thenReturn(response2)
        whenever(runner.messageEncryptor.decrypt(cnSharedMemo.address, cnSharedMemo.encrypted_payload)).thenReturn(decrypted)
        val transactionNotification = TransactionNotification()
        whenever(runner.transactionNotificationMapper.toNotification(decrypted)).thenReturn(transactionNotification)
        val transactionNotificationId = 3L
        whenever(runner.daoSessionManager.insert(transactionNotification)).thenReturn(transactionNotificationId)

        runner.run()

        verify(runner.messageEncryptor).decrypt(cnSharedMemo.address, cnSharedMemo.encrypted_payload)
        verify(runner.transactionHelper).requiringNotificationCheck
        verify(runner.signedCoinKeeperApiClient).getTransactionNotification(T1_ID)
        verify(runner.signedCoinKeeperApiClient).getTransactionNotification(T2_ID)

        verify(transactions.get(0)).transactionNotification = transactionNotification
        verify(transactions.get(0)).soughtNotification = true
        verify(transactions.get(0)).update()

        verify(transactions.get(1)).soughtNotification = true
        verify(transactions.get(1)).update()
    }


    @Test
    fun skips_messages_that_are_not_in_expected_format() {
        val runner = setUp()
        val cnSharedMemo = CNSharedMemo()
        val response: Response<List<CNSharedMemo>> = Response.success(listOf(cnSharedMemo))
        whenever(runner.signedCoinKeeperApiClient.getTransactionNotification(T1_ID)).thenReturn(response)

        val json = ""

        val response2: Response<List<CNSharedMemo>> = Response.error<List<CNSharedMemo>>(404, ResponseBody.create(MediaType.parse("application/json"),
                json)) as Response<List<CNSharedMemo>>
        whenever(runner.signedCoinKeeperApiClient.getTransactionNotification(T2_ID)).thenReturn(response2)
        whenever(runner.messageEncryptor.decrypt(cnSharedMemo.address, cnSharedMemo.encrypted_payload)).thenThrow(IndexOutOfBoundsException())

        runner.run()

        verify(runner.messageEncryptor).decrypt(cnSharedMemo.address, cnSharedMemo.encrypted_payload)
        verify(runner.transactionHelper).requiringNotificationCheck
        verify(runner.signedCoinKeeperApiClient).getTransactionNotification(T1_ID)
        verify(runner.signedCoinKeeperApiClient).getTransactionNotification(T2_ID)

        verify(transactions.get(0), times(0)).transactionNotificationId = anyLong()
        verify(transactions.get(0)).soughtNotification = true
        verify(transactions.get(0)).update()

        verify(transactions.get(1), times(0)).transactionNotificationId = anyLong()
        verify(transactions.get(1)).soughtNotification = true
        verify(transactions.get(1)).update()
    }

}