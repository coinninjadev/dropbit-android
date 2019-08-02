package com.coinninja.coinkeeper.view.fragment

import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.android.helpers.Views.clickOn
import com.coinninja.android.helpers.Views.withId
import com.coinninja.bindings.TransactionData
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.interactor.UserPreferences
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult
import com.coinninja.coinkeeper.service.client.model.TransactionFee
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.util.*
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.ClipboardUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import com.coinninja.coinkeeper.util.crypto.uri.UriException
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.CryptoCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.view.activity.PickUserActivity
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.coinkeeper.view.widget.PaymentReceiverView
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView
import com.coinninja.coinkeeper.wallet.data.TestData
import com.coinninja.matchers.TextViewMatcher.hasText
import com.coinninja.matchers.ViewMatcher.isGone
import com.coinninja.matchers.ViewMatcher.isVisible
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowToast
import java.util.*


@RunWith(AndroidJUnit4::class)
class PayDialogFragmentTest {
    private lateinit var shadowActivity: ShadowActivity
    private val defaultCurrencies: DefaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())

    private val currencyPreference: CurrencyPreference = mock()
    private val phoneNumber = PhoneNumber(PHONE_NUMBER_STRING)
    private val analytics: Analytics = mock()
    private val dropbitAccountHelper: DropbitAccountHelper = mock()
    private val clipboardUtil: ClipboardUtil = mock()
    private val bitcoinUtil: BitcoinUtil = mock()
    private val userPreferences: UserPreferences = mock()
    private var paymentBarCallbacks: PaymentBarCallbacks = mock()
    private var cnAddressLookupDelegate: CNAddressLookupDelegate = mock()
    private val transactionFundingManger: TransactionFundingManager = mock()
    private var countryCodeLocales: MutableList<CountryCodeLocale> = mutableListOf()
    private val paymentHolder = PaymentHolder(USDCurrency(5000.00)).also {
        it.defaultCurrencies = defaultCurrencies
    }
    private var paymentUtil: PaymentUtil = PaymentUtil(application, bitcoinUtil, transactionFundingManger).also {
        it.paymentHolder = paymentHolder
    }

    private val validTransactionData = TransactionData(arrayOf(mock()),
            10000L, 1000L, 0, mock(), "")
    private val invalidTransactionData = TransactionData(emptyArray(),
            0, 0, 0, mock(), "")

    private lateinit var scenario: ActivityScenario<HomeActivity>
    private val application: TestCoinKeeperApplication get() = ApplicationProvider.getApplicationContext()

    private val dialog: PayDialogFragment = PayDialogFragment.newInstance(paymentUtil, paymentBarCallbacks, false)

    @Before
    fun setUp() {
        setupDI()
    }

    @After
    fun tearDown() {
        dialog.dismiss()
        scenario.close()
    }

    @Test
    fun tracks_payment_screen_view() {
        start()
        verify(dialog.analytics).trackEvent(Analytics.EVENT_PAY_SCREEN_LOADED)
        verify(dialog.analytics).flush()
    }

    // INITIALIZATION WITH VALUES

    @Test
    fun shows_payment_address_when_initialized_from_scan() {
        val address = TestData.EXTERNAL_ADDRESSES[0]
        paymentUtil.setAddress(address)
        start()

        val receiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)

        assertThat(receiverView.paymentAddress, equalTo(address))
    }

    @Test
    fun funded_payments_get_confirmed() {
        val address = "-- expected--"
        whenever(bitcoinUtil.isValidBTCAddress(address)).thenReturn(true)
        paymentHolder.updateValue(USDCurrency(1.00))
        paymentUtil.setAddress(address)
        whenever(transactionFundingManger.buildFundedTransactionData(any(), any(), any())).thenReturn(validTransactionData)
        start()

        clickOn(dialog.findViewById(R.id.pay_footer_send_btn))

        verify(paymentBarCallbacks).confirmPaymentFor(paymentUtil)
    }

    @Test
    fun contact_sends_get_confirmed() {
        val identity = Identity(Contact(phoneNumber, "Joe Smoe", true))
        whenever(transactionFundingManger.buildFundedTransactionData(any(), any(), any())).thenReturn(validTransactionData)
        whenever(transactionFundingManger.buildFundedTransactionData(eq(null), any(), any())).thenReturn(validTransactionData)
        paymentUtil.setIdentity(identity)
        paymentHolder.publicKey = "-pub-key-"
        paymentHolder.paymentAddress = "-pay-address-"
        whenever(dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        start()

        clickOn(dialog.findViewById(R.id.pay_footer_send_btn))

        verify(paymentBarCallbacks).confirmPaymentFor(paymentUtil, identity)
    }

    // CHECK FUNDED

    @Test
    fun verified_contacts_with_out_addresses_get_invited_without_help_confirmation() {
        whenever(userPreferences.shouldShowInviteHelp).thenReturn(true)
        whenever(transactionFundingManger.buildFundedTransactionData(any(), any(), any())).thenReturn(validTransactionData)
        whenever(transactionFundingManger.buildFundedTransactionData(eq(null), any(), any())).thenReturn(validTransactionData)
        val identity = Identity(Contact(phoneNumber, "Joe Smoe", true))
        paymentHolder.paymentAddress = ""
        paymentHolder.updateValue(USDCurrency(1.00))
        paymentUtil.setIdentity(identity)
        whenever(dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        start()

        clickOn(withId<View>(dialog.view, R.id.pay_footer_send_btn))

        verify(paymentBarCallbacks).confirmInvite(paymentUtil, identity)
    }

    @Test
    @Throws(UriException::class)
    fun given_btc_as_primary_pasting_address_with_out_amount_keeps_btc() {
        val value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=0"
        whenever(clipboardUtil.raw).thenReturn(value)
        paymentHolder.updateValue(BTCCurrency("1.0"))
        val uri: BitcoinUri = mock()
        whenever(bitcoinUtil.parse(value)).thenReturn(uri)
        whenever(uri.address).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R")
        whenever(uri.satoshiAmount).thenReturn(0L)
        start()

        dialog.findViewById<View>(R.id.paste_address_btn)!!.performClick()

        assertThat(paymentHolder.primaryCurrency.toLong(), equalTo(100000000L))
    }

    // PRIMARY / SECONDARY CURRENCIES

    @Test
    @Throws(UriException::class)
    fun given_usd_as_primary_pasting_address_with_amount_toggles_primary_to_btc() {
        val value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=100000000"
        whenever(clipboardUtil.raw).thenReturn(value)
        val uri: BitcoinUri = mock()
        whenever(bitcoinUtil.parse(value)).thenReturn(uri)
        whenever(uri.address).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R")
        whenever(uri.satoshiAmount).thenReturn(100000000L)
        start()

        clickOn(dialog.view, R.id.paste_address_btn)

        scenario.onActivity { activity -> assertThat(dialog.clipboardUtil, equalTo(clipboardUtil)) }

        assertTrue(paymentHolder.primaryCurrency.isCrypto)
        assertThat(paymentHolder.primaryCurrency.toLong(), equalTo(100000000L))
    }

    @Test
    fun shows_error_when_address_not_valid() {
        paymentUtil.setAddress(null)
        val message = application.resources.getString(R.string.pay_error_add_valid_bitcoin_address)
        start()

        dialog.findViewById<View>(R.id.pay_footer_send_btn)!!.performClick()
        val alert = dialog.activity!!.supportFragmentManager.findFragmentByTag("INVALID_PAYMENT") as GenericAlertDialog

        assertThat(alert.message, equalTo(message))
        assertThat(paymentUtil.errorMessage, equalTo(""))
    }

    @Test
    fun sending_adds_memo_and_sharing_to_payment_holder() {
        whenever(dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        start()
        dialog.memoToggleView = mock()
        whenever(dialog.memoToggleView.isSharing).thenReturn(false)
        whenever(dialog.memoToggleView.memo).thenReturn("--memo--")

        paymentUtil.setAddress("--address--")
        paymentHolder.memo = ""
        dialog.sendPayment()
        assertThat(paymentHolder.memo, equalTo("--memo--"))
        assertThat(paymentHolder.isSharingMemo, equalTo(false))

        paymentHolder.isSharingMemo = true
        paymentHolder.memo = ""
        paymentHolder.paymentAddress = "--address--"
        paymentUtil.setIdentity(Identity(Contact(phoneNumber, "Joe Dirt", true)))

        dialog.sendPayment()
        assertThat(paymentHolder.memo, equalTo("--memo--"))
        assertThat(paymentHolder.isSharingMemo, equalTo(false))

        paymentHolder.memo = ""
        paymentHolder.isSharingMemo = true
        paymentUtil.setIdentity(Identity(Contact(phoneNumber, "", true)))
        dialog.sendPayment()
        assertThat(paymentHolder.memo, equalTo("--memo--"))
        assertThat(paymentHolder.isSharingMemo, equalTo(false))
    }

    // VALIDATION

    @Test
    fun canceling_transaction_clears_memo() {
        start()
        paymentHolder.memo = ":grinning: hi"

        withId<View>(dialog.view, R.id.pay_header_close_btn).performClick()

        assertThat(paymentHolder.memo, equalTo(""))
    }


    // SHARED MEMOS

    @Test
    fun allows_user_to_share_memo_for_invite_phone_number_entry__simulate_non_verified_user_lookup() {
        val argumentCaptor = ArgumentCaptor.forClass(CNAddressLookupDelegate.CNAddressLookupCompleteCallback::class.java)
        val identity = Identity(Contact(phoneNumber, "Joe Dirt", false))
        paymentUtil.setIdentity(identity)
        val intent = Intent()
        intent.putExtra(DropbitIntents.EXTRA_IDENTITY, identity)
        start()
        dialog.memoToggleView = mock()

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent)
        verify(cnAddressLookupDelegate).fetchAddressFor(eq(identity), argumentCaptor.capture())
        val callback = argumentCaptor.value
        callback.onAddressLookupComplete(mock())

        verify(dialog.memoToggleView).showSharedMemoViews()
    }

    @Test
    fun shows_shared_memos_when_valid_number_returns_with_pub_key() {
        val identity = Identity(Contact(phoneNumber, "Joe Smoe", true))
        whenever(dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        paymentUtil.setIdentity(identity)
        val addressLookupResult = AddressLookupResult(
                "-phone-hash-",
                "-payment-address-",
                "-pub-key-")
        start()
        dialog.memoToggleView = mock()

        dialog.onFetchContactComplete(addressLookupResult)

        verify(dialog.memoToggleView).showSharedMemoViews()
    }

    @Test
    fun hides_shared_memos_when_valid_number_does_not_return_with_pub_key() {
        val identity = Identity(Contact(phoneNumber, "Joe Smoe", true))
        paymentUtil.setIdentity(identity)

        val addressLookupResult = AddressLookupResult(
                "-phone-hash-",
                "",
                "")
        start()
        dialog.memoToggleView = mock()


        dialog.onFetchContactComplete(addressLookupResult)

        verify(dialog.memoToggleView).hideSharedMemoViews()
    }

    // PAYMENT RECEIVER CONFIGURATION
    @Test
    fun sets_country_codes_on_payment_receiver_view() {
        start()
        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)

        assertThat(paymentReceiverView.countryCodeLocales, equalTo(countryCodeLocales))
    }

    @Test
    @Throws(UriException::class)
    fun pasting_address_with_valid_address_sets_address_on_payment_receiver_view() {
        val rawString = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo"
        mockClipboardWithData(rawString)
        start()

        withId<View>(dialog.view, R.id.paste_address_btn).performClick()

        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        assertThat(paymentReceiverView.paymentAddress, equalTo(rawString))
        assertThat(paymentUtil.getAddress(), equalTo(rawString))
        assertThat(paymentHolder.paymentAddress, equalTo(rawString))
    }

    @Test
    @Throws(UriException::class)
    fun pasting_invalid_address_shows_error_and_does_not_set_address() {
        val rawString = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKooere"
        whenever(clipboardUtil.raw).thenReturn(rawString)
        whenever(bitcoinUtil.parse(rawString)).thenThrow(UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58))
        start()

        withId<View>(dialog.view, R.id.paste_address_btn).performClick()

        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        assertThat(paymentReceiverView.paymentAddress, equalTo(""))
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Address Failed Base 58 check"))
    }

    // PASTING ADDRESS

    @Test
    @Throws(UriException::class)
    fun pasting_with_empty_clipboard_does_nothing() {
        whenever(clipboardUtil.raw).thenReturn("")
        whenever(bitcoinUtil.parse("")).thenThrow(UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS))
        paymentHolder.paymentAddress = "--address--"
        start()

        withId<View>(dialog.view, R.id.paste_address_btn).performClick()

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Nothing to paste"))
        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        assertThat(paymentReceiverView.paymentAddress, equalTo(""))
        assertNull(paymentUtil.getAddress())
        assertThat(paymentHolder.paymentAddress, equalTo(""))
    }


    @Test
    @Throws(UriException::class)
    fun clears_pub_key_from_holder_when_pasting_address() {
        val address = "34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R"
        paymentHolder.publicKey = "--pub-key--"
        whenever(clipboardUtil.raw).thenReturn("--bitcoin uri--")
        val uri = mock<BitcoinUri>()
        whenever(uri.address).thenReturn(address)
        whenever(uri.satoshiAmount).thenReturn(0L)
        whenever(bitcoinUtil.parse(any())).thenReturn(uri)
        start()

        dialog.onPasteClicked()

        assertThat(paymentUtil.getAddress(), equalTo(address))
        assertThat(paymentHolder.publicKey, equalTo(""))
    }

    @Test
    @Throws(UriException::class)
    fun given_usd_as_primary_pasting_address_with_out_amount_keeps_usd() {
        paymentHolder.updateValue(USDCurrency(1.0))
        val value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=0"
        whenever(clipboardUtil.raw).thenReturn(value)
        val uri = mock<BitcoinUri>()
        whenever(bitcoinUtil.parse(value)).thenReturn(uri)
        whenever(uri.address).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R")
        whenever(uri.satoshiAmount).thenReturn(0L)
        start()

        dialog.findViewById<View>(R.id.paste_address_btn)!!.performClick()

        assertTrue(paymentHolder.primaryCurrency.isFiat)
        assertThat(paymentHolder.primaryCurrency.toLong(), equalTo(100L))
    }

    @Test
    @Throws(UriException::class)
    fun pasting_address_over_contact_clears_contact_and_shows_address() {
        val address = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo"
        mockClipboardWithData(address)
        val identity = Identity(Contact(phoneNumber, "Joe Dirt", true))
        val intent = Intent()
        intent.putExtra(DropbitIntents.EXTRA_IDENTITY, identity)
        start()

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent)
        withId<View>(dialog.view, R.id.paste_address_btn).performClick()

        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        assertThat(paymentReceiverView, isVisible())
        assertThat(paymentReceiverView.paymentAddress, equalTo("3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo"))
        assertThat(paymentReceiverView.phoneNumber, equalTo("+1"))
    }

    @Test
    @Throws(UriException::class)
    fun scanning_address__valid_address__no_amount() {
        val cryptoString = "bitcoin:38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"
        val data = Intent()
        data.putExtra(DropbitIntents.EXTRA_SCANNED_DATA, cryptoString)
        val bitcoinUri = mock<BitcoinUri>()
        whenever(bitcoinUtil.parse(cryptoString)).thenReturn(bitcoinUri)
        whenever(bitcoinUri.address).thenReturn("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4")
        start()

        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        val primaryCurrency = withId<EditText>(dialog.view, R.id.primary_currency)
        primaryCurrency.setText("$1.00")
        dialog.onActivityResult(DropbitIntents.REQUEST_QR_FRAGMENT_SCAN, DropbitIntents.RESULT_SCAN_OK, data)

        assertThat(paymentReceiverView.paymentAddress, equalTo("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"))
        assertTrue(paymentHolder.primaryCurrency.isFiat)
        assertThat(paymentHolder.primaryCurrency.toLong(), equalTo(100L))
        assertThat(paymentHolder.isSharingMemo, equalTo(false))
    }

    @Test
    @Throws(UriException::class)
    fun updates_btc_on_scan_with_amount() {
        val cryptoString = "bitcoin:38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4?amount=.01542869"
        val data = Intent()
        data.putExtra(DropbitIntents.EXTRA_SCANNED_DATA, cryptoString)
        val bitcoinUri = mock<BitcoinUri>()
        whenever(bitcoinUtil.parse(cryptoString)).thenReturn(bitcoinUri)
        whenever(bitcoinUri.satoshiAmount).thenReturn(1542869L)
        whenever(bitcoinUri.address).thenReturn("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4")
        dialog.memoToggleView = mock()
        start()

        dialog.onActivityResult(DropbitIntents.REQUEST_QR_FRAGMENT_SCAN, DropbitIntents.RESULT_SCAN_OK, data)

        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        assertThat(paymentReceiverView.paymentAddress, equalTo("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"))
        assertTrue(paymentHolder.primaryCurrency.isCrypto)
        assertThat(paymentHolder.primaryCurrency.toLong(), equalTo(1542869L))
    }

    @Test
    @Throws(UriException::class)
    fun clears_pub_key_when_scanning() {
        paymentHolder.publicKey = "--pub-key--"
        val data = Intent()
        val bitcoinUri = mock<BitcoinUri>()
        val address = "xfdkjvhbw43hfbwkehvbw43jhkf"
        whenever(bitcoinUri.address).thenReturn(address)
        data.putExtra(DropbitIntents.EXTRA_SCANNED_DATA, "bitcoin:xfdkjvhbw43hfbwkehvbw43jhkf")
        whenever(bitcoinUtil.parse(any())).thenReturn(bitcoinUri)
        start()

        dialog.onActivityResult(DropbitIntents.REQUEST_QR_FRAGMENT_SCAN, DropbitIntents.RESULT_SCAN_OK, data)

        assertThat(paymentUtil.getAddress(), equalTo(address))
        assertThat(paymentHolder.paymentAddress, equalTo(address))
        assertThat(paymentHolder.publicKey, equalTo(""))
    }

    // SCAN PAYMENT ADDRESS

    @Test
    fun fetches_address_for_phone_number_when_number_is_valid() {
        start()

        dialog.onPhoneNumberValid(phoneNumber.phoneNumber)

        verify(cnAddressLookupDelegate).fetchAddressFor(eq(phoneNumber), any())
    }

    @Test
    fun nulls_out_contact_on_invalid_phone_number() {
        start()

        val text = "0000000000"
        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        withId<View>(paymentReceiverView, R.id.show_phone_input).performClick()
        val phoneNumberInputView = withId<PhoneNumberInputView>(paymentReceiverView, R.id.phone_number_input)
        phoneNumberInputView.text = text

        assertThat(phoneNumberInputView.text, equalTo("+1"))
        assertNull(paymentUtil.getIdentity())
    }

    @Test
    fun sets_phone_number_on_payment_util_when_valid_input() {
        start()

        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        withId<View>(paymentReceiverView, R.id.show_phone_input).performClick()
        val phoneNumberInputView = withId<PhoneNumberInputView>(paymentReceiverView, R.id.phone_number_input)
        phoneNumberInputView.text = "3305551111"

        assertThat(paymentUtil.getIdentity()!!.value, equalTo(phoneNumber.toString()))
    }

    @Test
    fun picks_contact_to_send() {
        whenever(dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        start()

        clickOn(withId<View>(dialog.view, R.id.contacts_btn))

        Intents.intending(hasComponent(PickUserActivity::class.java.name))
        //TODO verify request code passed
    }

    @Test
    fun picking_contact_hides_payment_receiver_view() {
        start()
        val identity = Identity(Contact(phoneNumber, "Joe Dirt", true))
        val intent = Intent()
        intent.putExtra(DropbitIntents.EXTRA_IDENTITY, identity)
        val paymentReceiverView = withId<PaymentReceiverView>(dialog.view, R.id.payment_receiver)
        paymentReceiverView.paymentAddress = TestData.EXTERNAL_ADDRESSES[0]

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent)

        assertThat(paymentReceiverView, isGone())
        assertThat(paymentReceiverView.paymentAddress, equalTo(""))
        assertThat(paymentReceiverView.phoneNumber, equalTo("+1"))
    }

    @Test
    fun does_not_show_invite_help_when_preference_selected() {
        whenever(userPreferences.shouldShowInviteHelp).thenReturn(false)
        start()

        val identity = Identity(Contact(phoneNumber, "Joe Blow", false))

        dialog.startContactInviteFlow(identity)

        verify(paymentBarCallbacks).confirmInvite(paymentUtil, identity)
    }

    @Test
    fun fetches_address_for_verified_contact_when_user_picks_one() {
        val identity = Identity(Contact(phoneNumber, "Joe Blow", true))
        val intent = Intent()
        intent.putExtra(DropbitIntents.EXTRA_IDENTITY, identity)
        start()

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent)

        assertThat(withId(dialog.view, R.id.contact_name), hasText("Joe Blow"))
        assertThat(withId(dialog.view, R.id.contact_number), hasText(phoneNumber.toInternationalDisplayText()))
        verify(cnAddressLookupDelegate).fetchAddressFor(eq(identity), any())
    }

    @Test
    fun does_fetch_address_for_non_verified_contact_when_user_picks_one() {
        val identity = Identity(Contact(phoneNumber, "Joe Dirt", false))
        val intent = Intent()
        intent.putExtra(DropbitIntents.EXTRA_IDENTITY, identity)
        start()

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent)

        verify(cnAddressLookupDelegate).fetchAddressFor(eq(identity), any())
    }

    @Test
    fun invites_unverified_contact_after_confirming_help_screen() {
        val identity = Identity(Contact(phoneNumber, "Joe Blow", false))
        start()

        dialog.onInviteHelpAccepted(identity)

        verify(paymentBarCallbacks).confirmInvite(paymentUtil, identity)
    }

    @Ignore
    @Test
    fun directs_user_to_verify_phone_when_selecting_contacts() {
        whenever(dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        start()

        dialog.findViewById<View>(R.id.contacts_btn)!!.performClick()
        val intent = shadowActivity.peekNextStartedActivity()
        assertThat(intent.component.className, equalTo(VerificationActivity::class.java.name))
    }

    @Test
    fun can_cancel_payment_request() {
        paymentHolder.paymentAddress = "--address--"
        paymentHolder.publicKey = "--pub-key--"
        start()

        dialog.findViewById<View>(R.id.pay_header_close_btn)!!.performClick()

        verify(paymentBarCallbacks).cancelPayment(dialog)
        assertThat(paymentHolder.publicKey, equalTo(""))
        assertThat(paymentHolder.paymentAddress, equalTo(""))
    }

    @Test
    fun tells_view_types_to_teardown_when_detached() {
        start()
        dialog.memoToggleView = mock()

        dialog.onDetach()

        verify(dialog.memoToggleView).tearDown()
    }

    @Test
    fun observes_sending_max() {
        whenever(transactionFundingManger.buildFundedTransactionData(eq(null), any())).thenReturn(validTransactionData)
        start()

        clickOn(withId<View>(dialog.view, R.id.send_max))

        assertTrue(paymentHolder.primaryCurrency is CryptoCurrency)
        assertThat(paymentHolder.primaryCurrency.toLong(), equalTo(paymentHolder.transactionData.amount))
    }

    @Test
    fun observes_send_max_cleared() {
        whenever(transactionFundingManger.buildFundedTransactionData(eq(null), any())).thenReturn(validTransactionData)
        start()

        clickOn(withId<View>(dialog.view, R.id.send_max))
        val view = withId<TextView>(dialog.view, R.id.primary_currency)
        view.text = "0"

        assertTrue(paymentHolder.primaryCurrency is CryptoCurrency)
        assertThat(paymentHolder.primaryCurrency.toLong(), equalTo(0L))
        assertThat(paymentHolder.transactionData.amount, equalTo(0L))
        assertThat(paymentHolder.transactionData.utxos.size, equalTo(0))
        assertFalse(paymentUtil.isFunded())
    }

    @Test
    fun shows_invalid_btc_address_when_pasting_dropbitme_url_in_payment_request() {
        start()

        dialog.bip70Callback.onFailure(mock(), RuntimeException("garbage"))

        val errorMessage = ShadowToast.getTextOfLatestToast()

        assertThat(errorMessage, equalTo(dialog.getString(R.string.invalid_bitcoin_address_error)))
    }

    private fun start() {
        scenario = ActivityScenario.launch(HomeActivity::class.java)
        scenario.onActivity { activity ->
            dialog.show(activity.supportFragmentManager, "tag")
            shadowActivity = shadowOf(activity)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
    }

    private fun setupDI() {
        application.cnAddressLookupDelegae = cnAddressLookupDelegate
        application.analytics = analytics
        application.bitcoinUtil = bitcoinUtil
        application.clipboardUtil = clipboardUtil
        application.userPreferences = userPreferences
        application.countryCodeLocales = countryCodeLocales
        application.dropbitAccountHelper = dropbitAccountHelper
        application.countryCodeLocaleGenerator = mock()
        whenever(application.countryCodeLocaleGenerator!!.generate()).thenReturn(countryCodeLocales)
        whenever(currencyPreference.currenciesPreference).thenReturn(defaultCurrencies)
        whenever(userPreferences.shouldShowInviteHelp).thenReturn(true)
        paymentHolder.updateValue(USDCurrency(5000.00))
        countryCodeLocales.add(CountryCodeLocale(Locale("en", "GB"), 44))
        countryCodeLocales.add(CountryCodeLocale(Locale("en", "US"), 1))
        paymentUtil.setFee(TransactionFee(5.0, 10.0, 15.0).slow)
    }

    @Throws(UriException::class)
    private fun mockClipboardWithData(rawString: String) {
        whenever(clipboardUtil.raw).thenReturn(rawString)
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn(rawString)
        whenever(bitcoinUtil.parse(rawString)).thenReturn(bitcoinUri)
    }

    @Module
    class TestPayDialogFragmentModule {
        @Provides
        fun feesManager(): FeesManager = mock()
    }

    companion object {

        private const val PHONE_NUMBER_STRING = "+13305551111"
    }
}