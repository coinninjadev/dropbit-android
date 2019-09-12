package com.coinninja.coinkeeper.model

import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class PaymentHolderTest {

    private fun createHolder(): PaymentHolder = PaymentHolder(
            USDCurrency(5000.00)
    )

    @Test
    fun copies_address_from_current_to_new_transaction_data_when_set() {
        val holder = createHolder()
        val transactionData = TransactionData(emptyArray(), 0, 0,
                0, mock(), "")

        val paymentAddress = "--address--"
        holder.paymentAddress = paymentAddress
        holder.transactionData = transactionData

        assertThat(holder.paymentAddress, equalTo(paymentAddress))
    }

    @Test
    fun copies_address_from_current_to_new_transaction_data_when_set__only_overrides_when_currently_has_address() {
        val holder = createHolder()
        val paymentAddress = "--address--"
        val transactionData = TransactionData(emptyArray(), 0, 0,
                0, mock(), paymentAddress)

        holder.paymentAddress = ""
        holder.transactionData = transactionData

        assertThat(holder.paymentAddress, equalTo(paymentAddress))
    }

    @Test
    fun sets_address_on_transaction_data_when_available() {
        val holder = createHolder()
        val paymentAddress = "--address--"
        holder.paymentAddress = paymentAddress

        assertThat(holder.paymentAddress, equalTo(paymentAddress))
    }

    @Test
    fun provides_crypto_currency_when_primary() {
        val holder = createHolder()

        holder.updateValue(USDCurrency(5000_00L))

        assertThat(holder.cryptoCurrency.toLong(), equalTo(100000000L))
    }

    @Test
    fun provides_secondary_currency__BTC() {
        val holder = createHolder()
        val usd = USDCurrency(10.0)

        holder.updateValue(usd)

        assertThat(holder.secondaryCurrency.toLong(), equalTo(200000L))
    }

    @Test
    fun provides_secondary_currency__FIAT() {
        val holder = createHolder()
        holder.toggleCurrencies()
        val btcCurrency = BTCCurrency(1.0)

        holder.updateValue(btcCurrency)

        assertThat(holder.secondaryCurrency.toFormattedCurrency(), equalTo("$5,000.00"))
    }

    @Test
    fun provides_primary_currency__FIAT() {
        val holder = createHolder()
        val usd = USDCurrency(10.0)

        holder.updateValue(usd)

        assertThat(holder.primaryCurrency.toFormattedCurrency(),
                equalTo(usd.toFormattedCurrency()))
    }

    @Test
    fun provides_primary_currency__CRYPTO() {
        val holder = createHolder()
        holder.toggleCurrencies()
        val btcCurrency = BTCCurrency(1.0)

        holder.updateValue(btcCurrency)

        assertThat(holder.primaryCurrency.toFormattedCurrency(),
                equalTo(btcCurrency.toFormattedCurrency()))
    }

    @Test
    fun holds_available_spendable_balance() {
        val holder = createHolder()
        holder.spendableBalance = BTCCurrency(1000L)

        assertThat(holder.spendableBalance.toSatoshis(), equalTo(1000L))
    }


    @Test
    fun given_btc_to_spend_converts_to_fiat() {
        val holder = createHolder()
        holder.toggleCurrencies()
        val usd = holder.updateValue(BTCCurrency(1.0))

        assertThat(usd.toFormattedCurrency(), equalTo("$5,000.00"))
    }

    @Test
    fun given_dollars_to_spend_it_converts_fiat() {
        val holder = createHolder()
        holder.updateValue(USDCurrency(25.0))
        holder.toggleCurrencies()
        assertThat(holder.btcCurrency.toSatoshis(), equalTo(500000L))
    }

    @Test
    fun toggle_currency_when_provided_with_secondary() {
        val holder = createHolder()
        holder.updateValue(USDCurrency(25.0))
        holder.updateValue(BTCCurrency(1L))
        assertThat(holder.primaryCurrency.toLong(), equalTo(1L))
        assertTrue(holder.primaryCurrency is BTCCurrency)
    }

    @Test
    fun clear_payment_clears_pub_key_and_address() {
        val holder = createHolder()
        holder.updateValue(USDCurrency(25.0))
        holder.transactionData = TransactionData(arrayOf(mock()), 10000000L, 1000L, 45000L, mock(), "")
        holder.publicKey = "-pub-key-"
        holder.paymentAddress = "-address-"
        holder.updateValue(USDCurrency(1.0))

        holder.clearPayment()

        assertThat(holder.paymentAddress, equalTo(""))
        assertThat(holder.publicKey, equalTo(""))
        assertThat(holder.transactionData.utxos.size, equalTo(0))
        assertThat(holder.transactionData.amount, equalTo(0L))
        assertThat(holder.transactionData.feeAmount, equalTo(0L))
        assertThat(holder.transactionData.changeAmount, equalTo(0L))
        assertThat(holder.primaryCurrency.toLong(), equalTo(0L))
    }

    @Test
    fun sets_max_limit_for_fiat() {
        val holder = createHolder()
        holder.evaluationCurrency = USDCurrency(1.0)

        holder.setMaxLimitForFiat()

        assertThat((holder.evaluationCurrency as USDCurrency).maxLongValue, equalTo(2099999998L))
    }

    @Test
    fun updating_evaluation_value_invalidates_secondary_value() {
        val holder = createHolder()
        holder.updateValue(USDCurrency(5000_00L))
        assertThat(holder.cryptoCurrency.toLong(), equalTo(100000000L))

        holder.evaluationCurrency = USDCurrency(4000_00)

        assertThat(holder.cryptoCurrency.toLong(), equalTo(125000000L))
    }
}