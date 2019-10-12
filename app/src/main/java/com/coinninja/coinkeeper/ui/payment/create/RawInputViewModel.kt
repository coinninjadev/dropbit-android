package com.coinninja.coinkeeper.ui.payment.create

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coinninja.cn.thunderdome.model.RequestInvoice
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import com.coinninja.coinkeeper.service.callbacks.BasicCallbackHandler
import com.coinninja.coinkeeper.service.callbacks.Bip70Callback
import com.coinninja.coinkeeper.service.client.Bip70Client
import com.coinninja.coinkeeper.service.client.model.MerchantResponse
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.ClipboardUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@Mockable
class RawInputViewModel @Inject constructor(
        val thunderDomeRepository: ThunderDomeRepository,
        val clipboardUtil: ClipboardUtil,
        val bitcoinUriBuilder: BitcoinUri.Builder,
        val bip70Client: Bip70Client
) : ViewModel() {

    var invalidRawInput: MutableLiveData<String> = MutableLiveData()
    var validRawInput: MutableLiveData<BitcoinUri> = MutableLiveData()
    var validRequestInvoice: MutableLiveData<RequestInvoice> = MutableLiveData()


    fun clear() {
        invalidRawInput = MutableLiveData()
        validRawInput = MutableLiveData()
        validRequestInvoice = MutableLiveData()
    }

    fun onQrScanResult(data: Intent?) {
        data?.let {
            it.getStringExtra(DropbitIntents.EXTRA_SCANNED_DATA)?.let { cryptoUriInput ->
                processCryptoUriInput(cryptoUriInput)
            }
        }
    }

    fun inputFromPaste() {
        val value = clipboardUtil.raw
        if (value.isNullOrEmpty()) {
            invalidRawInput.value = ""
        } else {
            processCryptoUriInput(value)
        }
    }

    fun processCryptoUriInput(cryptoUriString: String) {
        if (cryptoUriString.startsWith("ln")
                || cryptoUriString.startsWith("lightning:")) {
            assumeLightning(cryptoUriString)
        } else {
            assumeBlockchain(cryptoUriString)
        }

    }

    private fun assumeLightning(cryptoUriString: String) {
        val value = if (cryptoUriString.startsWith("lightning:")) cryptoUriString.split(":")[1] else cryptoUriString
        viewModelScope.launch {
            // TODO validate locally validity of encoded value before unpacking it
            // prefix: ln + BIP-0173 currency prefix (e.g. lnbc for Bitcoin mainnet, lntb for Bitcoin testnet, and lnbcrt for Bitcoin regtest)
            // consider bech32 validation on pattern
            val requestInvoice = withContext(Dispatchers.IO) {
                thunderDomeRepository.decode(value)
            }

            withContext(Dispatchers.Main) {
                requestInvoice?.let {
                    validRequestInvoice.value = it
                }
            }
        }

    }

    private fun assumeBlockchain(cryptoUriString: String) {
        val bitcoinUri = bitcoinUriBuilder.parse(cryptoUriString)

        if (bitcoinUri.isValidPaymentAddress) {
            viewModelScope.launch {
                processCryptoUri(bitcoinUri)
            }
        } else {
            invalidRawInput.value = cryptoUriString
        }
    }

    private suspend fun processCryptoUri(bitcoinUri: BitcoinUri) = withContext(Dispatchers.IO) {
        if (bitcoinUri.isBip70) {
            bip70Client.getMerchantInformation(bitcoinUri.merchantUri, bip70Callback)
        } else if (bitcoinUri.isValidPaymentAddress) {
            valid(bitcoinUri)
        } else {
            error(bitcoinUri.toString())
        }
    }

    private suspend fun valid(bitcoinUri: BitcoinUri) = withContext(Dispatchers.Main) {
        validRawInput.value = bitcoinUri
    }

    private suspend fun error(errorText: String) = withContext(Dispatchers.Main) {
        invalidRawInput.value = errorText
    }

    internal val bip70Callback = Bip70Callback(object : BasicCallbackHandler<MerchantResponse> {
        override fun failure(errorMessage: String?) {
            viewModelScope.launch {
                error("invalid payment uri")
            }
        }

        override fun success(response: MerchantResponse) {
            response.paymentAddress?.let {
                bitcoinUriBuilder.setAddress(it)
            }
            bitcoinUriBuilder.setFee(response.requiredFee)
            bitcoinUriBuilder.setAmount(BTCCurrency(response.paymentAmount))
            bitcoinUriBuilder.setMemo(response.memo)
            val uri = bitcoinUriBuilder.build()

            viewModelScope.launch {

                if (uri.isValidPaymentAddress) {
                    valid(uri)
                } else {
                    error("invalid payment uri")
                }
            }
        }


    })
}