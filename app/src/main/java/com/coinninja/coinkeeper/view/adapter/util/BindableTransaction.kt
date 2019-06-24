package com.coinninja.coinkeeper.view.adapter.util

import android.content.Context
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.CryptoCurrency
import com.coinninja.coinkeeper.util.currency.FiatCurrency
import javax.inject.Inject

@Mockable
class BindableTransaction @Inject constructor(
        @ApplicationContext internal val context: Context,
        internal val walletHelper: WalletHelper) {
    var profileUrl: String?  = null
    var txTime: String? = null
    var confirmationState: ConfirmationState? = null
    var inviteState: InviteState? = null
    var sendState: SendState? = null
    var value: Long = 0
    var fee: Long = 0
    var fundingAddress: String? = null
    var targetAddress: String? = null
    var txID: String? = null
    var identity: String? = null
    var historicalInviteUSDValue: Long? = null
    var serverInviteId: String? = null
    var confirmationCount: Int = 0
    var historicalTransactionUSDValue: Long = 0
    var isSharedMemo: Boolean = false
    var memo: String? = null
    var identityType: IdentityType = IdentityType.UNKNOWN

    val valueCurrency: CryptoCurrency
        get() = BTCCurrency(value)

    val feeCurrency: CryptoCurrency
        get() = BTCCurrency(fee)

    val totalTransactionCost: Long
        get() = fee + value

    val totalTransactionCostCurrency: CryptoCurrency
        get() = BTCCurrency(totalTransactionCost)

    val identifiableTarget: String?
        get() {
            if (basicDirection == SendState.TRANSFER)
                return context.getResources().getString(R.string.send_to_self)
            else if (identity.isNullOrEmpty())
                return targetAddress
            else return identity
        }

    val basicDirection: SendState
        get() {
            when (sendState) {
                SendState.FAILED_TO_BROADCAST_SEND, SendState.SEND_CANCELED, SendState.SEND -> return SendState.SEND
                SendState.FAILED_TO_BROADCAST_TRANSFER, SendState.TRANSFER -> return SendState.TRANSFER
                SendState.FAILED_TO_BROADCAST_RECEIVE, SendState.RECEIVE_CANCELED, SendState.RECEIVE -> return SendState.RECEIVE
                else -> return SendState.RECEIVE
            }
        }

    init {
        reset()
    }

    fun reset() {
        txTime = ""
        confirmationState = null
        inviteState = null
        sendState = null
        value = 0L
        fee = 0L
        identityType = IdentityType.UNKNOWN
        fundingAddress = ""
        targetAddress = ""
        txID = ""
        identity = ""
        historicalInviteUSDValue = 0L
        serverInviteId = ""
        profileUrl = null
        confirmationCount = 0
        historicalTransactionUSDValue = 0L
        isSharedMemo = false
        memo = ""
    }

    fun totalCryptoForSendState(): CryptoCurrency {
        when (basicDirection) {
            SendState.SEND -> return totalTransactionCostCurrency
            SendState.TRANSFER -> return feeCurrency
            SendState.RECEIVE -> return valueCurrency
            else -> return valueCurrency
        }
    }

    fun totalFiatForSendState(): FiatCurrency {
        val total = totalCryptoForSendState()
        return total.toFiat(walletHelper.latestPrice)
    }

    enum class SendState {
        RECEIVE, TRANSFER, SEND, SEND_CANCELED, RECEIVE_CANCELED, FAILED_TO_BROADCAST_TRANSFER, FAILED_TO_BROADCAST_SEND, FAILED_TO_BROADCAST_RECEIVE, DOUBLESPEND_SEND
    }

    enum class ConfirmationState {
        ONE_CONFIRM, TWO_CONFIRMS, CONFIRMED, UNCONFIRMED

    }

    enum class InviteState {
        SENT_PENDING, RECEIVED_PENDING, CONFIRMED, EXPIRED, CANCELED, SENT_ADDRESS_PROVIDED, RECEIVED_ADDRESS_PROVIDED
    }
}