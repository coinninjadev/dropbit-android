package com.coinninja.coinkeeper.ui.lightning.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.coinninja.cn.persistance.model.LedgerDirection
import app.coinninja.cn.persistance.model.LedgerStatus
import app.coinninja.cn.persistance.model.LedgerType
import app.coinninja.cn.persistance.model.LightningInvoice
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView

@Mockable
class LightningHistoryAdapter constructor(
        val activityNavigationUtil: ActivityNavigationUtil,
        val walletHelper: WalletHelper,
        val currencyPreference: CurrencyPreference
) : RecyclerView.Adapter<LightningHistoryAdapter.ViewHolder>() {

    val defaultCurrencies: DefaultCurrencies = currencyPreference.currenciesPreference

    var invoices: List<LightningInvoice> = emptyList()
        get() = field
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun getItemViewType(position: Int): Int = if (invoices.isEmpty()) EMPTYSTATE_VIEW_TYPE else ITEM_VIEW_TYPE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == EMPTYSTATE_VIEW_TYPE) {
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_lightning_empty_state, parent, false), viewType)
        } else {
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_transaction_record, parent, false), viewType)
        }
    }

    override fun getItemCount(): Int = if (invoices.isEmpty()) 1 else invoices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.type == ITEM_VIEW_TYPE) {
            bindItem(holder, position)
        } else {
            bindEmptyState(holder)
        }
    }

    private fun bindEmptyState(holder: ViewHolder) {
        holder.itemView.findViewById<View>(R.id.quick_load_5)
                ?.setOnClickListener { activityNavigationUtil.showLoadLightningWith(it.context, USDCurrency(5_00)) }
        holder.itemView.findViewById<View>(R.id.quick_load_20)
                ?.setOnClickListener { activityNavigationUtil.showLoadLightningWith(it.context, USDCurrency(20_00)) }
        holder.itemView.findViewById<View>(R.id.quick_load_50)
                ?.setOnClickListener { activityNavigationUtil.showLoadLightningWith(it.context, USDCurrency(50_00)) }
        holder.itemView.findViewById<View>(R.id.quick_load_100)
                ?.setOnClickListener { activityNavigationUtil.showLoadLightningWith(it.context, USDCurrency(100_00)) }
        holder.itemView.findViewById<View>(R.id.custom_amount)?.setOnClickListener {
            activityNavigationUtil.showLoadLightningWith(it.context)
        }
    }

    private fun bindItem(holder: ViewHolder, position: Int) {
        val invoice = invoices[position]
        holder.bindIconWith(invoice)
                .bindAddressWith(invoice)
                .bindAmount(invoice, defaultCurrencies, walletHelper.latestPrice)
                .bindMemo(invoice)
                .bindState(invoice)
    }

    class ViewHolder(itemView: View, val type: Int) : RecyclerView.ViewHolder(itemView) {
        fun bindIconWith(invoice: LightningInvoice): ViewHolder {
            itemView.findViewById<ImageView>(R.id.icon).also {
                when {
                    (invoice.status == LedgerStatus.FAILED) ->
                        it.failedInvoice()
                    (invoice.status == LedgerStatus.EXPIRED) ->
                        it.expiredInvoice()
                    (invoice.direction == LedgerDirection.IN && invoice.type == LedgerType.BTC) ->
                        it.lightningTransferIn()
                    (invoice.direction == LedgerDirection.OUT && invoice.type == LedgerType.BTC) ->
                        it.lightningTransferOut()
                    (invoice.direction == LedgerDirection.IN && invoice.type == LedgerType.LIGHTNING && invoice.status == LedgerStatus.PENDING) ->
                        it.lightningPendingInvoice()
                    (invoice.direction == LedgerDirection.IN && invoice.type == LedgerType.LIGHTNING && invoice.status == LedgerStatus.COMPLETED) ->
                        it.lightningCompletedInvoice()
                    (invoice.direction == LedgerDirection.OUT && invoice.type == LedgerType.LIGHTNING) ->
                        it.lightningPayedInvoice()
                    else -> it.clear()
                }
            }
            return this
        }

        fun bindAddressWith(invoice: LightningInvoice): ViewHolder {
            itemView.findViewById<TextView>(R.id.address).also {
                when {
                    (invoice.direction == LedgerDirection.IN && invoice.type == LedgerType.BTC) ->
                        it.lightningTransferIn()
                    (invoice.direction == LedgerDirection.OUT && invoice.type == LedgerType.BTC) ->
                        it.lightningTransferOut()
                    (invoice.direction == LedgerDirection.OUT && invoice.type == LedgerType.LIGHTNING) ->
                        it.lightningPaidOut()
                    (invoice.direction == LedgerDirection.IN && invoice.type == LedgerType.LIGHTNING) ->
                        it.lightningRequestIn()
                    else -> it.text = ""
                }
            }
            return this
        }

        fun bindAmount(invoice: LightningInvoice, defaultCurrencies: DefaultCurrencies, latestPrice: USDCurrency): ViewHolder {
            itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view).apply {
                accountMode = AccountMode.LIGHTNING
                renderValues(defaultCurrencies, invoice.value, invoice.value.toFiat(latestPrice))
                when {
                    (invoice.status == LedgerStatus.FAILED) -> failedInvoice()
                    (invoice.status == LedgerStatus.EXPIRED) -> expiredInvoice()
                    (invoice.direction == LedgerDirection.IN && invoice.type == LedgerType.BTC) ->
                        lightningTransferIn()
                    (invoice.direction == LedgerDirection.OUT && invoice.type == LedgerType.BTC) ->
                        lightningTransferOut()
                    (invoice.direction == LedgerDirection.IN && invoice.type == LedgerType.LIGHTNING && invoice.status == LedgerStatus.PENDING) ->
                        lightningPendingInvoice()
                    (invoice.direction == LedgerDirection.IN && invoice.type == LedgerType.LIGHTNING && invoice.status == LedgerStatus.COMPLETED) ->
                        lightningCompletedInvoice()
                    (invoice.direction == LedgerDirection.OUT && invoice.type == LedgerType.LIGHTNING) ->
                        lightningPayedInvoice()
                    else -> clear()
                }

            }
            return this
        }

        fun bindState(invoice: LightningInvoice): ViewHolder {
            itemView.findViewById<TextView>(R.id.confirmations).also {
                it.gone()
                if (invoice.status == LedgerStatus.PENDING
                        && invoice.direction == LedgerDirection.IN
                        && invoice.type == LedgerType.BTC
                ) {
                    it.historyItemPending()
                }
            }
            return this
        }

        fun bindMemo(invoice: LightningInvoice): ViewHolder {
            val memo = itemView.findViewById<TextView>(R.id.transaction_memo)
            when {
                invoice.type == LedgerType.BTC -> memo.gone()
                else -> memo.setMemo(invoice.memo)
            }
            return this
        }
    }

    companion object {
        internal const val ITEM_VIEW_TYPE = 0
        internal const val EMPTYSTATE_VIEW_TYPE = 1
    }
}