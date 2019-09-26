package com.coinninja.coinkeeper.viewModel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.CoroutineContextProvider
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.file.QRFileManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Mockable
class QrViewModel internal constructor(internal val qrFileManager: QRFileManager,
                                       internal val coroutineContextProvider: CoroutineContextProvider) : ViewModel() {
    val qrCodeUri: MutableLiveData<Uri> = MutableLiveData()

    fun requestQrCodeFor(address: BitcoinUri) {
        GlobalScope.launch(coroutineContextProvider.Main) {
            val uri: Uri? = withContext(coroutineContextProvider.IO) {
                qrFileManager.createQrCode(address)
            }

            uri?.let {
                withContext(coroutineContextProvider.Main) {
                    qrCodeUri.postValue(it)
                }
            }
        }
    }

    fun requestQrCodeFor(data: String) {
        GlobalScope.launch(coroutineContextProvider.Main) {
            val uri: Uri? = withContext(coroutineContextProvider.IO) {
                qrFileManager.createQrCode(data)
            }

            uri?.let {
                withContext(coroutineContextProvider.Main) {
                    qrCodeUri.postValue(it)
                }
            }
        }
    }
}
