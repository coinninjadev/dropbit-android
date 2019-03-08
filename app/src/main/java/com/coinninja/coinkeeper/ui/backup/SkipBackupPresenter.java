package com.coinninja.coinkeeper.ui.backup;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.NotificationUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.view.activity.CalculatorActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import javax.inject.Inject;

public class SkipBackupPresenter implements DialogInterface.OnClickListener {

    private Activity activity;
    private String[] words;
    private Analytics analytics;
    private CNWalletManager cnWalletManager;
    private NotificationUtil notificationUtil;

    @Inject
    SkipBackupPresenter(Analytics analytics, CNWalletManager cnWalletManager, NotificationUtil notificationUtil) {
        this.analytics = analytics;
        this.cnWalletManager = cnWalletManager;
        this.notificationUtil = notificationUtil;
    }

    public void presentSkip(Activity activity, String[] words) {
        this.activity = activity;
        this.words = words;
        GenericAlertDialog.newInstance("",
                activity.getString(R.string.skip_recovery_warning_message),
                activity.getString(R.string.skip_recovery_warning_postive_button_text),
                activity.getString(R.string.skip_recovery_warning_negative_button_text),
                this, false, false)
                .show(activity.getFragmentManager(), SkipBackupPresenter.class.getSimpleName());
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            notificationUtil.dispatchInternalError(activity.getString(R.string.message_dont_forget_to_backup));
            analytics.trackEvent(Analytics.EVENT_WALLET_BACKUP_SKIPPED);
            cnWalletManager.skipBackup(words);
            Intent intent = new Intent(activity, CalculatorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        }

    }
}
