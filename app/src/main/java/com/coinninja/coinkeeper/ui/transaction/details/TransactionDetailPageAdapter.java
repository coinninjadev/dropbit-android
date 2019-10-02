package com.coinninja.coinkeeper.ui.transaction.details;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;

import com.coinninja.android.helpers.Resources;
import com.coinninja.android.helpers.Views;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.dropbit.DropBitService;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.memo.MemoCreator;
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier;
import com.coinninja.coinkeeper.ui.transaction.history.DefaultCurrencyChangeObserver;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.TwitterUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder;
import com.coinninja.coinkeeper.util.uri.UriUtil;
import com.coinninja.coinkeeper.util.uri.routes.DropbitRoute;
import com.coinninja.coinkeeper.view.ConfirmationsView;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;
import com.coinninja.coinkeeper.view.subviews.SharedMemoView;
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView;

import org.greenrobot.greendao.query.LazyList;

import javax.inject.Inject;

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.USDCurrency;

public class TransactionDetailPageAdapter extends PagerAdapter implements DefaultCurrencyChangeObserver {

    final WalletHelper walletHelper;
    TransactionAdapterUtil transactionAdapterUtil;
    LazyList<TransactionsInvitesSummary> transactions;
    private DefaultCurrencies defaultCurrencies;
    private TwitterUtil twitterUtil;
    private Analytics analytics;
    private TransactionDetailObserver transactionDetailObserver;
    private DropbitUriBuilder dropbitUriBuilder;
    private DropbitRoute tooltipId;
    private DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier;
    private MemoCreator memoCreator;

    @Inject
    TransactionDetailPageAdapter(WalletHelper walletHelper, TransactionAdapterUtil transactionAdapterUtil, DefaultCurrencies defaultCurrencies,
                                 MemoCreator memoCreator, TwitterUtil twitterUtil, Analytics analytics) {
        this.defaultCurrencies = defaultCurrencies;
        this.twitterUtil = twitterUtil;
        this.analytics = analytics;
        dropbitUriBuilder = new DropbitUriBuilder();
        this.walletHelper = walletHelper;
        this.transactionAdapterUtil = transactionAdapterUtil;
        this.memoCreator = memoCreator;
    }

    public int lookupTransactionBy(String txid) {
        for (int i = 0; i < transactions.size(); i++) {
            TransactionsInvitesSummary summary = transactions.get(i);
            if (summary.getTransactionSummary() != null &&
                    summary.getTransactionSummary().getTxid().equals(txid)) {
                return i;
            }
        }
        return 0;
    }

    public int lookupTransactionById(long id) {
        for (int i = 0; i < transactions.size(); i++) {
            TransactionsInvitesSummary summary = transactions.get(i);
            if (summary.getId() == id) {
                return i;
            }
        }
        return 0;
    }

    public void refreshData() {
        if (transactions != null)
            transactions.close();
        transactions = walletHelper.getTransactionsLazily();
        notifyDataSetChanged();
    }

    public void setShowTransactionDetailRequestObserver(TransactionDetailObserver transactionDetailObserver) {
        this.transactionDetailObserver = transactionDetailObserver;
    }

    public void tearDown() {
        transactions.close();
    }

    public long getTransactionIdForIndex(int index) {
        return transactions.get(index).getId();
    }

    @Override
    public int getCount() {
        return transactions.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (transactions.isClosed()) refreshData();

        TransactionsInvitesSummary summary = transactions.get(position);
        BindableTransaction bindableTransaction = transactionAdapterUtil.translateTransaction(summary);
        View page = LayoutInflater.from(container.getContext()).inflate(R.layout.page_transaction_detail, container, false);
        container.addView(page);
        bindTo(page, bindableTransaction, position);
        return page;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
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

    void bindTo(View page, BindableTransaction bindableTransaction, int position) {
        page.findViewById(R.id.call_to_action).setVisibility(View.INVISIBLE);
        page.findViewById(R.id.ic_close).setOnClickListener(this::close);
        Button twitterShareButton = page.findViewById(R.id.share_twitter_button);
        twitterShareButton.setTag(position);
        page.findViewById(R.id.share_twitter_button).setOnClickListener(v -> shareButtonClicked(v));

        Button addMemoButton = page.findViewById(R.id.add_memo_button);
        addMemoButton.setOnClickListener(v -> addMemoClicked((Button) v));
        addMemoButton.setTag(position);

        renderIcon(page, bindableTransaction);
        renderConfirmations(page, bindableTransaction);
        renderIdentity(page, bindableTransaction);
        renderHistoricalPricing(page, bindableTransaction);
        bindTransactionValue(page, bindableTransaction);
        renderTransactionTime(page, bindableTransaction.getTxTime());
        renderDropBitState(page, bindableTransaction);
        renderShowDetails(page, bindableTransaction, position);
        renderMemo(page, bindableTransaction);
        renderTooltip(page, bindableTransaction);
        renderTwitterShare(page, bindableTransaction);
    }

    private void renderTwitterShare(View page, BindableTransaction bindableTransaction) {
        if (bindableTransaction.getTxID() == null || bindableTransaction.getTxID() == "") {
            page.findViewById(R.id.share_twitter_button).setVisibility(View.GONE);
        } else {
            page.findViewById(R.id.share_twitter_button).setVisibility(View.VISIBLE);
        }
    }

    private TransactionsInvitesSummary getTransactionSummaryWithTaggedView(View view) {
        int position = view.getTag() == null ? 0 : (int) view.getTag();
        return transactions.get(position);
    }

    private void shareButtonClicked(View view) {
        TransactionsInvitesSummary transactionsInvitesSummary = getTransactionSummaryWithTaggedView(view);
        analytics.trackEvent(Analytics.Companion.EVENT_SHARE_VIA_TWITTER);

        String memo = transactionsInvitesSummary.getTransactionSummary() == null ||
                transactionsInvitesSummary.getTransactionSummary().getTransactionNotification() == null ||
                transactionsInvitesSummary.getTransactionSummary().getTransactionNotification().getMemo() == null ? "" : transactionsInvitesSummary.getTransactionSummary().getTransactionNotification().getMemo();
        Intent intent = twitterUtil.createTwitterIntent(view.getContext(), twitterUtil.getShareMessage(memo));
        if (intent != null) {
            view.getContext().startActivity(intent);
        }
    }

    private void addMemoClicked(Button view) {
        TransactionsInvitesSummary summary = getTransactionSummaryWithTaggedView(view);
        memoCreator.createMemo((AppCompatActivity) view.getContext(), memo -> {
            setupNewTransactionNotification(view, summary, memo);
        }, "");
    }

    private void setupNewTransactionNotification(View view, TransactionsInvitesSummary summary, String memo) {
        Intent intent = new Intent(view.getContext(), DropBitService.class);
        intent.setAction(DropbitIntents.ACTION_CREATE_NOTIFICATION);
        intent.putExtra(DropbitIntents.EXTRA_DROPBIT_MEMO, memo);
        intent.putExtra(DropbitIntents.EXTRA_DROPBIT_TXID, summary.getTransactionSummary().getTxid());
        view.getContext().startService(intent);
    }

    private void bindTransactionValue(View page, BindableTransaction bindableTransaction) {
        DefaultCurrencyDisplayView view = page.findViewById(R.id.default_currency_view);
        view.renderValues(defaultCurrencies, bindableTransaction.getBasicDirection(), bindableTransaction.totalCryptoForSendState(), bindableTransaction.totalFiatForSendState());
        if (defaultCurrencyChangeViewNotifier != null)
            defaultCurrencyChangeViewNotifier.observeDefaultCurrencyChange(view);
    }

    private void renderMemo(View page, BindableTransaction bindableTransaction) {
        View memoView = page.findViewById(R.id.shared_transaction_subview);
        Button addMemoButton = page.findViewById(R.id.add_memo_button);
        String memo = bindableTransaction.getMemo();
        if (memo == null || "".equals(memo)) {
            addMemoButton.setVisibility(View.VISIBLE);
            memoView.setVisibility(View.GONE);
        } else {
            addMemoButton.setVisibility(View.GONE);
            memoView.setVisibility(View.VISIBLE);
            new SharedMemoView().render(memoView, bindableTransaction.isSharedMemo(), bindableTransaction.getMemo(), bindableTransaction.getIdentity());
        }

        if (bindableTransaction.getInviteState() == BindableTransaction.InviteState.CANCELED) {
            addMemoButton.setVisibility(View.GONE);
        }
    }

    private void renderTooltip(View page, BindableTransaction bindableTransaction) {
        if (bindableTransaction.getServerInviteId() == null || bindableTransaction.getServerInviteId().equals("")) {
            tooltipId = DropbitRoute.REGULAR_TRANSACTION;
        } else {
            tooltipId = DropbitRoute.DROPBIT_TRANSACTION;
        }

        page.findViewById(R.id.tooltip).setOnClickListener(this::showTooltipInfo);
    }

    private void showTooltipInfo(View view) {
        UriUtil.openUrl(dropbitUriBuilder.build(tooltipId), (AppCompatActivity) view.getContext());
    }

    private void close(View view) {
        ((AppCompatActivity) view.getContext()).finish();
    }

    private void renderShowDetails(View page, BindableTransaction bindableTransaction, int position) {
        String txID = bindableTransaction.getTxID();
        if (txID == null || txID.isEmpty())
            return;

        Button cta = page.findViewById(R.id.call_to_action);
        cta.setVisibility(View.VISIBLE);
        cta.setTag(position);
        cta.setOnClickListener(this::onSeeDetailsClickListener);
    }

    private void onSeeDetailsClickListener(View view) {
        int position = (int) view.getTag();
        TransactionsInvitesSummary summary = transactions.get(position);
        BindableTransaction bindableTransaction = transactionAdapterUtil.translateTransaction(summary);

        if (transactionDetailObserver != null) {
            transactionDetailObserver.onTransactionDetailsRequested(bindableTransaction);
        }
    }

    private void renderHistoricalPricing(View page, BindableTransaction bindableTransaction) {
        TextView historyTextView = page.findViewById(R.id.value_when_sent);
        long txTimeValue = calculateHistoricUSDPrice(bindableTransaction).toLong();
        long inviteValue = bindableTransaction.getHistoricalInviteUSDValue();
        String text = "";

        switch (bindableTransaction.getBasicDirection()) {
            case SEND:
                text = getHistoricalPriceForSentTransaction(historyTextView.getContext(), inviteValue, txTimeValue);
                break;
            case RECEIVE:
                text = getHistoricalPriceForReceivedTransaction(historyTextView.getContext(), inviteValue, txTimeValue);
                break;
        }

        historyTextView.setText(text);
    }

    private String getHistoricalPriceForReceivedTransaction(Context context, long inviteValue, long txTimeValue) {
        String text = "";
        if (inviteValue > 0 && txTimeValue > 0) {
            text = context.getString(R.string.tx_details_when_both,
                    context.getString(R.string.tx_details_when_received, new USDCurrency(inviteValue).toFormattedCurrency()),
                    context.getString(R.string.tx_details_at_send, new USDCurrency(txTimeValue).toFormattedCurrency()));
        } else if (inviteValue > 0) {
            text = context.getString(R.string.tx_details_when_received, new USDCurrency(inviteValue).toFormattedCurrency());
        } else if (txTimeValue > 0) {
            text = context.getString(R.string.tx_details_when_received, new USDCurrency(txTimeValue).toFormattedCurrency());
        }

        return text;
    }

    private String getHistoricalPriceForSentTransaction(Context context, long inviteValue, long txTimeValue) {
        String text = "";
        if (inviteValue > 0 && txTimeValue > 0) {
            text = context.getString(R.string.tx_details_when_both,
                    context.getString(R.string.tx_details_when_received, new USDCurrency(inviteValue).toFormattedCurrency()),
                    context.getString(R.string.tx_details_when_sent, new USDCurrency(txTimeValue).toFormattedCurrency()));
        } else if (inviteValue > 0) {
            text = context.getString(R.string.tx_details_when_sent, new USDCurrency(inviteValue).toFormattedCurrency());
        } else if (txTimeValue > 0) {
            text = context.getString(R.string.tx_details_when_sent, new USDCurrency(txTimeValue).toFormattedCurrency());
        }

        return text;
    }

    private void renderDropBitState(View page, BindableTransaction bindableTransaction) {
        BindableTransaction.InviteState inviteState = bindableTransaction.getInviteState();
        if (inviteState == null) return;
        switch (inviteState) {
            case SENT_PENDING:
                setupCancelDropbit(page, bindableTransaction);
                break;
        }
    }

    private void setupCancelDropbit(View page, BindableTransaction bindableTransaction) {
        if (bindableTransaction.getSendState() == BindableTransaction.SendState.RECEIVE) return;

        Button button = page.findViewById(R.id.button_cancel_dropbit);
        button.setOnClickListener(this::onCancelDropbit);
        button.setTag(bindableTransaction.getServerInviteId());
        page.findViewById(R.id.button_cancel_dropbit).setVisibility(View.VISIBLE);
    }

    private void onCancelDropbit(View view) {
        Intent intent = new Intent(view.getContext(), DropBitService.class);
        intent.setAction(DropbitIntents.ACTION_CANCEL_DROPBIT);
        intent.putExtra(DropbitIntents.EXTRA_INVITATION_ID, (String) view.getTag());
        view.getContext().startService(intent);
    }

    private void renderTransactionTime(View page, String txTime) {
        ((TextView) page.findViewById(R.id.transaction_date)).setText(txTime);
    }

    private USDCurrency calculateHistoricUSDPrice(BindableTransaction bindableTransaction) {
        USDCurrency usd = new USDCurrency(bindableTransaction.getHistoricalTransactionUSDValue());
        BTCCurrency btc = (BTCCurrency) bindableTransaction.getValueCurrency();

        return btc.toUSD(usd);
    }

    private void renderIdentity(View page, BindableTransaction bindableTransaction) {
        TextView contact = page.findViewById(R.id.identity);
        if (bindableTransaction.getIdentityType() == IdentityType.TWITTER) {
            Views.INSTANCE.setCompondDrawableOnStart(contact, R.drawable.twitter_icon_blue, .8F);
            contact.setCompoundDrawablePadding(10);
        } else {
            Views.INSTANCE.clearCompoundDrawablesOn(contact);
        }
        contact.setText(bindableTransaction.getIdentity());
    }

    private void renderIcon(View page, BindableTransaction bindableTransaction) {
        ImageView icon = page.findViewById(R.id.ic_send_state);
        Context context = icon.getContext();

        switch (bindableTransaction.getSendState()) {
            case TRANSFER:
            case SEND:
                icon.setImageDrawable(Resources.INSTANCE.getDrawable(context, R.drawable.ic_transaction_send));
                icon.setTag(R.drawable.ic_transaction_send);
                icon.setContentDescription(Resources.INSTANCE.getString(context, R.string.transaction_detail_cd_send_state__dropbit_sent));
                break;
            case RECEIVE:
                icon.setImageDrawable(Resources.INSTANCE.getDrawable(context, R.drawable.ic_transaction_receive));
                icon.setTag(R.drawable.ic_transaction_receive);
                icon.setContentDescription(Resources.INSTANCE.getString(context, R.string.transaction_detail_cd_send_state__dropbit_received));
                break;
            default:
                icon.setImageDrawable(Resources.INSTANCE.getDrawable(context, R.drawable.ic_transaction_canceled));
                icon.setTag(R.drawable.ic_transaction_canceled);
                break;
        }
    }

    private void renderConfirmations(View page, BindableTransaction bindableTransaction) {
        ConfirmationsView confirmationsView = page.findViewById(R.id.confirmation_beads);
        TextView confirmationsLabel = page.findViewById(R.id.confirmations);

        if (bindableTransaction.getInviteState() != null) {
            renderConfirmationsForDropbit(confirmationsView, confirmationsLabel, bindableTransaction.getInviteState());
        } else {
            confirmationsView.setConfiguration(ConfirmationsView.CONFIGURATION_TRANSACTION);
        }

        if (bindableTransaction.getConfirmationState() != null) {
            renderConfirmationsForTransaction(confirmationsView, confirmationsLabel, bindableTransaction.getConfirmationState());
        }
    }

    private void renderConfirmationsForTransaction(ConfirmationsView confirmationsView, TextView confirmationsLabel, BindableTransaction.ConfirmationState confirmationState) {
        switch (confirmationState) {
            case UNCONFIRMED:
                confirmationsView.setStage(ConfirmationsView.STAGE_PENDING);
                confirmationsLabel.setText(R.string.confirmations_view_stage_4);
                break;
            case CONFIRMED:
                confirmationsView.setStage(ConfirmationsView.STAGE_COMPLETE);
                confirmationsLabel.setText(R.string.confirmations_view_stage_5);
        }
    }

    private void renderConfirmationsForDropbit(ConfirmationsView confirmationsView, TextView confirmationsLabel, BindableTransaction.InviteState inviteState) {
        confirmationsView.setConfiguration(ConfirmationsView.CONFIGURATION_DROPBIT);
        switch (inviteState) {
            case RECEIVED_PENDING:
            case SENT_PENDING:
                confirmationsView.setStage(ConfirmationsView.STAGE_DROPBIT_SENT);
                confirmationsLabel.setText(R.string.confirmations_view_stage_1);
                break;
            case RECEIVED_ADDRESS_PROVIDED:
                confirmationsView.setStage(ConfirmationsView.STAGE_ADDRESS_RECEIVED);
                confirmationsLabel.setText(R.string.received_confirmations_view_stage_2);
                break;
            case SENT_ADDRESS_PROVIDED:
                confirmationsView.setStage(ConfirmationsView.STAGE_ADDRESS_RECEIVED);
                confirmationsLabel.setText(R.string.confirmations_view_stage_2);
                break;
            case EXPIRED:
                confirmationsView.setVisibility(View.GONE);
                confirmationsLabel.setText(R.string.transaction_details_dropbit_expired);
                confirmationsLabel.setTextColor(Resources.INSTANCE.getColor(confirmationsLabel.getContext(), R.color.color_error));
                break;
            case CANCELED:
                confirmationsView.setVisibility(View.GONE);
                confirmationsLabel.setText(R.string.transaction_details_dropbit_canceled);
                confirmationsLabel.setTextColor(Resources.INSTANCE.getColor(confirmationsLabel.getContext(), R.color.color_error));
                break;
        }
    }

}
