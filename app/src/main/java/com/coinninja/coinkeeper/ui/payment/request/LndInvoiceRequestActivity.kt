package com.coinninja.coinkeeper.ui.payment.request

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.FiatCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.button.CopyToBufferButton
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView
import com.coinninja.coinkeeper.viewModel.QrViewModel
import javax.inject.Inject

class LndInvoiceRequestActivity : BaseActivity() {


    @Inject
    internal lateinit var qrViewModel: QrViewModel

    lateinit var lndInvoiceRequest: LndInvoiceRequest

    val memo: TextView get() = findViewById(R.id.memo)
    val amountDisplayView: DefaultCurrencyDisplayView get() = findViewById(R.id.default_currency_view)
    val copyToBufferButton: CopyToBufferButton get() = findViewById(R.id.request_copy_button)
    val qrCodeImage: ImageView get() = findViewById(R.id.qr_code)
    val requestButton: Button get() = findViewById(R.id.request_funds)
    val closeButton: View get() = findViewById(R.id.close)

    var latestPrice: USDCurrency = USDCurrency(0)

    var qrImageUri: Uri = Uri.EMPTY
        set(value) {
            field = value
            qrCodeImage.apply {
                setImageURI(value)
                visibility = View.VISIBLE
                tag = value.toString()
            }
        }

    val qrCodeUriObserver = Observer<Uri> { uri ->
        qrImageUri = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lnd_invoice_request)
        if (intent.hasExtra(DropbitIntents.EXTRA_LND_INVOICE_REQUEST))
            lndInvoiceRequest = intent.getParcelableExtra(DropbitIntents.EXTRA_LND_INVOICE_REQUEST)
        else
            finish()

        closeButton.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        copyToBufferButton.text = lndInvoiceRequest.request
        requestButton.setOnClickListener { onRequestFunds() }
        qrViewModel.qrCodeUri.observe(this, qrCodeUriObserver)
        qrViewModel.requestQrCodeFor(lndInvoiceRequest.request)

        renderAmount()

        if (lndInvoiceRequest.memo.isNullOrEmpty()) {
            memo.gone()
        } else {
            memo.text = lndInvoiceRequest.memo
            memo.show()
        }
    }

    override fun onPause() {
        super.onPause()
        qrViewModel.qrCodeUri.removeObserver(qrCodeUriObserver)
    }

    override fun onLatestPriceChanged(currentPrice: FiatCurrency) {
        super.onLatestPriceChanged(currentPrice)
        latestPrice = currentPrice as USDCurrency
        renderAmount()
    }

    private fun renderAmount() {
        val view = amountDisplayView
        view.setAccountMode(AccountMode.LIGHTNING)
        val btcCurrency = lndInvoiceRequest.btcCurrency
        if (btcCurrency.toLong() > 0) {
            view.renderValues(
                    DefaultCurrencies(USDCurrency(), BTCCurrency()),
                    btcCurrency,
                    btcCurrency.toUSD(latestPrice)
            )
            view.show()
        } else {
            view.gone()
        }
    }

    private fun onRequestFunds() {
        Intent(Intent.ACTION_SEND).also { intent ->
            intent.putExtra(Intent.EXTRA_TEXT, lndInvoiceRequest.request)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Request Bitcoin")
            intent.putExtra("subject", "Request Bitcoin")
            intent.putExtra("sms_body", lndInvoiceRequest.request)
            intent.type = Intent.normalizeMimeType("image/*")


            qrImageUri?.let { uri ->
                try {
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                } catch (e: Exception) {
                }
            }
            startActivity(Intent.createChooser(intent, "Request Bitcoin"))
        }

    }

}
