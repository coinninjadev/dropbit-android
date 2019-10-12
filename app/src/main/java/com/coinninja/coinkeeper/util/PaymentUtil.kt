package com.coinninja.coinkeeper.util

import android.content.Context
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.bitcoin.isFunded
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import javax.inject.Inject

@Mockable
class PaymentUtil @Inject constructor(
        @ApplicationContext internal val context: Context,
        internal val bitcoinUtil: BitcoinUtil,
        internal val transactionFundingManager: TransactionFundingManager) {

    internal var fee: Double = 0.0
    internal var address: String? = null
    internal var identity: Identity? = null

    var paymentMethod: PaymentMethod = PaymentMethod.INVALID
    var errorMessage: String? = null
    lateinit var paymentHolder: PaymentHolder
    var isSendingMax = false

    val isValid: Boolean get() = isValidPaymentMethod && isValidPaymentAmount

    fun isFunded(): Boolean {
        val spendableBalance = paymentHolder.spendableBalance
        val funded = paymentHolder.transactionData.isFunded()
        val total = paymentHolder.btcCurrency.toFormattedCurrency()

        if (!funded) {
            val builder = StringBuilder()
            builder.append(getString(R.string.pay_not_attempting_to_send))
            builder.append(" ")
            builder.append(total)
            builder.append(". ")
            builder.append(getString(R.string.pay_not_enough_funds_error))
            builder.append("\n")
            builder.append(getString(R.string.pay_not_available_funds_error))
            builder.append(" ")
            builder.append(spendableBalance.toFormattedCurrency())
            builder.append(" ")
            builder.append(spendableBalance.toUSD(this.paymentHolder.evaluationCurrency).toFormattedCurrency())
            errorMessage = builder.toString()
        }
        return funded
    }

    val isVerifiedContact: Boolean
        get() = getIdentity()!!.isVerified

    val isValidPaymentMethod: Boolean
        get() {
            if (paymentMethod == PaymentMethod.ADDRESS) {
                validateAddress()
            }
            return paymentMethod != PaymentMethod.INVALID
        }

    private val isValidPaymentAmount: Boolean
        get() {
            val btcCurrency = paymentHolder.btcCurrency
            val usdCurrency = paymentHolder.fiat as USDCurrency

            var isValid = btcCurrency.isValid && btcCurrency.toSatoshis() > 0

            if (!isValid) {
                errorMessage = getString(R.string.pay_error_invalid_amount)
                return false
            }

            val amountSpending = usdCurrency.toLong()
            isValid = amountSpending > 0
            if (!isValid) {
                errorMessage = getString(R.string.pay_error_too_little_transaction)
                return false
            }

            isValid = !(paymentMethod == PaymentMethod.INVITE && paymentHolder.fiat.toLong() > DropbitIntents.MAX_DOLLARS_SENT_THROUGH_CONTACTS)

            if (!isValid) {
                errorMessage = getString(R.string.payment_error_too_much_sent_to_contact)
            }

            return isValid
        }

    fun getAddress(): String? {
        return address
    }

    fun setAddress(address: String?) {
        errorMessage = ""
        identity = null
        this.address = if (address == null || address.isEmpty()) null else address
        setPaymentMethod()
    }

    fun getIdentity(): Identity? {
        return identity
    }

    fun setIdentity(identity: Identity?) {
        errorMessage = ""
        address = null
        this.identity = identity
        setPaymentMethod()
    }

    fun fundMax(): Boolean {
        isSendingMax = true
        paymentHolder.transactionData = transactionFundingManager.buildFundedTransactionData(address, fee)
        return isFunded()
    }

    fun clearFunding() {
        isSendingMax = false
        val address = paymentHolder.paymentAddress
        paymentHolder.clearPayment()
        paymentHolder.paymentAddress = address
    }

    fun checkFunding(): Boolean {
        if (!isSendingMax) {
            val transactionData = transactionFundingManager
                    .buildFundedTransactionData(address, fee, paymentHolder.cryptoCurrency.toLong())
            paymentHolder.transactionData = transactionData
        }
        return isFunded()
    }

    fun reset() {
        setAddress(null)
    }

    fun clearErrors() {
        errorMessage = ""
    }

    fun setFee(fee: Double) {
        this.fee = fee
    }

    private fun setPaymentMethod() {
        if (null != address) {
            paymentMethod = PaymentMethod.ADDRESS
            paymentHolder.paymentAddress = address ?: ""
        } else if (identity != null && identity?.isVerified == true) {
            paymentMethod = PaymentMethod.VERIFIED_CONTACT
        } else if (identity != null && identity?.isVerified == false) {
            paymentMethod = PaymentMethod.INVITE
        } else {
            errorMessage = getString(R.string.pay_error_add_valid_bitcoin_address)
            paymentMethod = PaymentMethod.INVALID
        }
        if (isSendingMax) {
            fundMax()
        }
    }

    private fun validateAddress() {
        if (!bitcoinUtil.isValidBTCAddress(address ?: "")) {
            paymentMethod = PaymentMethod.INVALID
            errorMessage = context.getString(R.string.invalid_bitcoin_address_error)
        }
    }

    private fun getString(res_id: Int): String {
        return context.getString(res_id)
    }

    enum class PaymentMethod {
        INVALID, ADDRESS, VERIFIED_CONTACT, INVITE
    }
}
