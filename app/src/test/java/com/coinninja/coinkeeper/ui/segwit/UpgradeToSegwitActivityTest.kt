package com.coinninja.coinkeeper.ui.segwit

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.MetaAddress
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.model.db.Wallet
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpgradeToSegwitActivityTest {

    private fun createScenario(): ActivityScenario<UpgradeToSegwitActivity> = ActivityScenario.launch(UpgradeToSegwitActivity::class.java)

    private fun given_syncing_state_of(activity: UpgradeToSegwitActivity, isSyncing: Boolean = true) {
        whenever(activity.syncManagerViewNotifier.isSyncing).thenReturn(isSyncing)
    }

    private fun given_a_wallet_balance(activity: UpgradeToSegwitActivity) {
        whenever(activity.walletHelper.balance).thenReturn(BTCCurrency(100_010_000))
        whenever(activity.walletHelper.latestPrice).thenReturn(USDCurrency(10_000))
        given_a_segwit_wallet(activity)
    }

    private fun given_no_wallet_balance(activity: UpgradeToSegwitActivity) {
        whenever(activity.walletHelper.balance).thenReturn(BTCCurrency(0))
    }

    private fun given_a_segwit_wallet(activity: UpgradeToSegwitActivity) {
        val wallet: Wallet = mock()
        whenever(wallet.purpose).thenReturn(84)
        whenever(wallet.coinType).thenReturn(0)
        whenever(wallet.accountIndex).thenReturn(0)
        whenever(activity.cnWalletManager.segwitWalletForUpgrade).thenReturn(wallet)
        whenever(activity.hdWalletWrapper.getAddressForSegwitUpgrade(wallet,
                DerivationPath(84, 0, 0, 1, 0))
        ).thenReturn(MetaAddress("--segwit-address--", ""))
    }

    private fun when_sync_state_changed(activity: UpgradeToSegwitActivity) {
        activity.syncChangeObserver.onSyncStatusChanged()
    }

    @Test
    fun inits_properly() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            assertThat(activity.upgradeButton.isEnabled).isFalse()
            assertThat(activity.upgradePermission.visibility).isEqualTo(View.GONE)
            assertThat(activity.transferPermission.visibility).isEqualTo(View.GONE)
            assertThat(activity.syncProgressView.visibility).isEqualTo(View.VISIBLE)
            verify(activity.syncWalletManager).cancel30SecondSync()
            verify(activity.syncWalletManager).syncNow()
            verify(activity.syncManagerViewNotifier).observeSyncManagerChange(activity.syncChangeObserver)
            verify(activity.fundingViewModel.transactionData).observe(activity, activity.fundingObserver)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun sync_complete_no_funds() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            given_syncing_state_of(activity, false)
            given_no_wallet_balance(activity)

            when_sync_state_changed(activity)

            assertThat(activity.upgradeButton.isEnabled).isFalse()
            assertThat(activity.upgradePermission.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transferPermission.visibility).isEqualTo(View.GONE)
            assertThat(activity.syncProgressView.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun sync_complete_with_funds_prepares_transfer() {
        val scenario = createScenario()
        val address = "--segwit-address--"

        scenario.onActivity { activity ->
            given_syncing_state_of(activity, false)
            given_a_wallet_balance(activity)

            when_sync_state_changed(activity)

            assertThat(activity.upgradeButton.isEnabled).isFalse()
            assertThat(activity.upgradePermission.visibility).isEqualTo(View.GONE)
            assertThat(activity.transferPermission.visibility).isEqualTo(View.GONE)
            assertThat(activity.syncProgressView.visibility).isEqualTo(View.VISIBLE)
            verify(activity.fundingViewModel).fundMaxForUpgrade(address)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun enables_button_when_acknowledgements_checked__with_out_funds() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            given_syncing_state_of(activity, false)
            given_no_wallet_balance(activity)
            when_sync_state_changed(activity)

            // on
            activity.upgradePermission.isChecked = true
            assertThat(activity.upgradeButton.isEnabled).isTrue()

            // off
            activity.upgradePermission.isChecked = false
            assertThat(activity.upgradeButton.isEnabled).isFalse()

            // on again
            activity.upgradePermission.isChecked = true
            assertThat(activity.upgradeButton.isEnabled).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun enables_button_when_acknowledgements_checked__with_funds() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            given_a_wallet_balance(activity)
            when_sync_state_changed(activity)

            // upgrade on
            activity.upgradePermission.performClick()
            assertThat(activity.upgradeButton.isEnabled).isFalse()
            // transfer on
            activity.transferPermission.performClick()
            assertThat(activity.upgradeButton.isEnabled).isTrue()

            // upgrade off
            activity.upgradePermission.performClick()
            assertThat(activity.upgradeButton.isEnabled).isFalse()

            // upgrade on again
            activity.upgradePermission.performClick()
            assertThat(activity.upgradeButton.isEnabled).isTrue()

            // transfer off
            activity.transferPermission.performClick()
            assertThat(activity.upgradeButton.isEnabled).isFalse()

            // transfer on again
            activity.transferPermission.performClick()
            assertThat(activity.upgradeButton.isEnabled).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_permissions_once_funding_completes() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            given_syncing_state_of(activity, false)
            given_a_wallet_balance(activity)

            when_sync_state_changed(activity)
            activity.fundingObserver.onChanged(TransactionData(
                    arrayOf(mock(), mock()),
                    100_000_000,
                    10_000,
                    0,
                    DerivationPath(84, 0, 0, 0, 0),
                    "--address--"
            ))

            assertThat(activity.upgradeButton.isEnabled).isFalse()
            assertThat(activity.upgradePermission.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transferPermission.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transferPermission.text)
                    .isEqualTo("I understand that DropBit will be transferring my funds of \$100.00 with a transaction fee of \$0.01 to my upgraded wallet.")
            assertThat(activity.syncProgressView.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun upgrade_progresses_to_next_step() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            given_syncing_state_of(activity, false)
            given_a_wallet_balance(activity)
            when_sync_state_changed(activity)
            val transactionData = TransactionData(
                    arrayOf(mock(), mock()),
                    100_000_000,
                    10_000,
                    0,
                    DerivationPath(84, 0, 0, 1, 0),
                    "--segwit-address--"
            )
            activity.fundingObserver.onChanged(transactionData)
            activity.upgradePermission.performClick()
            activity.transferPermission.performClick()

            activity.upgradeButton.performClick()

            verify(activity.activityNavigationUtil).navigateToUpgradeToSegwitStepTwo(activity, transactionData)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun upgrade_progresses_to_next_step__without_transaction_data__when_not_funded() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            whenever(activity.syncManagerViewNotifier.isSyncing).thenReturn(false)
            whenever(activity.walletHelper.balance).thenReturn(BTCCurrency(0))
            when_sync_state_changed(activity)
            activity.upgradePermission.performClick()

            activity.upgradeButton.performClick()

            verify(activity.activityNavigationUtil).navigateToUpgradeToSegwitStepTwo(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Module
    class UpgradeToSegwitActivityTestModule {
        @Provides
        fun provideFundingViewModelProvider(): FundingViewModelProvider {
            val fundingViewModelProvider: FundingViewModelProvider = mock()
            val fundingViewModel: FundingViewModel = mock()
            whenever(fundingViewModel.transactionData).thenReturn(mock())
            whenever(fundingViewModelProvider.provide(any())).thenReturn(fundingViewModel)
            return fundingViewModelProvider
        }
    }

}