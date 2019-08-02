package com.coinninja.coinkeeper.view.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import app.dropbit.annotations.Mockable
import com.coinninja.android.helpers.Views.shakeInError
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate
import com.coinninja.coinkeeper.interactor.UserPreferences
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks
import com.coinninja.coinkeeper.service.callbacks.BasicCallbackHandler
import com.coinninja.coinkeeper.service.callbacks.Bip70Callback
import com.coinninja.coinkeeper.service.client.Bip70Client
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult
import com.coinninja.coinkeeper.service.client.model.MerchantResponse
import com.coinninja.coinkeeper.ui.account.verify.UserAccountVerificationActivity
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment
import com.coinninja.coinkeeper.ui.payment.PaymentInputView
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.util.PaymentUtil
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.ClipboardUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import com.coinninja.coinkeeper.util.crypto.uri.UriException
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.Currency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.view.activity.PickUserActivity
import com.coinninja.coinkeeper.view.activity.QrScanActivity
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.coinkeeper.view.subviews.SharedMemoToggleView
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder
import com.coinninja.coinkeeper.view.widget.PaymentReceiverView
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator
import com.google.i18n.phonenumbers.Phonenumber
import javax.inject.Inject
import kotlin.math.ceil

@Mockable
class PayDialogFragment : BaseBottomDialogFragment() {

    @Inject
    internal lateinit var feesManager: FeesManager
    @Inject
    internal lateinit var cnAddressLookupDelegate: CNAddressLookupDelegate
    @Inject
    internal lateinit var analytics: Analytics
    @Inject
    internal lateinit var dropbitAccountHelper: DropbitAccountHelper
    @Inject
    internal lateinit var bitcoinUtil: BitcoinUtil
    @Inject
    internal lateinit var clipboardUtil: ClipboardUtil
    @Inject
    internal lateinit var userPreferences: UserPreferences
    @Inject
    internal lateinit var application: CoinKeeperApplication
    @Inject
    internal lateinit var memoToggleView: SharedMemoToggleView
    @Inject
    internal lateinit var countryCodeLocaleGenerator: CountryCodeLocaleGenerator
    internal lateinit var countryCodeLocales: List<CountryCodeLocale>
    @Inject
    internal lateinit var bip70Client: Bip70Client

    internal lateinit var paymentBarCallbacks: PaymentBarCallbacks
    internal lateinit var paymentUtil: PaymentUtil
    internal lateinit var paymentHolder: PaymentHolder

    internal val paymentReceiverView: PaymentReceiverView? get() = findViewById(R.id.payment_receiver)
    internal val paymentInputView: PaymentInputView? get() = findViewById(R.id.payment_input_view)

    internal val bip70Callback = Bip70Callback(object : BasicCallbackHandler<MerchantResponse> {

        override fun success(`object`: MerchantResponse) {
            commonCompletion()
            setBip70UriParameters(`object`)
        }

        override fun failure(message: String) {
            commonCompletion()
            showPasteAttemptFail(getString(R.string.invalid_bitcoin_address_error))
        }

        private fun commonCompletion() {
            hideIndeterminantProgress()
        }
    })

    internal var shouldShowScanOnAttach = false
    var bitcoinUri: BitcoinUri? = null
    var initialized = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            onPickUserResult(data)
        } else if (requestCode == DropbitIntents.REQUEST_QR_FRAGMENT_SCAN && resultCode == Activity.RESULT_CANCELED) {
            onCloseClicked()
        } else if (requestCode == DropbitIntents.REQUEST_QR_FRAGMENT_SCAN) {
            onQrScanResult(resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        paymentInputView?.requestFocus()
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.fragment_pay_dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        USDCurrency.setMaxLimit(paymentHolder.evaluationCurrency as USDCurrency)
        processBitcoinUriIfNecessary(bitcoinUri)
        paymentHolder.setMaxLimitForFiat()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        analytics.trackEvent(Analytics.EVENT_PAY_SCREEN_LOADED)
        analytics.flush()
        countryCodeLocales = countryCodeLocaleGenerator.generate()

        if (shouldShowScanOnAttach) {
            startScan()
            shouldShowScanOnAttach = false
        }
    }

    override fun onDetach() {
        super.onDetach()
        memoToggleView.tearDown()
        paymentUtil.reset()
        cnAddressLookupDelegate.teardown()
    }

    override fun onStart() {
        super.onStart()
        if (!initialized) {
            memoToggleView.render(activity as AppCompatActivity?, view)
            setupView()
            initialized = true
        }
    }

    fun onPaymentAddressChange(address: String) {
        paymentHolder.publicKey = ""
        paymentUtil.setAddress(address)
        paymentReceiverView?.paymentAddress = address
        showSendToInput()
        updateSharedMemosUI()
        paymentInputView?.requestFocus()
    }

    fun showPasteAttemptFail(message: String) {
        paymentUtil.setAddress("")
        paymentHolder.paymentAddress = ""
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun onPhoneNumberValid(phoneNumber: Phonenumber.PhoneNumber) {
        cnAddressLookupDelegate.fetchAddressFor(PhoneNumber(phoneNumber)) { this.onFetchContactComplete(it) }
        paymentUtil.setIdentity(Identity(Contact(PhoneNumber(phoneNumber), "", false)))
        updateSharedMemosUI()
    }

    fun onPhoneNumberInvalid(text: String) {
        paymentReceiverView?.clear()
        paymentUtil.setIdentity(null)
        shakeInError(paymentReceiverView)
    }

    fun onFetchContactComplete(addressLookupResult: AddressLookupResult) {
        paymentHolder.publicKey = addressLookupResult.addressPubKey
        paymentHolder.paymentAddress = if (addressLookupResult.address == null) "" else addressLookupResult.address
        updateSharedMemosUI()
    }

    internal fun startContactInviteFlow(identity: Identity?) {
        if (userPreferences.shouldShowInviteHelp) {
            showInviteHelpScreen(identity)
        } else {
            inviteUnverifiedIdentity(identity)
        }
    }

    internal fun onInviteHelpAccepted(identity: Identity?) {
        fragmentManager?.findFragmentByTag(InviteHelpDialogFragment.TAG)?.let {
            val dialog: DialogFragment = it as DialogFragment
            dialog.dismiss()
        }
        inviteUnverifiedIdentity(identity)
    }

    internal fun inviteUnverifiedIdentity(identity: Identity?) {
        paymentBarCallbacks.confirmInvite(paymentUtil, identity)
    }

    internal fun sendPaymentTo(identity: Identity?) {
        paymentBarCallbacks.confirmPaymentFor(paymentUtil, identity)
    }

    internal fun sendPayment() {
        setMemoOnPayment()
        when (paymentUtil.paymentMethod) {
            PaymentUtil.PaymentMethod.ADDRESS -> paymentBarCallbacks.confirmPaymentFor(paymentUtil)
            PaymentUtil.PaymentMethod.INVITE, PaymentUtil.PaymentMethod.VERIFIED_CONTACT -> sendPaymentToContact()
            else -> {
            }
        }

    }

    internal fun onPasteClicked() {
        val cryptoUriString = clipboardUtil.raw
        if (cryptoUriString == null || cryptoUriString.isEmpty()) {
            val reason = getString(R.string.clipboard_empty_error_message)
            showPasteAttemptFail(reason)
        } else {
            onCryptoStringReceived(cryptoUriString)
        }
    }

    private fun onPickUserResult(data: Intent?) {
        data?.let { intent ->
            if (intent.hasExtra(DropbitIntents.EXTRA_IDENTITY)) {
                intent.getParcelableExtra<Identity>(DropbitIntents.EXTRA_IDENTITY)?.let {
                    setIdentityResult(it)
                }
            }
        }
    }

    private fun showError(message: String) = activity?.supportFragmentManager?.let {
        GenericAlertDialog.newInstance(message).show(it, "INVALID_PAYMENT")
    }

    private fun setupView() {
        configureButtons()
        configureSharedMemo()
        configurePaymentReceiver()
        configurePaymentInput()
    }

    private fun configurePaymentInput() = paymentInputView?.apply {
        setPaymentHolder(paymentHolder)
        setOnSendMaxObserver { this@PayDialogFragment.onSendMaxObserved() }
        setOnSendMaxClearedObserver { this@PayDialogFragment.onSendMaxClearedObserved() }
    }

    private fun onSendMaxClearedObserved() {
        if (paymentUtil.isSendingMax) {
            paymentUtil.clearFunding()
        }
    }

    private fun onSendMaxObserved() {
        if (paymentUtil.fundMax()) {
            paymentHolder.updateValue(BTCCurrency(paymentHolder.transactionData.amount))
            paymentInputView?.setPaymentHolder(paymentHolder)
        }
    }


    private fun configureButtons() {
        findViewById<View>(R.id.pay_footer_send_btn)?.setOnClickListener { onSendButtonClicked() }
        findViewById<View>(R.id.twitter_contacts_button)?.setOnClickListener { onTwitterClicked() }
        findViewById<View>(R.id.pay_header_close_btn)?.setOnClickListener { onCloseClicked() }
        findViewById<View>(R.id.paste_address_btn)?.setOnClickListener { onPasteClicked() }
        findViewById<View>(R.id.contacts_btn)?.setOnClickListener { onContactsClicked() }
    }

    private fun configureSharedMemo() {
        if (!dropbitAccountHelper.hasVerifiedAccount) {
            paymentHolder.isSharingMemo = false
        }
    }

    private fun configurePaymentReceiver() {
        val initialSendTo = if (paymentUtil.getAddress() == null) "" else paymentUtil.getAddress()
        paymentReceiverView?.apply {
            setOnValidPhoneNumberObserver { this@PayDialogFragment.onPhoneNumberValid(it) }
            setOnInvalidPhoneNumberObserver { this@PayDialogFragment.onPhoneNumberInvalid(it) }
            countryCodeLocales = this@PayDialogFragment.countryCodeLocales
            paymentAddress = initialSendTo
            setOnScanObserver { this@PayDialogFragment.startScan() }

        }
    }

    private fun onPaymentChange(currency: Currency) {
        paymentHolder.updateValue(currency)
        paymentInputView?.setPaymentHolder(paymentHolder)
    }

    private fun updateSharedMemosUI() {
        if (paymentHolder.hasPubKey() || paymentUtil.paymentMethod === PaymentUtil.PaymentMethod.INVITE) {
            memoToggleView.showSharedMemoViews()
        } else {
            memoToggleView.hideSharedMemoViews()
        }
    }

    private fun onSendButtonClicked() {
        if (paymentUtil.isValid && paymentUtil.checkFunding()) {
            sendPayment()
        } else {
            invalidPayment()
        }
    }

    private fun invalidPayment() {
        var errorMessage = paymentUtil.errorMessage
        if (null == errorMessage || "" == errorMessage)
            errorMessage = getString(R.string.pay_error_add_valid_bitcoin_address)
        showError(errorMessage)
        paymentUtil.clearErrors()
    }

    private fun onContactsClicked() {
        if (dropbitAccountHelper.hasVerifiedAccount) {
            startPickContactActivity(DropbitIntents.ACTION_CONTACTS_SELECTION)
        } else {
            showNonVerifiedAccountAlert()
        }
    }

    private fun onTwitterClicked() {
        if (dropbitAccountHelper.isTwitterVerified) {
            startPickContactActivity(DropbitIntents.ACTION_TWITTER_SELECTION)
        } else {
            showNonTwitterVerifiedAccountAlert()
        }
    }

    private fun showNonVerifiedAccountAlert() {
        val alertDialog = AlertDialogBuilder.build(view?.context, "You do not have a verified account, please verify.")
        setupActionsForVerificationAlertBuilder(alertDialog)
    }

    private fun showNonTwitterVerifiedAccountAlert() {
        val alertDialog = AlertDialogBuilder.build(view?.context, "Your twitter account is not verified, please verify.")
        setupActionsForVerificationAlertBuilder(alertDialog)
    }

    private fun setupActionsForVerificationAlertBuilder(builder: AlertDialog.Builder) {
        builder.setNegativeButton("Not now") { dialog, which -> }
        builder.setPositiveButton("Verify") { dialog, which -> showUserVerificationActivity() }.show()
    }

    private fun showUserVerificationActivity() {
        val verificationActivity = Intent(activity, UserAccountVerificationActivity::class.java)
        startActivity(verificationActivity)
    }

    private fun startPickContactActivity(action: String) {
        val contactIntent = Intent(activity, PickUserActivity::class.java)
        contactIntent.action = action
        startActivityForResult(contactIntent, PICK_CONTACT_REQUEST)
    }

    private fun startScan() {
        val qrScanIntent = Intent(activity, QrScanActivity::class.java)
        startActivityForResult(qrScanIntent, DropbitIntents.REQUEST_QR_FRAGMENT_SCAN)
    }

    private fun setMemoOnPayment() {
        paymentHolder.memo = memoToggleView.memo
        paymentHolder.isSharingMemo = memoToggleView.isSharing
    }

    private fun sendPaymentToContact() {
        if (dropbitAccountHelper.hasVerifiedAccount) {
            when {
                paymentHolder.hasPaymentAddress() -> sendPaymentTo(paymentUtil.getIdentity())
                paymentUtil.isVerifiedContact -> inviteUnverifiedIdentity(paymentUtil.getIdentity())
                else -> startContactInviteFlow(paymentUtil.getIdentity())
            }
        } else {
            activity?.startActivity(Intent(activity, VerificationActivity::class.java))
        }
    }

    private fun showInviteHelpScreen(identity: Identity?) {
        val dialogFragment = InviteHelpDialogFragment.newInstance(userPreferences, identity) { onInviteHelpAccepted(identity) }

        fragmentManager?.beginTransaction()?.let {
            val prev = fragmentManager?.findFragmentByTag(InviteHelpDialogFragment.TAG)
            if (prev != null) {
                it.remove(prev)
            }
            it.addToBackStack(null)
            dialogFragment.show(it, InviteHelpDialogFragment.TAG)
        }
    }

    private fun onCloseClicked() {
        paymentHolder.memo = null
        paymentHolder.clearPayment()
        paymentBarCallbacks.cancelPayment(this)
    }

    private fun setIdentityResult(identity: Identity?) {
        paymentUtil.setIdentity(identity)
        hideSendToInput()
        identity?.let { identityUnwrapped ->
            findViewById<TextView>(R.id.contact_name)?.apply {
                text = identity.displayName
            }
            findViewById<TextView>(R.id.contact_number)?.apply {
                text = identity.secondaryDisplayName
            }

            cnAddressLookupDelegate.fetchAddressFor(identityUnwrapped) { this.onFetchContactComplete(it) }
        }
    }

    private fun hideSendToInput() {
        paymentReceiverView?.apply {
            visibility = View.GONE
            clear()
        }
        findViewById<View>(R.id.contact_name)?.visibility = View.VISIBLE
        findViewById<View>(R.id.contact_number)?.visibility = View.VISIBLE
    }

    private fun showSendToInput() {
        paymentReceiverView?.visibility = View.VISIBLE
        findViewById<View>(R.id.contact_name)?.visibility = View.GONE
        findViewById<View>(R.id.contact_number)?.visibility = View.GONE
    }

    private fun onQrScanResult(resultCode: Int, data: Intent?) {
        data?.let {
            if (resultCode == DropbitIntents.RESULT_SCAN_OK) {
                onCryptoStringReceived(it.getStringExtra(DropbitIntents.EXTRA_SCANNED_DATA))
            }
        }
    }

    private fun onCryptoStringReceived(cryptoUriString: String) {
        resetPaymentHolderIfNecessary()
        try {
            val bitcoinUri = bitcoinUtil.parse(cryptoUriString)
            processBitcoinUriIfNecessary(bitcoinUri)
        } catch (e: UriException) {
            onInvalidBitcoinUri(e.reason)
        }

    }

    private fun processBitcoinUriIfNecessary(bitcoinUri: BitcoinUri?) {
        if (bitcoinUri == null) {
            return
        }
        onReceiveBitcoinUri(bitcoinUri)
        checkForBip70Url(bitcoinUri)
    }

    private fun resetPaymentHolderIfNecessary() {
        paymentUtil.setFee(feesManager.currentFee())
        paymentHolder.memo = null
    }

    private fun checkForBip70Url(bitcoinUri: BitcoinUri?) {
        if (bitcoinUri == null) {
            return
        }
        val bip70Uri = bitcoinUri.bip70UrlIfApplicable ?: return

        showIndeterminantProgress()
        bip70Client.getMerchantInformation(bip70Uri, bip70Callback)
    }

    private fun setBip70UriParameters(merchantResponse: MerchantResponse) {
        if (merchantResponse.memo != null) {
            memoToggleView.setText(merchantResponse.memo)
            paymentHolder.memo = merchantResponse.memo
        }

        if (merchantResponse.outputs != null && merchantResponse.outputs.size > 0) {
            val output = merchantResponse.outputs[0]

            onPaymentAddressChange(output.address)
            setAmount(output.amount)
        }

        if (merchantResponse.requiredFeeRate != 0.0) {
            val roundedUpFeeRate = ceil(merchantResponse.requiredFeeRate)
            paymentUtil.setFee(roundedUpFeeRate)
        }
    }

    private fun setAmount(satoshiAmount: Long?) {
        if (satoshiAmount != null && satoshiAmount > 0) {
            onPaymentChange(BTCCurrency(satoshiAmount))
        }
    }

    private fun onReceiveBitcoinUri(bitcoinUri: BitcoinUri?) {
        if (bitcoinUri == null || bitcoinUri.address == null) {
            return
        }
        onPaymentAddressChange(bitcoinUri.address)
        setAmount(bitcoinUri.satoshiAmount)
    }

    private fun onInvalidBitcoinUri(pasteError: BitcoinUtil.ADDRESS_INVALID_REASON) {
        when (pasteError) {
            BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS -> showPasteAttemptFail(getString(R.string.invalid_bitcoin_address_error))
            BitcoinUtil.ADDRESS_INVALID_REASON.IS_BC1 -> showPasteAttemptFail(getString(R.string.bc1_error_message))
            BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58 -> showPasteAttemptFail(getString(R.string.invalid_btc_adddress__base58))
            BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN -> showPasteAttemptFail(getString(R.string.invalid_bitcoin_address_error))
        }
    }

    companion object {
        internal const val PICK_CONTACT_REQUEST = 1001

        fun newInstance(paymentUtil: PaymentUtil, paymentBarCallbacks: PaymentBarCallbacks, shouldShowScanOnAttach: Boolean): PayDialogFragment {
            val payFragment = commonInit(paymentUtil, paymentBarCallbacks)
            payFragment.shouldShowScanOnAttach = shouldShowScanOnAttach
            return payFragment
        }

        fun newInstance(paymentUtil: PaymentUtil, paymentBarCallbacks: PaymentBarCallbacks, bitcoinUri: BitcoinUri): PayDialogFragment {
            val payFragment = commonInit(paymentUtil, paymentBarCallbacks)
            payFragment.bitcoinUri = bitcoinUri
            return payFragment
        }

        private fun commonInit(paymentUtil: PaymentUtil, paymentBarCallbacks: PaymentBarCallbacks): PayDialogFragment {
            val payFragment = PayDialogFragment()
            payFragment.paymentBarCallbacks = paymentBarCallbacks
            payFragment.paymentUtil = paymentUtil
            payFragment.paymentHolder = paymentUtil.paymentHolder ?: PaymentHolder()
            paymentUtil.paymentHolder = payFragment.paymentHolder
            return payFragment
        }
    }
}
