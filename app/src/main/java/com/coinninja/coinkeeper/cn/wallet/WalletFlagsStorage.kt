package com.coinninja.coinkeeper.cn.wallet

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject

@Mockable
class WalletFlagsStorage @Inject constructor(val preferencesUtils: PreferencesUtil) {

    var flags: Long
        get() = preferencesUtils.getLong(key, 0)
        set(value) = preferencesUtils.savePreference(key, value)


    companion object {
        internal const val key: String = "cn_wallet_flags"
    }

}
