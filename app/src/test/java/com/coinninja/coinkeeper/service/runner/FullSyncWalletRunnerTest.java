package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.RemoteAddressCache;
import com.coinninja.coinkeeper.cn.service.runner.AccountDeverificationServiceRunner;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.receiver.WalletSyncCompletedReceiver;
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class FullSyncWalletRunnerTest {

    @Mock
    private CNWalletManager cnWalletManager;
    @Mock
    private LocalBroadCastUtil localBroadCastUtil;
    @Mock
    private SyncRunnable syncRunnable;
    @Mock
    private WalletRegistraionRunner walletRegistrationRunner;
    @Mock
    private TransactionConfirmationUpdateRunner transactionConfirmationUpdateRunner;
    @Mock
    private CurrentBTCStateRunner currentBTCstateRunner;
    @Mock
    private SyncIncomingInvitesRunner syncIncomingInvitesRunner;
    @Mock
    private FulfillSentInvitesRunner fulfillSentInvitesRunner;
    @Mock
    private ReceivedInvitesStatusRunner receivedInvitesStatusRunner;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private NegativeBalanceRunner negativeBalanceRunner;
    @Mock
    private FailedBroadcastCleaner failedBroadcastCleaner;
    @Mock
    private AccountDeverificationServiceRunner accountDeverificationServiceRunner;
    @Mock
    private RemoteAddressCache remoteAddressCache;
    @Mock
    private SyncManagerViewNotifier syncManagerViewNotifier;

    @InjectMocks
    FullSyncWalletRunner runner;

    @Before
    public void setUp() {
        when(cnWalletManager.hasWallet()).thenReturn(true);
    }

    @After
    public void tearDown() {
        cnWalletManager = null;
        localBroadCastUtil = null;
        syncRunnable = null;
        walletRegistrationRunner = null;
        transactionConfirmationUpdateRunner = null;
        currentBTCstateRunner = null;
        syncIncomingInvitesRunner = null;
        fulfillSentInvitesRunner = null;
        receivedInvitesStatusRunner = null;
        remoteAddressCache = null;
        walletHelper = null;
        negativeBalanceRunner = null;
        failedBroadcastCleaner = null;
        accountDeverificationServiceRunner = null;
        runner = null;
    }

    @Test
    public void does_not_execute_when_no_wallet() {
        when(cnWalletManager.hasWallet()).thenReturn(false);

        runner.run();

        verify(accountDeverificationServiceRunner, times(0)).run();
    }

    @Test
    public void tasks_are_executed_in_correct_order() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        InOrder inOrder = inOrder(accountDeverificationServiceRunner, walletRegistrationRunner,
                currentBTCstateRunner, syncRunnable, transactionConfirmationUpdateRunner,
                syncIncomingInvitesRunner, fulfillSentInvitesRunner, receivedInvitesStatusRunner, cnWalletManager,
                negativeBalanceRunner, failedBroadcastCleaner, localBroadCastUtil, remoteAddressCache);

        runner.run();

        inOrder.verify(accountDeverificationServiceRunner).run();
        inOrder.verify(walletRegistrationRunner).run();
        inOrder.verify(currentBTCstateRunner).run();
        inOrder.verify(syncRunnable).run();
        inOrder.verify(transactionConfirmationUpdateRunner).run();
        inOrder.verify(syncIncomingInvitesRunner).run();
        inOrder.verify(fulfillSentInvitesRunner).run();
        inOrder.verify(receivedInvitesStatusRunner).run();
        inOrder.verify(negativeBalanceRunner).run();
        inOrder.verify(remoteAddressCache).cacheAddresses();
        inOrder.verify(failedBroadcastCleaner).run();
        inOrder.verify(cnWalletManager).updateBalances();
        inOrder.verify(localBroadCastUtil).sendGlobalBroadcast(
                WalletSyncCompletedReceiver.class,
                Intents.ACTION_WALLET_SYNC_COMPLETE);

    }

    @Test
    public void tasks_are_executed_in_correct_order_when_user_has_not_verified_account() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(false);

        InOrder inOrder = inOrder(accountDeverificationServiceRunner, walletRegistrationRunner,
                currentBTCstateRunner, syncRunnable, transactionConfirmationUpdateRunner,
                syncIncomingInvitesRunner, fulfillSentInvitesRunner, receivedInvitesStatusRunner,
                negativeBalanceRunner, failedBroadcastCleaner, cnWalletManager, syncManagerViewNotifier);

        runner.run();

        inOrder.verify(syncManagerViewNotifier).setSyncing(true);
        inOrder.verify(accountDeverificationServiceRunner).run();
        inOrder.verify(walletRegistrationRunner).run();
        inOrder.verify(currentBTCstateRunner).run();
        inOrder.verify(syncRunnable).run();
        inOrder.verify(transactionConfirmationUpdateRunner).run();
        inOrder.verify(failedBroadcastCleaner).run();
        inOrder.verify(cnWalletManager).updateBalances();
        inOrder.verify(syncManagerViewNotifier).setSyncing(false);

        verify(remoteAddressCache, times(0)).cacheAddresses();
        verify(syncIncomingInvitesRunner, times(0)).run();
        verify(fulfillSentInvitesRunner, times(0)).run();
        verify(receivedInvitesStatusRunner, times(0)).run();
        verify(negativeBalanceRunner, times(0)).run();
    }

    @Test
    public void notifiesThatWalletSyncHasCompleted() {
        runner.run();

        verify(localBroadCastUtil).sendGlobalBroadcast(WalletSyncCompletedReceiver.class, Intents.ACTION_WALLET_SYNC_COMPLETE);
    }

    @Test
    public void updates_confirmation_counts() {
        runner.run();

        verify(transactionConfirmationUpdateRunner).run();
    }

    @Test
    public void runs_current_btc_state_runner() {
        runner.run();

        verify(currentBTCstateRunner).run();
    }

    @Test
    public void registers_wallet_with_CN() {
        runner.run();

        verify(walletRegistrationRunner).run();
    }

    @Test
    public void startsSync() {
        runner.run();

        verify(syncRunnable).run();
    }

    @Test
    public void if_backed_up_kickoff_invite_flow() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);

        runner.run();


        verify(syncIncomingInvitesRunner).run();
        verify(fulfillSentInvitesRunner).run();
        verify(receivedInvitesStatusRunner).run();
    }

    @Test
    public void if_NOT_backed_up_do_not_kickoff_invite_flow() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(false);

        runner.run();

        verify(syncIncomingInvitesRunner, times(0)).run();
        verify(fulfillSentInvitesRunner, times(0)).run();
        verify(receivedInvitesStatusRunner, times(0)).run();
    }
}