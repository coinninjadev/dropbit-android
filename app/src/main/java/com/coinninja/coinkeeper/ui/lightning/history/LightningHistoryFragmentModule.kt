package com.coinninja.coinkeeper.ui.lightning.history

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.image.TwitterCircleTransform
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides

@Module
class LightningHistoryFragmentModule {

    @Provides
    fun provideLightningHistoryAdapter(
            activityNavigationUtil: ActivityNavigationUtil,
            walletHelper: WalletHelper,
            currencyPreference: CurrencyPreference, twitterCircleTransform: TwitterCircleTransform)
            : LightningHistoryAdapter = LightningHistoryAdapter(
            activityNavigationUtil, walletHelper, currencyPreference,
            Picasso.get(), twitterCircleTransform)

    @Provides
    fun provideLightningHistoryViewModel(thunderDomeRepository: ThunderDomeRepository)
            : LightningHistoryViewModel = LightningHistoryViewModel(thunderDomeRepository)

}
