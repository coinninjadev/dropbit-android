package com.coinninja.coinkeeper.model

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.enum.ReplaceableOption
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.libbitcoin.model.UnspentTransactionOutput
import app.coinninja.cn.thunderdome.model.RequestInvoice
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentHolderTest {

    private fun createHolder(): PaymentHolder = PaymentHolder(
            USDCurrency(5000.00)
    )

    @Test
    fun is_parcelable() {
        val holder = PaymentHolder(
                USDCurrency(10_000),
                BTCCurrency(20_000),
                true,
                "--pub-key--",
                "--memo--",
                defaultCurrencies = DefaultCurrencies(USDCurrency(0), BTCCurrency(0)),
                toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", "", true)
        )
        holder.transactionData = TransactionData(
                arrayOf(
                        UnspentTransactionOutput("--txid---", 0, 5400,
                                DerivationPath(84, 0, 0, 0, 1), true)
                ),
                5100, 200, 100,
                DerivationPath(84, 0, 0, 1, 1),
                "--payment-address--", ReplaceableOption.MUST_NOT_BE_RBF
        )
        holder.requestInvoice = RequestInvoice("--destination--", "--payment-hash--",
                12345, "now", "later", "description",
                "descripion hash", "--fallback-address--", "cltvExpiry")
        holder.requestInvoice!!.encoded = "--encoded--invoice--"

        val parcel = Parcel.obtain()
        holder.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val pHolder = PaymentHolder.CREATOR.createFromParcel(parcel)

        assertThat(pHolder.evaluationCurrency.toLong(), equalTo(holder.evaluationCurrency.toLong()))
        assertThat(pHolder.spendableBalance.toLong(), equalTo(holder.spendableBalance.toLong()))
        assertThat(pHolder.defaultCurrencies.fiat.toLong(), equalTo(holder.defaultCurrencies.fiat.toLong()))
        assertThat(pHolder.defaultCurrencies.crypto.toLong(), equalTo(holder.defaultCurrencies.crypto.toLong()))
        assertThat(pHolder.toUser, equalTo(holder.toUser))
        assertThat(pHolder.transactionData, equalTo(holder.transactionData))
        assertThat(pHolder.requestInvoice, equalTo(holder.requestInvoice))
    }

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
        holder.requestInvoice = RequestInvoice()
        holder.updateValue(USDCurrency(1.0))

        holder.clearPayment()

        assertThat(holder.paymentAddress, equalTo(""))
        assertThat(holder.publicKey, equalTo(""))
        assertThat(holder.transactionData.utxos.size, equalTo(0))
        assertThat(holder.transactionData.amount, equalTo(0L))
        assertThat(holder.transactionData.feeAmount, equalTo(0L))
        assertThat(holder.transactionData.changeAmount, equalTo(0L))
        assertThat(holder.primaryCurrency.toLong(), equalTo(0L))
        assertNull(holder.requestInvoice)
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

    @Test
    fun lnd_when_request_invoice_not_null() {
        val holder = createHolder()
        assertThat(holder.isLnd(), equalTo(false))
        holder.requestInvoice = RequestInvoice()
        assertThat(holder.isLnd(), equalTo(true))
    }

    @Test
    fun payment_knows_payment_type() {
        val holder = PaymentHolder()
        holder.accountMode = AccountMode.BLOCKCHAIN

        assertThat(holder.paymentType(), equalTo(PaymentType.INVALID))

        holder.toUser = Identity(IdentityType.PHONE, "+13305551111")
        holder.transactionData = TransactionData(paymentAddress = "--payment-address--")
        assertThat(holder.paymentType(), equalTo(PaymentType.BLOCKCHAIN))

        holder.paymentAddress = ""
        assertThat(holder.paymentType(), equalTo(PaymentType.BLOCKCHAIN_INVITE))

        holder.requestInvoice = RequestInvoice()
        holder.requestInvoice!!.encoded = "ld--encoded"
        assertThat(holder.paymentType(), equalTo(PaymentType.LIGHTNING))

        holder.requestInvoice!!.encoded = ""
        assertThat(holder.paymentType(), equalTo(PaymentType.LIGHTNING_INVITE))

        holder.accountMode = AccountMode.LIGHTNING
        holder.requestInvoice = null
        assertThat(holder.paymentType(), equalTo(PaymentType.LIGHTNING_INVITE))
    }
}