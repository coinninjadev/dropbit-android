package com.coinninja.coinkeeper.model.helpers

import com.coinninja.coinkeeper.model.db.FundingStat
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.service.client.model.ScriptPubKey
import com.coinninja.coinkeeper.service.client.model.VIn
import com.coinninja.coinkeeper.service.client.model.VOut
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.greenrobot.greendao.query.QueryBuilder
import org.junit.Test

class FundingStatHelperTest {

    private fun createHelper(): FundingStatHelper = FundingStatHelper(mock())

    @Test
    fun updates_existing_input_with_current_details() {
        val helper = createHelper()
        val transaction = mock<TransactionSummary>()
        val input = VIn(
                txid = "--funding-txid--",
                previousOutput = VOut(
                        value = 1000,
                        index = 1,
                        scriptPubKey = ScriptPubKey(
                                addresses = arrayOf("--address-1--")
                        )

                )
        )
        val fundingStat = mock<FundingStat>()
        whenever(fundingStat.id).thenReturn(1)
        val queryBuilder: QueryBuilder<FundingStat> = mock()
        whenever(helper.daoSessionManager.fundingStatDao).thenReturn(mock())
        whenever(helper.daoSessionManager.fundingStatDao.queryBuilder()).thenReturn(queryBuilder)
        whenever(queryBuilder.where(any(), any(), any(), any(), any())).thenReturn(queryBuilder)
        whenever(queryBuilder.limit(1)).thenReturn(queryBuilder)
        whenever(queryBuilder.unique()).thenReturn(fundingStat)

        helper.getOrCreateFundingStat(transaction, input)

        val ordered = inOrder(fundingStat)
        ordered.verify(fundingStat).addr = "--address-1--"
        ordered.verify(fundingStat).position = 1
        ordered.verify(fundingStat).transaction = transaction
        ordered.verify(fundingStat).fundedTransaction = "--funding-txid--"
        ordered.verify(fundingStat).value = 1000
        ordered.verify(fundingStat).update()
    }

    @Test
    fun inserts_new_input_with_details() {
        val helper = createHelper()
        val transaction = mock<TransactionSummary>()
        val input = VIn(
                txid = "--funding-txid--",
                previousOutput = VOut(
                        value = 1000,
                        index = 1,
                        scriptPubKey = ScriptPubKey(
                                addresses = arrayOf("--address-1--")
                        )

                )
        )
        val fundingStat = mock<FundingStat>()
        whenever(fundingStat.id).thenReturn(0)
        val queryBuilder: QueryBuilder<FundingStat> = mock()
        whenever(helper.daoSessionManager.fundingStatDao).thenReturn(mock())
        whenever(helper.daoSessionManager.fundingStatDao.queryBuilder()).thenReturn(queryBuilder)
        whenever(queryBuilder.where(any(), any(), any(), any(), any())).thenReturn(queryBuilder)
        whenever(queryBuilder.limit(1)).thenReturn(queryBuilder)
        whenever(queryBuilder.unique()).thenReturn(null)
        whenever(helper.daoSessionManager.newFundingStat()).thenReturn(fundingStat)

        helper.getOrCreateFundingStat(transaction, input)

        val ordered = inOrder(fundingStat, helper.daoSessionManager)
        ordered.verify(helper.daoSessionManager).newFundingStat()
        ordered.verify(fundingStat).addr = "--address-1--"
        ordered.verify(fundingStat).position = 1
        ordered.verify(fundingStat).transaction = transaction
        ordered.verify(fundingStat).fundedTransaction = "--funding-txid--"
        ordered.verify(fundingStat).value = 1000
        ordered.verify(helper.daoSessionManager).insert(fundingStat)
    }
}