package com.coinninja.coinkeeper.cn.wallet.mode

import app.dropbit.annotations.Mockable
import java.lang.ref.WeakReference

@Mockable
data class AccountModeManager constructor(
        var accountMode: AccountMode,
        private var balanceModeOverride: AccountMode? = null
) {
    val changeObservers: MutableList<WeakReference<AccountModeChangeObserver>> = mutableListOf()

    val balanceAccountMode: AccountMode get() = balanceModeOverride ?: accountMode

    fun overrideBalanceWith(accountMode: AccountMode) {
        balanceModeOverride = accountMode
        notifyOfChange()
    }

    fun observeChanges(accountModeChangeObserver: AccountModeChangeObserver) {
        changeObservers.add(WeakReference(accountModeChangeObserver))
    }

    fun changeMode(mode: AccountMode) {
        accountMode = mode
        notifyOfChange()
    }


    fun clearOverrides() {
        balanceModeOverride = null
        notifyOfChange()
    }

    private fun notifyOfChange() {
        changeObservers.forEach {
            it.get()?.onAccountModeChanged(accountMode)
        }
    }
}