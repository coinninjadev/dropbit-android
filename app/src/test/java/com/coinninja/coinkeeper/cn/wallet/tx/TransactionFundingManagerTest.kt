package com.coinninja.coinkeeper.cn.wallet.tx

import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.UnspentTransactionOutput
import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.model.db.TargetStat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.mock
import kotlin.math.floor

class TransactionFundingManagerTest {
    private val feeRate: Double get() = 5.0
    private val payToAddress get() = "--address--"

    @Test
    fun `check to see if new fee can fund the transaction`() {
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, 9999L, 50000L, 100000L)
        unspentTransactionOutputs.remove(unspentTransactionOutputs[2])
        unspentTransactionOutputs.remove(unspentTransactionOutputs[1])

        assertTrue(transactionFundingManager.isTransactionFundableWithFee(payToAddress, 155000L, 15.0))
        assertFalse(transactionFundingManager.isTransactionFundableWithFee(payToAddress, 155000L, 45.0))
    }

    @Test
    fun creates_exact_with_fee_and_amount_provided() {
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, 9999L, 50000L, 100000L)
        unspentTransactionOutputs.remove(unspentTransactionOutputs[2])
        unspentTransactionOutputs.remove(unspentTransactionOutputs[1])

        val transactionData = transactionFundingManager.buildFundedTransactionDataForDropBit(payToAddress, 5000L, 500L)

        assertThat(transactionData.amount, equalTo(5000L))
        assertThat(transactionData.feeAmount, equalTo(500L))
        assertThat(transactionData.changeAmount, equalTo(4499L))
        assertThat(transactionData.utxos, equalTo(unspentTransactionOutputs.toTypedArray()))
    }

    @Test
    fun uses_wallet_balance__send_max() {
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        val expectedFee = 1580L
        val amountToSend = 9999 + 50000 + 100000 - expectedFee

        mockTargets(transactionFundingManager, unspentTransactionOutputs, 9999L, 50000L, 100000L)
        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate)

        assertThat(transactionData.feeAmount, equalTo(expectedFee))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.amount, equalTo(amountToSend))
        assertThat(transactionData.utxos, equalTo(unspentTransactionOutputs.toTypedArray()))
    }

    @Test
    fun sets_zero_for_sending_amounts_when_requested_amount_exceeds_available_balance() {
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, 9999L, 50000L, 100000L)

        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, 1600000L)

        assertThat(transactionData.amount, equalTo(0L))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.feeAmount, equalTo(0L))
        assertThat(transactionData.changePath, equalTo(transactionFundingManager.fundingModel.nextChangePath))
        assertThat(transactionData.utxos, equalTo(arrayOfNulls(0)))
    }

    @Test
    fun sets_zero_for_sending_amounts_when_requested_amount_exceeds_available_balance__fees_push_it_over() {
        val input1: Long = 9000
        val input2: Long = 19000
        val input3: Long = 500

        val expectedFee = 1500L
        val amountToSend = input1 + input2 + input3 - expectedFee + 100L
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, input1, input2, input3)

        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, amountToSend)

        assertThat(transactionData.amount, equalTo(0L))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.feeAmount, equalTo(0L))
        assertThat(transactionData.changePath, equalTo(transactionFundingManager.fundingModel.nextChangePath))
        assertThat(transactionData.utxos, equalTo(arrayOfNulls(0)))
    }

    @Test
    fun sets_amount_for_sending_amount_when_requested_amount_exceeds_available_balance() {
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, 9999L, 50000L, 100000L)

        val amountToSend = 55000L
        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, amountToSend)

        assertThat(transactionData.amount, equalTo(amountToSend))
    }

    @Test
    fun calculates_fees_when_generating_inputs() {
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        val expectedFee = 1285L
        mockTargets(transactionFundingManager, unspentTransactionOutputs, 9999L, 50000L, 100000L)
        unspentTransactionOutputs.remove(unspentTransactionOutputs[2])

        val amountToSend = 55000L
        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, amountToSend)

        assertThat(transactionData.amount, equalTo(amountToSend))
        assertThat(transactionData.feeAmount, equalTo(expectedFee))
        assertThat(transactionData.changeAmount, equalTo(9999L + 50000L - expectedFee - amountToSend))
        assertThat(transactionData.utxos, equalTo(unspentTransactionOutputs.toTypedArray()))
    }

    @Test
    fun calculates_fees_when_generating_inputs__with_dust_to_minor() {
        val transactionFundingManager = createTransactionFundingManager()
        val valueOfFirstInput: Long = 9000
        val dust: Long = 100
        val expectedFee = 1500L
        val amountToSend = valueOfFirstInput - dust - expectedFee
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, valueOfFirstInput, 50000L, 100000L)
        unspentTransactionOutputs.remove(unspentTransactionOutputs[2])
        unspentTransactionOutputs.remove(unspentTransactionOutputs[1])

        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, amountToSend)

        assertThat(transactionData.amount, equalTo(amountToSend))
        assertThat(transactionData.feeAmount, equalTo(expectedFee + dust))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.utxos, equalTo(unspentTransactionOutputs.toTypedArray()))
    }

    @Test
    fun calculates_fees_when_generating_inputs__two_inputs_with_dust_to_minor() {
        val valueOfFirstInput: Long = 9000
        val valueOfSecondInput = 50000L
        val dust: Long = 100
        val expectedFee = 1500L
        val amountToSend = valueOfFirstInput + valueOfSecondInput - dust - expectedFee
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, valueOfFirstInput, valueOfSecondInput, 100000L)
        unspentTransactionOutputs.remove(unspentTransactionOutputs[2])

        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, amountToSend)

        assertThat(transactionData.amount, equalTo(amountToSend))
        assertThat(transactionData.feeAmount, equalTo(expectedFee + dust))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.utxos, equalTo(unspentTransactionOutputs.toTypedArray()))
    }

    @Test
    fun calculates_fees_when_generating_inputs__with_one_input_exact_funds() {
        val valueOfFirstInput: Long = 9000
        val expectedFee = 1500L
        val amountToSend = valueOfFirstInput - expectedFee
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, valueOfFirstInput, 50000L, 100000L)
        unspentTransactionOutputs.remove(unspentTransactionOutputs[2])
        unspentTransactionOutputs.remove(unspentTransactionOutputs[1])

        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, amountToSend)

        assertThat(transactionData.amount, equalTo(amountToSend))
        assertThat(transactionData.feeAmount, equalTo(expectedFee))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.utxos, equalTo(unspentTransactionOutputs.toTypedArray()))
    }

    @Test
    fun calculates_fees_when_generating_inputs__with_two_input_exact_funds() {
        val valueOfFirstInput: Long = 9000
        val valueOfSecondInput: Long = 19000
        val expectedFee: Long = 1740L
        val amountToSend = valueOfFirstInput + valueOfSecondInput - expectedFee
        val transactionFundingManager = createTransactionFundingManager()
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, valueOfFirstInput, valueOfSecondInput, 100000L)
        unspentTransactionOutputs.remove(unspentTransactionOutputs[2])

        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, amountToSend)

        assertThat(transactionData.amount, equalTo(amountToSend))
        assertThat(transactionData.feeAmount, equalTo(expectedFee))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.utxos, equalTo(unspentTransactionOutputs.toTypedArray()))
    }

    @Test
    fun gives_change_to_miner_when_cost_to_keep_change_is_not_equitable() {
        val valueOfFirstInput: Long = 2900
        val expectedFee: Long = floor((11 + 91 + 32) * feeRate).toLong()
        val amountToSend: Long = valueOfFirstInput - expectedFee - 900
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        val transactionFundingManager = createTransactionFundingManager()
        mockTargets(transactionFundingManager, unspentTransactionOutputs, valueOfFirstInput)

        val transactionData = transactionFundingManager.buildFundedTransactionData(payToAddress, feeRate, amountToSend)

        assertThat(transactionData.feeAmount, equalTo(expectedFee + 900))
        assertThat(transactionData.amount, equalTo(amountToSend))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.utxos, equalTo(unspentTransactionOutputs.toTypedArray()))
    }

    private fun createTransactionFundingManager(): TransactionFundingManager {
        val expectedChangePath = DerivationPath(49, 0, 0, HDWallet.INTERNAL, 25)
        val fundingModel = mock(FundingModel::class.java)
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
        whenever(fundingModel.nextChangePath).thenReturn(expectedChangePath)
        whenever(fundingModel.unspentTransactionOutputs).thenReturn(unspentTransactionOutputs)
        return TransactionFundingManager(fundingModel)
    }

    private fun mockTargets(transactionFundingManager: TransactionFundingManager, unspentTransactionOutputs: MutableList<UnspentTransactionOutput>, vararg values: Long) {
        var spendableAmount: Long = 0

        for (value in values) {
            spendableAmount += value
            val stat = mock(TargetStat::class.java)
            val unspentTransactionOutput = mock(UnspentTransactionOutput::class.java)
            whenever(unspentTransactionOutput.amount).thenReturn(value)
            whenever(stat.value).thenReturn(value)
            whenever(stat.toUnspentTranasactionOutput()).thenReturn(unspentTransactionOutput)
            unspentTransactionOutputs.add(unspentTransactionOutput)
        }
        transactionFundingManager.fundingModel.unspentTransactionOutputs.addAll(unspentTransactionOutputs)
        whenever(transactionFundingManager.fundingModel.outPutSizeInBytesForAddress(payToAddress)).thenReturn(FundingModel.outputSizeP2SH)
        whenever(transactionFundingManager.fundingModel.calculateFeeForBytes(any(), anyDouble())).thenCallRealMethod()
        whenever(transactionFundingManager.fundingModel.spendableAmount).thenReturn(spendableAmount)
        whenever(transactionFundingManager.fundingModel.transactionDustValue).thenReturn(1000L)
        whenever(transactionFundingManager.fundingModel.inputSizeInBytes).thenCallRealMethod()
        whenever(transactionFundingManager.fundingModel.changeSizeInBytes).thenCallRealMethod()
    }

}