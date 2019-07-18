package com.coinninja.coinkeeper.util

import android.content.Context
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import javax.inject.Inject

@Mockable
class PaymentUtil @Inject constructor(
        @ApplicationContext internal val context: Context,
        internal val bitcoinUtil: BitcoinUtil,
        internal val transactionFundingManager: TransactionFundingManager) {


    internal var fee: Double = 0.0
    internal var address: String? = null
    internal var identity: Identity? = null

    var paymentMethod: PaymentMethod? = null
    var errorMessage: String? = null
    var paymentHolder: PaymentHolder? = null
    var isSendingMax = false

    val isValid: Boolean get() = isValidPaymentMethod && isValidPaymentAmount

    fun isFunded(): Boolean {
        paymentHolder?.let { holder ->
            val spendableBalance = holder.spendableBalance
            val transactionData = holder.transactionData
            val funded = isValidFunding && transactionData.utxos.isNotEmpty() && transactionData.amount > 0
            val total = holder.btcCurrency.toFormattedCurrency()

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
                builder.append(spendableBalance.toUSD(paymentHolder!!.evaluationCurrency).toFormattedCurrency())
                errorMessage = builder.toString()
            }
            return funded
        }
        return false
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
            val btcCurrency = paymentHolder!!.btcCurrency
            val usdCurrency = paymentHolder!!.fiat as USDCurrency

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

            isValid = !(paymentMethod == PaymentMethod.INVITE && paymentHolder!!.fiat.toLong() > DropbitIntents.MAX_DOLLARS_SENT_THROUGH_CONTACTS)

            if (!isValid) {
                errorMessage = getString(R.string.payment_error_too_much_sent_to_contact)
            }

            return isValid
        }

    private val isValidFunding: Boolean
        get() = !(hasNegativeValue() || hasTooLargeValue())

    init {
        paymentMethod = PaymentMethod.INVALID
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
        val transactionData = transactionFundingManager.buildFundedTransactionData(address, fee)
        paymentHolder!!.transactionData = transactionData
        return isFunded()
    }

    fun clearFunding() {
        isSendingMax = false
        val address = paymentHolder?.paymentAddress
        paymentHolder?.clearPayment()
        paymentHolder?.paymentAddress = address
    }

    fun checkFunding(): Boolean {
        if (!isSendingMax) {
            val transactionData = transactionFundingManager
                    .buildFundedTransactionData(address, fee, paymentHolder?.cryptoCurrency?.toLong()
                            ?: 0)
            paymentHolder!!.transactionData = transactionData
        }
        return isFunded()
    }

    fun isTransactionFundableWithFee(fee: Double?, amountToSend: Long): Boolean {
        return transactionFundingManager.isTransactionFundableWithFee(address, amountToSend, fee!!)
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
            paymentHolder?.paymentAddress = address
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
        val invalidBtcAddress = context.getString(R.string.invalid_bitcoin_address_error)

        if (null == address) {
            errorMessage = invalidBtcAddress
            return
        }

        if (!bitcoinUtil.isValidBTCAddress(address)) {
            val invalidReason = bitcoinUtil.invalidReason
            paymentMethod = PaymentMethod.INVALID
            errorMessage =
                    when (invalidReason) {
                        BitcoinUtil.ADDRESS_INVALID_REASON.IS_BC1 -> context.getString(R.string.bc1_error_message)
                        BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58 -> context.getString(R.string.invalid_btc_adddress__base58)
                        else -> invalidBtcAddress
                    }
        }
    }

    private fun hasNegativeValue(): Boolean {
        val transactionData = paymentHolder!!.transactionData
        return transactionData.utxos.isEmpty() ||
                transactionData.amount < 0 ||
                transactionData.feeAmount < 0 ||
                transactionData.changeAmount < 0
    }

    private fun hasTooLargeValue(): Boolean {
        val transactionData = paymentHolder!!.transactionData
        val max = BTCCurrency.MAX_SATOSHI
        return transactionData.amount >= max ||
                transactionData.changeAmount >= max ||
                transactionData.feeAmount >= max
    }

    private fun getString(res_id: Int): String {
        return context.getString(res_id)
    }

    enum class PaymentMethod {
        INVALID, ADDRESS, VERIFIED_CONTACT, INVITE
    }
}
