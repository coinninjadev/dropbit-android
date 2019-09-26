package com.coinninja.coinkeeper.ui.payment.request

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.Observer
import app.coinninja.cn.thunderdome.CreateInvoiceViewModel
import app.dropbit.commons.currency.FiatCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.android.helpers.styleAsBitcoin
import com.coinninja.android.helpers.styleAsLightning
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.memo.MemoCreator
import com.coinninja.coinkeeper.ui.payment.PaymentInputView
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.view.button.CopyToBufferButton
import com.coinninja.coinkeeper.view.widget.AccountModeToggleButton
import com.coinninja.coinkeeper.viewModel.QrViewModel
import kotlinx.android.synthetic.main.fragment_pay_dialog.*
import javax.inject.Inject


class PayRequestActivity : BaseActivity() {
    @Inject
    internal lateinit var cnLogger: CNLogger

    @Inject
    internal lateinit var accountManager: AccountManager

    @Inject
    internal lateinit var qrViewModel: QrViewModel

    @Inject
    internal lateinit var bitcoinUriBuilder: BitcoinUri.Builder

    @Inject
    internal lateinit var memoCreator: MemoCreator

    @Inject
    internal lateinit var createInvoiceViewModel: CreateInvoiceViewModel

    private var qrImageURI: Uri? = null
    lateinit var requestAddress: BitcoinUri

    val closeButton: View get() = findViewById(R.id.close)
    val requestFundsButton: Button get() = findViewById(R.id.request_funds)
    val copyToBufferButton: CopyToBufferButton get() = findViewById(R.id.request_copy_button)
    val qrCodeImage: ImageView get() = findViewById(R.id.qr_code)
    val addAmountButton: View get() = findViewById(R.id.add_amount)
    val addMemoButton: Button get() = findViewById(R.id.add_memo_button)
    val accountModeToggle: AccountModeToggleButton get() = findViewById(R.id.account_mode_toggle)
    val amountInputView: PaymentInputView get() = findViewById(R.id.payment_input_view)
    val copyLabel: View get() = findViewById(R.id.request_footer_copy_description_txt)

    val latestPriceObserver: Observer<FiatCurrency> = Observer {
        val paymentHolder = amountInputView.paymentHolder
        paymentHolder.evaluationCurrency = it
        amountInputView.paymentHolder = paymentHolder
    }

    val validPaymentAmountObserver = object : PaymentInputView.OnValidEntryObserver {
        override fun onValidEntry() {
            requestAddress = bitcoinUriBuilder.setAmount(payment_input_view.paymentHolder.btcCurrency).build()
            qrViewModel.requestQrCodeFor(requestAddress)
        }
    }

    val createInvoiceRequestObserver: Observer<String?> = Observer {
        it?.let { request ->
            val paymentHolder = amountInputView.paymentHolder
            val memo: String = memoForInvoice()
            activityNavigationUtil.navigateToShowLndInvoice(this, LndInvoiceRequest(
                    request, paymentHolder.btcCurrency, memo))
            finish()
        }
    }

    val zeroedObserver = object : PaymentInputView.OnZeroedObserver {
        override fun onZeroed() {
            requestAddress = bitcoinUriBuilder.removeAmount().build()
            qrViewModel.requestQrCodeFor(requestAddress)
        }
    }

    val qrCodeUriObserver = Observer<Uri> { uri ->
        qrImageURI = uri
        updateImageUri(uri)
    }

    val isLightningLockedObserver: Observer<Boolean> = Observer {
        accountModeToggle.isLightningLocked = it
    }

    val createMemoCallback = object : MemoCreator.OnMemoCreatedCallback {
        override fun onMemoCreated(memo: String) {
            addMemoButton.text = if (memo.isEmpty()) getString(R.string.add_a_memo) else memo
        }
    }

    val accountModeToggleObserver = object : AccountModeToggleButton.AccountModeSelectedObserver {
        override fun onSelectionChange(mode: AccountMode) {
            when (mode) {
                AccountMode.BLOCKCHAIN -> showBlockChain()
                AccountMode.LIGHTNING -> showLightning()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_request)
        requestFundsButton.setOnClickListener { onRequestFunds() }
        closeButton.setOnClickListener { onBackPressed() }
        amountInputView.apply {
            canToggleCurrencies = false
            canSendMax = false
            paymentHolder = PaymentHolder()
        }
        addAmountButton.setOnClickListener {
            amountInputView.visibility = View.VISIBLE
            amountInputView.requestFocus()
            it.visibility = View.GONE
        }

        accountModeToggle.isLightningLocked = true
        accountModeToggle.onModeSelectedObserver = accountModeToggleObserver
        addMemoButton.gone()
    }

    override fun onStart() {
        super.onStart()
        requestAddress = bitcoinUriBuilder.setAddress(accountManager.nextReceiveAddress).build()
        copyToBufferButton.text = requestAddress.address
    }

    override fun onResume() {
        super.onResume()
        walletViewModel.currentPrice.observe(this, latestPriceObserver)
        walletViewModel.isLightningLocked.observe(this, isLightningLockedObserver)
        walletViewModel.checkLightningLock()

        when (accountModeManager.accountMode) {
            AccountMode.BLOCKCHAIN -> showBlockChain()
            AccountMode.LIGHTNING -> showLightning()
        }
    }

    override fun onPause() {
        super.onPause()
        qrViewModel.qrCodeUri.removeObserver(qrCodeUriObserver)
        walletViewModel.currentPrice.removeObserver(latestPriceObserver)
        walletViewModel.isLightningLocked.removeObserver(isLightningLockedObserver)
        createInvoiceViewModel.request.removeObserver(createInvoiceRequestObserver)
    }


    private fun updateImageUri(uri: Uri) {
        qrCodeImage.apply {
            setImageURI(qrImageURI)
            visibility = View.VISIBLE
            tag = uri.toString()
            cnLogger.debug(TAG, uri.toString())
        }
    }

    private fun showLightning() {
        qrViewModel.qrCodeUri.removeObserver(qrCodeUriObserver)
        amountInputView.paymentHolder.clearPayment()
        amountInputView.gone()
        addMemoButton.show()
        addAmountButton.show()
        qrCodeImage.gone()
        copyToBufferButton.gone()
        copyLabel.gone()
        requestFundsButton.apply {
            text = getString(R.string.create_invoice)
            styleAsLightning()
            setOnClickListener { createInvoice() }
        }
        addMemoButton.setOnClickListener { memoCreator.createMemo(this, createMemoCallback, memoForInvoice()) }
        accountModeToggle.mode = AccountMode.LIGHTNING
        amountInputView.onValidEntryObserver = null
        amountInputView.onZeroedObserver = null
    }

    private fun showBlockChain() {
        amountInputView.paymentHolder.clearPayment()
        amountInputView.gone()
        addMemoButton.gone()
        addAmountButton.show()
        qrCodeImage.show()
        copyToBufferButton.show()
        copyLabel.show()
        accountModeToggle.mode = AccountMode.BLOCKCHAIN
        requestFundsButton.apply {
            text = getString(R.string.send_request)
            styleAsBitcoin()
            setOnClickListener { onRequestFunds() }
        }
        qrViewModel.qrCodeUri.observe(this, qrCodeUriObserver)
        qrViewModel.requestQrCodeFor(requestAddress)
        amountInputView.onValidEntryObserver = validPaymentAmountObserver
        amountInputView.onZeroedObserver = zeroedObserver
        addMemoButton.setOnClickListener {}
    }

    private fun createInvoice() {
        createInvoiceViewModel.request.observe(this, createInvoiceRequestObserver)
        createInvoiceViewModel.createInvoiceFor(amountInputView.paymentHolder.btcCurrency.toLong(), memoForInvoice())
    }

    private fun memoForInvoice(): String {
        val memo: String = addMemoButton.text.toString()
        val default: String = getString(R.string.add_a_memo)
        return if (memo == default) "" else memo
    }

    private fun onRequestFunds() {
        val requestAddressString = requestAddress.toString()
        Intent(Intent.ACTION_SEND).also { intent ->
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
            startActivity(Intent.createChooser(intent, "Request Bitcoin"))
        }

    }

    companion object {
        private val TAG: String = PayRequestActivity::class.java.name
    }

}