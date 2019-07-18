package com.coinninja.coinkeeper.di.module

import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import dagger.Module
import dagger.Provides

@Module
class FeeManagerModule {

    @Provides
    fun feesManager(preferencesUtil: PreferencesUtil): FeesManager = FeesManager(preferencesUtil)
}
