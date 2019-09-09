package com.coinninja.coinkeeper.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.bindings.TransactionData
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.service.client.model.TransactionFee
import com.coinninja.coinkeeper.util.PaymentUtil.PaymentMethod
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.*
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentUtilTest {

    private fun createPaymentHolder(): PaymentHolder {
        val defaultCurrencies = DefaultCurrencies(usdCurrency, btcCurrency)
        val paymentHolder = PaymentHolder(USDCurrency(5000.00))
        paymentHolder.defaultCurrencies = defaultCurrencies
        paymentHolder.spendableBalance = BTCCurrency("1.0")
        paymentHolder.updateValue(USDCurrency(25.0))
        return paymentHolder
    }

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private val usdCurrency: USDCurrency get() = USDCurrency(2.0)
    private val btcCurrency: BTCCurrency get() = BTCCurrency(1.0)
    private val validTransactionData: TransactionData = TransactionData(arrayOf(mock()), 10000, 100,
            500, mock(), BTC_ADDRESS)
    private val invalidTransactionData: TransactionData = TransactionData(emptyArray())
    private val transactionFee: TransactionFee = TransactionFee(5.0, 10.0, 15.0)
    private val identity: Identity get() = Identity(Contact(PHONE_NUMBER, DISPLAY_NAME, false))

    private fun createUtil(): PaymentUtil = PaymentUtil(context, mock(), mock()).also {
        whenever(it.bitcoinUtil.isValidBase58Address(any())).thenReturn(true)
        whenever(it.bitcoinUtil.isValidBTCAddress(BTC_ADDRESS)).thenReturn(true)
        whenever(it.bitcoinUtil.isValidBTCAddress(BASE58_BAD_ADDRESS)).thenReturn(false)
        whenever(it.bitcoinUtil.isValidBTCAddress(BC1_ADDRESS)).thenReturn(false)
        whenever(it.bitcoinUtil.isValidBTCAddress(INVALID_BTC_ADDRESS)).thenReturn(false)
        it.setFee(transactionFee.slow)
        it.paymentHolder = createPaymentHolder()
    }

    @Test
    fun resetting_payment_util_nulls_out_address_and_fundingUTXO() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)
        paymentUtil.reset()

        assertNull(paymentUtil.getAddress())
    }

    @Test
    fun bc1_addresses_are_not_supported() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BC1_ADDRESS)
        whenever(paymentUtil.bitcoinUtil.invalidReason).thenReturn(IS_BC1)

        assertFalse(paymentUtil.isValid)
        assertThat(paymentUtil.errorMessage, equalTo(context.resources.getString(R.string.bc1_error_message)))
    }

    @Test
    fun bad_base58_address_check_test() {
        val paymentUtil = createUtil()
        whenever(paymentUtil.bitcoinUtil.isValidBase58Address(BASE58_BAD_ADDRESS)).thenReturn(false)
        whenever(paymentUtil.bitcoinUtil.invalidReason).thenReturn(NOT_BASE58)

        paymentUtil.setAddress(BASE58_BAD_ADDRESS)

        assertFalse(paymentUtil.isValidPaymentMethod)
        assertThat(paymentUtil.errorMessage, equalTo("Address Failed Base 58 check"))
    }

    @Test
    fun good_base58_address_check_test() {
        val paymentUtil = createUtil()
        whenever(paymentUtil.bitcoinUtil.isValidBase58Address(BASE58_BAD_ADDRESS)).thenReturn(true)

        paymentUtil.setAddress(BTC_ADDRESS)

        assertTrue(paymentUtil.isValidPaymentMethod)
        assertTrue(paymentUtil.errorMessage.isNullOrEmpty())
        assertThat(paymentUtil.paymentHolder.paymentAddress, equalTo(BTC_ADDRESS))
    }

    @Test
    fun confirms_insufficient_funding_with_given_fees() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)
        val btcCurrency = paymentUtil.paymentHolder.updateValue(USDCurrency(25.0)) as BTCCurrency
        val spendableBalance = btcCurrency.toSatoshis() - 100L
        paymentUtil.paymentHolder.spendableBalance = BTCCurrency(spendableBalance)

        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(eq(paymentUtil.getAddress()),
                eq(transactionFee.slow), any(), eq(null))).thenReturn(invalidTransactionData)

        assertFalse(paymentUtil.checkFunding())
        assertThat(paymentUtil.errorMessage, equalTo("Attempting to send "
                + btcCurrency.toFormattedCurrency() + ". Not enough spendable funds\nAvailable "
                + BTCCurrency(spendableBalance).toFormattedCurrency() + " $25.00"))
    }

    @Test
    fun confirms_funding_with_given_fees() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)
        paymentUtil.paymentHolder.spendableBalance = paymentUtil.paymentHolder.updateValue(USDCurrency(50.0)) as BTCCurrency
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(eq(paymentUtil.getAddress()), eq(transactionFee.slow), any(), eq(null))).thenReturn(validTransactionData)

        assertTrue(paymentUtil.checkFunding())
        assertTrue(paymentUtil.isFunded())
    }

    @Test
    fun can_send_more_than_limit_to_address() {
        val paymentUtil = createUtil()
        paymentUtil.paymentHolder.updateValue(USDCurrency(25.0))
        paymentUtil.setAddress(BTC_ADDRESS)
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(eq(paymentUtil.getAddress()), eq(transactionFee.slow),
                any(), eq(null))).thenReturn(validTransactionData)

        assertTrue(paymentUtil.checkFunding())
        assertTrue(paymentUtil.isValid)
    }

    @Test
    fun will_not_limit_amount_can_send_to_verified_contact() {
        val paymentUtil = createUtil()
        paymentUtil.paymentHolder.updateValue(USDCurrency(101.0))
        val identity = Identity(identity.identityType, identity.value, identity.hash, identity.displayName, "", true, identity.avatarUrl)
        paymentUtil.setIdentity(identity)
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(eq(paymentUtil.getAddress()), eq(transactionFee.slow), any(), eq(null)))
                .thenReturn(validTransactionData)

        assertTrue(paymentUtil.checkFunding())
        assertTrue(paymentUtil.isValid)
        assertThat(paymentUtil.errorMessage, equalTo(""))
    }

    @Test
    fun will_limit_amount_can_send_to_contact_invite() {
        val paymentUtil = createUtil()
        paymentUtil.paymentHolder.updateValue(USDCurrency(101.0))
        paymentUtil.setIdentity(identity)

        assertFalse(paymentUtil.isValid)

        assertThat(paymentUtil.errorMessage,
                equalTo(context.getString(R.string.payment_error_too_much_sent_to_contact)))
    }

    @Test
    fun can_send_less_than_one_usd() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)
        paymentUtil.paymentHolder.updateValue(USDCurrency(0.99))

        assertTrue(paymentUtil.isValid)
    }

    @Test
    fun invalid_payment_amount_has_reason() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)
        paymentUtil.paymentHolder.updateValue(USDCurrency(0.0))

        assertFalse(paymentUtil.isValid)

        assertThat(paymentUtil.errorMessage, equalTo(context.resources
                .getString(R.string.pay_error_invalid_amount)))
    }

    @Test
    fun valid_payment_is_valid() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(any(), any(), any(), eq(null)))
                .thenReturn(validTransactionData)

        paymentUtil.checkFunding()

        assertTrue(paymentUtil.isValid)
    }

    @Test
    fun invalid_payment_method_makes_payment_not_valid() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(INVALID_BTC_ADDRESS)
        whenever(paymentUtil.bitcoinUtil.invalidReason).thenReturn(NOT_STANDARD_BTC_PATTERN)

        assertFalse(paymentUtil.isValid)
    }

    @Test
    fun updating_price_to_spend_returns_converted_btc() {
        val paymentUtil = createUtil()
        val btc = paymentUtil.paymentHolder.updateValue(USDCurrency(5.0)) as BTCCurrency

        assertThat(btc.toSatoshis(), equalTo(100000L))
    }

    @Test
    fun invalid_address_and_contact_message() {
        val paymentUtil = createUtil()
        paymentUtil.setIdentity(null)

        assertThat(paymentUtil.errorMessage, equalTo(context.resources
                .getString(R.string.pay_error_add_valid_bitcoin_address)))
    }

    @Test
    fun invalid_address_provides_error_message() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(INVALID_BTC_ADDRESS)
        whenever(paymentUtil.bitcoinUtil.invalidReason).thenReturn(NOT_STANDARD_BTC_PATTERN)

        paymentUtil.isValid

        assertThat(paymentUtil.errorMessage, equalTo("Invalid BTC address"))
    }

    @Test
    fun setting_invalid_address_is_not_valid() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(INVALID_BTC_ADDRESS)
        whenever(paymentUtil.bitcoinUtil.invalidReason).thenReturn(NOT_STANDARD_BTC_PATTERN)

        paymentUtil.isValid

        assertThat(paymentUtil.paymentMethod, equalTo(PaymentMethod.INVALID))
    }

    @Test
    fun no_contact_is_invalid_method() {
        val paymentUtil = createUtil()
        paymentUtil.setIdentity(null)
        assertThat(paymentUtil.paymentMethod, equalTo(PaymentMethod.INVALID))
    }

    @Test
    fun no_address_is_invalid_method() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(null)
        assertThat(paymentUtil.paymentMethod, equalTo(PaymentMethod.INVALID))
    }

    @Test
    fun setting_verified_contact_defines_payment_method() {
        val paymentUtil = createUtil()
        val identity = Identity(identity.identityType, identity.value, identity.hash, identity.displayName, "", true, identity.avatarUrl)

        paymentUtil.setIdentity(identity)

        assertThat(paymentUtil.paymentMethod, equalTo(PaymentMethod.VERIFIED_CONTACT))
    }

    @Test
    fun setting_contact_defines_payment_method() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)

        paymentUtil.setIdentity(identity)

        assertThat(paymentUtil.paymentMethod, equalTo(PaymentMethod.INVITE))
    }

    @Test
    fun setting_address_defines_payment_method() {
        val paymentUtil = createUtil()
        paymentUtil.setIdentity(identity)

        paymentUtil.setAddress(BTC_ADDRESS)

        assertThat(paymentUtil.paymentMethod, equalTo(PaymentMethod.ADDRESS))
    }

    @Test
    fun accepts_contact_for_receiving_payment() {
        val paymentUtil = createUtil()
        paymentUtil.setIdentity(identity)

        assertThat(paymentUtil.getIdentity(), equalTo(identity))
    }

    @Test
    fun accepts_null_contacts() {
        val paymentUtil = createUtil()
        paymentUtil.setIdentity(null)

        assertNull(paymentUtil.getIdentity())
    }

    @Test
    fun empty_addresses_are_null() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress("")

        assertNull(paymentUtil.getAddress())
    }

    @Test
    fun accepts_address_for_payment() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)

        assertThat(paymentUtil.getAddress(), equalTo(BTC_ADDRESS))
    }

    @Test
    fun payment_method_initially_invalid() {
        val paymentUtil = createUtil()
        assertThat(paymentUtil.paymentMethod, equalTo(PaymentMethod.INVALID))
    }

    @Test
    fun is_valid_checks_payment_type_and_payment_amount() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)
        paymentUtil.paymentHolder.updateValue(BTCCurrency(0.0))
        assertFalse(paymentUtil.isValid)

        paymentUtil.paymentHolder.updateValue(BTCCurrency(1.0))
        assertTrue(paymentUtil.isValid)

        verifyZeroInteractions(paymentUtil.transactionFundingManager)
    }

    @Test
    fun check_funding_validates_funding() {
        val paymentUtil = createUtil()
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(eq(paymentUtil.getAddress()),
                eq(transactionFee.slow), any(), eq(null)))
                .thenReturn(validTransactionData)

        assertTrue(paymentUtil.checkFunding())
    }

    @Test
    fun funding_max_calculates_max() {
        val paymentUtil = createUtil()
        paymentUtil.setAddress(BTC_ADDRESS)
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(eq(BTC_ADDRESS), any())).thenReturn(validTransactionData)

        assertTrue(paymentUtil.fundMax())

        assertThat(paymentUtil.paymentHolder.transactionData, equalTo(validTransactionData))
    }

    @Test
    fun can_clear_funding() {
        val paymentUtil = createUtil()
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(any(), any())).thenReturn(validTransactionData)
        assertNotNull(paymentUtil.paymentHolder)
        paymentUtil.setAddress(BTC_ADDRESS)
        paymentUtil.fundMax()

        paymentUtil.clearFunding()

        assertFalse(paymentUtil.isFunded())
        val transactionData = paymentUtil.paymentHolder.transactionData
        assertThat(transactionData.utxos.size, equalTo(0))
        assertThat(transactionData.amount, equalTo(0L))
        assertThat(transactionData.changeAmount, equalTo(0L))
        assertThat(transactionData.feeAmount, equalTo(0L))
        assertThat(paymentUtil.paymentHolder.paymentAddress, equalTo(BTC_ADDRESS))
    }

    @Test
    fun ignores_floor_amount_when_sending_max() {
        val paymentUtil = createUtil()
        paymentUtil.paymentHolder.evaluationCurrency = USDCurrency(1000.00)
        paymentUtil.paymentHolder.updateValue(USDCurrency(.99))
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(any(), any())).thenReturn(validTransactionData)
        paymentUtil.setAddress(BTC_ADDRESS)
        paymentUtil.fundMax()

        assertTrue(paymentUtil.isValid)
        assertTrue(paymentUtil.isFunded())
    }

    @Test
    fun checking_funding_uses_send_max_flag() {
        val paymentUtil = createUtil()
        paymentUtil.paymentHolder.evaluationCurrency = USDCurrency(1000.00)
        paymentUtil.paymentHolder.updateValue(USDCurrency(5.99))
        paymentUtil.setAddress(BTC_ADDRESS)
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(any(), any())).thenReturn(validTransactionData)
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(any(), any(), any(), eq(null))).thenReturn(invalidTransactionData)
        paymentUtil.fundMax()

        paymentUtil.checkFunding()

        assertTrue(paymentUtil.isValid)
        assertTrue(paymentUtil.isFunded())
        verify(paymentUtil.transactionFundingManager).buildFundedTransactionData(any(), any())
        verify(paymentUtil.transactionFundingManager, times(0))
                .buildFundedTransactionData(any(), any(), any(), eq(null))
    }

    @Test
    fun recalculates_when_address_set() {
        val paymentUtil = createUtil()
        val txData1 = TransactionData(arrayOf(mock()))

        val txData2 = TransactionData(arrayOf(mock(), mock()))

        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(eq(null), any())).thenReturn(txData1)
        whenever(paymentUtil.transactionFundingManager.buildFundedTransactionData(eq(BTC_ADDRESS), any())).thenReturn(txData2)
        paymentUtil.fundMax()
        assertThat(paymentUtil.paymentHolder?.transactionData, equalTo(txData1))

        paymentUtil.setAddress(BTC_ADDRESS)
        assertThat(paymentUtil.paymentHolder.paymentAddress, equalTo(BTC_ADDRESS))
        assertThat(paymentUtil.paymentHolder.transactionData, equalTo(txData2))

        paymentUtil.paymentHolder.clearPayment()

        paymentUtil.setAddress(BTC_ADDRESS)
        paymentUtil.fundMax()
        assertThat(paymentUtil.paymentHolder.transactionData, equalTo(txData2))
        assertThat(paymentUtil.paymentHolder.paymentAddress, equalTo(BTC_ADDRESS))
    }

    companion object {
        private const val BTC_ADDRESS = "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX"
        private const val BASE58_BAD_ADDRESS = "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkXEEEEEE"
        private const val INVALID_BTC_ADDRESS = "---btc-address---"
        private const val DISPLAY_NAME = "Joe Smoe"
        private const val BC1_ADDRESS = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4"
        private val PHONE_NUMBER = PhoneNumber("+13305551111")
    }
}