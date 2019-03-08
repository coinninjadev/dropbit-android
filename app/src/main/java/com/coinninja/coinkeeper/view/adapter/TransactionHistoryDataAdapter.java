package com.coinninja.coinkeeper.view.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.ConfirmationState;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.InviteState;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;

import org.greenrobot.greendao.query.LazyList;

import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static com.coinninja.android.helpers.Views.withId;

public class TransactionHistoryDataAdapter extends Adapter<TransactionHistoryDataAdapter.ViewHolder> {


    private LazyList<TransactionsInvitesSummary> transactions;
    private OnItemClickListener onItemClickListener;
    private TransactionAdapterUtil transactionAdapterUtil;
    private Currency conversionCurrency;

    public TransactionHistoryDataAdapter(LazyList<TransactionsInvitesSummary> transactions,
                                         TransactionHistoryDataAdapter.OnItemClickListener onItemClickListener,
                                         TransactionAdapterUtil transactionAdapterUtil) {
        this.transactions = transactions;
        this.onItemClickListener = onItemClickListener;
        this.transactionAdapterUtil = transactionAdapterUtil;
    }

    @Override
    public TransactionHistoryDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_transaction_record, parent, false);
        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setCurrency(conversionCurrency);
        if (!transactions.isClosed()) {
            BindableTransaction transaction =
                    transactionAdapterUtil.translateTransaction(transactions.get(position));
            holder.bindToTransaction(transaction, position);
        }
    }

    public void setTransactions(LazyList<TransactionsInvitesSummary> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public Currency getConversionCurrency() {
        return conversionCurrency;
    }

    public void setConversionCurrency(Currency conversionCurrency) {
        this.conversionCurrency = conversionCurrency;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final OnItemClickListener onItemClickListener;
        private View itemView;
        private Currency currency;
        private int position;

        public ViewHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(V -> onClick(this));
            this.onItemClickListener = onItemClickListener;
        }


        private void onClick(ViewHolder viewHolder) {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(itemView, getItemPosition());
        }

        public View getItemView() {
            return itemView;
        }

        protected void bindToTransaction(BindableTransaction transaction, int position) {
            this.position = position;
            bindIcon(itemView.findViewById(R.id.icon), transaction);
            bindConfirmations(itemView.findViewById(R.id.confirmations), transaction);
            bindAddress(itemView.findViewById(R.id.address), transaction);
            bindBTCValue(itemView.findViewById(R.id.btc_value), transaction);
            bindTransactionTime(itemView.findViewById(R.id.blocktime), transaction);
            calculateValue(itemView.findViewById(R.id.usd_value), transaction);
            bindSendState(itemView, transaction);
            bindMemo(itemView, transaction);
        }

        private void bindMemo(View itemView, BindableTransaction transaction) {
            TextView memoView = withId(itemView, R.id.transaction_memo);
            if ("".equals(transaction.getMemo())) {
                memoView.setVisibility(View.GONE);
                return;
            }
            memoView.setText(transaction.getMemo());
            memoView.setVisibility(View.VISIBLE);
        }

        private void calculateValue(TextView view, BindableTransaction transaction) {
            String text = "";
            int color = 0;
            switch (transaction.getSendState()) {
                case FAILED_TO_BROADCAST_RECEIVE:
                case RECEIVE_CANCELED:
                case RECEIVE:
                    text += "+ " + new BTCCurrency(transaction.getValue()).toUSD(currency).toFormattedCurrency();
                    color = view.getResources().getColor(R.color.colorPrimaryDark);
                    break;
                case FAILED_TO_BROADCAST_SEND:
                case FAILED_TO_BROADCAST_TRANSFER:
                case SEND_CANCELED:
                case SEND:
                    text += "- " + new BTCCurrency(transaction.getTotalTransactionCost()).toUSD(currency).toFormattedCurrency();
                    color = view.getResources().getColor(R.color.color_error);
                    break;
                case TRANSFER:
                    text += "- " + new BTCCurrency(transaction.getFee()).toUSD(currency).toFormattedCurrency();
                    color = view.getResources().getColor(R.color.color_error);
                    break;
            }

            view.setText(text);
            view.setTextColor(color);
        }

        private void bindTransactionTime(TextView view, BindableTransaction transaction) {
            view.setText(transaction.getTxTime());
        }

        public void bindIdentifyingTarget(TextView view, BindableTransaction transaction) {
            view.setText(transaction.getIdentifiableTarget());
        }

        private void bindAddress(TextView view, BindableTransaction transaction) {
            switch (transaction.getSendState()) {
                case FAILED_TO_BROADCAST_TRANSFER:
                case TRANSFER:
                    view.setText(view.getResources().getText(R.string.send_to_self));
                    break;
                default:
                    bindIdentifyingTarget(view, transaction);
            }
        }

        private void bindBTCValue(TextView view, BindableTransaction transaction) {
            String text = "";
            switch (transaction.getSendState()) {
                case TRANSFER:
                    text = new BTCCurrency(transaction.getFee()).toFormattedString();
                    break;
                default:
                    text = new BTCCurrency(transaction.getValue()).toFormattedString();
                    break;
            }

            view.setText(text);
        }

        private void bindIcon(ImageView icon, BindableTransaction transaction) {
            switch (transaction.getSendState()) {
                case TRANSFER:
                case SEND:
                    icon.setTag(R.drawable.ic_transaction_send);
                    icon.setImageResource(R.drawable.ic_transaction_send);
                    break;
                case RECEIVE:
                    icon.setTag(R.drawable.ic_transaction_receive);
                    icon.setImageResource(R.drawable.ic_transaction_receive);
                    break;
                default:
                    icon.setTag(R.drawable.ic_transaction_canceled);
                    icon.setImageResource(R.drawable.ic_transaction_canceled);
                    break;
            }
        }

        private void bindConfirmations(TextView view, BindableTransaction transaction) {
            bindTxConfirmations(view, transaction);
            bindInviteState(view, transaction);
        }

        private void bindTxConfirmations(TextView view, BindableTransaction transaction) {
            ConfirmationState confirmationState = transaction.getConfirmationState();

            if (confirmationState == null) {
                return;
            }

            String text;
            Resources resources = view.getResources();
            view.setTextColor(resources.getColor(R.color.font_default));
            switch (confirmationState) {
                case CONFIRMED:
                case TWO_CONFIRMS:
                case ONE_CONFIRM:
                    text = view.getResources().getString(R.string.confirmations_view_stage_5);
                    break;
                case UNCONFIRMED:
                    text = view.getResources().getString(R.string.confirmations_view_stage_4);
                    break;
                default:
                    text = view.getResources().getString(R.string.confirmations_view_stage_3);
            }

            if (text != null)
                view.setText(text);
        }

        private void bindSendState(View view, BindableTransaction transaction) {
            TextView confirmations = view.findViewById(R.id.confirmations);
            switch (transaction.getSendState()) {
                case FAILED_TO_BROADCAST_RECEIVE:
                case FAILED_TO_BROADCAST_SEND:
                case FAILED_TO_BROADCAST_TRANSFER:
                    confirmations.setText(R.string.history_failed_to_broadcast);
                    confirmations.setTextColor(view.getResources().getColor(R.color.color_error));
                    break;
            }
        }

        private void bindInviteState(TextView view, BindableTransaction transaction) {
            InviteState inviteState = transaction.getInviteState();

            if (inviteState == null)
                return;

            String text;
            Resources resources = view.getResources();
            view.setTextColor(resources.getColor(R.color.font_default));
            switch (inviteState) {
                case SENT_PENDING:
                    text = resources.getString(R.string.history_invite_sent_pending);
                    break;
                case RECEIVED_PENDING:
                    text = resources.getString(R.string.history_invite_received_pending);
                    break;
                case EXPIRED:
                    text = resources.getString(R.string.history_invite_expired);
                    view.setTextColor(resources.getColor(R.color.color_error));
                    break;
                case CANCELED:
                    text = resources.getString(R.string.history_invite_canceled);
                    view.setTextColor(resources.getColor(R.color.color_error));
                    break;
                default:
                    text = null;
            }

            if (text != null)
                view.setText(text);
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
        }

        public int getItemPosition() {
            return position;
        }

    }
}
