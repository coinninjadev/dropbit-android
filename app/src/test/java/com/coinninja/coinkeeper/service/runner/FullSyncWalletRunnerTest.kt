package com.coinninja.coinkeeper.service.runner

import com.coinninja.coinkeeper.receiver.WalletSyncCompletedReceiver
import com.coinninja.coinkeeper.util.DropbitIntents
import com.nhaarman.mockitokotlin2.*
import org.junit.Test


class FullSyncWalletRunnerTest {

    private fun createRunner(): FullSyncWalletRunner {
        val runner = FullSyncWalletRunner(mock(), mock(), mock(), mock(), mock(), mock(), mock(),
                mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock())

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
                runner.negativeBalanceRunner, runner.failedBroadcastCleaner, runner.localBroadCastUtil,
                runner.remoteAddressCache, runner.thunderDomeRepository, runner.lightningWithdrawlLinker, runner.lightningInviteLinker)

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
        inOrder.verify(runner.thunderDomeRepository).sync()
        inOrder.verify(runner.lightningInviteLinker).linkInvitesToInvoices()
        inOrder.verify(runner.lightningWithdrawlLinker).linkWithdraws()
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
                runner.negativeBalanceRunner, runner.failedBroadcastCleaner, runner.cnWalletManager, runner.thunderDomeRepository)

        runner.run()

        inOrder.verify(runner.accountDeverificationServiceRunner).run()
        inOrder.verify(runner.walletRegistrationRunner).run()
        inOrder.verify(runner.currentBTCStateRunner).run()
        inOrder.verify(runner.syncRunnable).run()
        inOrder.verify(runner.thunderDomeRepository).sync()
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
    fun if_NOT_backed_up_do_not_kickoff_invite_flow() {
        val runner = createRunner()
        whenever(runner.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)

        runner.run()

        verify(runner.syncIncomingInvitesRunner, times(0)).run()
        verify(runner.fulfillSentInvitesRunner, times(0)).run()
        verify(runner.receivedInvitesStatusRunner, times(0)).run()
    }
}