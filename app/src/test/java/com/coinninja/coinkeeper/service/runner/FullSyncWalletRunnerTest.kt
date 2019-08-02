package com.coinninja.coinkeeper.service.runner

import com.coinninja.coinkeeper.cn.account.RemoteAddressCache
import com.coinninja.coinkeeper.cn.dropbit.DropBitMeServiceManager
import com.coinninja.coinkeeper.cn.service.runner.AccountDeverificationServiceRunner
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.receiver.WalletSyncCompletedReceiver
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.mockito.Mockito.*


class FullSyncWalletRunnerTest {

    private fun createRunner(): FullSyncWalletRunner {
        val runner = FullSyncWalletRunner(
                mock(CNWalletManager::class.java),
                mock(AccountDeverificationServiceRunner::class.java),
                mock(WalletRegistraionRunner::class.java),
                mock(CurrentBTCStateRunner::class.java),
                mock(SyncRunnable::class.java),
                mock(TransactionConfirmationUpdateRunner::class.java),
                mock(FailedBroadcastCleaner::class.java),
                mock(SyncIncomingInvitesRunner::class.java),
                mock(FulfillSentInvitesRunner::class.java),
                mock(ReceivedInvitesStatusRunner::class.java),
                mock(NegativeBalanceRunner::class.java),
                mock(DropbitAccountHelper::class.java),
                mock(LocalBroadCastUtil::class.java),
                mock(RemoteAddressCache::class.java),
                mock(DropBitMeServiceManager::class.java)
        )

        whenever(runner.cnWalletManager.hasWallet).thenReturn(true)
        return runner
    }

    @Test
    fun does_not_execute_when_no_wallet() {
        val runner = createRunner()
        whenever(runner.cnWalletManager.hasWallet).thenReturn(false)

        runner.run()

        verify(runner.accountDeverificationServiceRunner, times(0)).run()
    }

    @Test
    fun tasks_are_executed_in_correct_order() {
        val runner = createRunner()
        whenever(runner.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        val inOrder = inOrder(runner.accountDeverificationServiceRunner, runner.walletRegistrationRunner,
                runner.dropBitMeServiceManager, runner.currentBTCStateRunner, runner.syncRunnable, runner.transactionConfirmationUpdateRunner,
                runner.syncIncomingInvitesRunner, runner.fulfillSentInvitesRunner, runner.receivedInvitesStatusRunner, runner.cnWalletManager,
                runner.negativeBalanceRunner, runner.failedBroadcastCleaner, runner.localBroadCastUtil, runner.remoteAddressCache)

        runner.run()

        inOrder.verify(runner.accountDeverificationServiceRunner).run()
        inOrder.verify(runner.walletRegistrationRunner).run()
        inOrder.verify(runner.currentBTCStateRunner).run()
        inOrder.verify(runner.dropBitMeServiceManager).syncIdentities()

        inOrder.verify(runner.syncIncomingInvitesRunner).run()
        inOrder.verify(runner.receivedInvitesStatusRunner).run()
        inOrder.verify(runner.negativeBalanceRunner).run()
        inOrder.verify(runner.fulfillSentInvitesRunner).run()
        inOrder.verify(runner.remoteAddressCache).cacheAddresses()

        inOrder.verify(runner.syncRunnable).run()
        inOrder.verify(runner.transactionConfirmationUpdateRunner).run()
        inOrder.verify(runner.failedBroadcastCleaner).run()
        inOrder.verify(runner.cnWalletManager).updateBalances()
        inOrder.verify(runner.localBroadCastUtil).sendGlobalBroadcast(
                WalletSyncCompletedReceiver::class.java,
                DropbitIntents.ACTION_WALLET_SYNC_COMPLETE)

    }

    @Test
    fun tasks_are_executed_in_correct_order_when_user_has_not_verified_account() {
        val runner = createRunner()
        whenever(runner.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)

        val inOrder = inOrder(runner.accountDeverificationServiceRunner, runner.walletRegistrationRunner,
                runner.currentBTCStateRunner, runner.syncRunnable, runner.transactionConfirmationUpdateRunner,
                runner.syncIncomingInvitesRunner, runner.fulfillSentInvitesRunner, runner.receivedInvitesStatusRunner,
                runner.negativeBalanceRunner, runner.failedBroadcastCleaner, runner.cnWalletManager)

        runner.run()

        inOrder.verify(runner.accountDeverificationServiceRunner).run()
        inOrder.verify(runner.walletRegistrationRunner).run()
        inOrder.verify(runner.currentBTCStateRunner).run()
        inOrder.verify(runner.syncRunnable).run()
        inOrder.verify(runner.transactionConfirmationUpdateRunner).run()
        inOrder.verify(runner.failedBroadcastCleaner).run()
        inOrder.verify(runner.cnWalletManager).updateBalances()

        verify(runner.remoteAddressCache, times(0)).cacheAddresses()
        verify(runner.syncIncomingInvitesRunner, times(0)).run()
        verify(runner.fulfillSentInvitesRunner, times(0)).run()
        verify(runner.receivedInvitesStatusRunner, times(0)).run()
        verify(runner.negativeBalanceRunner, times(0)).run()
    }

    @Test
    fun notifiesThatWalletSyncHasCompleted() {
        val runner = createRunner()
        runner.run()

        verify(runner.localBroadCastUtil).sendGlobalBroadcast(WalletSyncCompletedReceiver::class.java, DropbitIntents.ACTION_WALLET_SYNC_COMPLETE)
    }

    @Test
    fun updates_confirmation_counts() {
        val runner = createRunner()
        runner.run()

        verify(runner.transactionConfirmationUpdateRunner).run()
    }

    @Test
    fun runs_current_btc_state_runner() {
        val runner = createRunner()
        runner.run()

        verify(runner.currentBTCStateRunner).run()
    }

    @Test
    fun registers_wallet_with_CN() {
        val runner = createRunner()
        runner.run()

        verify(runner.walletRegistrationRunner).run()
    }

    @Test
    fun startsSync() {
        val runner = createRunner()
        runner.run()

        verify(runner.syncRunnable).run()
    }

    @Test
    fun if_backed_up_kickoff_invite_flow() {
        val runner = createRunner()
        whenever(runner.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)

        runner.run()


        verify(runner.syncIncomingInvitesRunner).run()
        verify(runner.fulfillSentInvitesRunner).run()
        verify(runner.receivedInvitesStatusRunner).run()
    }

    @Test
    fun if_NOT_backed_up_do_not_kickoff_invite_flow() {
        val runner = createRunner()
        whenever(runner.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)

        runner.run()

        verify(runner.syncIncomingInvitesRunner, times(0)).run()
        verify(runner.fulfillSentInvitesRunner, times(0)).run()
        verify(runner.receivedInvitesStatusRunner, times(0)).run()
    }
}