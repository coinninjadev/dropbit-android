package com.coinninja.coinkeeper.ui.home

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import dagger.Module
import dagger.Provides


@Module
class HomeModule {
    @Provides
    fun provideHomeScreenViewPagerAdapterProvider(
            thunderDomeRepository: ThunderDomeRepository
    ): HomePagerAdapterProvider = HomePagerAdapterProvider(
            thunderDomeRepository
    )

}