package com.coinninja.coinkeeper.ui.payment.create

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.thunderdome.model.LedgerInvoice
import app.coinninja.cn.thunderdome.model.RequestInvoice
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.bitcoin.isFunded
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.view.activity.QrScanActivity
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.coinkeeper.view.fragment.InviteHelpDialogFragment
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale
import com.google.common.truth.Truth.assertThat
import com.google.i18n.phonenumbers.Phonenumber
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import kotlinx.android.synthetic.main.activity_create_payment.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.util.*

@RunWith(AndroidJUnit4::class)
class CreatePaymentActivityTest {

    private val countries: List<CountryCodeLocale>
        get() = listOf(
                CountryCodeLocale(Locale("en", "GB"), 44),
                CountryCodeLocale(Locale("en", "US"), 1)
        )


    private val creationIntent: Intent get() = Intent(ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>(), CreatePaymentActivity::class.java)

    private fun createScenario(intent: Intent = creationIntent): ActivityScenario<CreatePaymentActivity> {

        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().also {
            it.countryCodeLocaleGenerator = mock()
            whenever(it.countryCodeLocaleGenerator.generate()).thenReturn(countries)
        }

        val scenario: ActivityScenario<CreatePaymentActivity> = ActivityScenario.launch(intent)
        scenario.onActivity { activity ->
            activity.latestPriceObserver.onChanged(USDCurrency(10_000_00))
        }

        return scenario
    }

    // INITIALIZATION

    @Test
    fun init_with_scan_launches_scanner_for_result__with_scan_intent() {
        val creationIntent = creationIntent
        creationIntent.putExtra(DropbitIntents.EXTRA_SHOULD_SCAN, true)
        val scenario = createScenario(creationIntent)

        scenario.onActivity { activity ->
            val intent = shadowOf(activity).peekNextStartedActivityForResult()

            assertThat(intent).isNotNull()
            assertThat(intent.intent.component.className).isEqualTo(QrScanActivity::class.java.name)
            assertThat(activity.intent.hasExtra(DropbitIntents.EXTRA_SHOULD_SCAN)).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun init__with_bitcoin_uri_intent() {
        val creationIntent = creationIntent
        creationIntent.putExtra(DropbitIntents.EXTRA_BITCOIN_URI, "bitcoin:--address--")
        val scenario = createScenario(creationIntent)

        scenario.onActivity { activity ->
            verify(activity.rawInputViewModel).processCryptoUriInput("bitcoin:--address--")
            assertThat(activity.intent.hasExtra(DropbitIntents.EXTRA_BITCOIN_URI)).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun common_init() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            verify(activity.walletViewModel.currentPrice).observe(activity, activity.latestPriceObserver)
            verify(activity.walletViewModel.isLightningLocked).observe(activity, activity.isLightningLockedObserver)
            verify(activity.rawInputViewModel.invalidRawInput).observe(activity, activity.invalidRawInputObserver)
            verify(activity.rawInputViewModel.validRawInput).observe(activity, activity.validRawInputObserver)
            verify(activity.rawInputViewModel.validRequestInvoice).observe(activity, activity.validRequestInvoiceObserver)
            verify(activity.fundingViewModel.addressLookupResult).observe(activity, activity.accountLookupResultObserver)
            verify(activity.fundingViewModel.pendingLedgerInvoice).observe(activity, activity.pendingLedgerInvoiceObserver)
            assertThat(activity.accountModeToggle.onModeSelectedObserver).isNotNull()
            assertThat(activity.payment_receiver.countryCodeLocales).isNotNull()
            assertThat(activity.payment_receiver.countryCodeLocales.size).isEqualTo(2)
            assertThat(activity.payment_receiver.countryCodeLocales[0].locale.country).isEqualTo("GB")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()

    }

    @Test
    fun locked_lightning_hides_account_mode_toggle() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.isLightningLockedObserver.onChanged(true)

            assertThat(activity.accountModeToggle.visibility).isEqualTo(View.GONE)
            assertThat(activity.accountModeToggle.onModeSelectedObserver).isNull()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun stop_observing_price_updates_once_one_is_fetched() {
        val scenario = createScenario()

        scenario.onActivity { activity ->

            assertThat(activity.paymentHolder.evaluationCurrency.toLong()).isEqualTo(10_000_00)
            assertThat(activity.amountInputView.paymentHolder.evaluationCurrency.toLong()).isEqualTo(10_000_00)

            activity.latestPriceObserver.onChanged(USDCurrency(11_000_00))

            assertThat(activity.paymentHolder.evaluationCurrency.toLong()).isEqualTo(10_000_00)
            assertThat(activity.amountInputView.paymentHolder.evaluationCurrency.toLong()).isEqualTo(10_000_00)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun pressing_close_button_finishes_payment() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.closeButton.performClick()

            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun account_mode_change_sets_mode() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.accountModeToggleObserver.onSelectionChange(AccountMode.LIGHTNING)
            verify(activity.walletViewModel).setMode(AccountMode.LIGHTNING)

            activity.accountModeToggleObserver.onSelectionChange(AccountMode.BLOCKCHAIN)
            verify(activity.walletViewModel).setMode(AccountMode.BLOCKCHAIN)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun observing_account_mode_change_sets_mode_on_views() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.isSendingMax = true
            activity.onAccountModeChanged(AccountMode.LIGHTNING)
            assertThat(activity.amountInputView.accountMode).isEqualTo(AccountMode.LIGHTNING)
            assertThat(activity.amountInputView.canToggleCurrencies).isFalse()
            assertThat(activity.amountInputView.canSendMax).isFalse()
            assertThat(activity.isSendingMax).isFalse()
            assertThat(activity.accountModeToggle.mode).isEqualTo(AccountMode.LIGHTNING)

            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)
            assertThat(activity.amountInputView.accountMode).isEqualTo(AccountMode.BLOCKCHAIN)
            assertThat(activity.amountInputView.canToggleCurrencies).isTrue()
            assertThat(activity.accountModeToggle.mode).isEqualTo(AccountMode.BLOCKCHAIN)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // BACKGROUNDING SCREEN

    @Test
    fun pausing_activity_removes_observers_from_view_models() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(activity.rawInputViewModel.invalidRawInput).removeObserver(activity.invalidRawInputObserver)
            verify(activity.rawInputViewModel.validRawInput).removeObserver(activity.validRawInputObserver)
            verify(activity.walletViewModel.currentPrice).removeObserver(activity.latestPriceObserver)
            verify(activity.walletViewModel.isLightningLocked).removeObserver(activity.isLightningLockedObserver)
            verify(activity.fundingViewModel.addressLookupResult).removeObserver(activity.accountLookupResultObserver)
            verify(activity.rawInputViewModel.validRequestInvoice).removeObserver(activity.validRequestInvoiceObserver)
            verify(activity.fundingViewModel.pendingLedgerInvoice).removeObserver(activity.pendingLedgerInvoiceObserver)
        }

        scenario.close()
    }

    // PAYMENT INPUT
    @Test
    fun payment_input__send_max() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.amountInputView.sendMax.performClick()

            assertThat(activity.isSendingMax).isTrue()
            assertThat(activity.paymentHolder.transactionData.isFunded()).isFalse()
            verify(activity.fundingViewModel).fundMax(null)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun sending_max_sets_value_when_funded() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.amountInputView.sendMax.performClick()
            assertThat(activity.isSendingMax).isTrue()

            activity.transactionDataObserver.onChanged(TransactionData(
                    arrayOf(mock()),
                    100_000,
                    1_000,
                    0
            ))

            assertThat(activity.paymentHolder.cryptoCurrency.toLong()).isEqualTo(100_000)
            assertThat(activity.amountInputView.primaryCurrency.text.toString()).isEqualTo("0.001")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun payment_input__send_max__clears_when_amount_zeroed() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.isSendingMax = true
            activity.amountInputView.sendMax.performClick()

            activity.amountInputView.primaryCurrency.setText("")

            assertThat(activity.isSendingMax).isFalse()
            verify(activity.fundingViewModel).fundMax(null)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }


    @Test
    fun payment_input__observed_input_clears_funding() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.paymentHolder.transactionData = TransactionData(arrayOf(mock()))
            activity.amountInputView.primaryCurrency.setText("")
            activity.amountInputView.primaryCurrency.setText("1")

            assertThat(activity.isSendingMax).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // CONTACT Lookup
    @Test
    fun contact_lookup__shares_memo__when_contact_set__phone_input() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.memoToggleView.toggleSharingMemo()
            assertThat(activity.memoToggleView.isSharing).isFalse()

            val phoneNumber: Phonenumber.PhoneNumber = mock()
            whenever(phoneNumber.countryCode).thenReturn(1)
            whenever(phoneNumber.nationalNumber).thenReturn(3305551111)

            activity.onValidPhoneNumberInput(phoneNumber)

            assertThat(activity.memoToggleView.isSharing).isTrue()
            verify(activity.rawInputViewModel).clear()
            verify(activity.fundingViewModel).clear()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun contact_lookup__shares_memo__when_contact_set__contact_pick() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.memoToggleView.toggleSharingMemo()
            assertThat(activity.memoToggleView.isSharing).isFalse()

            val intent = Intent().also {
                it.putExtra(DropbitIntents.EXTRA_IDENTITY, Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = false))
            }
            activity.onActivityResult(DropbitIntents.REQUEST_PICK_CONTACT, Activity.RESULT_OK, intent)


            assertThat(activity.memoToggleView.isSharing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun contact_lookup__triggers_lookup__valid_phone_number_input() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val phoneNumber: Phonenumber.PhoneNumber = mock()
            whenever(phoneNumber.countryCode).thenReturn(1)
            whenever(phoneNumber.nationalNumber).thenReturn(3305551111)
            activity.onValidPhoneNumberInput(phoneNumber)
            verify(activity.fundingViewModel).lookupIdentityHash("710c3ec37d3bbab4d9b140656ea8ab28d14bad091e12b912dc73d0fbcd78664d", AccountMode.LIGHTNING)
            verify(activity.rawInputViewModel).clear()
            verify(activity.fundingViewModel).clear()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun contact_lookup__triggers_lookup__lookup_from_contact_selection() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val intent = Intent().also {
                it.putExtra(DropbitIntents.EXTRA_IDENTITY, Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = false))
            }
            activity.onActivityResult(DropbitIntents.REQUEST_PICK_CONTACT, Activity.RESULT_OK, intent)

            verify(activity.fundingViewModel).lookupIdentityHash("--hash--", AccountMode.LIGHTNING)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun contact_lookup__sets_identity_values__phone_only() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val intent = Intent().also {
                it.putExtra(DropbitIntents.EXTRA_IDENTITY, Identity(IdentityType.PHONE, "+13305551111", "--hash--", "", isVerified = false))
            }

            activity.onActivityResult(DropbitIntents.REQUEST_PICK_CONTACT, Activity.RESULT_OK, intent)

            assertThat(activity.paymentReceiverView.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.contactName.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.contactNumber.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.contactNumber.text).isEqualTo("+1 330-555-1111")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun contact_lookup__sets_identity_values__phone_and_name() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val intent = Intent().also {
                it.putExtra(DropbitIntents.EXTRA_IDENTITY, Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = false))
            }

            activity.onActivityResult(DropbitIntents.REQUEST_PICK_CONTACT, Activity.RESULT_OK, intent)

            assertThat(activity.paymentReceiverView.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.contactName.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.contactNumber.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.contactNumber.text).isEqualTo("+1 330-555-1111")
            assertThat(activity.contactName.text).isEqualTo("Joe Smoe")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun contact_lookup__sets_identity_values__twitter() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val intent = Intent().also {
                it.putExtra(DropbitIntents.EXTRA_IDENTITY, Identity(IdentityType.TWITTER, "123456789", "123456789", "Joe Smoe", handle = "@JOE", isVerified = false))
            }
            activity.onActivityResult(DropbitIntents.REQUEST_PICK_CONTACT, Activity.RESULT_OK, intent)

            assertThat(activity.paymentReceiverView.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.contactName.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.contactNumber.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.contactNumber.text).isEqualTo("@JOE")
            assertThat(activity.contactName.text).isEqualTo("Joe Smoe")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun contact_lookup__sets_identity_values__clearing_pasted_address() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.paymentHolder.paymentAddress = "--payment-address--"
            activity.paymentReceiverView.paymentAddress = activity.paymentHolder.paymentAddress
            val intent = Intent().also {
                it.putExtra(DropbitIntents.EXTRA_IDENTITY, Identity(IdentityType.TWITTER, "123456789", "123456789", "Joe Smoe", handle = "@JOE", isVerified = false))
            }
            activity.onActivityResult(DropbitIntents.REQUEST_PICK_CONTACT, Activity.RESULT_OK, intent)

            assertThat(activity.paymentReceiverView.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.contactName.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.contactNumber.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.contactNumber.text).isEqualTo("@JOE")
            assertThat(activity.contactName.text).isEqualTo("Joe Smoe")
            assertThat(activity.paymentHolder.paymentAddress).isEqualTo("")
            assertThat(activity.paymentReceiverView.paymentAddress).isEqualTo("")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // Payment Validation

    @Test
    fun validation__error__non_sufficient_funds() {
        val scenario = createScenario()

        scenario.onActivity { activity ->

            activity.paymentHolder.paymentAddress = "--payment-address--"
            activity.amountInputView.primaryCurrency.setText("$100.00")
            activity.onHoldingsWorthChanged(USDCurrency(50_00))
            activity.onHoldingsChanged(BTCCurrency(500_000))
            activity.onLatestPriceChanged(USDCurrency(10_000_00))
            activity.nextButton.performClick()
            activity.paymentHolder.paymentAddress = "--address--"

            activity.transactionDataObserver.onChanged(TransactionData())

            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.pay_error_insufficient_funds, "$100.00", "$50.00"))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__insufficient_funds_for_lightning_invite() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.changeAccountMode(AccountMode.LIGHTNING)
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.onHoldingsChanged(BTCCurrency(activity.paymentHolder.cryptoCurrency.toLong() - 10_000))
            activity.onHoldingsWorthChanged(activity.holdings.toUSD(activity.paymentHolder.evaluationCurrency))
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.accountLookupResultObserver.onChanged(AddressLookupResult())

            activity.nextButton.performClick()

            verifyZeroInteractions(activity.activityNavigationUtil)
            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.pay_error_insufficient_funds, "$10.00", "$9.00"))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__funding_for_lightning() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.changeAccountMode(AccountMode.LIGHTNING)
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.onHoldingsChanged(BTCCurrency(activity.paymentHolder.cryptoCurrency.toLong() - 10_000))
            activity.onHoldingsWorthChanged(activity.holdings.toUSD(activity.paymentHolder.evaluationCurrency))
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.accountLookupResultObserver.onChanged(AddressLookupResult(phoneNumberHash = "--hash--", address = "ln-address", addressType = "lightning"))
            activity.nextButton.performClick()

            verify(activity.fundingViewModel).estimateLightningPayment("ln-address", activity.paymentHolder.cryptoCurrency.toLong())
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__insufficient_funds_for_lightning() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.changeAccountMode(AccountMode.LIGHTNING)
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.onHoldingsChanged(BTCCurrency(activity.paymentHolder.cryptoCurrency.toLong() - 10_000))
            activity.onHoldingsWorthChanged(activity.holdings.toUSD(activity.paymentHolder.evaluationCurrency))
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.accountLookupResultObserver.onChanged(AddressLookupResult(phoneNumberHash = "--hash--", address = "ln-address", addressType = "lightning"))
            activity.nextButton.performClick()
            activity.pendingLedgerInvoiceObserver.onChanged(LedgerInvoice(value = 0))

            verifyZeroInteractions(activity.activityNavigationUtil)
            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.pay_error_insufficient_funds, "$10.00", "$9.00"))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__error__non_sufficient_funds__no_amount_defined() {
        val scenario = createScenario()

        scenario.onActivity { activity ->

            activity.paymentHolder.paymentAddress = "--payment-address--"
            activity.onHoldingsWorthChanged(USDCurrency(50_00))
            activity.onHoldingsChanged(BTCCurrency(500_000))
            activity.onLatestPriceChanged(USDCurrency(10_000_00))
            activity.nextButton.performClick()


            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.pay_error_invalid_amount))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__error__receiver_not_specified__no_address_or_contact() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.nextButton.performClick()

            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.pay_error_add_valid_bitcoin_address))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__receiver_specified__with_address() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.amountInputView.primaryCurrency.setText("$100.00")
            activity.onLatestPriceChanged(USDCurrency(10_000_00))

            activity.paymentHolder.paymentAddress = "--payment-address--"
            activity.nextButton.performClick()

            verify(activity.fundingViewModel).fundTransaction("--payment-address--", 1_000_000)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__receiver_specified__with_contact() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.changeAccountMode(AccountMode.BLOCKCHAIN)
            activity.accountModeToggle.mode = AccountMode.BLOCKCHAIN
            activity.amountInputView.primaryCurrency.setText("$100.00")
            activity.onLatestPriceChanged(USDCurrency(10_000_00))

            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.nextButton.performClick()

            verify(activity.fundingViewModel).fundTransactionForDropbit(1_000_000)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__memo_sent_with_payment_request__empty_when_not_filled() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.memoToggleView.setText("")
            activity.amountInputView.primaryCurrency.setText("$100.00")
            activity.onLatestPriceChanged(USDCurrency(10_000_00))

            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.nextButton.performClick()

            assertThat(activity.paymentHolder.memo).isEmpty()
            assertThat(activity.paymentHolder.isSharingMemo).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__memo_sent_with_payment_request__present_not_shared() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.memoToggleView.setText("foo bar")
            activity.memoToggleView.toggleSharingMemo()

            activity.amountInputView.primaryCurrency.setText("$100.00")
            activity.onLatestPriceChanged(USDCurrency(10_000_00))

            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.nextButton.performClick()

            assertThat(activity.paymentHolder.memo).isEqualTo("foo bar")
            assertThat(activity.paymentHolder.isSharingMemo).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__memo_sent_with_payment_request__present_and_shared() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.memoToggleView.setText("foo bar")

            activity.amountInputView.primaryCurrency.setText("$100.00")
            activity.onLatestPriceChanged(USDCurrency(10_000_00))

            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.nextButton.performClick()

            assertThat(activity.paymentHolder.memo).isEqualTo("foo bar")
            assertThat(activity.paymentHolder.isSharingMemo).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__memo_set_with_payment_request__present__not_sharing_with_address_only() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.memoToggleView.setText("foo bar")
            activity.paymentHolder.paymentAddress = "--payment-address--"

            activity.amountInputView.primaryCurrency.setText("$100.00")
            activity.onLatestPriceChanged(USDCurrency(10_000_00))

            activity.toUser = null
            activity.nextButton.performClick()

            assertThat(activity.paymentHolder.memo).isEqualTo("foo bar")
            assertThat(activity.paymentHolder.isSharingMemo).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__send_max_recalculates_after_raw_input() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.amountInputView.sendMax.performClick()
            assertThat(activity.isSendingMax).isTrue()

            val bitcoinUri: BitcoinUri = mock()
            whenever(bitcoinUri.address).thenReturn("--address--")
            activity.validRawInputObserver.onChanged(bitcoinUri)

            activity.nextButton.performClick()

            verify(activity.fundingViewModel).fundMax("--address--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__send_max_recalculates_after_contact_selection() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.memoToggleView.setText("foo bar")
            activity.paymentHolder.paymentAddress = "--payment-address--"
            activity.amountInputView.sendMax.performClick()
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.accountLookupResultObserver.onChanged(AddressLookupResult("--hash--", "--address--", "--pub-key--", addressType = "btc"))

            activity.nextButton.performClick()

            verify(activity.fundingViewModel).fundMax("--address--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // VERIFIED CONTACT
    @Test
    fun validation__caches_pub_key_when_present() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)
            activity.memoToggleView.setText("foo bar")
            activity.paymentHolder.paymentAddress = "--payment-address--"
            activity.amountInputView.sendMax.performClick()
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.accountLookupResultObserver.onChanged(AddressLookupResult("--hash--", "--address--", "--pub-key--", addressType = "btc"))

            activity.nextButton.performClick()

            assertThat(activity.paymentHolder.publicKey).isEqualTo("--pub-key--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun validation__uses_payment_address_from_lookup() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)
            activity.memoToggleView.setText("foo bar")
            activity.paymentHolder.paymentAddress = "--payment-address--"
            activity.amountInputView.sendMax.performClick()
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111")
            activity.accountLookupResultObserver.onChanged(AddressLookupResult("--hash--", "--address--", "--pub-key--", addressType = "btc"))

            activity.nextButton.performClick()

            assertThat(activity.paymentHolder.paymentAddress).isEqualTo("--address--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun confirms_payment_for__address() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)
            activity.paymentHolder.paymentAddress = "--payment-address--"
            activity.amountInputView.sendMax.performClick()
            activity.transactionDataObserver.onChanged(TransactionData(arrayOf(mock()), 10_000, 1_000))
            activity.nextButton.performClick()
            activity.transactionDataObserver.onChanged(TransactionData(arrayOf(mock()), 10_000, 1_000))

            verify(activity.activityNavigationUtil).navigateToConfirmPaymentScreen(activity, activity.paymentHolder)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun confirms_payment_for__lnd() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onAccountModeChanged(AccountMode.LIGHTNING)
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--",
                    "Joe Smoe", isVerified = true)
            activity.accountLookupResultObserver.onChanged(AddressLookupResult(
                    "--hash--",
                    address = "ln--address--",
                    addressPubKey = "--pub-key--",
                    addressType = "lightning"
            ))

            activity.nextButton.performClick()
            activity.pendingLedgerInvoiceObserver.onChanged(
                    LedgerInvoice(value = activity.paymentHolder.cryptoCurrency.toLong()))

            assertThat(activity.paymentHolder.requestInvoice).isNotNull()
            assertThat(activity.paymentHolder.requestInvoice!!.encoded).isEqualTo("ln--address--")
            verify(activity.activityNavigationUtil).navigateToConfirmPaymentScreen(activity, activity.paymentHolder)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun confirms_payment_for__lnd_invite() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onAccountModeChanged(AccountMode.LIGHTNING)
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--",
                    "Joe Smoe", isVerified = true)
            activity.accountLookupResultObserver.onChanged(AddressLookupResult(
                    "--hash--",
                    address = "",
                    addressPubKey = "",
                    addressType = "lightning"
            ))

            assertThat(activity.paymentHolder.publicKey).isEqualTo("")
            assertThat(activity.paymentHolder.requestInvoice).isNotNull()
            assertThat(activity.paymentHolder.requestInvoice!!.encoded).isEqualTo("")


            activity.nextButton.performClick()
            activity.transactionDataObserver.onChanged(TransactionData(arrayOf(mock()),
                    10_000,
                    1_000))

            assertThat(activity.paymentHolder.requestInvoice).isNotNull()
            assertThat(activity.paymentHolder.requestInvoice!!.encoded).isEqualTo("")
            verify(activity.activityNavigationUtil).navigateToConfirmPaymentScreen(activity, activity.paymentHolder)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // INVITE CONTACT

    @Test
    fun confirms_invite__prompts_user_to_invite() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)
            whenever(activity.userPreferences.shouldShowInviteHelp).thenReturn(true)
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = false)
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.nextButton.performClick()
            activity.transactionDataObserver.onChanged(TransactionData(arrayOf(mock()),
                    activity.paymentHolder.cryptoCurrency.toLong(), 1_000))


            assertThat(activity.supportFragmentManager.findFragmentByTag(InviteHelpDialogFragment.TAG)).isNotNull()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun confirms_invite__skips_prompt_because_of_permission() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)
            whenever(activity.userPreferences.shouldShowInviteHelp).thenReturn(false)
            activity.toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = false)
            activity.amountInputView.primaryCurrency.setText("$10.00")
            activity.nextButton.performClick()
            activity.transactionDataObserver.onChanged(TransactionData(arrayOf(mock()),
                    activity.paymentHolder.cryptoCurrency.toLong(), 1_000))

            verify(activity.activityNavigationUtil).navigateToConfirmPaymentScreen(activity, activity.paymentHolder)

            assertThat(activity.supportFragmentManager.findFragmentByTag(InviteHelpDialogFragment.TAG)).isNull()
            verify(activity.activityNavigationUtil).navigateToConfirmPaymentScreen(activity, activity.paymentHolder)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // PASTING
    @Test
    fun pasting__action_requests_processing_of_clipboard() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.pasteButton.performClick()

            verify(activity.rawInputViewModel).inputFromPaste()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }


    // SCANNING

    @Test
    fun scan_button_launches_scanner_for_result() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.paymentReceiverView.scanButton.performClick()

            val intent = shadowOf(activity).peekNextStartedActivityForResult()
            assertThat(intent).isNotNull()
            assertThat(intent.intent.component.className).isEqualTo(QrScanActivity::class.java.name)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun canceling_scan_allows_user_to_continue_with_payment() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onActivityResult(DropbitIntents.REQUEST_QR_SCAN, QrScanActivity.RESULT_CANCELED, null)
            assertThat(activity.isFinishing).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun scan_results_are_processed() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val intent = Intent()
            intent.putExtra(DropbitIntents.EXTRA_SCANNED_DATA, "bitcoin:--address--")
            activity.onActivityResult(DropbitIntents.REQUEST_QR_SCAN, DropbitIntents.RESULT_SCAN_OK, intent)

            verify(activity.rawInputViewModel).onQrScanResult(intent)
            assertThat(activity.isFinishing).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // FEEDBACK FROM RAW INPUT
    @Test
    fun raw_input_from_btc_changes_mode() {
        val scenario = createScenario()
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")

        scenario.onActivity { activity ->
            activity.validRawInputObserver.onChanged(bitcoinUri)

            verify(activity.walletViewModel).setMode(AccountMode.BLOCKCHAIN)
            verify(activity.rawInputViewModel).clear()
            verify(activity.fundingViewModel).clear()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun raw_input__does_not_share_memo() {
        val scenario = createScenario()
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")

        scenario.onActivity { activity ->
            activity.validRawInputObserver.onChanged(bitcoinUri)

            assertThat(activity.memoToggleView.isSharing).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun invalid_address_from_raw_source_notifies_user() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.invalidRawInputObserver.onChanged("")

            val dialog = activity.supportFragmentManager.findFragmentByTag("ERROR_DIALOG") as GenericAlertDialog

            assertThat(dialog.message).isEqualTo(activity.getString(R.string.invalid_bitcoin_address_error))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun valid_results_populate_screen__address_only() {
        val scenario = createScenario()
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")

        scenario.onActivity { activity ->
            activity.validRawInputObserver.onChanged(bitcoinUri)

            assertThat(activity.memoToggleView.memo).isEqualTo("")
            assertThat(activity.paymentReceiverView.paymentAddress).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency).isInstanceOf(USDCurrency::class.java)
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency.toLong()).isEqualTo(0)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun valid_results_populate_screen__address_amount() {
        val scenario = createScenario()
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        whenever(bitcoinUri.satoshiAmount).thenReturn(150_000_000)

        scenario.onActivity { activity ->
            activity.validRawInputObserver.onChanged(bitcoinUri)

            assertThat(activity.memoToggleView.memo).isEqualTo("")
            assertThat(activity.paymentReceiverView.paymentAddress).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency).isInstanceOf(BTCCurrency::class.java)
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency.toLong()).isEqualTo(150_000_000)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun valid_results_populate_screen__address_amount_required_fee() {
        val scenario = createScenario()
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        whenever(bitcoinUri.satoshiAmount).thenReturn(150_000_000)
        whenever(bitcoinUri.requiredFee).thenReturn(5.0)

        scenario.onActivity { activity ->
            activity.validRawInputObserver.onChanged(bitcoinUri)

            assertThat(activity.memoToggleView.memo).isEqualTo("")
            assertThat(activity.paymentReceiverView.paymentAddress).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency).isInstanceOf(BTCCurrency::class.java)
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency.toLong()).isEqualTo(150_000_000)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun valid_results_populate_screen__address_amount_required_fee_and_memo() {
        val scenario = createScenario()
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        whenever(bitcoinUri.satoshiAmount).thenReturn(150_000_000)
        whenever(bitcoinUri.requiredFee).thenReturn(5.0)
        whenever(bitcoinUri.memo).thenReturn("food bar")

        scenario.onActivity { activity ->
            activity.validRawInputObserver.onChanged(bitcoinUri)

            assertThat(activity.memoToggleView.memo).isEqualTo("food bar")
            assertThat(activity.paymentReceiverView.paymentAddress).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency).isInstanceOf(BTCCurrency::class.java)
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency.toLong()).isEqualTo(150_000_000)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun valid_raw_input__result_shows_address__hides_contact_selection() {
        val scenario = createScenario()
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")

        scenario.onActivity { activity ->
            val intent = Intent().also {
                it.putExtra(DropbitIntents.EXTRA_IDENTITY, Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = false))
            }

            activity.onActivityResult(DropbitIntents.REQUEST_PICK_CONTACT, Activity.RESULT_OK, intent)

            assertThat(activity.contactName.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.contactNumber.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.paymentReceiverView.visibility).isEqualTo(View.INVISIBLE)

            activity.validRawInputObserver.onChanged(bitcoinUri)

            assertThat(activity.paymentReceiverView.paymentAddress).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
            assertThat(activity.contactName.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.contactNumber.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.paymentReceiverView.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun lnd_from_raw_input() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onLatestPriceChanged(USDCurrency(10_000_00))
            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)

            val requestInvoice = RequestInvoice(
                    "--addr--",
                    "--payment-hash--",
                    1_000_000,
                    "--time-stamp--",
                    "--expiry--",
                    "--description--",
                    "--description-hash--",
                    "--fallback--",
                    "--cltv-expiry--"
            )
            requestInvoice.encoded = "lnd--encoded-request"
            activity.validRequestInvoiceObserver.onChanged(requestInvoice)

            assertThat(activity.contactName.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.contactNumber.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.paymentReceiverView.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.paymentReceiverView.paymentAddress).isEqualTo(requestInvoice.encoded)
            assertThat(activity.paymentHolder.cryptoCurrency.toLong()).isEqualTo(1_000_000)
            assertThat(activity.amountInputView.primaryCurrency.text.toString()).isEqualTo("$100.00")
            assertThat(activity.memoToggleView.memo).isEqualTo("--description--")
            assertThat(activity.amountInputView.primaryCurrency.isEnabled).isFalse()
            verify(activity.walletViewModel).setMode(AccountMode.LIGHTNING)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // CONTACT SELECTION

    @Test
    fun picked_from_twitter_source__starts_pick_user__when_verified() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.dropbitAccountHelper.isTwitterVerified).thenReturn(true)

            activity.twitterButton.performClick()

            verify(activity.activityNavigationUtil).startPickContactActivity(activity, DropbitIntents.ACTION_TWITTER_SELECTION)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun picked_from_twitter_source__shows_verification_alert__when_not_verified__positive_verifies() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.dropbitAccountHelper.isTwitterVerified).thenReturn(false)

            activity.twitterButton.performClick()

            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.your_twitter_account_is_not_verified))
            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text).isEqualTo(activity.getString(R.string.not_now))
            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).text).isEqualTo(activity.getString(R.string.verify_now))

            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick())
            assertThat(activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag)).isNull()

            verify(activity.activityNavigationUtil).navigateToUserVerification(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun picked_from_twitter_source__shows_verification_alert__when_not_verified__negative_dismisses() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.dropbitAccountHelper.isTwitterVerified).thenReturn(false)

            activity.twitterButton.performClick()

            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.your_twitter_account_is_not_verified))
            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text).isEqualTo(activity.getString(R.string.not_now))
            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).text).isEqualTo(activity.getString(R.string.verify_now))

            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick())
            assertThat(activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag)).isNull()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun picked_from_contact_source_with_name_and_number__when_verified() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.dropbitAccountHelper.isPhoneVerified).thenReturn(true)

            activity.phoneButton.performClick()

            verify(activity.activityNavigationUtil).startPickContactActivity(activity, DropbitIntents.ACTION_CONTACTS_SELECTION)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun picked_from_contact_source_with_number_only__shows_verified___when_not_verified__positive_verifies() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.dropbitAccountHelper.isPhoneVerified).thenReturn(false)

            activity.phoneButton.performClick()

            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.your_phone_is_not_verified))
            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text).isEqualTo(activity.getString(R.string.not_now))
            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).text).isEqualTo(activity.getString(R.string.verify_now))

            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick())
            assertThat(activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag)).isNull()

            verify(activity.activityNavigationUtil).navigateToUserVerification(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun picked_from_contact_source_with_number_only__shows_verified___when_not_verified__negative_dismisses() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.dropbitAccountHelper.isPhoneVerified).thenReturn(false)

            activity.phoneButton.performClick()

            val dialog = activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.your_phone_is_not_verified))
            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).text).isEqualTo(activity.getString(R.string.not_now))
            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).text).isEqualTo(activity.getString(R.string.verify_now))

            assertThat(dialog.alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick())
            assertThat(activity.supportFragmentManager.findFragmentByTag(CreatePaymentActivity.errorDialogTag)).isNull()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // INTEGRATION TESTS

    @Test
    fun does_not_clear_amount_after_paste_from_same_account() {
        val scenario = createScenario()
        val bitcoinUri: BitcoinUri = mock()
        whenever(bitcoinUri.address).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")

        scenario.onActivity { activity ->
            activity.changeAccountMode(AccountMode.BLOCKCHAIN)
            activity.amountInputView.primaryCurrency.setText("$10.00")

            assertThat(activity.paymentHolder.fiat.toLong()).isEqualTo(10_00)
            assertThat(activity.paymentHolder.paymentAddress).isEmpty()

            activity.validRawInputObserver.onChanged(bitcoinUri)

            assertThat(activity.paymentHolder.fiat.toLong()).isEqualTo(10_00)
            assertThat(activity.paymentHolder.paymentAddress).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Module
    class CreatePaymentActivityTestModule {
        @Provides
        fun rawInputViewModel(): RawInputViewModel {
            val viewModel: RawInputViewModel = mock()
            whenever(viewModel.validRawInput).thenReturn(mock())
            whenever(viewModel.invalidRawInput).thenReturn(mock())
            whenever(viewModel.validRequestInvoice).thenReturn(mock())
            return viewModel

        }

        @Provides
        fun fundingViewModelProvider(): FundingViewModelProvider {
            val provider = mock<FundingViewModelProvider>()
            val viewModel: FundingViewModel = mock()
            whenever(provider.provide(any())).thenReturn(viewModel)
            whenever(viewModel.transactionData).thenReturn(mock())
            whenever(viewModel.addressLookupResult).thenReturn(mock())
            whenever(viewModel.pendingLedgerInvoice).thenReturn(mock())
            return provider
        }

    }

}