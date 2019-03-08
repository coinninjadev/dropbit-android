package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.RemoteAddressCache;
import com.coinninja.coinkeeper.cn.service.runner.AccountDeverificationServiceRunner;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.receiver.WalletSyncCompletedReceiver;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.greenrobot.greendao.DaoException;

import javax.inject.Inject;

public class FullSyncWalletRunner implements Runnable {
    private final CNWalletManager cnWalletManager;
    private final AccountDeverificationServiceRunner accountDeverificationServiceRunner;
    private final WalletRegistraionRunner walletRegistraionRunner;
    private final CurrentBTCStateRunner currentBTCStateRunner;
    private final SyncRunnable syncRunnable;
    private final TransactionConfirmationUpdateRunner transactionConfirmationUpdateRunner;
    private final FailedBroadcastCleaner failedBroadcastCleaner;
    private final SyncIncomingInvitesRunner syncIncomingInvitesRunner;
    private final FulfillSentInvitesRunner fulfillSentInvitesRunner;
    private final ReceivedInvitesStatusRunner receivedInvitesStatusRunner;
    private final NegativeBalanceRunner negativeBalanceRunner;
    private final WalletHelper walletHelper;
    private final LocalBroadCastUtil localBroadCastUtil;
    private final RemoteAddressCache remoteAddressCache;

    @Inject
    FullSyncWalletRunner(CNWalletManager cnWalletManager, AccountDeverificationServiceRunner accountDeverificationServiceRunner,
                         WalletRegistraionRunner walletRegistraionRunner, CurrentBTCStateRunner currentBTCStateRunner,
                         SyncRunnable syncRunnable, TransactionConfirmationUpdateRunner transactionConfirmationUpdateRunner,
                         FailedBroadcastCleaner failedBroadcastCleaner, SyncIncomingInvitesRunner syncIncomingInvitesRunner,
                         FulfillSentInvitesRunner fulfillSentInvitesRunner, ReceivedInvitesStatusRunner receivedInvitesStatusRunner,
                         NegativeBalanceRunner negativeBalanceRunner,
                         WalletHelper walletHelper, LocalBroadCastUtil localBroadCastUtil, RemoteAddressCache remoteAddressCache
    ) {

        this.cnWalletManager = cnWalletManager;
        this.accountDeverificationServiceRunner = accountDeverificationServiceRunner;
        this.walletRegistraionRunner = walletRegistraionRunner;
        this.currentBTCStateRunner = currentBTCStateRunner;
        this.syncRunnable = syncRunnable;
        this.transactionConfirmationUpdateRunner = transactionConfirmationUpdateRunner;
        this.failedBroadcastCleaner = failedBroadcastCleaner;
        this.syncIncomingInvitesRunner = syncIncomingInvitesRunner;
        this.fulfillSentInvitesRunner = fulfillSentInvitesRunner;
        this.receivedInvitesStatusRunner = receivedInvitesStatusRunner;
        this.negativeBalanceRunner = negativeBalanceRunner;
        this.walletHelper = walletHelper;
        this.localBroadCastUtil = localBroadCastUtil;
        this.remoteAddressCache = remoteAddressCache;
    }

    @Override
    public void run() {
        if (!cnWalletManager.hasWallet())
            return;

        try {
            syncTransactions();
            if (walletHelper.hasVerifiedAccount()) {
                syncDropbits();
            }
            updateWallet();
        } catch (DaoException e) {
            e.printStackTrace();
        }

        localBroadCastUtil.sendGlobalBroadcast(WalletSyncCompletedReceiver.class,
                Intents.ACTION_WALLET_SYNC_COMPLETE);
    }

    private void syncTransactions() {
        accountDeverificationServiceRunner.run();
        walletRegistraionRunner.run();
        currentBTCStateRunner.run();
        syncRunnable.run();
        transactionConfirmationUpdateRunner.run();
    }

    private void syncDropbits() {
        if (!walletHelper.hasVerifiedAccount()) return;
        syncIncomingInvitesRunner.run();
        fulfillSentInvitesRunner.run();
        receivedInvitesStatusRunner.run();
        negativeBalanceRunner.run();
        remoteAddressCache.cacheAddresses();
    }

    private void updateWallet() {
        failedBroadcastCleaner.run();
        cnWalletManager.updateBalances();
    }
}
