package com.coinninja.coinkeeper.ui.payment.request

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder
import com.coinninja.coinkeeper.util.uri.routes.BitcoinRoute
import com.coinninja.coinkeeper.view.button.CopyToBufferButton
import com.coinninja.coinkeeper.viewModel.QrViewModel
import javax.inject.Inject

class PayRequestFragment : BaseFragment() {
    companion object {
        private val TAG: String = PayRequestFragment::class.java.name
    }

    @Inject
    internal lateinit var cnLogger: CNLogger

    @Inject
    internal lateinit var accountManager: AccountManager

    @Inject
    internal lateinit var bitcoinUriBuilder: BitcoinUriBuilder

    @Inject
    internal lateinit var qrViewModel: QrViewModel

    private var qrImageURI: Uri? = null

    val qrCodeUriObserver = Observer<Uri> { uri ->
        qrImageURI = uri
        updateImageUir(uri)
    }

    internal lateinit var requestAddress: BitcoinUri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_pay_request, container, false)
    }

    override fun onStart() {
        super.onStart()
        requestAddress = bitcoinUriBuilder.build(BitcoinRoute.DEFAULT.setAddress(accountManager.nextReceiveAddress))

        view?.findViewById<CopyToBufferButton>(R.id.request_copy_button)?.text = requestAddress.address
        view?.findViewById<View>(R.id.request_funds)?.setOnClickListener { onRequestFunds() }
    }

    override fun onResume() {
        super.onResume()
        qrViewModel.qrCodeUri.observe(this, qrCodeUriObserver)
        qrViewModel.requestQrCodeFor(requestAddress)
    }

    override fun onPause() {
        super.onPause()
        qrViewModel.qrCodeUri.removeObserver(qrCodeUriObserver)
    }

    private fun onRequestFunds() {
        val requestAddressString = requestAddress.toString()
        val intent = Intent(Intent.ACTION_SEND).also { intent ->
            intent.putExtra(Intent.EXTRA_TEXT, requestAddressString)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Request Bitcoin")
            intent.putExtra("subject", "Request Bitcoin")
            intent.putExtra("sms_body", requestAddressString)
            intent.type = Intent.normalizeMimeType("image/*")


            qrImageURI?.let { uri ->
                try {
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                } catch (e: Exception) {
                    cnLogger.debug(TAG, "unable to share qr image")
                }
            }
        }

        startActivity(Intent.createChooser(intent, "Request Bitcoin"))
    }

    private fun updateImageUir(uri: Uri) {
        findViewById<ImageView>(R.id.qr_code)?.apply {
            setImageURI(qrImageURI)
            visibility = View.VISIBLE
            tag = uri.toString()
            cnLogger.debug(TAG, uri.toString())
        }
    }
}

