package com.coinninja.coinkeeper.model.helpers

import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.TransactionData
import com.coinninja.bindings.UnspentTransactionOutput
import com.coinninja.coinkeeper.model.db.Address
import com.coinninja.coinkeeper.model.db.FundingStat
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.service.client.model.ScriptPubKey
import com.coinninja.coinkeeper.service.client.model.VIn
import com.coinninja.coinkeeper.service.client.model.VOut
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.greenrobot.greendao.query.QueryBuilder
import org.junit.Test

class FundingStatHelperTest {

    private fun createHelper(): FundingStatHelper = FundingStatHelper(mock(), mock())

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

    @Test
    fun creates_inputs_for_provided_transaction() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val transactionData = TransactionData(
                utxos = arrayOf(
                        UnspentTransactionOutput("--proof-txid-1--", 1, 100000,
                                DerivationPath(49, 0, 0, 0, 1)
                        ),
                        UnspentTransactionOutput("--proof-txid-2--", 2, 200000,
                                DerivationPath(49, 0, 0, 0, 2)
                        )
                ),
                amount = 200000, feeAmount = 100, changeAmount = 99900,
                changePath = DerivationPath(49, 0, 0, 1, 0),
                paymentAddress = "--pay-to-address--"
        )
        val input1: FundingStat = mock()
        val input2: FundingStat = mock()
        whenever(helper.daoSessionManager.newFundingStat()).thenReturn(input1).thenReturn(input2)
        whenever(transaction.wallet).thenReturn(mock())

        helper.createInputsFor(transaction, transactionData)

        val ordered = inOrder(helper.daoSessionManager, input1, input2)
        ordered.verify(helper.daoSessionManager).newFundingStat()
        ordered.verify(input1).transaction = transaction
        ordered.verify(input1).wallet = transaction.wallet
        ordered.verify(input1).state = FundingStat.State.PENDING
        ordered.verify(helper.daoSessionManager).insert(input1)
        ordered.verify(helper.daoSessionManager).newFundingStat()
        ordered.verify(input2).transaction = transaction
        ordered.verify(input2).wallet = transaction.wallet
        ordered.verify(input2).state = FundingStat.State.PENDING
        ordered.verify(helper.daoSessionManager).insert(input2)
    }

    @Test
    fun creates_and_populates_input_from_provided_utxo() {
        val helper = createHelper()
        val derivationPath = DerivationPath(49, 0, 0, 0, 1)
        val utxo = UnspentTransactionOutput("--proof-txid-1--", 1, 100000,
                derivationPath
        )
        val address = mock<Address>()
        val input: FundingStat = mock()
        whenever(helper.daoSessionManager.newFundingStat()).thenReturn(input)
        whenever(helper.addressHelper.addressForPath(derivationPath)).thenReturn(address)
        whenever(address.address).thenReturn("--address--")

        assertThat(helper.createInputFor(utxo)).isEqualTo(input)
        val ordered = inOrder(input)
        ordered.verify(input).fundedTransaction = "--proof-txid-1--"
        ordered.verify(input).position = 1
        ordered.verify(input).value = 100000
        ordered.verify(input).address = address
        ordered.verify(input).addr = "--address--"
    }
}