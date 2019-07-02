package com.coinninja.coinkeeper.cn.wallet.tx

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.AddressType
import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.UnspentTransactionOutput
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.cn.wallet.LibBitcoinProvider
import com.coinninja.coinkeeper.model.db.Address
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper
import kotlin.math.floor

@Mockable
class FundingModel(internal val libBitcoinProvider: LibBitcoinProvider,
                   internal val targetStatHelper: TargetStatHelper,
                   internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
                   internal val accountManager: AccountManager,
                   val transactionDustValue: Long) {

    val unspentTransactionOutputs: MutableList<UnspentTransactionOutput>
        get() {
            val list = mutableListOf<UnspentTransactionOutput>()
            targetStatHelper.spendableTargets.forEach { target ->
                list.add(target.toUnspentTranasactionOutput())
            }
            return list
        }

    val spendableAmount: Long
        get() {
            var value = 0L

            targetStatHelper.spendableTargets.forEach { targetStat ->
                value += targetStat.value
            }

            return value + valueOfPendingDropbits
        }

    val nextChangePath: DerivationPath
        get() {
            val address = Address().also {
                it.changeIndex = HDWallet.INTERNAL
                it.index = accountManager.nextChangeIndex
            }
            return address.derivationPath
        }

    val inputSizeInBytes: Int get() = inputSizeP2SH

    val changeSizeInBytes: Int get() = outputSizeP2SH

    fun outPutSizeInBytesForAddress(address: String?): Int {
        address?.let {
            when (libBitcoinProvider.provide().getTypeOfPaymentAddress(address)) {
                AddressType.P2PKH -> return outputSizeP2PKH
                AddressType.P2SH -> return outputSizeP2SH
                else -> return Int.MAX_VALUE
            }
        }
        return outputSizeP2PKH
    }

    fun calculateFeeForBytes(txBytes: Int, fee: Double): Long =
            floor(txBytes * kotlin.math.max(fee, minFee)).toLong()

    private val valueOfPendingDropbits: Long
        get() {
            var value: Long = 0
            inviteTransactionSummaryHelper.unfulfilledSentInvites.forEach { dropbit ->
                value -= dropbit.valueFeesSatoshis!!
                value -= dropbit.valueSatoshis!!
            }
            return value
        }

    companion object {
        const val baseTransactionBytes: Int = 11
        const val minFee: Double = 5.0
        const val outputSizeP2PKH: Int = 34
        const val outputSizeP2SH: Int = 32
        const val inputSizeP2SH: Int = 91
    }

}
