package com.coinninja.coinkeeper.ui.lightning.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.coinninja.cn.persistance.model.LedgerDirection
import app.coinninja.cn.persistance.model.LedgerSettlementDetail
import app.coinninja.cn.persistance.model.LedgerSettlementDetail.PaymentType
import app.coinninja.cn.persistance.model.LedgerStatus
import app.coinninja.cn.persistance.model.LedgerType
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.image.TwitterCircleTransform
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView
import com.squareup.picasso.Picasso

@Mockable
class LightningHistoryAdapter constructor(
        val activityNavigationUtil: ActivityNavigationUtil,
        val walletHelper: WalletHelper,
        val currencyPreference: CurrencyPreference,
        val picasso: Picasso,
        val twitterCircleTransform: TwitterCircleTransform
) : RecyclerView.Adapter<LightningHistoryAdapter.ViewHolder>() {

    val defaultCurrencies: DefaultCurrencies = currencyPreference.currenciesPreference

    var settlements: List<LedgerSettlementDetail> = emptyList()
        get() = field
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun getItemViewType(position: Int): Int = if (settlements.isEmpty()) EMPTYSTATE_VIEW_TYPE else ITEM_VIEW_TYPE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == EMPTYSTATE_VIEW_TYPE) {
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_lightning_empty_state, parent, false), viewType, picasso, twitterCircleTransform)
        } else {
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_item_transaction_record, parent, false), viewType, picasso, twitterCircleTransform)
        }
    }

    override fun getItemCount(): Int = if (settlements.isEmpty()) 1 else settlements.size

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
        val settlement = settlements[position]
        holder.bindIconWith(settlement)
                .bindAddressWith(settlement)
                .bindAmount(settlement, defaultCurrencies, walletHelper.latestPrice)
                .bindMemo(settlement)
                .bindState(settlement)
    }

    class ViewHolder(itemView: View, val type: Int, val picasso: Picasso, val twitterCircleTransform: TwitterCircleTransform) : RecyclerView.ViewHolder(itemView) {
        fun bindIconWith(settlement: LedgerSettlementDetail): ViewHolder {
            itemView.findViewById<ImageView>(R.id.icon).also {
                when (settlement.paymentType) {
                    PaymentType.CanceledSentInvite, PaymentType.CanceledReceivedInvite, PaymentType.FailedInvoice -> it.failedInvoice()
                    PaymentType.ExpiredReceivedInvite, PaymentType.ExpiredSentInvite, PaymentType.ExpiredInvoice -> it.expiredInvoice()
                    PaymentType.TransferIn -> it.lightningTransferIn()
                    PaymentType.TransferOut -> it.lightningTransferOut()
                    PaymentType.PendingReceiveInvoice -> it.lightningPendingInvoice()
                    PaymentType.ReceivedInvite, PaymentType.CompletedReceiveInvoice -> it.lightningCompletedInvoice()
                    PaymentType.SentInvite, PaymentType.CompletedPayment -> it.lightningPayedInvoice()
                    else -> it.clear()
                }

                settlement.avatar?.let { avatar ->
                    picasso.load(avatar).transform(twitterCircleTransform).into(it)
                }
            }
            return this
        }

        fun bindAddressWith(settlement: LedgerSettlementDetail): ViewHolder {
            itemView.findViewById<TextView>(R.id.address).also {
                when {
                    (settlement.invoiceDirection == LedgerDirection.IN && settlement.invoiceType == LedgerType.BTC) ->
                        it.lightningTransferIn()
                    (settlement.invoiceDirection == LedgerDirection.OUT && settlement.invoiceType == LedgerType.BTC) ->
                        it.lightningTransferOut()
                    (settlement.invoiceDirection == LedgerDirection.OUT && settlement.invoiceType == LedgerType.LIGHTNING) ->
                        it.lightningPaidOut()
                    (settlement.invoiceDirection == LedgerDirection.IN && settlement.invoiceType == LedgerType.LIGHTNING) ->
                        it.lightningRequestIn()
                    else -> it.text = ""
                }

                settlement.identityFormatted()?.let { identityString ->
                    it.text = identityString
                }
            }
            return this
        }

        fun bindAmount(settlement: LedgerSettlementDetail, defaultCurrencies: DefaultCurrencies, latestPrice: USDCurrency): ViewHolder {
            itemView.findViewById<DefaultCurrencyDisplayView>(R.id.default_currency_view).apply {
                accountMode = AccountMode.LIGHTNING
                renderValues(defaultCurrencies, settlement.cryptoAmount, settlement.usdValueConsidering(latestPrice))
                when (settlement.paymentType) {
                    PaymentType.FailedInvoice -> failedInvoice()
                    PaymentType.CanceledReceivedInvite, PaymentType.CanceledSentInvite -> canceledInvoice()
                    PaymentType.ExpiredReceivedInvite, PaymentType.ExpiredSentInvite, PaymentType.ExpiredInvoice -> expiredInvoice()
                    PaymentType.TransferIn -> lightningTransferIn()
                    PaymentType.TransferOut -> lightningTransferOut()
                    PaymentType.PendingReceiveInvoice -> lightningPendingInvoice()
                    PaymentType.ReceivedInvite, PaymentType.CompletedReceiveInvoice -> lightningCompletedInvoice()
                    PaymentType.SentInvite, PaymentType.CompletedPayment -> lightningPayedInvoice()
                    else -> clear()
                }

            }
            return this
        }

        fun bindState(settlement: LedgerSettlementDetail): ViewHolder {
            itemView.findViewById<TextView>(R.id.confirmations).also {
                it.gone()
                if (settlement.invoiceStatus == LedgerStatus.PENDING
                        && settlement.invoiceDirection == LedgerDirection.IN
                        && settlement.invoiceType == LedgerType.BTC
                ) {
                    it.historyItemPending()
                }
            }
            return this
        }

        fun bindMemo(settlement: LedgerSettlementDetail): ViewHolder {
            val memo = itemView.findViewById<TextView>(R.id.transaction_memo)
            when {
                settlement.invoiceType == LedgerType.BTC -> memo.gone()
                else -> memo.setMemo(settlement.invoiceMemo)
            }
            return this
        }
    }

    companion object {
        internal const val ITEM_VIEW_TYPE = 0
        internal const val EMPTYSTATE_VIEW_TYPE = 1
    }
}
