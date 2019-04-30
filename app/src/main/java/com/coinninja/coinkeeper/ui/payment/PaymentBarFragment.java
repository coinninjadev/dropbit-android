package com.coinninja.coinkeeper.ui.payment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.ui.base.BaseFragment;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.view.fragment.ConfirmPayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.RequestDialogFragment;
import com.coinninja.coinkeeper.view.widget.PaymentBarView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static com.coinninja.android.helpers.Views.withId;

public class PaymentBarFragment extends BaseFragment implements PaymentBarCallbacks {

    @Inject
    LocalBroadCastUtil localBroadcastUtil;

    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    @Inject
    PaymentUtil paymentUtil;

    @Inject
    BitcoinUtil bitcoinUtil;

    @Inject
    WalletHelper walletHelper;

    @Inject
    CurrencyPreference currencyPreference;

    PaymentHolder paymentHolder;
    IntentFilter intentFilter = new IntentFilter(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE);
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DropbitIntents.ACTION_WALLET_SYNC_COMPLETE.equals(intent.getAction())) {
                paymentHolder.setSpendableBalance(walletHelper.getSpendableBalance());
            }
        }
    };
    private PaymentBarView paymentBarView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paymentHolder = new PaymentHolder();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_payment_bar, container, false);
        paymentBarView = withId(root, R.id.payment_bar);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        localBroadcastUtil.registerReceiver(receiver, intentFilter);
        paymentHolder.setSpendableBalance(walletHelper.getSpendableBalance());
    }

    @Override
    public void onResume() {
        super.onResume();
        paymentBarView.setOnRequestPressedObserver(this::onRequestButtonPressed);
        paymentBarView.setOnSendPressedObserver(this::showPayDialogWithDefault);
        paymentBarView.setOnScanPressedObserver(this::onQrScanPressed);
    }

    @Override
    public void onStop() {
        super.onStop();
        localBroadcastUtil.unregisterReceiver(receiver);
    }

    @Override
    public void onQrScanPressed() {
        showPayDialog(true);
    }

    @Override
    public void confirmPaymentFor(PaymentHolder paymentHolder) {
        dismissPayDialog();
        ConfirmPayDialogFragment confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(paymentHolder, this);
        confirmPayDialogFragment.setCancelable(false);
        activityNavigationUtil.showDialogWithTag(getFragmentManager(), confirmPayDialogFragment, ConfirmPayDialogFragment.class.getSimpleName());
    }

    @Override
    public void confirmPaymentFor(PaymentHolder paymentHolder, Contact phoneNumber) {
        dismissPayDialog();
        ConfirmPayDialogFragment confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(phoneNumber, paymentHolder, this);
        confirmPayDialogFragment.setCancelable(false);
        activityNavigationUtil.showDialogWithTag(getFragmentManager(), confirmPayDialogFragment, ConfirmPayDialogFragment.class.getSimpleName());
    }


    @Override
    public void confirmInvite(Contact phoneNumber) {
        dismissPayDialog();
        ConfirmPayDialogFragment confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(phoneNumber, paymentHolder, this);
        confirmPayDialogFragment.setCancelable(false);
        activityNavigationUtil.showDialogWithTag(getFragmentManager(), confirmPayDialogFragment, ConfirmPayDialogFragment.class.getSimpleName());
    }

    @Override
    public void cancelPayment(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
        currencyPreference.reset();
        paymentHolder.setDefaultCurrencies(currencyPreference.getCurrenciesPreference());
        paymentUtil.setAddress(null);
        paymentHolder.clearPayment();

    }

    public void showPayDialogWithBitcoinUri(BitcoinUri uri) {
        showPayDialog(uri);
    }

    private void onRequestButtonPressed() {
        RequestDialogFragment requestDialog = new RequestDialogFragment();
        activityNavigationUtil.showDialogWithTag(getFragmentManager(), requestDialog, RequestDialogFragment.class.getSimpleName());
    }


    private void dismissPayDialog() {
        PayDialogFragment dialog = (PayDialogFragment) getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());
        getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());
        dialog.dismiss();
    }

    void showPayDialogWithDefault() {
        showPayDialog(false);
    }

    private void showPayDialog(boolean shouldShowScan) {
        resetPaymentUtilForPayDialogFragment();
        PayDialogFragment payDialog = PayDialogFragment.newInstance(paymentUtil, this, shouldShowScan);
        activityNavigationUtil.showDialogWithTag(getFragmentManager(), payDialog, PayDialogFragment.class.getSimpleName());
    }

    private void showPayDialog(BitcoinUri injectedBitcoinUri) {
        resetPaymentUtilForPayDialogFragment();
        PayDialogFragment payDialog = PayDialogFragment.newInstance(paymentUtil, this, injectedBitcoinUri);
        activityNavigationUtil.showDialogWithTag(getFragmentManager(), payDialog, PayDialogFragment.class.getSimpleName());
    }


    private void resetPaymentUtilForPayDialogFragment() {
        currencyPreference.reset();
        paymentHolder.setDefaultCurrencies(currencyPreference.getCurrenciesPreference());
        paymentHolder.setEvaluationCurrency(walletHelper.getLatestPrice());
        paymentHolder.setSpendableBalance(walletHelper.getSpendableBalance());
        paymentUtil.setTransactionFee(walletHelper.getLatestFee());
        paymentUtil.setPaymentHolder(paymentHolder);
        paymentHolder.clearPayment();
    }
}
