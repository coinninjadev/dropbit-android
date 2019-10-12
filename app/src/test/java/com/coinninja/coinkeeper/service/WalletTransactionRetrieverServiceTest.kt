package com.coinninja.coinkeeper.service

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.robolectric.Robolectric


@RunWith(AndroidJUnit4::class)
class WalletTransactionRetrieverServiceTest {

    @Test
    fun executes_full_sync_on_handle_work() {
        val service = Robolectric.setupService(WalletTransactionRetrieverService::class.java)
        service.fullSyncWalletRunner = mock()
        service.onHandleWork(Intent())

        verify(service.fullSyncWalletRunner).run()
    }

    @Module
    class TestWalletTransactionRetrieverServiceModule {
        @Provides
        fun provideFullSyncWalletRunner(): FullSyncWalletRunner = mock()
    }

}