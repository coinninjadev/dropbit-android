package com.coinninja.coinkeeper.ui.lightning.history

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.persistance.model.LedgerDirection
import app.coinninja.cn.persistance.model.LedgerStatus
import app.coinninja.cn.persistance.model.LedgerType
import app.coinninja.cn.persistance.model.LightningInvoice
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LightningHistoryAdapterTest {

    private fun createAdapter(): LightningHistoryAdapter {
        val currencyPreference: CurrencyPreference = mock()
        whenever(currencyPreference.currenciesPreference).thenReturn(DefaultCurrencies(USDCurrency(), BTCCurrency()))
        val adapter = LightningHistoryAdapter(mock(), mock(), currencyPreference)
        adapter.registerAdapterDataObserver(mock())
        whenever(adapter.walletHelper.latestPrice).thenReturn(USDCurrency(10_000_00))
        return adapter
    }

    private fun createParent(): ViewGroup = LinearLayout(ApplicationProvider.getApplicationContext())

    @Test
    fun empty_lists_provide_1_item() {
        assertThat(createAdapter().itemCount).isEqualTo(1)
    }

    @Test
    fun returns_empty_state_view_type() {
        assertThat(createAdapter().getItemViewType(0)).isEqualTo(LightningHistoryAdapter.EMPTYSTATE_VIEW_TYPE)
    }

    @Test
    fun returns_item_view_type_when_invoices() {
        val adapter = createAdapter()
        adapter.invoices = listOf(mock())
        assertThat(adapter.getItemViewType(0)).isEqualTo(LightningHistoryAdapter.ITEM_VIEW_TYPE)
    }

    @Test
    fun shows_empty_state_when_no_transactions_in_ledger() {
        val adapter = createAdapter()
        val parent = createParent()
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.EMPTYSTATE_VIEW_TYPE)

        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<View>(R.id.quick_load_5)).isNotNull()
    }

    @Test
    fun clicking_on_deposit_amounts_from_empty_state_navigates_to_deposit_for_amount__5_dollars() {
        val adapter = createAdapter()
        val parent = createParent()
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.EMPTYSTATE_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.findViewById<View>(R.id.quick_load_5).performClick()

        verify(adapter.activityNavigationUtil).showLoadLightningWith(parent.context, USDCurrency(5_00))
    }

    @Test
    fun clicking_on_deposit_amounts_from_empty_state_navigates_to_deposit_for_amount__20_dollars() {
        val adapter = createAdapter()
        val parent = createParent()
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.EMPTYSTATE_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.findViewById<View>(R.id.quick_load_20).performClick()

        verify(adapter.activityNavigationUtil).showLoadLightningWith(parent.context, USDCurrency(20_00))
    }

    @Test
    fun clicking_on_deposit_amounts_from_empty_state_navigates_to_deposit_for_amount__50_dollars() {
        val adapter = createAdapter()
        val parent = createParent()
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.EMPTYSTATE_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.findViewById<View>(R.id.quick_load_50).performClick()

        verify(adapter.activityNavigationUtil).showLoadLightningWith(parent.context, USDCurrency(50_00))
    }

    @Test
    fun clicking_on_deposit_amounts_from_empty_state_navigates_to_deposit_for_amount__100_dollars() {
        val adapter = createAdapter()
        val parent = createParent()
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.EMPTYSTATE_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.findViewById<View>(R.id.quick_load_100).performClick()

        verify(adapter.activityNavigationUtil).showLoadLightningWith(parent.context, USDCurrency(100_00))
    }

    @Test
    fun clicking_on_custom_amount_shows_loading_lightning_with_no_amount() {
        val adapter = createAdapter()
        val parent = createParent()
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.EMPTYSTATE_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        holder.itemView.findViewById<View>(R.id.custom_amount).performClick()

        verify(adapter.activityNavigationUtil).showLoadLightningWith(parent.context)
    }

    // Loading Lightning
    private fun createInvoice(): LightningInvoice = LightningInvoice()

    // Loading Lightning

    @Test
    fun generic__funds_fromatted_in_satoshis() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.IN
            type = LedgerType.BTC
            value = BTCCurrency(10000)
            status = LedgerStatus.PENDING
            memo = "Deposit 10,000..."
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .secondaryCurrencyText).isEqualTo("10,000 sats")
    }

    @Test
    fun deposit__not_top_up() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.IN
            type = LedgerType.BTC
            value = BTCCurrency(10000)
            status = LedgerStatus.PENDING
            memo = "Deposit 10,000..."
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<ImageView>(R.id.icon).tag).isEqualTo(R.drawable.ic_transfer_in)
        assertThat(holder.itemView.findViewById<TextView>(R.id.address).text).isEqualTo("Load Lightning")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).text).isEqualTo("pending")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).visibility).isEqualTo(View.VISIBLE)
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
    }

    @Test
    fun deposit__top_up() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.IN
            type = LedgerType.BTC
            value = BTCCurrency(10000)
            status = LedgerStatus.COMPLETED
            memo = "Withdraw 10,000..."
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<ImageView>(R.id.icon).tag).isEqualTo(R.drawable.ic_transfer_in)
        assertThat(holder.itemView.findViewById<TextView>(R.id.address).text).isEqualTo("Load Lightning")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
    }

    @Test
    fun withdrawing__lightning__pending() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.OUT
            type = LedgerType.BTC
            value = BTCCurrency(10000)
            status = LedgerStatus.COMPLETED
            memo = "Withdraw 10,000..."
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<ImageView>(R.id.icon).tag).isEqualTo(R.drawable.ic_transfer_out)
        assertThat(holder.itemView.findViewById<TextView>(R.id.address).text).isEqualTo("Lightning Withdraw")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
    }

    @Test
    fun lightning__invoice__request() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.IN
            type = LedgerType.LIGHTNING
            value = BTCCurrency(10000)
            status = LedgerStatus.PENDING
            memo = "--memo--"
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<ImageView>(R.id.icon).tag).isEqualTo(R.drawable.ic_lightning_invoice)
        assertThat(holder.itemView.findViewById<TextView>(R.id.address).text).isEqualTo("Lightning Invoice")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).text).isEqualTo("--memo--")
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).visibility).isEqualTo(View.VISIBLE)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
    }

    @Test
    fun lightning_invoice__request__expired() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.IN
            type = LedgerType.LIGHTNING
            value = BTCCurrency(10000)
            status = LedgerStatus.EXPIRED
            memo = "--memo--"
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<ImageView>(R.id.icon).tag).isEqualTo(R.drawable.ic_transaction_canceled)
        assertThat(holder.itemView.findViewById<TextView>(R.id.address).text).isEqualTo("Lightning Invoice")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).text).isEqualTo("--memo--")
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).visibility).isEqualTo(View.VISIBLE)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .primaryCurrencyText).isEqualTo("expired")
    }

    @Test
    fun lightning_invoice__request__failed() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.IN
            type = LedgerType.LIGHTNING
            value = BTCCurrency(10000)
            status = LedgerStatus.FAILED
            memo = "--memo--"
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<ImageView>(R.id.icon).tag).isEqualTo(R.drawable.ic_transaction_canceled)
        assertThat(holder.itemView.findViewById<TextView>(R.id.address).text).isEqualTo("Lightning Invoice")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).text).isEqualTo("--memo--")
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).visibility).isEqualTo(View.VISIBLE)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .primaryCurrencyText).isEqualTo("failed")
    }

    @Test
    fun lightning_invoice__request__completed() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.IN
            type = LedgerType.LIGHTNING
            value = BTCCurrency(10000)
            status = LedgerStatus.COMPLETED
            memo = "--memo--"
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<ImageView>(R.id.icon).tag).isEqualTo(R.drawable.ic_transaction_receive)
        assertThat(holder.itemView.findViewById<TextView>(R.id.address).text).isEqualTo("Lightning Invoice")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).text).isEqualTo("--memo--")
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).visibility).isEqualTo(View.VISIBLE)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
    }

    @Test
    fun lightning_invoice__payment() {
        val adapter = createAdapter()
        val parent = createParent()

        val invoice = createInvoice().apply {
            direction = LedgerDirection.OUT
            type = LedgerType.LIGHTNING
            value = BTCCurrency(10000)
            status = LedgerStatus.COMPLETED
            memo = "--memo--"
        }

        adapter.invoices = listOf(invoice)
        val holder = adapter.onCreateViewHolder(parent, LightningHistoryAdapter.ITEM_VIEW_TYPE)
        adapter.onBindViewHolder(holder, 0)

        assertThat(holder.itemView.findViewById<ImageView>(R.id.icon).tag).isEqualTo(R.drawable.ic_transaction_send)
        assertThat(holder.itemView.findViewById<TextView>(R.id.address).text).isEqualTo("Paid Invoice")
        assertThat(holder.itemView.findViewById<TextView>(R.id.confirmations).visibility).isEqualTo(View.GONE)
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).text).isEqualTo("--memo--")
        assertThat(holder.itemView.findViewById<TextView>(R.id.transaction_memo).visibility).isEqualTo(View.VISIBLE)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .totalCrypto.toLong()).isEqualTo(10000)
        assertThat(holder.itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view)
                .fiatValue.toLong()).isEqualTo(100)
    }
}