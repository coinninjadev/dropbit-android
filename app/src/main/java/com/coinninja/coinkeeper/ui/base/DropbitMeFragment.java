package com.coinninja.coinkeeper.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.home.HomeActivity;
import com.coinninja.coinkeeper.ui.lightning.deposit.LightningDepositActivity;
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalActivity;
import com.coinninja.coinkeeper.ui.payment.confirm.ConfirmPaymentActivity;
import com.coinninja.coinkeeper.ui.payment.create.CreatePaymentActivity;
import com.coinninja.coinkeeper.ui.payment.request.LndInvoiceRequestActivity;
import com.coinninja.coinkeeper.ui.payment.request.PayRequestActivity;
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.widget.DropbitMeImageView;

import javax.inject.Inject;

import static java.util.Arrays.asList;

public class DropbitMeFragment extends BaseFragment {

    private final String[] visibleWithList = {
            HomeActivity.class.getName(),
            TransactionDetailsActivity.class.getName(),
            LightningDepositActivity.class.getName(),
            LightningWithdrawalActivity.class.getName(),
            PayRequestActivity.class.getName(),
            LndInvoiceRequestActivity.class.getName(),
            CreatePaymentActivity.class.getName(),
            ConfirmPaymentActivity.class.getName(),
    };

    @Inject
    DropbitMeConfiguration dropbitMeConfiguration;
    @Inject
    DropbitMeDialogFactory dropbitMeDialogFactory;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    IntentFilter filter;
    private DropbitMeImageView button;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DropbitIntents.ACTION_DEVERIFY_TWITTER_COMPLETED.equals(intent.getAction())) {
                resetButton();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dropbit_me, container, false);
        button = view.findViewById(R.id.dropbit_me_button);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        filter = new IntentFilter(DropbitIntents.ACTION_DEVERIFY_TWITTER_COMPLETED);
        localBroadCastUtil.registerReceiver(receiver, filter);
        if (!asList(visibleWithList).contains(getActivity().getClass().getName())) return;
        setup();
    }

    @Override
    public void onPause() {
        super.onPause();
        localBroadCastUtil.unregisterReceiver(receiver);
        button.setOnClickListener(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        dropbitMeConfiguration.setOnViewDropBitMeViewRequestedObserver(null);
    }

    private void setup() {
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> onDropbitClicked());
        dropbitMeConfiguration.setOnViewDropBitMeViewRequestedObserver(this::showIfRequested);
        showIfRequested();
    }

    private void showIfRequested() {
        if (dropbitMeConfiguration.shouldShowWhenPossible()) {
            showDropbitMeDialog();
        }

        dropbitMeConfiguration.acknowledge();
    }

    private void showDropbitMeDialog() {
        dropbitMeDialogFactory.newInstance().show(getActivity()
                .getSupportFragmentManager(), DropBitMeDialog.TAG);
    }

    private void resetButton() {
        button.setImageResource(R.drawable.ic_dropbit_me);
    }

    private void onDropbitClicked() {
        showDropbitMeDialog();
    }

}
