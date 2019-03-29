package com.coinninja.coinkeeper.ui.payment;

import android.app.DialogFragment;
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
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.view.activity.QrScanActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;
import com.coinninja.coinkeeper.view.fragment.ConfirmPayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.RequestDialogFragment;
import com.coinninja.coinkeeper.view.widget.PaymentBarView;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.coinninja.android.helpers.Views.withId;

public class PaymentBarFragment extends BaseFragment implements PaymentBarCallbacks {

    @Inject
    LocalBroadCastUtil localBroadcastUtil;

    @Inject
    PaymentUtil paymentUtil;


    @Inject
    BitcoinUtil bitcoinUtil;

    @Inject
    WalletHelper walletHelper;

    @Inject
    CurrencyPreference currencyPreference;

    private PaymentBarView paymentBarView;
    PaymentHolder paymentHolder;

    IntentFilter intentFilter = new IntentFilter(Intents.ACTION_WALLET_SYNC_COMPLETE);

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intents.ACTION_WALLET_SYNC_COMPLETE.equals(intent.getAction())) {
                paymentHolder.setSpendableBalance(walletHelper.getSpendableBalance());
            }
        }
    };

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

    private PayDialogFragment showPayDialogWithDefault() {
        return showPayDialog(currencyPreference.getCurrenciesPreference(), false);
    }

    @Override
    public void onStop() {
        super.onStop();
        localBroadcastUtil.unregisterReceiver(receiver);
    }

    @Override
    public void confirmPaymentFor(String btcAddress) {
        ConfirmPayDialogFragment confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(btcAddress, paymentHolder, this);
        confirmPayDialogFragment.setCancelable(false);
        confirmPayDialogFragment.show(getFragmentManager(), ConfirmPayDialogFragment.class.getSimpleName());
    }

    @Override
    public void confirmPaymentFor(String btcAddress, Contact phoneNumber) {
        ConfirmPayDialogFragment confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(btcAddress, phoneNumber, paymentHolder, this);
        confirmPayDialogFragment.setCancelable(false);
        confirmPayDialogFragment.show(getFragmentManager(), ConfirmPayDialogFragment.class.getSimpleName());
    }


    @Override
    public void confirmInvite(Contact phoneNumber) {
        ConfirmPayDialogFragment confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(phoneNumber, paymentHolder, this);
        confirmPayDialogFragment.setCancelable(false);
        confirmPayDialogFragment.show(getFragmentManager(), ConfirmPayDialogFragment.class.getSimpleName());
    }

    @Override
    public void onQrScanPressed() {
        PayDialogFragment fragment = showPayDialog(currencyPreference.getCurrenciesPreference(), true);
    }

    @Override
    public void cancelPayment(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
        currencyPreference.reset();
        paymentHolder.setDefaultCurrencies(currencyPreference.getCurrenciesPreference());
        paymentUtil.setAddress(null);
        paymentHolder.clearPayment();
    }

    void onRequestButtonPressed() {
        paymentHolder = new PaymentHolder();
        paymentHolder.setDefaultCurrencies(currencyPreference.getCurrenciesPreference());
        paymentHolder.setEvaluationCurrency(walletHelper.getLatestPrice());
        paymentHolder.setSpendableBalance(walletHelper.getSpendableBalance());
        paymentHolder.setTransactionFee(walletHelper.getLatestFee());
        RequestDialogFragment requestDialog = new RequestDialogFragment();
        requestDialog.setPaymentHolder(paymentHolder);
        requestDialog.show(getFragmentManager(), RequestDialogFragment.class.getSimpleName());
    }

    private PayDialogFragment showPayDialog(DefaultCurrencies defaultCurrencies, boolean shouldShowScan) {
        currencyPreference.reset();
        paymentHolder.clearPayment();
        paymentHolder.setDefaultCurrencies(defaultCurrencies);
        paymentHolder.setEvaluationCurrency(walletHelper.getLatestPrice());
        paymentHolder.setSpendableBalance(walletHelper.getSpendableBalance());
        paymentHolder.setTransactionFee(walletHelper.getLatestFee());
        paymentUtil.setPaymentHolder(paymentHolder);
        PayDialogFragment payDialog = PayDialogFragment.newInstance(paymentUtil, this, shouldShowScan);
        payDialog.show(getFragmentManager(), PayDialogFragment.class.getSimpleName());
        return payDialog;
    }
}
