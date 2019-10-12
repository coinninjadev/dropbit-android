package com.coinninja.coinkeeper.ui.transaction.history;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coinninja.android.helpers.Views;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.image.TwitterCircleTransform;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.ConfirmationState;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.InviteState;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.query.LazyList;

import javax.inject.Inject;

import static androidx.recyclerview.widget.RecyclerView.Adapter;

public class TransactionHistoryDataAdapter extends Adapter<TransactionHistoryDataAdapter.ViewHolder> implements DefaultCurrencyChangeObserver {

    static int FOOTER_TYPE = 1;
    static int STANDARD_TYPE = 0;
    private final Picasso picasso;
    private final TwitterCircleTransform circleTransform;
    private final WalletHelper walletHelper;
    private final Analytics analytics;
    private final ActivityNavigationUtil activityNavigationUtil;
    private LazyList<TransactionsInvitesSummary> transactions;
    private OnItemClickListener onItemClickListener;
    private TransactionAdapterUtil transactionAdapterUtil;
    private DefaultCurrencies defaultCurrencies;
    private DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier;

    @Inject
    public TransactionHistoryDataAdapter(TransactionAdapterUtil transactionAdapterUtil,
                                         DefaultCurrencies defaultCurrencies, Picasso picasso,
                                         TwitterCircleTransform circleTransform, WalletHelper walletHelper,
                                         Analytics analytics, ActivityNavigationUtil activityNavigationUtil

    ) {
        this.transactionAdapterUtil = transactionAdapterUtil;
        this.defaultCurrencies = defaultCurrencies;
        this.picasso = picasso;
        this.circleTransform = circleTransform;
        this.walletHelper = walletHelper;
        this.analytics = analytics;
        this.activityNavigationUtil = activityNavigationUtil;
    }

    @Override
    public TransactionHistoryDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == STANDARD_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_transaction_record, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_empty_transactions, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == STANDARD_TYPE) {
            bindToTransactionRecord(holder, position);
        } else {
            bindToFooter(holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position >= transactions.size() ? FOOTER_TYPE : STANDARD_TYPE;
    }

    @Override
    public int getItemCount() {
        return transactions.size() < 2 ? transactions.size() + 1 : transactions.size();
    }

    public void setTransactions(LazyList<TransactionsInvitesSummary> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setDefaultCurrencyChangeViewNotifier(DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier) {
        this.defaultCurrencyChangeViewNotifier = defaultCurrencyChangeViewNotifier;
        defaultCurrencyChangeViewNotifier.observeDefaultCurrencyChange(this);
    }

    @Override
    public void onDefaultCurrencyChanged(DefaultCurrencies defaultCurrencies) {
        this.defaultCurrencies = defaultCurrencies;
    }

    public DefaultCurrencies getDefaultCurrencies() {
        return defaultCurrencies;
    }

    public void setDefaultCurrencies(DefaultCurrencies defaultCurrencies) {
        this.defaultCurrencies = defaultCurrencies;
    }

    void onClick(View view, int position) {
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(view, position);
    }

    private void bindToFooter(ViewHolder holder) {
        TransactionEmptyStateView emptyStateView = holder.itemView.findViewById(R.id.empty_state_view);
        emptyStateView.setupUIForWallet(transactions.size(), walletHelper.getBalance().toSatoshis());
        emptyStateView.setGetBitcoinButtonClickListener(v -> {
            analytics.trackEvent(Analytics.EVENT_GET_BITCOIN);
            activityNavigationUtil.navigateToBuyBitcoin(v.getContext());
        });

        emptyStateView.setLearnBitcoinButtonClickListener(v -> {
            analytics.trackEvent(Analytics.EVENT_LEARN_BITCOIN);
            activityNavigationUtil.navigateToLearnBitcoin(v.getContext());
        });

        emptyStateView.setSpendBitcoinButtonClickListener(v -> {
            analytics.trackEvent(Analytics.EVENT_SPEND_BITCOIN);
            activityNavigationUtil.navigateToSpendBitcoin(v.getContext());
        });
    }

    private void bindToTransactionRecord(ViewHolder holder, int position) {
        if (!transactions.isClosed()) {
            BindableTransaction transaction =
                    transactionAdapterUtil.translateTransaction(transactions.get(position));
            holder.setDefaultCurrencyChangeViewNotifier(defaultCurrencyChangeViewNotifier);
            holder.bindToTransaction(transaction, defaultCurrencies, picasso, circleTransform);
            holder.getItemView().setOnClickListener(v -> onClick(holder.getItemView(), position));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        private DefaultCurrencies defaultCurrencies;
        private DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }


        public View getItemView() {
            return itemView;
        }

        public void setDefaultCurrencyChangeViewNotifier(DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier) {
            this.defaultCurrencyChangeViewNotifier = defaultCurrencyChangeViewNotifier;
        }

        public void bindIdentifyingTarget(TextView view, BindableTransaction transaction) {
            view.setText(transaction.getIdentifiableTarget());
            if (transaction.getIdentityType() == IdentityType.TWITTER) {
                Views.INSTANCE.setCompondDrawableOnStart(view, R.drawable.twitter_icon_blue, .6F);
                view.setCompoundDrawablePadding(10);
            } else {
                Views.INSTANCE.clearCompoundDrawablesOn(view);
            }

            if (transaction.getSendState() == BindableTransaction.SendState.LOAD_LIGHTNING) {
                view.setText(R.string.tx_history_load_lightning);
            } else if (transaction.getSendState() == BindableTransaction.SendState.UNLOAD_LIGHTNING) {
                view.setText(R.string.tx_history_withdraw_lightning);
            }
        }

        protected void bindToTransaction(BindableTransaction transaction, DefaultCurrencies defaultCurrencies, Picasso picasso, TwitterCircleTransform circleTransform) {
            this.defaultCurrencies = defaultCurrencies;
            bindIcon(itemView.findViewById(R.id.icon), transaction, picasso, circleTransform);
            bindConfirmations(itemView.findViewById(R.id.confirmations), transaction);
            bindAddress(itemView.findViewById(R.id.address), transaction);
            bindValue(transaction);
            bindSendState(itemView, transaction);
            bindMemo(itemView, transaction);
        }

        private void bindValue(BindableTransaction transaction) {
            DefaultCurrencyDisplayView view = itemView.findViewById(R.id.default_currency_view);
            view.renderValues(defaultCurrencies, transaction.getBasicDirection(),
                    transaction.totalCryptoForSendState(), transaction.totalFiatForSendState());
            if (defaultCurrencyChangeViewNotifier != null)
                defaultCurrencyChangeViewNotifier.observeDefaultCurrencyChange(view);
        }

        private void bindMemo(View itemView, BindableTransaction transaction) {
            TextView memoView = itemView.findViewById(R.id.transaction_memo);
            if ("".equals(transaction.getMemo())) {
                memoView.setVisibility(View.GONE);
                return;
            }
            memoView.setText(transaction.getMemo());
            memoView.setVisibility(View.VISIBLE);
        }


        private void bindAddress(TextView view, BindableTransaction transaction) {
            bindIdentifyingTarget(view, transaction);
        }

        private void bindIcon(ImageView icon, BindableTransaction transaction, Picasso picasso, TwitterCircleTransform circleTransform) {
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
                case UNLOAD_LIGHTNING:
                    icon.setTag(R.drawable.ic_transfer_in);
                    icon.setImageResource(R.drawable.ic_transfer_in);
                    break;
                case LOAD_LIGHTNING:
                    icon.setTag(R.drawable.ic_transfer_out);
                    icon.setImageResource(R.drawable.ic_transfer_out);
                    break;
                default:
                    icon.setTag(R.drawable.ic_transaction_canceled);
                    icon.setImageResource(R.drawable.ic_transaction_canceled);
                    break;
            }

            if (transaction.getProfileUrl() != null) {
                picasso.load(transaction.getProfileUrl()).transform(circleTransform).into(icon);
            }
        }

        private void bindConfirmations(TextView view, BindableTransaction transaction) {
            bindTxConfirmations(view, transaction);
            bindInviteState(view, transaction);
        }

        private void bindTxConfirmations(TextView view, BindableTransaction transaction) {
            ConfirmationState confirmationState = transaction.getConfirmationState();
            view.setText("");

            if (confirmationState == null) {
                return;
            }

            String text;
            view.setVisibility(View.VISIBLE);
            switch (confirmationState) {
                case CONFIRMED:
                case TWO_CONFIRMS:
                case ONE_CONFIRM:
                    text = view.getResources().getString(R.string.confirmations_view_stage_5);
                    view.setVisibility(View.GONE);
                    break;
                case UNCONFIRMED:
                    text = view.getResources().getString(R.string.confirmations_view_stage_4);
                    view.setVisibility(View.GONE);
                    break;
                default:
                    text = view.getResources().getString(R.string.confirmations_view_stage_3);
            }

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

            if (inviteState == null || transaction.getConfirmationCount() > 0)
                return;

            String text;
            Resources resources = view.getResources();
            switch (inviteState) {
                case SENT_ADDRESS_PROVIDED:
                case SENT_PENDING:
                    text = resources.getString(R.string.history_invite_sent_pending);
                    view.setVisibility(View.VISIBLE);
                    break;
                case RECEIVED_ADDRESS_PROVIDED:
                case RECEIVED_PENDING:
                    text = resources.getString(R.string.history_invite_received_pending);
                    view.setVisibility(View.VISIBLE);
                    break;
                case EXPIRED:
                    text = resources.getString(R.string.history_invite_expired);
                    view.setVisibility(View.VISIBLE);
                    break;
                case CANCELED:
                    text = resources.getString(R.string.history_invite_canceled);
                    view.setVisibility(View.VISIBLE);
                    break;
                default:
                    text = null;
            }

            if (text != null) {
                view.setText(text);
            }
        }
    }
}
