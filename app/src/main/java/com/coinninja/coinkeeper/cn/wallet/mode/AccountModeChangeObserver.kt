package com.coinninja.coinkeeper.cn.wallet.mode

interface AccountModeChangeObserver {
    fun onAccountModeChanged(accountMode: AccountMode)
}
