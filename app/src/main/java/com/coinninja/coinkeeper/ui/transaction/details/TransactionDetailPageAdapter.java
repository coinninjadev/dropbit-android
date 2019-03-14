package com.coinninja.coinkeeper.ui.transaction.details;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.dropbit.DropBitService;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.ConfirmationsView;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;
import com.coinninja.coinkeeper.view.subviews.SharedMemoView;

import org.greenrobot.greendao.query.LazyList;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import static com.coinninja.android.helpers.Resources.getDrawable;
import static com.coinninja.android.helpers.Resources.getString;
import static com.coinninja.android.helpers.Views.makeViewInvisible;
import static com.coinninja.android.helpers.Views.makeViewVisible;
import static com.coinninja.android.helpers.Views.withId;

public class TransactionDetailPageAdapter extends PagerAdapter {

    final WalletHelper walletHelper;
    private final TransactionAdapterUtil transactionAdapterUtil;
    LazyList<TransactionsInvitesSummary> transactions;
    private TransactionDetailObserver transactionDetailObserver;
    private SharedMemoView sharedMemoView;

    TransactionDetailPageAdapter(WalletHelper walletHelper, TransactionAdapterUtil transactionAdapterUtil) {
        this.walletHelper = walletHelper;
        this.transactionAdapterUtil = transactionAdapterUtil;
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

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (transactions.isClosed()) refreshData();

        TransactionsInvitesSummary summary = transactions.get(position);
        BindableTransaction bindableTransaction = transactionAdapterUtil.translateTransaction(summary);
        View page = LayoutInflater.from(container.getContext()).inflate(R.layout.page_transaction_detail, container, false);
        container.addView(page);
        bindTo(page, bindableTransaction);
        return page;
    }

    void bindTo(View page, BindableTransaction bindableTransaction) {
        makeViewInvisible(page, R.id.call_to_action);
        withId(page, R.id.ic_close).setOnClickListener(this::close);
        renderIcon(page, bindableTransaction);
        renderConfirmations(page, bindableTransaction);
        renderContact(page, bindableTransaction);
        renderHistoricalPricing(page, bindableTransaction);
        renderPrimaryCost(page, bindableTransaction);
        renderCryptoCost(page, bindableTransaction);
        renderTransactionTime(page, bindableTransaction.getTxTime());
        renderDropBitState(page, bindableTransaction);
        renderShowDetails(page, bindableTransaction);
        renderMemo(page, bindableTransaction);
    }

    private void renderMemo(View page, BindableTransaction bindableTransaction) {
        View memoView = withId(page, R.id.shared_transaction_subview);
        String memo = bindableTransaction.getMemo();
        if (memo == null || "".equals(memo)) {
            memoView.setVisibility(View.GONE);
        } else {
            memoView.setVisibility(View.VISIBLE);
            sharedMemoView = new SharedMemoView(memoView, bindableTransaction.getIsSharedMemo(), bindableTransaction.getMemo(), bindableTransaction.getContactOrPhoneNumber());
        }
    }

    private void close(View view) {
        ((Activity) view.getContext()).finish();
    }

    private void renderShowDetails(View page, BindableTransaction bindableTransaction) {
        String txID = bindableTransaction.getTxID();
        if (txID == null || txID.isEmpty())
            return;

        Button cta = withId(page, R.id.call_to_action);
        cta.setVisibility(View.VISIBLE);
        cta.setTag(bindableTransaction);
        cta.setOnClickListener(this::onSeeDetailsClickListener);
    }

    private void onSeeDetailsClickListener(View view) {
        BindableTransaction bindableTransaction = (BindableTransaction) view.getTag();
        if (transactionDetailObserver != null) {
            transactionDetailObserver.onTransactionDetailsRequested(bindableTransaction);
        }
    }

    private void renderHistoricalPricing(View page, BindableTransaction bindableTransaction) {
        TextView historyTextView = page.findViewById(R.id.value_when_sent);

        Currency value = calculateHistoricUSDPrice(bindableTransaction);
        String atSend = " at send";
        String atSendFinalString = "";
        String whenReceived = " when received";
        String whenReceivedFinalString = "";

        if (bindableTransaction.getInviteState() != null && bindableTransaction.getTxID() != null) {
            Currency inviteValue = calculateHistoricUSDInvitePrice(bindableTransaction);
            switch (bindableTransaction.getSendState()) {
                case SEND_CANCELED:
                    if (!inviteValue.isZero()) {
                        atSendFinalString = inviteValue.toFormattedCurrency() + atSend;
                    }
                    break;
                case SEND:
                case TRANSFER:
                case RECEIVE_CANCELED:
                case RECEIVE:
                    if(!value.isZero()) {
                        atSendFinalString = value.toFormattedCurrency() + atSend;
                    }

                    if(!inviteValue.isZero()) {
                        whenReceivedFinalString = inviteValue.toFormattedCurrency() + whenReceived + " ";
                    }
                    break;
            }
        } else if (bindableTransaction.getTxID() == null) {
            Currency inviteValue = calculateHistoricUSDInvitePrice(bindableTransaction);
            if (!inviteValue.isZero()) {
                whenReceivedFinalString = inviteValue.toFormattedCurrency() + whenReceived;
            }
        } else {
            if(!value.isZero()) {
                switch (bindableTransaction.getSendState()) {
                    case SEND_CANCELED:
                    case SEND:
                        atSendFinalString = value.toFormattedCurrency() + atSend;
                        break;
                    case TRANSFER:
                    case RECEIVE_CANCELED:
                    case RECEIVE:
                        whenReceivedFinalString = value.toFormattedCurrency() + whenReceived;
                        break;
                }
            }
        }

        historyTextView.setText(whenReceivedFinalString + atSendFinalString);
    }

    private void renderDropBitState(View page, BindableTransaction bindableTransaction) {
        BindableTransaction.InviteState inviteState = bindableTransaction.getInviteState();
        if (inviteState == null) return;
        switch (inviteState) {
            case SENT_PENDING:
                setupCancelDropbit(page, bindableTransaction);
                break;
            default:
                return;
        }
    }


    private void setupCancelDropbit(View page, BindableTransaction bindableTransaction) {
        if (bindableTransaction.getSendState() == BindableTransaction.SendState.RECEIVE) return;

        Button button = withId(page, R.id.button_cancel_dropbit);
        button.setOnClickListener(this::onCancelDropbit);
        button.setTag(bindableTransaction.getServerInviteId());
        makeViewVisible(page, R.id.button_cancel_dropbit);
    }

    private void onCancelDropbit(View view) {
        Intent intent = new Intent(view.getContext(), DropBitService.class);
        intent.setAction(Intents.ACTION_CANCEL_DROPBIT);
        intent.putExtra(Intents.EXTRA_INVITATION_ID, (String) view.getTag());
        view.getContext().startService(intent);
    }

    private void renderTransactionTime(View page, String txTime) {
        ((TextView) withId(page, R.id.transaction_date)).setText(txTime);
    }

    private void renderCryptoCost(View page, BindableTransaction bindableTransaction) {
        TextView primaryValue = withId(page, R.id.secondary_value);
        String value = "";

        switch (bindableTransaction.getSendState()) {
            case SEND_CANCELED:
            case SEND:
                value = bindableTransaction.getTotalTransactionCostCurrency().toFormattedString();
                break;
            case TRANSFER:
                value = bindableTransaction.getFeeCurrency().toFormattedString();
                break;
            case RECEIVE_CANCELED:
            case RECEIVE:
                value = bindableTransaction.getValueCurrency().toFormattedString();
                break;
        }

        primaryValue.setText(value);
    }

    private void renderPrimaryCost(View page, BindableTransaction bindableTransaction) {
        TextView primaryValue = withId(page, R.id.primary_value);
        String value = "";
        String format = "%s";

        switch (bindableTransaction.getSendState()) {
            case TRANSFER:
                value = convertToUSD(bindableTransaction.getFeeCurrency()).toFormattedCurrency();
                format = "-%s";
                break;
            case SEND_CANCELED:
            case SEND:
                value = convertToUSD(bindableTransaction.getTotalTransactionCostCurrency()).toFormattedCurrency();
                format = "-%s";
                break;
            case RECEIVE_CANCELED:
            case RECEIVE:
                value = convertToUSD(bindableTransaction.getValueCurrency()).toFormattedCurrency();
                break;
            default:
        }

        primaryValue.setText(String.format(format, value));
    }

    private USDCurrency calculateHistoricUSDInvitePrice(BindableTransaction bindableTransaction) {
        if (bindableTransaction.getValueCurrency() == null) {
            return null;
        }
        USDCurrency usd = new USDCurrency(bindableTransaction.getHistoricalInviteUSDValue());
        BTCCurrency btc = (BTCCurrency) bindableTransaction.getValueCurrency();

        return convertToUSD(btc, usd);
    }

    private USDCurrency calculateHistoricUSDPrice(BindableTransaction bindableTransaction) {
        USDCurrency usd = new USDCurrency(bindableTransaction.getHistoricalTransactionUSDValue());
        BTCCurrency btc = (BTCCurrency) bindableTransaction.getValueCurrency();

        return convertToUSD(btc, usd);
    }

    private USDCurrency convertToUSD(Currency currency) {
        return currency.toUSD(walletHelper.getLatestPrice());
    }

    private USDCurrency convertToUSD(Currency currency, USDCurrency historicPrice) {
        return currency.toUSD(historicPrice);
    }

    private void renderContact(View page, BindableTransaction bindableTransaction) {
        TextView contact = withId(page, R.id.contact);

        String contactName = bindableTransaction.getContactName();
        if (bindableTransaction.getSendState() == BindableTransaction.SendState.TRANSFER) {
            contact.setText(getString(page.getContext(), R.string.send_to_self));
        } else if (contactName == null || "".equals(contactName)) {
            contact.setText(bindableTransaction.getContactPhoneNumber());
        } else {
            contact.setText(bindableTransaction.getContactName());
        }

    }

    private void renderIcon(View page, BindableTransaction bindableTransaction) {
        ImageView icon = withId(page, R.id.ic_send_state);
        Context context = icon.getContext();

        switch (bindableTransaction.getSendState()) {
            case TRANSFER:
            case SEND:
                icon.setImageDrawable(getDrawable(context, R.drawable.ic_transaction_send));
                icon.setTag(R.drawable.ic_transaction_send);
                icon.setContentDescription(getString(context, R.string.transaction_detail_cd_send_state__dropbit_sent));
                break;
            case RECEIVE:
                icon.setImageDrawable(getDrawable(context, R.drawable.ic_transaction_receive));
                icon.setTag(R.drawable.ic_transaction_receive);
                icon.setContentDescription(getString(context, R.string.transaction_detail_cd_send_state__dropbit_received));
                break;
            default:
                icon.setImageDrawable(getDrawable(context, R.drawable.ic_transaction_canceled));
                icon.setTag(R.drawable.ic_transaction_canceled);
                break;
        }
    }

    private void renderConfirmations(View page, BindableTransaction bindableTransaction) {
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);
        TextView confirmationsLabel = withId(page, R.id.confirmations);

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
            case SENT_ADDRESS_PROVIDED:
                confirmationsView.setStage(ConfirmationsView.STAGE_ADDRESS_RECEIVED);
                confirmationsLabel.setText(R.string.confirmations_view_stage_2);
                break;
            case EXPIRED:
                confirmationsView.setVisibility(View.GONE);
                confirmationsLabel.setText(R.string.transaction_details_dropbit_expired);
                confirmationsLabel.setTextColor(Resources.getColor(confirmationsLabel.getContext(), R.color.color_error));
                break;
            case CANCELED:
                confirmationsView.setVisibility(View.GONE);
                confirmationsLabel.setText(R.string.transaction_details_dropbit_canceled);
                confirmationsLabel.setTextColor(Resources.getColor(confirmationsLabel.getContext(), R.color.color_error));
                break;
        }
    }

}
