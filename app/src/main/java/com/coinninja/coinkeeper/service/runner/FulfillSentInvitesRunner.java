package com.coinninja.coinkeeper.service.runner;


import android.content.Context;
import android.content.res.Resources;

import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;

import java.util.List;

import javax.inject.Inject;

public class FulfillSentInvitesRunner implements Runnable {
    private final Resources resources;

    private final TransactionHelper transactionHelper;
    private final SentInvitesStatusGetter sentInvitesStatusGetter;
    private final SentInvitesStatusSender sentInvitesStatusSender;
    private final BroadcastBtcInviteRunner broadcastBtcInviteRunner;

    @Inject
    FulfillSentInvitesRunner(@ApplicationContext Context context,
                             TransactionHelper transactionHelper,
                             SentInvitesStatusGetter sentInvitesStatusGetter,
                             SentInvitesStatusSender sentInvitesStatusSender,
                             BroadcastBtcInviteRunner broadcastBtcInviteRunner) {

        resources = context.getResources();
        this.broadcastBtcInviteRunner = broadcastBtcInviteRunner;
        this.sentInvitesStatusGetter = sentInvitesStatusGetter;
        this.transactionHelper = transactionHelper;
        this.sentInvitesStatusSender = sentInvitesStatusSender;
    }

    @Override
    public void run() {

        //Step 1. grab all sent invites from server, save/update the ones that now have an address
        sentInvitesStatusGetter.run();

        //Step 2. get any sent invites that do not have a tx id but have an address
        List<InviteTransactionSummary> unfulfilledTransactions = transactionHelper.gatherUnfulfilledInviteTrans();
        broadcastRealTxForInvites(unfulfilledTransactions);

        //Step 3. report to coinninja server of any invites that have been newly fulfilled (with TX ID)
        sentInvitesStatusSender.run();
    }

    public void broadcastRealTxForInvites(List<InviteTransactionSummary> unfulfilledTransactions) {
        for (InviteTransactionSummary transaction : unfulfilledTransactions) {
            if (alreadyHasTxId(transaction))
                continue;//if this invite already has a tx id then skip it

            broadcastBtcInviteRunner.setInvite(transaction);
            broadcastBtcInviteRunner.run();

            sleep(1000);//see if this fixes spending change address issue?
        }
    }

    protected boolean alreadyHasTxId(InviteTransactionSummary transaction) {
        String currentTXID = transaction.getBtcTransactionId();
        return !(currentTXID == null || currentTXID.isEmpty());
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
