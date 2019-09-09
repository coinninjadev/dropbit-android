package com.coinninja.coinkeeper.ui.lightning.history

import android.widget.ImageView
import android.widget.TextView
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.coinkeeper.R

fun TextView.lightningTransferIn() {
    text = context.getString(R.string.tx_history_transfer_lightning_in)
}

fun TextView.lightningTransferOut() {
    text = context.getString(R.string.tx_history_transfer_lightning_out)
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

fun ImageView.lightningTransferIn() {
    tag = R.drawable.ic_transfer_in
    setImageResource(R.drawable.ic_transfer_in)
}

fun ImageView.lightningTransferOut() {
    tag = R.drawable.ic_transfer_out
    setImageResource(R.drawable.ic_transfer_out)
}

fun ImageView.clear() {
    tag = null
}