package com.coinninja.coinkeeper.di.component

import com.coinninja.coinkeeper.cn.wallet.runner.SaveRecoveryWordsRunner
import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner
import com.coinninja.coinkeeper.twitter.MyTwitterProfile
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.image.CircleTransform
import java.util.*

interface CoinKeeperComponent {
    val fullSyncRunner: FullSyncWalletRunner

    val saveRecoveryWordsRunner: SaveRecoveryWordsRunner

    val analytics: Analytics

    val locale: Locale

    fun provideMyTwitter(): MyTwitterProfile
    fun provideCircleTransform(): CircleTransform
}
