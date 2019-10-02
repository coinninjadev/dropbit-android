package com.coinninja.coinkeeper.cn.wallet.tx

import app.coinninja.cn.libbitcoin.enum.ReplaceableOption
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.libbitcoin.model.UnspentTransactionOutput
import app.dropbit.annotations.Mockable
import javax.inject.Inject

@Mockable
class TransactionFundingManager @Inject internal constructor(
        val fundingModel: FundingModel) {

    data class BuiltInput(val fees: Long = 0, val utxos: Array<UnspentTransactionOutput> = emptyArray()) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BuiltInput

            if (fees != other.fees) return false
            if (!utxos.contentEquals(other.utxos)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = fees.hashCode()
            result = 31 * result + utxos.contentHashCode()
            return result
        }
    }

    fun buildFundedTransactionData(payToAddress: String?, feeRate: Double): TransactionData {
        val transactionData = createTransactionData()
        val walletValue = fundingModel.spendableAmount
        val builtInput: BuiltInput = if (walletValue > 0) {
            buildInputsWithFees(payToAddress, feeRate, walletValue)
        } else BuiltInput()

        val amountSending = walletValue - builtInput.fees

        if (amountSending > 0) {
            transactionData.apply {
                amount = amountSending
                feeAmount = builtInput.fees
                utxos = builtInput.utxos
                paymentAddress = payToAddress
            }
        }

        return transactionData
    }

    fun buildFundedTransactionData(payToAddress: String?, feeRate: Double, amountToSend: Long, rbf: Boolean? = null): TransactionData {
        val transactionData = createTransactionData()
        val walletValue = fundingModel.spendableAmount

        val builtInput: BuiltInput = if (walletValue > 0) {
            buildInputsWithFees(payToAddress, feeRate, amountToSend)
        } else BuiltInput()

        if (walletValue >= amountToSend + builtInput.fees) {
            transactionData.apply {
                amount = amountToSend
                feeAmount = builtInput.fees
                utxos = builtInput.utxos
                changeAmount = calculateChange(amountToSend, builtInput.fees, builtInput.utxos)
                utxos = builtInput.utxos
                rbf?.let {
                    replaceableOption = if (rbf) ReplaceableOption.RBF_ALLOWED else ReplaceableOption.MUST_NOT_BE_RBF
                }
            }
        }

        return transactionData
    }

    fun buildFundedTransactionDataForDropBit(payToAddress: String?, amountToSend: Long, explicitFee: Long): TransactionData {
        val transactionData = createTransactionData()
        val walletValue = fundingModel.spendableAmount
        val totalTransactionCost = amountToSend + explicitFee

        val input = buildInputsWithFees(payToAddress, 0.0, amountToSend, explicitFee = explicitFee)

        if (walletValue >= totalTransactionCost) {
            transactionData.apply {
                amount = amountToSend
                feeAmount = explicitFee
                changeAmount = calculateChange(totalTransactionCost, utxos = input.utxos)
                utxos = input.utxos
                replaceableOption = ReplaceableOption.MUST_BE_RBF
            }
        }

        return transactionData
    }

    fun isTransactionFundableWithFee(address: String?, amountToSend: Long, feeRate: Double): Boolean {
        return buildFundedTransactionData(address, feeRate, amountToSend).amount > 0
    }


    internal fun buildInputsWithFees(payToAddress: String?, feeRate: Double, amountToSend: Long, explicitFee: Long? = null): BuiltInput {
        val unspentTransactionOutputs: MutableList<UnspentTransactionOutput> = mutableListOf()
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
        return BuiltInput(currentFee, unspentTransactionOutputs.toTypedArray())
    }

    private fun costOFPotentialChangeAndDust(feeRate: Double) =
            fundingModel.calculateFeeForBytes(fundingModel.changeSizeInBytes, feeRate) + fundingModel.transactionDustValue


    protected fun createTransactionData(): TransactionData {
        return TransactionData(emptyArray(), 0, 0, 0,
                fundingModel.nextChangePath, "")
    }

    protected fun calculateChange(totalTransactionCost: Long, fees: Long = 0, utxos: Array<UnspentTransactionOutput> = emptyArray()): Long {
        var value = 0L
        utxos.forEach { output ->
            value += output.amount
        }
        return value - totalTransactionCost - fees
    }
}
