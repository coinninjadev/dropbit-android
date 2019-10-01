package com.coinninja.coinkeeper.cn.wallet.mode

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test


class AccountModeManagerTest {

    @Test
    fun returns_current_mode() {
        assertThat(AccountModeManager(AccountMode.BLOCKCHAIN).accountMode).isEqualTo(AccountMode.BLOCKCHAIN)
        assertThat(AccountModeManager(AccountMode.LIGHTNING).accountMode).isEqualTo(AccountMode.LIGHTNING)
    }

    @Test
    fun allows_observers_to_mode_changes() {
        val accountModeChangeObserver: AccountModeChangeObserver = mock()
        val manager = AccountModeManager(AccountMode.LIGHTNING)
        manager.observeChanges(accountModeChangeObserver)

        manager.changeMode(AccountMode.BLOCKCHAIN)

        assertThat(manager.accountMode).isEqualTo(AccountMode.BLOCKCHAIN)
        verify(accountModeChangeObserver).onAccountModeChanged(AccountMode.BLOCKCHAIN)
    }

    @Test
    fun allows_screens_to_override_display_mode_for_balances() {
        val accountModeChangeObserver: AccountModeChangeObserver = mock()
        val accountModeManager = AccountModeManager(AccountMode.BLOCKCHAIN)
        accountModeManager.observeChanges(accountModeChangeObserver)
        assertThat(accountModeManager.accountMode).isEqualTo(AccountMode.BLOCKCHAIN)
        assertThat(accountModeManager.balanceAccountMode).isEqualTo(AccountMode.BLOCKCHAIN)

        accountModeManager.overrideBalanceWith(AccountMode.LIGHTNING)
        assertThat(accountModeManager.balanceAccountMode).isEqualTo(AccountMode.LIGHTNING)

        accountModeManager.clearOverrides()
        assertThat(accountModeManager.balanceAccountMode).isEqualTo(AccountMode.BLOCKCHAIN)

        verify(accountModeChangeObserver, times(2)).onAccountModeChanged(any())
    }

    @Test
    fun remove_observer() {
        val observer1: AccountModeChangeObserver = mock()
        val observer2: AccountModeChangeObserver = mock()
        val accountModeManager = AccountModeManager(AccountMode.BLOCKCHAIN)

        accountModeManager.observeChanges(observer1)
        accountModeManager.observeChanges(observer2)

        assertThat(accountModeManager.changeObservers.size).isEqualTo(2)

        accountModeManager.removeObserver(observer1)
        assertThat(accountModeManager.changeObservers.size).isEqualTo(1)
    }
}
