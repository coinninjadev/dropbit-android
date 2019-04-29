package com.coinninja.coinkeeper.ui.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.service.DeverifyAccountService;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import javax.inject.Inject;

public class RemovePhoneNumberController {
    public static final String TAG = "TAG_REMOVE_PHONE_NUMBER";
    private final LocalBroadCastUtil localBroadCastUtil;
    AppCompatActivity activity;
    IntentFilter intentFilter;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED.equals(intent.getAction())) {
                showDeverificationFailed();
            } else {
                dismissCurrentNotice();
            }
        }
    };

    @Inject
    RemovePhoneNumberController(LocalBroadCastUtil localBroadCastUtil) {
        this.localBroadCastUtil = localBroadCastUtil;
    }

    public void onRemovePhoneNumber(AppCompatActivity activity) {
        this.activity = activity;
        showNotice();
    }

    public void onStart() {
        intentFilter = new IntentFilter(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED);
        intentFilter.addAction(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED);
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
    }

    public void onStop() {
        dismissCurrentNotice();
        localBroadCastUtil.unregisterReceiver(receiver);
        activity = null;
    }

    void onConfirmedRemovePhoneNumber() {
        dismissCurrentNotice();
        showConfirmationDialog();
    }

    private void showConfirmationDialog() {
        GenericAlertDialog.newInstance(null,
                activity.getString(R.string.deverification_message_are_you_sure),
                activity.getString(R.string.deverification_dialog_are_you_sure_positive),
                activity.getString(R.string.deverification_dialog_are_you_sure_negative),
                this::deverifyAccountWhenPositiveClickListener,
                false,
                false
        ).show(activity.getSupportFragmentManager(), TAG);
    }

    private void showNotice() {
        View alertView = activity.getLayoutInflater().inflate(R.layout.dialog_remove_phone_number, null);
        GenericAlertDialog.newInstance(
                alertView,
                false,
                false
        ).show(activity.getSupportFragmentManager(), TAG);
        alertView.findViewById(R.id.ok).setOnClickListener(v -> onConfirmedRemovePhoneNumber());
    }

    private void showDeverificationFailed() {
        dismissCurrentNotice();
        GenericAlertDialog.newInstance(
                null,
                activity.getString(R.string.deverification_dialog_failed_message),
                activity.getString(R.string.deverification_dialog_failed_positive_button),
                activity.getString(R.string.deverification_dialog_failed_negative_button),
                this::deverifyAccountWhenPositiveClickListener,
                false,
                false
        ).show(activity.getSupportFragmentManager(), TAG);
    }

    private void dismissCurrentNotice() {
        if (activity == null) return;

        GenericAlertDialog dialog = (GenericAlertDialog) activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void deverifyAccountWhenPositiveClickListener(DialogInterface dialogInterface, int which) {
        if (which == dialogInterface.BUTTON_POSITIVE) {
            deverifyAccount();
        }

    }

    private void deverifyAccount() {
        if (activity != null)
            activity.startService(new Intent(activity, DeverifyAccountService.class));
    }
}
