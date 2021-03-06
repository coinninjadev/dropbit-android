package com.coinninja.coinkeeper.ui.transaction.details;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder;
import com.coinninja.coinkeeper.util.uri.UriUtil;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import javax.inject.Inject;

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.Currency;
import app.dropbit.commons.currency.USDCurrency;

import static com.coinninja.coinkeeper.util.uri.routes.DropbitRoute.TRANSACTION_DETAILS;


public class TransactionDetailDialogController {
    static final String FORMAT_WHEN_SENT = "%s (%s)";

    private final WalletHelper walletHelper;
    private final ActivityNavigationUtil activityNavigationUtil;
    private DropbitUriBuilder dropbitUriBuilder;

    @Inject
    TransactionDetailDialogController(WalletHelper walletHelper,
                                      ActivityNavigationUtil activityNavigationUtil,
                                      DropbitUriBuilder dropbitUriBuilder) {
        this.dropbitUriBuilder = dropbitUriBuilder;
        this.walletHelper = walletHelper;
        this.activityNavigationUtil = activityNavigationUtil;
    }

    public void showTransaction(@NonNull AppCompatActivity activity, @NonNull BindableTransaction bindableTransaction) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_transaction_block_details, null);
        bindTransactionToView(view, bindableTransaction);
        GenericAlertDialog alertDialog = GenericAlertDialog.newInstance(view, true, true);
        alertDialog.asWide();
        alertDialog.show(activity.getSupportFragmentManager(), getClass().getSimpleName());
    }

    private void bindTransactionToView(View view, BindableTransaction transaction) {
        bindIcon(view.findViewById(R.id.ic_send_state), transaction.getSendState());
        bindConfirmations(view.findViewById(R.id.confirmations), transaction.getConfirmationState());
        bindConfirmationsCount(view.findViewById(R.id.value_confirmations), transaction.getConfirmationCount());
        bindValueWhenSent(view.findViewById(R.id.value_when_sent), transaction);
        bindFee(view.findViewById(R.id.value_network_fee), transaction.getFeeCurrency());
        bindAddress(view.findViewById(R.id.receive_address), transaction.getTargetAddress());
        bindShareTX(view.findViewById(R.id.share_transaction), transaction.getTxID());
        bindTooltip(view.findViewById(R.id.tooltip));
        bindShowTX(view.findViewById(R.id.see_on_block), transaction.getTxID());
        bindClose(view.findViewById(R.id.ic_close));
    }

    private void bindTooltip(View view) {
        view.setOnClickListener(this::showTooltipUrl);
    }

    private void showTooltipUrl(View view) {
        UriUtil.openUrl(dropbitUriBuilder.build(TRANSACTION_DETAILS), (AppCompatActivity) view.getContext());
    }

    private void bindClose(View closeBtn) {
        closeBtn.setOnClickListener(this::closeDialog);
    }

    private void closeDialog(View view) {
        FragmentManager fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
        GenericAlertDialog dialog = (GenericAlertDialog) fragmentManager.findFragmentByTag(getClass().getSimpleName());
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void bindShareTX(Button shareTx, String txID) {
        shareTx.setTag(txID);
        shareTx.setOnClickListener(this::onShareTx);
    }

    private void onShareTx(View view) {
        String txid = (String) view.getTag();
        activityNavigationUtil.shareTransaction(view.getContext(), txid);
    }

    private void bindShowTX(Button seeTxDetails, String txID) {
        seeTxDetails.setTag(txID);
        seeTxDetails.setOnClickListener(this::onShowTxOnBlock);
    }

    private void onShowTxOnBlock(View view) {
        String txid = (String) view.getTag();
        activityNavigationUtil.showTxidOnBlock(view.getContext(), txid);
    }

    private void bindAddress(TextView addressView, String address) {
        addressView.setText(address);
        addressView.setTag(address);
        addressView.setOnClickListener(view -> onSeeAddress(view));
    }

    private void onSeeAddress(View view) {
        String address = (String) view.getTag();
        activityNavigationUtil.showAddressOnBlock(view.getContext(), address);
    }

    private void bindValueWhenSent(TextView whenSent, BindableTransaction transaction) {
        String value = "";
        switch (transaction.getSendState()) {
            case TRANSFER:
                value = String.format(FORMAT_WHEN_SENT,
                        new BTCCurrency().toFormattedString(),
                        new USDCurrency().toFormattedCurrency());
                break;
            case SEND:
            case RECEIVE:
                value = getValueOfTransaction(transaction);
                break;
        }

        whenSent.setText(value);
    }

    private void bindFee(TextView fee, Currency feeCurrency) {
        fee.setText(String.format(FORMAT_WHEN_SENT,
                feeCurrency.toFormattedString(),
                convertToBase(feeCurrency).toFormattedCurrency()));
    }

    private String getValueOfTransaction(BindableTransaction transaction) {
        Currency costCurrency = transaction.getValueCurrency();
        return formatWhenSentValue(costCurrency);
    }

    private String formatWhenSentValue(Currency costCurrency) {
        return String.format(FORMAT_WHEN_SENT,
                costCurrency.toFormattedString(),
                convertToBase(costCurrency).toFormattedCurrency());
    }

    private Currency convertToBase(Currency costCurrency) {
        return costCurrency.toUSD(walletHelper.getLatestPrice());
    }

    private void bindConfirmationsCount(TextView confirmations, int confirmationCount) {
        if (confirmationCount > 5) {
            confirmations.setText("6+");
        } else {
            confirmations.setText(String.valueOf(confirmationCount));
        }
    }

    private void bindConfirmations(TextView confirmations, BindableTransaction.ConfirmationState confirmationState) {
        switch (confirmationState) {
            case CONFIRMED:
                confirmations.setText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_5));
                break;
            case UNCONFIRMED:
                confirmations.setText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_4));
                break;
        }
    }

    private void bindIcon(ImageView icon, BindableTransaction.SendState sendState) {
        switch (sendState) {
            case RECEIVE:
                icon.setImageResource(R.drawable.ic_transaction_receive);
                icon.setTag(R.drawable.ic_transaction_receive);
                break;
            case TRANSFER:
            case SEND:
                icon.setImageResource(R.drawable.ic_transaction_send);
                icon.setTag(R.drawable.ic_transaction_send);
                break;
        }
    }
}
