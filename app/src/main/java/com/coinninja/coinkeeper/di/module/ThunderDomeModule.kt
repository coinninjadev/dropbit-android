package com.coinninja.coinkeeper.di.module

import app.coinninja.cn.persistance.DropbitDatabase
import app.coinninja.cn.thunderdome.CreateInvoiceViewModel
import app.coinninja.cn.thunderdome.client.ThunderDomeApiClient
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.di.interfaces.LightningDepositAddress
import com.coinninja.coinkeeper.service.interceptors.SignedRequestInterceptor
import dagger.Module
import dagger.Provides

@Module
class ThunderDomeModule {
    @Provides
    fun thunderDomeRepository(
            apiClient: ThunderDomeApiClient,
            dropbitDatabase: DropbitDatabase
    ): ThunderDomeRepository = ThunderDomeRepository(apiClient, dropbitDatabase)

    @Provides
    fun thunderDomeClient(signedRequestInterceptor: SignedRequestInterceptor): ThunderDomeApiClient {
        signedRequestInterceptor.isThunderDome = true
        return ThunderDomeApiClient.create(BuildConfig.THUNDERDOME_API_BASE, signedRequestInterceptor)
    }


    @Provides
    fun thunderDomeCreateInvoiceViewModel(thunderDomeRepository: ThunderDomeRepository) = CreateInvoiceViewModel(thunderDomeRepository)

    @LightningDepositAddress
    @Provides
    fun lightningDepositAddress(thunderDomeRepository: ThunderDomeRepository): String {
        return thunderDomeRepository.lightningAccount?.address ?: ""
    }
}
