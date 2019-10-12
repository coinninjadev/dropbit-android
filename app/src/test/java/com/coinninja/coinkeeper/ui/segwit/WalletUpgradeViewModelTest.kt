package com.coinninja.coinkeeper.ui.segwit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.Transaction
import app.coinninja.cn.libbitcoin.model.TransactionData
import com.coinninja.coinkeeper.bitcoin.BroadcastResult
import com.coinninja.coinkeeper.cn.wallet.WalletFlags
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.service.client.model.CNWallet
import com.coinninja.coinkeeper.service.client.model.ReplaceWalletRequest
import com.coinninja.rules.CoroutinesTestRule
import com.coinninja.rules.Retry
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

// Test is flaky --- good for on at a time execution for tdd but that is about it
@ExperimentalCoroutinesApi
class WalletUpgradeViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

/*
    @get:Rule
    val retryRule = RetryRule(10)
*/

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun createViewModel(): WalletUpgradeViewModel = WalletUpgradeViewModel().also {
        it.delay = 0
        it.cnWalletManager = mock()
        it.syncWalletManager = mock()
        it.dropbitAccountHelper = mock()
        it.remoteAddressCache = mock()
        it.hdWalletWrapper = mock()
        it.transactionBroadcaster = mock()
        it.serviceWorkUtil = mock()
        it.cnClient = mock()
        it.dateUtil = mock()
    }

    @Ignore
    @Retry
    @Test
    fun create_wallet_sleeps_then_updates_state() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()
        viewModel.performStepOne()

        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }

        assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.StepOneCompleted)
    }

    @Ignore
    @Retry
    @Test
    fun upgrade_segwit_sleeps_then_updates_state__not_verified() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()
        whenever(viewModel.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)

        viewModel.performStepTwo()

        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }

        verifyZeroInteractions(viewModel.remoteAddressCache)
        assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.StepTwoCompleted)
    }

    @Ignore
    @Retry
    @Test
    fun upgrade_segwit_sleeps_then_updates_state__verified() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()
        whenever(viewModel.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)

        viewModel.performStepTwo()

        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }

        verify(viewModel.remoteAddressCache).removeAll()
        assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.StepTwoCompleted)
    }

    @Ignore
    @Retry
    @Test
    fun transfer_funds__does_nothing_when_no_transaction_data() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()

        viewModel.performStepThree(null)

        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }

        verifyZeroInteractions(viewModel.transactionBroadcaster)
        verify(viewModel.cnWalletManager).replaceLegacyWithSegwit()
        assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.StepThreeCompleted)
    }

    @Ignore
    @Retry
    @Test
    fun transfer_funds__does_nothing_when_no_transaction_data__replaces_when_verified() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()
        val signature = "--signature--"
        val publicSigningKey = "--pubkey--"
        val timestamp = "2019-09-17T17:19:56.762Z"
        val segwitWallet: Wallet = mock()
        val replaceWalletRequest = ReplaceWalletRequest(
                publicKey = publicSigningKey,
                flags = WalletFlags.purpose84v2,
                timestamp = timestamp,
                signature = signature
        )
        val cnWallet = CNWallet(id = "--wallet-id--")
        whenever(viewModel.dateUtil.timeInMillis()).thenReturn(1568740796762)
        whenever(viewModel.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        whenever(viewModel.cnWalletManager.segwitWalletForUpgrade).thenReturn(segwitWallet)
        whenever(viewModel.hdWalletWrapper.sign(segwitWallet, timestamp)).thenReturn(signature)
        whenever(viewModel.hdWalletWrapper.verificationKeyFor(segwitWallet)).thenReturn(publicSigningKey)
        whenever(viewModel.cnClient.replaceWalletWith(eq(replaceWalletRequest))).thenReturn(Response.success(cnWallet))

        //viewModel.performStepThree(null)
        viewModel.executeWalletReplacement()

        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }

        verifyZeroInteractions(viewModel.transactionBroadcaster)
        verify(viewModel.cnWalletManager).replaceLegacyWithSegwit()
        verify(viewModel.cnClient).disableWallet()
        verify(viewModel.cnClient).replaceWalletWith(replaceWalletRequest)
        verify(viewModel.dropbitAccountHelper).updateAccountIds(cnWallet.id)
        //assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.StepThreeCompleted)
    }

    @Ignore
    @Retry
    @Test
    fun transfer_funds__does_nothing_when_no_transaction_data__disables_when_not_verified() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()
        whenever(viewModel.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)

        viewModel.performStepThree(null)

        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }

        verifyZeroInteractions(viewModel.transactionBroadcaster)
        verify(viewModel.cnWalletManager).replaceLegacyWithSegwit()
        verify(viewModel.cnClient).disableWallet()
        assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.StepThreeCompleted)
    }

    @Ignore
    @Test
    @Retry
    fun transfer_funds__broadcasts_transaction_on_block() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()
        val transactionData = TransactionData(arrayOf(mock()),
                100_000_000, 1_000, 0,
                DerivationPath(84, 0, 0, 1, 0),
                "--segwit-address--")

        val transaction = Transaction("--txid--", "--encoded-tx--")
        whenever(viewModel.hdWalletWrapper.transactionFrom(transactionData)).thenReturn(transaction)
        whenever(viewModel.transactionBroadcaster.broadcast(transaction)).thenReturn(BroadcastResult(isSuccess = true))

        viewModel.performStepThree(transactionData)

        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }

        assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.StepThreeCompleted)
    }

    @Ignore
    @Retry
    @Test
    fun transferring_does_generic_tasks__retry_failure() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()
        val transactionData = TransactionData(arrayOf(mock()),
                100_000_000, 1_000, 0,
                DerivationPath(84, 0, 0, 1, 0),
                "--segwit-address--")

        val transaction = Transaction("--txid--", "--encoded-tx--")
        whenever(viewModel.hdWalletWrapper.transactionFrom(transactionData)).thenReturn(transaction)
        val failureResponse: BroadcastResult = BroadcastResult()
        whenever(viewModel.transactionBroadcaster.broadcast(transaction)).thenReturn(failureResponse)

        viewModel.performStepThree(transactionData)

        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }
        verify(viewModel.cnWalletManager, times(0)).replaceLegacyWithSegwit()
        verify(viewModel.transactionBroadcaster, times(3)).broadcast(transaction)
        assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.Error)

    }

    @Ignore
    @Retry
    @Test
    fun executes_cleanup_and_updates_state() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val viewModel = createViewModel()

        viewModel.cleanUp()
        runBlocking {
            advanceTimeBy(1_000)
            advanceUntilIdle()
            resumeDispatcher()
            runCurrent()
        }

        verify(viewModel.syncWalletManager).schedule30SecondSync()
        verify(viewModel.syncWalletManager).syncNow()
        verify(viewModel.serviceWorkUtil).registerForPushNotifications()
        assertThat(viewModel.upgradeState.value).isEqualTo(UpgradeState.Finished)
    }
}