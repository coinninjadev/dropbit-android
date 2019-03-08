package com.coinninja.coinkeeper.service;

import android.content.Intent;

import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import dagger.android.AndroidInjection;

public class DropbitServicePatchService extends JobIntentService {

    public static final String PATCH_KEY = "dropbit_state_patch_applied";

    public DropbitServicePatchService() {
    }

    DropbitServicePatchService(PreferencesUtil preferencesUtil, TransactionHelper transactionHelper) {
        this.preferencesUtil = preferencesUtil;
        this.transactionHelper = transactionHelper;
    }

    @Inject
    PreferencesUtil preferencesUtil;

    @Inject
    TransactionHelper transactionHelper;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (preferencesUtil.contains(PATCH_KEY)) return;

        patchSummaries(transactionHelper.getAllCanceledDropbits());
        patchSummaries(transactionHelper.getAllExpiredDropbits());
        preferencesUtil.savePreference(PATCH_KEY, true);
    }

    private void patchSummaries(List<TransactionsInvitesSummary> transactionInviteSummaries) {
        for (TransactionsInvitesSummary summary : transactionInviteSummaries) {
            if (summary.getInviteTime() > 0) {
                summary.setBtcTxTime(summary.getInviteTime());
                summary.setInviteTime(0);
                summary.update();
            }
        }
    }
}
