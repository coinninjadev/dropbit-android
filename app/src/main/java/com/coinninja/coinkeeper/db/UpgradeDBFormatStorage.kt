package com.coinninja.coinkeeper.db

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject

@Mockable
class UpgradeDBFormatStorage @Inject constructor(val preferencesUtils: PreferencesUtil) {

    var isUpgraded: Boolean
        get() = preferencesUtils.getBoolean(key, false)
        set(value) = preferencesUtils.savePreference(key, value)


    companion object {
        internal const val key: String = "cn_db_upgraded"
    }

}
