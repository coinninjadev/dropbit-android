package com.coinninja.coinkeeper.cn.wallet.tx

import app.coinninja.cn.libbitcoin.AddressUtil
import app.coinninja.cn.libbitcoin.HDWallet
import app.coinninja.cn.libbitcoin.enum.AddressType
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.UnspentTransactionOutput
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import kotlin.math.floor

@Mockable
class FundingModel(internal val addressUtil: AddressUtil,
                   internal val targetStatHelper: TargetStatHelper,
                   internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
                   internal val walletHelper: WalletHelper,
                   internal val accountManager: AccountManager,
                   val transactionDustValue: Long) {

    val unspentTransactionOutputs: MutableList<UnspentTransactionOutput>
        get() {
            val list = mutableListOf<UnspentTransactionOutput>()
            targetStatHelper.spendableTargets.forEach { target ->
                list.add(target.toUnspentTransactionOutput())
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
            val wallet = walletHelper.wallet
            return DerivationPath(wallet.purpose, wallet.coinType, wallet.accountIndex,
                    HDWallet.INTERNAL, accountManager.nextChangeIndex)
        }

    val nextReceiveAddress: String get() = accountManager.nextReceiveAddress

    val inputSizeInBytes: Int
        get() = when (walletHelper.wallet.purpose) {
            84 -> inputSizeP2WSH
            else -> inputSizeP2SH
        }

    val changeSizeInBytes: Int
        get() = when (walletHelper.wallet.purpose) {
            84 -> outputSizeP2WPKH
            else -> outputSizeP2SH
        }

    fun outPutSizeInBytesForAddress(address: String?): Int {
        address?.let {
            when (addressUtil.typeOfPaymentAddress(address)) {
                AddressType.P2PKH -> return outputSizeP2PKH
                AddressType.P2SH -> return outputSizeP2SH
                AddressType.P2WSH -> return outputSizeP2WSH
                AddressType.P2WPKH -> return outputSizeP2WPKH
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
        const val outputSizeP2WSH: Int = 32
        const val outputSizeP2WPKH: Int = 31
        const val inputSizeP2SH: Int = 91
        const val inputSizeP2WSH: Int = 68
    }

}
