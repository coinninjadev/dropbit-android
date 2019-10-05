package com.coinninja.coinkeeper.ui.lightning.history

import android.widget.ImageView
import android.widget.TextView
import app.dropbit.commons.currency.FiatCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView

fun TextView.lightningTransferIn() {
    text = context.getString(R.string.tx_history_transfer_lightning_in)
}

fun TextView.lightningTransferOut() {
    text = context.getString(R.string.tx_history_transfer_lightning_out)
}

fun TextView.lightningPaidOut() {
    text = context.getString(R.string.tx_history_lightning_out)
}

fun TextView.lightningRequestIn() {
    text = context.getString(R.string.tx_history_lightning_in)
}

fun TextView.historyItemPending() {
    this.show()
    text = context.getString(R.string.lnd_state_pending)
}

fun TextView.setMemo(memo: String?) {
    this.gone()
    memo?.let {
        if (it.isNotEmpty()) {
            text = it
            this.show()
        }
    }
}

fun ImageView.clear() {
    tag = null
}

fun ImageView.lightningTransferIn() {
    tag = R.drawable.ic_transfer_in
    setImageResource(R.drawable.ic_transfer_in)
}

fun ImageView.lightningTransferOut() {
    tag = R.drawable.ic_transfer_out
    setImageResource(R.drawable.ic_transfer_out)
}

fun ImageView.lightningPayedInvoice() {
    tag = R.drawable.ic_transaction_send
    setImageResource(R.drawable.ic_transaction_send)
}

fun ImageView.lightningPendingInvoice() {
    tag = R.drawable.ic_lightning_invoice
    setImageResource(R.drawable.ic_lightning_invoice)
}

fun ImageView.lightningCompletedInvoice() {
    tag = R.drawable.ic_transaction_receive
    setImageResource(R.drawable.ic_transaction_receive)
}

fun ImageView.expiredInvoice() {
    tag = R.drawable.ic_transaction_canceled
    setImageResource(R.drawable.ic_transaction_canceled)
}

fun ImageView.failedInvoice() {
    tag = R.drawable.ic_transaction_canceled
    setImageResource(R.drawable.ic_transaction_canceled)
}

fun DefaultCurrencyDisplayView.lightningTransferIn() {
    tag = R.drawable.primary_receive_pill
    setPillResource(R.drawable.primary_receive_pill)
}

fun DefaultCurrencyDisplayView.lightningTransferOut() {
    tag = R.drawable.primary_sent_pill
    setPillResource(R.drawable.primary_sent_pill)
}

fun DefaultCurrencyDisplayView.lightningPayedInvoice() {
    tag = R.drawable.primary_sent_pill
    setPillResource(R.drawable.primary_sent_pill)
}

fun DefaultCurrencyDisplayView.lightningPendingInvoice() {
    tag = R.drawable.primary_lightning_pill
    setPillResource(R.drawable.primary_lightning_pill)
}

fun DefaultCurrencyDisplayView.lightningCompletedInvoice() {
    tag = R.drawable.primary_receive_pill
    setPillResource(R.drawable.primary_receive_pill)
}

fun DefaultCurrencyDisplayView.failedInvoice() {
    tag = R.drawable.primary_expires_pill
    setPillResource(R.drawable.primary_expires_pill)
    if (defaultCurrencies.primaryCurrency is FiatCurrency)
        primaryCurrencyView.text = resources.getString(R.string.failed)
    else secondaryCurrencyView.text = resources.getString(R.string.failed)
}

fun DefaultCurrencyDisplayView.expiredInvoice() {
    tag = R.drawable.primary_expires_pill
    setPillResource(R.drawable.primary_expires_pill)
    if (defaultCurrencies.primaryCurrency is FiatCurrency)
        primaryCurrencyView.text = resources.getString(R.string.expired)
    else secondaryCurrencyView.text = resources.getString(R.string.expired)
}

fun DefaultCurrencyDisplayView.clear() {
    tag = R.drawable.primary_expires_pill
    setPillResource(R.drawable.primary_expires_pill)
}

