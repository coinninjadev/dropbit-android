package com.coinninja.coinkeeper.ui.payment.create

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.thunderdome.model.LedgerInvoice
import app.coinninja.cn.thunderdome.model.RequestInvoice
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.CryptoCurrency
import app.dropbit.commons.currency.FiatCurrency
import app.dropbit.commons.currency.USDCurrency
import app.dropbit.commons.util.isNotNull
import app.dropbit.commons.util.isNotNullOrEmpty
import com.coinninja.android.helpers.*
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.bitcoin.isNotFunded
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.interactor.UserPreferences
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.payment.PaymentInputView
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.view.activity.QrScanActivity
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.coinkeeper.view.fragment.InviteHelpDialogFragment
import com.coinninja.coinkeeper.view.subviews.SharedMemoToggleView
import com.coinninja.coinkeeper.view.widget.AccountModeToggleButton
import com.coinninja.coinkeeper.view.widget.PaymentReceiverView
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator
import com.google.i18n.phonenumbers.Phonenumber
import javax.inject.Inject


class CreatePaymentActivity : BaseActivity() {


    @Inject
    internal lateinit var countryCodeLocaleGenerator: CountryCodeLocaleGenerator
    @Inject
    internal lateinit var rawInputViewModel: RawInputViewModel
    @Inject
    internal lateinit var memoToggleView: SharedMemoToggleView
    @Inject
    internal lateinit var dropbitAccountHelper: DropbitAccountHelper
    @Inject
    lateinit var fundingViewModelProvider: FundingViewModelProvider
    @Inject
    internal lateinit var userPreferences: UserPreferences

    lateinit var fundingViewModel: FundingViewModel

    var isReadyToProcess = false
    var isSendingMax = false
    var toUser: Identity? = null
    val paymentHolder = PaymentHolder()
    var bitcoinUri: BitcoinUri? = null
        set(value) {
            field = value
            field?.let {
                memoToggleView.setText(it.memo)
                paymentReceiverView.paymentAddress = it.address
                paymentHolder.paymentAddress = it.address
                if (it.satoshiAmount > 0) {
                    paymentHolder.updateValue(BTCCurrency(it.satoshiAmount))
                    amountInputView.paymentHolder = paymentHolder
                }
            }
        }

    val closeButton: View get() = findViewById(R.id.close)
    val accountModeToggle: AccountModeToggleButton get() = findViewById(R.id.account_mode_toggle)
    val amountInputView: PaymentInputView get() = findViewById(R.id.payment_input_view)
    val twitterButton: Button get() = findViewById(R.id.twitter_contacts_button)
    val phoneButton: Button get() = findViewById(R.id.contacts_btn)
    val pasteButton: Button get() = findViewById(R.id.paste_address_btn)
    val nextButton: Button get() = findViewById(R.id.next_button)
    val paymentReceiverView: PaymentReceiverView get() = findViewById(R.id.payment_receiver)
    val contactName: TextView get() = findViewById(R.id.contact_name)
    val contactNumber: TextView get() = findViewById(R.id.contact_number)

    var holdingsWorth: FiatCurrency = USDCurrency(0)
    var holdings: CryptoCurrency = BTCCurrency(0)

    val validRawInputObserver: Observer<BitcoinUri> = Observer {
        clearPaymentInput()
        changeAccountMode(AccountMode.BLOCKCHAIN)
        bitcoinUri = it
        memoToggleView.hideSharedMemoViews()
        contactName.hide()
        contactNumber.hide()
        paymentReceiverView.show()
    }

    val validRequestInvoiceObserver: Observer<RequestInvoice> = Observer {
        clearPaymentInput()
        changeAccountMode(AccountMode.LIGHTNING)
        contactName.hide()
        contactNumber.hide()
        paymentReceiverView.show()
        paymentHolder.requestInvoice = it
        paymentReceiverView.paymentAddress = it.encoded

        if (it.description.isNotEmpty())
            memoToggleView.setText(it.description)

        if (it.numSatoshis > 0) {
            paymentHolder.updateValue(BTCCurrency(it.numSatoshis))
            paymentHolder.updateValue(paymentHolder.fiat)
            amountInputView.paymentHolder = paymentHolder
            amountInputView.primaryCurrency.disable()
        }
    }

    val invalidRawInputObserver: Observer<String> = Observer {
        clearPaymentInput()
        amountInputView.primaryCurrency.enable()
        GenericAlertDialog.newInstance(
                getString(R.string.invalid_bitcoin_address_error)
        ).show(supportFragmentManager, errorDialogTag)
    }

    val accountLookupResultObserver: Observer<AddressLookupResult> = Observer {
        if (it.isBlockChain()) {
            paymentHolder.paymentAddress = it.address
            paymentHolder.publicKey = it.addressPubKey
        } else if (it.isLightning()) {
            paymentHolder.publicKey = it.addressPubKey
            paymentHolder.requestInvoice = RequestInvoice().also { request ->
                request.encoded = it.address
            }
        }
    }

    val pendingLedgerInvoiceObserver: Observer<LedgerInvoice> = Observer {
        if (it.value == 0L && isReadyToProcess) {
            showInsufficientFundsMessage()
            isReadyToProcess = false
        } else {
            confirmPayment()
        }

    }

    val accountModeToggleObserver = object : AccountModeToggleButton.AccountModeSelectedObserver {
        override fun onSelectionChange(mode: AccountMode) {
            changeAccountMode(mode)
        }
    }

    val onSendMaxObserved: PaymentInputView.OnSendMaxObserver = object : PaymentInputView.OnSendMaxObserver {
        override fun onSendMax() {
            isSendingMax = true
            paymentHolder.clearTransactionData()
            fundingViewModel.fundMax(if (paymentHolder.paymentAddress.isEmpty()) null else paymentHolder.paymentAddress)
        }
    }

    val transactionDataObserver: Observer<TransactionData> = Observer {
        paymentHolder.transactionData = it
        if (isSendingMax) {
            paymentHolder.updateValue(BTCCurrency(it.amount))
            amountInputView.paymentHolder = paymentHolder
        }

        if (isReadyToProcess)
            confirmPayment()
    }

    val onSendMaxClearedObserver = object : PaymentInputView.OnSendMaxClearedObserver {
        override fun onSendMaxCleared() {
            isSendingMax = false
            paymentHolder.clearTransactionData()
        }
    }

    val onValidPaymentInputObserver = object : PaymentInputView.OnValidEntryObserver {
        override fun onValidEntry() {
            isSendingMax = false
            paymentHolder.clearTransactionData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_payment)
        closeButton.setOnClickListener { onBackPressed() }
        fundingViewModel = fundingViewModelProvider.provide(this)
        paymentReceiverView.apply {
            countryCodeLocales = countryCodeLocaleGenerator.generate()
            setOnScanObserver { initScanner() }
            setOnInvalidPhoneNumberObserver { }
            setOnValidPhoneNumberObserver {
                onValidPhoneNumberInput(it)
            }
        }
        amountInputView.apply {
            setOnSendMaxObserver(onSendMaxObserved)
            setOnSendMaxClearedObserver(onSendMaxClearedObserver)
            onValidEntryObserver = onValidPaymentInputObserver
        }
        pasteButton.setOnClickListener { rawInputViewModel.inputFromPaste() }
        twitterButton.setOnClickListener { onTwitterPressed() }
        phoneButton.setOnClickListener { onPhoneContactPressed() }
        nextButton.setOnClickListener { onNextPressed() }
        if (intent.hasExtra(DropbitIntents.EXTRA_SHOULD_SCAN)) {
            initScanner()
        } else if (intent.hasExtra(DropbitIntents.EXTRA_BITCOIN_URI)) {
            rawInputViewModel.processCryptoUriInput(intent.getStringExtra(DropbitIntents.EXTRA_BITCOIN_URI))
            intent.removeExtra(DropbitIntents.EXTRA_BITCOIN_URI)
        }

        when (accountModeManager.accountMode) {
            AccountMode.BLOCKCHAIN -> showBlockChain()
            AccountMode.LIGHTNING -> showLightning()
        }
    }


    override fun onResume() {
        super.onResume()
        accountModeToggle.onModeSelectedObserver = accountModeToggleObserver
        observeLiveData()
        memoToggleView.render(this, findViewById<View>(R.id.transaction_memo))

    }


    override fun onPause() {
        super.onPause()
        accountModeToggle.onModeSelectedObserver = accountModeToggleObserver
        removeObservers()
    }

    override fun onStop() {
        super.onStop()
        memoToggleView.tearDown()
    }

    override fun onLightningLockedChanged(isLightningLocked: Boolean) {
        super.onLightningLockedChanged(isLightningLocked)
        accountModeToggle.isLightningLocked = isLightningLocked
        if (isLightningLocked) {
            accountModeToggle.onModeSelectedObserver = null
            accountModeToggle.gone()
        }
    }

    override fun onLatestPriceChanged(currentPrice: FiatCurrency) {
        super.onLatestPriceChanged(currentPrice)
        if (paymentHolder.evaluationCurrency.toLong() == 0L) {
            paymentHolder.evaluationCurrency = currentPrice
            amountInputView.paymentHolder = paymentHolder
        }
    }

    override fun onHoldingsChanged(balance: CryptoCurrency) {
        super.onHoldingsChanged(balance)
        holdings = balance
    }

    override fun onHoldingsWorthChanged(value: FiatCurrency) {
        super.onHoldingsWorthChanged(value)
        holdingsWorth = value
    }

    override fun onAccountModeChanged(mode: AccountMode) {
        super.onAccountModeChanged(mode)
        clearPaymentInput()
        amountInputView.accountMode = mode
        when (mode) {
            AccountMode.BLOCKCHAIN -> showBlockChain()
            AccountMode.LIGHTNING -> showLightning()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        memoToggleView.render(this, findViewById<View>(R.id.transaction_memo))
        clearPaymentInput()
        when {
            (requestCode == DropbitIntents.REQUEST_QR_SCAN &&
                    resultCode == DropbitIntents.RESULT_SCAN_OK) -> {
                rawInputViewModel.onQrScanResult(data)
            }
            (requestCode == DropbitIntents.REQUEST_QR_SCAN &&
                    resultCode == QrScanActivity.RESULT_CANCELED) -> {
                // intentional blank
            }
            (requestCode == DropbitIntents.REQUEST_PICK_CONTACT &&
                    resultCode == Activity.RESULT_OK) -> {
                onContactSelected(data)
            }
            (requestCode == DropbitIntents.REQUEST_PICK_CONTACT &&
                    resultCode == Activity.RESULT_CANCELED) -> {
                // intentional blank
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun clearPaymentInput() {
        removeObservers()
        rawInputViewModel.clear()
        fundingViewModel.clear()
        observeLiveData()
        paymentHolder.paymentAddress = ""
        paymentHolder.clearTransactionData()
        paymentReceiverView.paymentAddress = ""
    }

    private fun observeLiveData() {
        rawInputViewModel.invalidRawInput.observe(this, invalidRawInputObserver)
        rawInputViewModel.validRawInput.observe(this, validRawInputObserver)
        rawInputViewModel.validRequestInvoice.observe(this, validRequestInvoiceObserver)
        fundingViewModel.addressLookupResult.observe(this, accountLookupResultObserver)
        fundingViewModel.transactionData.observe(this, transactionDataObserver)
        fundingViewModel.pendingLedgerInvoice.observe(this, pendingLedgerInvoiceObserver)
    }

    private fun removeObservers() {
        rawInputViewModel.validRawInput.removeObserver(validRawInputObserver)
        rawInputViewModel.validRequestInvoice.removeObserver(validRequestInvoiceObserver)
        rawInputViewModel.invalidRawInput.removeObserver(invalidRawInputObserver)
        fundingViewModel.addressLookupResult.removeObserver(accountLookupResultObserver)
        fundingViewModel.transactionData.removeObserver(transactionDataObserver)
        fundingViewModel.pendingLedgerInvoice.removeObserver(pendingLedgerInvoiceObserver)
    }

    internal fun onValidPhoneNumberInput(it: Phonenumber.PhoneNumber?) {
        val value = PhoneNumber(it)
        clearPaymentInput()
        toUser = Identity(IdentityType.PHONE, value.toInternationalDisplayText(), value.toHash())
        toUser?.let {
            fundingViewModel.lookupIdentityHash(it.hashForType, accountModeToggle.mode)
        }
        memoToggleView.showSharedMemoViews()
    }

    private fun initScanner() {
        intent.removeExtra(DropbitIntents.EXTRA_SHOULD_SCAN)
        Intent(this, QrScanActivity::class.java).also {
            startActivityForResult(it, DropbitIntents.REQUEST_QR_SCAN)
        }
    }

    private fun onNextPressed() {
        if (paymentHolder.primaryCurrency.isZero()) {
            showEnterValidAmount()
            return
        }

        if (paymentHolder.paymentAddress.isEmpty() && toUser == null) {
            showInvalidPaymentTarget()
            return
        }

        isReadyToProcess = true

        paymentHolder.memo = memoToggleView.memo
        paymentHolder.isSharingMemo = memoToggleView.isSharing
                && toUser.isNotNull()
                && paymentHolder.memo.isNotEmpty()

        if (isSendingMax) {
            fundingViewModel.fundMax(paymentHolder.paymentAddress)
        } else if (paymentHolder.hasPaymentAddress()) {
            fundingViewModel.fundTransaction(paymentHolder.paymentAddress, paymentHolder.cryptoCurrency.toLong())
        } else if (toUser != null && paymentHolder.requestInvoice.isNotNull() && paymentHolder.requestInvoice?.encoded.isNotNull()) {
            paymentHolder.requestInvoice?.encoded?.let { fundingViewModel.estimateLightningPayment(it, paymentHolder.cryptoCurrency.toLong()) }
        } else if (toUser != null && accountModeToggle.mode == AccountMode.LIGHTNING) {
            if (paymentHolder.cryptoCurrency.toLong() > holdings.toLong()) {
                showInsufficientFundsMessage()
            } else {
                toUser?.let { startContactInviteFlow(it) }
            }

        } else if (toUser != null) {
            fundingViewModel.fundTransactionForDropbit(paymentHolder.cryptoCurrency.toLong())
        }


    }

    private fun confirmPayment() {
        isReadyToProcess = false

        if (paymentHolder.transactionData.isNotFunded()) {
            showInsufficientFundsMessage()
        }

        if (paymentHolder.paymentAddress.isEmpty()
                && paymentHolder.requestInvoice == null && paymentHolder.requestInvoice?.encoded == null) {
            if (toUser == null) {
                showInvalidPaymentTarget()
            } else {
                toUser?.let { startContactInviteFlow(it) }
            }
        } else {
            activityNavigationUtil.navigateToConfirmPaymentScreen(this, paymentHolder)
        }
    }

    private fun showEnterValidAmount() {
        GenericAlertDialog.newInstance(
                getString(R.string.pay_error_invalid_amount)
        ).show(supportFragmentManager, errorDialogTag)
    }

    private fun showInvalidPaymentTarget() {
        GenericAlertDialog.newInstance(
                getString(R.string.pay_error_add_valid_bitcoin_address)
        ).show(supportFragmentManager, errorDialogTag)
    }

    private fun showInsufficientFundsMessage() {
        GenericAlertDialog.newInstance(getString(R.string.pay_error_insufficient_funds,
                amountInputView.paymentHolder.fiat.toFormattedCurrency(),
                holdingsWorth.toFormattedCurrency()
        )).show(supportFragmentManager, errorDialogTag)
    }

    private fun showBlockChain() {
        nextButton.styleAsBitcoin()
        twitterButton.styleAsBitcoin()
        phoneButton.styleAsBitcoin()
        pasteButton.styleAsBitcoin()
        amountInputView.primaryCurrency.enable()
        if (accountModeToggle.mode != AccountMode.BLOCKCHAIN) {
            paymentHolder.clearPayment()
            amountInputView.showKeyboard()
        }
        accountModeToggle.mode = AccountMode.BLOCKCHAIN
        amountInputView.canToggleCurrencies = true
        amountInputView.canSendMax = true
        amountInputView.paymentHolder = paymentHolder
    }

    private fun showLightning() {
        nextButton.styleAsLightning()
        twitterButton.styleAsLightning()
        phoneButton.styleAsLightning()
        pasteButton.styleAsLightning()
        if (accountModeToggle.mode != AccountMode.LIGHTNING) {
            paymentHolder.clearPayment()
            paymentHolder.updateValue(USDCurrency())
            amountInputView.showKeyboard()
        }
        accountModeToggle.mode = AccountMode.LIGHTNING
        amountInputView.canToggleCurrencies = false
        amountInputView.canSendMax = false
        isSendingMax = false
        amountInputView.paymentHolder = paymentHolder
    }

    private fun onTwitterPressed() {
        if (dropbitAccountHelper.isTwitterVerified) {
            activityNavigationUtil.startPickContactActivity(this, DropbitIntents.ACTION_TWITTER_SELECTION)
        } else {
            showVerificationMessage(getString(R.string.your_twitter_account_is_not_verified))

        }
    }

    private fun onPhoneContactPressed() {
        if (dropbitAccountHelper.isPhoneVerified) {
            activityNavigationUtil.startPickContactActivity(this, DropbitIntents.ACTION_CONTACTS_SELECTION)
        } else {
            showVerificationMessage(getString(R.string.your_phone_is_not_verified))
        }
    }

    private fun onContactSelected(data: Intent?) {
        contactName.hide()
        contactNumber.hide()
        paymentReceiverView.paymentAddress = ""
        paymentHolder.paymentAddress = ""
        paymentReceiverView.hide()
        memoToggleView.showSharedMemoViews()

        if (data?.hasExtra(DropbitIntents.EXTRA_IDENTITY) == true) {
            toUser = data.getParcelableExtra(DropbitIntents.EXTRA_IDENTITY)
            toUser?.let { identity ->
                identity.hash?.let { fundingViewModel.lookupIdentityHash(it, accountModeToggle.mode) }
                identity.displayName?.let {
                    if (it.isNotNullOrEmpty()) {
                        contactName.apply {
                            text = it
                            show()
                        }

                    }
                }
                identity.secondaryDisplayName.let {
                    contactNumber.apply {
                        text = it
                        show()
                    }
                }
            }
        }
    }

    private fun showVerificationMessage(message: String) {
        GenericAlertDialog.newInstance(
                message,
                getString(R.string.verify_now),
                getString(R.string.not_now)
        ) { dialog, which ->
            dialog.dismiss()
            if (which == DialogInterface.BUTTON_POSITIVE) {
                activityNavigationUtil.navigateToUserVerification(this@CreatePaymentActivity)
            }
        }.show(supportFragmentManager, errorDialogTag)
    }

    private fun startContactInviteFlow(identity: Identity) {
        if (userPreferences.shouldShowInviteHelp) {
            showInviteHelpScreen(identity)
        } else {
            inviteUnverifiedIdentity()
        }
    }

    private fun showInviteHelpScreen(identity: Identity) {
        val dialogFragment = InviteHelpDialogFragment.newInstance(userPreferences, identity) { onInviteHelpAccepted() }

        supportFragmentManager.beginTransaction().let { fragmentTransaction ->
            val prev = supportFragmentManager.findFragmentByTag(InviteHelpDialogFragment.TAG)
            prev?.let {
                fragmentTransaction.remove(prev)
            }
            fragmentTransaction.addToBackStack(InviteHelpDialogFragment.TAG)
            dialogFragment.show(fragmentTransaction, InviteHelpDialogFragment.TAG)
        }
    }


    private fun onInviteHelpAccepted() {
        supportFragmentManager.findFragmentByTag(InviteHelpDialogFragment.TAG)?.let {
            val dialog: DialogFragment = it as DialogFragment
            dialog.dismiss()
        }
        inviteUnverifiedIdentity()
    }

    private fun inviteUnverifiedIdentity() {
        activityNavigationUtil.navigateToConfirmPaymentScreen(this, paymentHolder)
    }

    companion object {
        const val errorDialogTag: String = "ERROR_DIALOG"
    }
}