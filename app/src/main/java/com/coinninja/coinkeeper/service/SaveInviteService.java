package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.util.DropbitIntents;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SaveInviteService extends IntentService {
    public static final String TAG = SaveInviteService.class.getName();

    @Inject
    TransactionNotificationManager transactionNotificationManager;

    @Inject
    InviteTransactionSummaryHelper inviteTransactionSummaryHelper;

    @Inject
    CNWalletManager cnWalletManager;

    public SaveInviteService() {
        this(TAG);
    }

    public SaveInviteService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        CompletedInviteDTO completedInviteDTO = intent.getParcelableExtra(DropbitIntents.EXTRA_COMPLETED_INVITE_DTO);
        InviteTransactionSummary invite = inviteTransactionSummaryHelper.acknowledgeInviteTransactionSummary(completedInviteDTO);
        transactionNotificationManager.saveTransactionNotificationLocally(invite, completedInviteDTO);
        cnWalletManager.updateBalances();
    }
}
