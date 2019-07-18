package com.coinninja.coinkeeper.di.component

import com.coinninja.coinkeeper.twitter.MyTwitterProfile
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.image.CircleTransform
import java.util.*

interface CoinKeeperComponent {
    val analytics: Analytics

    val locale: Locale

    fun provideMyTwitter(): MyTwitterProfile
    fun provideCircleTransform(): CircleTransform
}
