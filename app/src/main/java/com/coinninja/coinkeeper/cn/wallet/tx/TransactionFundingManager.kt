package com.coinninja.coinkeeper.cn.wallet.tx

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.TransactionData
import com.coinninja.bindings.UnspentTransactionOutput
import javax.inject.Inject

@Mockable
class TransactionFundingManager @Inject internal constructor(
        val fundingModel: FundingModel) {

    private var unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()

    fun buildFundedTransactionData(payToAddress: String?, feeRate: Double): TransactionData {
        val transactionData = createTransactionData()
        val walletValue = fundingModel.spendableAmount
        val fees = if (walletValue > 0) {
            buildInputsWithFees(payToAddress, feeRate, walletValue)
        } else 0

        val amountSending = walletValue - fees

        if (amountSending > 0) {
            transactionData.apply {
                amount = amountSending
                feeAmount = fees
                utxos = unspentTransactionOutputs.toTypedArray()
            }
        }

        return transactionData
    }

    fun buildFundedTransactionData(payToAddress: String?, feeRate: Double, amountToSend: Long): TransactionData {
        val transactionData = createTransactionData()
        val walletValue = fundingModel.spendableAmount

        val fees = if (walletValue > amountToSend) {
            buildInputsWithFees(payToAddress, feeRate, amountToSend)
        } else 0

        if (walletValue >= amountToSend + fees) {
            transactionData.apply {
                amount = amountToSend
                feeAmount = fees
                changeAmount = calculateChange(amountToSend, fees)
                utxos = unspentTransactionOutputs.toTypedArray()
            }
        }

        return transactionData
    }

    fun buildFundedTransactionDataForDropBit(payToAddress: String?, amountToSend: Long, explicitFee: Long): TransactionData {
        val transactionData = createTransactionData()
        val walletValue = fundingModel.spendableAmount
        val totalTransactionCost = amountToSend + explicitFee

        buildInputsWithFees(payToAddress, 0.0, amountToSend, explicitFee = explicitFee)

        if (walletValue >= totalTransactionCost) {
            transactionData.apply {
                amount = amountToSend
                feeAmount = explicitFee
                changeAmount = calculateChange(totalTransactionCost)
                utxos = unspentTransactionOutputs.toTypedArray()
            }
        }

        return transactionData
    }

    fun isTransactionFundableWithFee(address: String?, amountToSend: Long, feeRate: Double): Boolean {
        return buildFundedTransactionData(address, feeRate, amountToSend).amount > 0
    }


    internal fun buildInputsWithFees(payToAddress: String?, feeRate: Double, amountToSend: Long, explicitFee: Long? = null): Long {
        unspentTransactionOutputs.clear()
        var inputsValue: Long = 0
        var txBytes = FundingModel.baseTransactionBytes + fundingModel.outPutSizeInBytesForAddress(payToAddress)
        var currentFee: Long = explicitFee ?: 0
        var totalSendingValue: Long

        fundingModel.unspentTransactionOutputs.forEach funding@{ unspentTransactionOutput ->
            totalSendingValue = amountToSend + currentFee

            if (totalSendingValue > inputsValue) {
                unspentTransactionOutputs.add(unspentTransactionOutput)
                inputsValue += unspentTransactionOutput.amount
                txBytes += fundingModel.inputSizeInBytes
                currentFee = explicitFee ?: fundingModel.calculateFeeForBytes(txBytes, feeRate)
                totalSendingValue = amountToSend + currentFee

                val changeValue = inputsValue - totalSendingValue
                if (changeValue > 0 && changeValue < costOFPotentialChangeAndDust(feeRate)) {
                    currentFee += changeValue
                    return@funding
                } else if (changeValue > 0) {
                    txBytes += fundingModel.changeSizeInBytes
                    currentFee = explicitFee ?: fundingModel.calculateFeeForBytes(txBytes, feeRate)
                    return@funding
                }
            }
        }
        return currentFee
    }

    private fun costOFPotentialChangeAndDust(feeRate: Double) =
            fundingModel.calculateFeeForBytes(fundingModel.changeSizeInBytes, feeRate) + fundingModel.transactionDustValue


    protected fun createTransactionData(): TransactionData {
        return TransactionData(emptyArray<UnspentTransactionOutput>(), 0, 0, 0,
                fundingModel.nextChangePath, null)
    }

    protected fun calculateChange(totalTransactionCost: Long, fees: Long = 0): Long {
        var value = 0L
        unspentTransactionOutputs.forEach { output ->
            value += output.amount
        }
        return value - totalTransactionCost - fees
    }
}
