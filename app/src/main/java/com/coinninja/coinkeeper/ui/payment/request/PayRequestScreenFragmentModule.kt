package com.coinninja.coinkeeper.ui.payment.request

import app.dropbit.commons.util.CoroutineContextProvider
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.util.file.FileOutputUtil
import com.coinninja.coinkeeper.util.file.FileProviderUtil
import com.coinninja.coinkeeper.util.file.FileUtil
import com.coinninja.coinkeeper.util.file.QRFileManager
import com.coinninja.coinkeeper.util.image.QRGeneratorUtil
import com.coinninja.coinkeeper.viewModel.QrViewModel
import com.google.zxing.qrcode.QRCodeWriter
import dagger.Module
import dagger.Provides

@Module
class PayRequestScreenFragmentModule {

    @Provides
    fun provideQRViewModel(application: CoinKeeperApplication): QrViewModel = QrViewModel(
            QRFileManager(application.applicationContext,
                    QRGeneratorUtil(QRCodeWriter()), FileUtil(FileOutputUtil()),
                    FileProviderUtil()),
            CoroutineContextProvider()
    )

}
