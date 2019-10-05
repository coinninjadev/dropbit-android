package com.coinninja.coinkeeper.cn.transaction

import app.coinninja.cn.persistance.model.LightningInvoice
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class LightningWithdrawalLinkerTest {

    @Test
    fun marks_transactions_as_lightning_withdraws() {
        val lightningWithdrawalLinker = LightningWithdrawalLinker(mock(), mock())
        val transactionSummary: TransactionSummary = mock()
        val invoice = LightningInvoice(serverId = "--txid--")

        whenever(lightningWithdrawalLinker.thunderDomeRepository.withdrawsFromAccount)
                .thenReturn(arrayOf(invoice))
        whenever(lightningWithdrawalLinker.transactionQueryManager
                .transactionByTxid("--txid--")).thenReturn(transactionSummary)

        lightningWithdrawalLinker.linkWithdraws()

        val ordered = inOrder(transactionSummary)
        ordered.verify(transactionSummary).isLightningWithdraw = true
        ordered.verify(transactionSummary).update()
    }


}