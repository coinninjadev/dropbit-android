package com.coinninja.coinkeeper.ui.twitter

import com.coinninja.coinkeeper.cn.dropbit.DropbitTwitterInviteTweetSuppressionCheck
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import dagger.Module
import dagger.Provides

@Module
class TwitterTweetModule {
    @Provides
    fun provideDropbitTwitterInviteTwitterSuppressionCheck(signedCoinKeeperApiClient: SignedCoinKeeperApiClient): DropbitTwitterInviteTweetSuppressionCheck {
        return DropbitTwitterInviteTweetSuppressionCheck(signedCoinKeeperApiClient)
    }

}
