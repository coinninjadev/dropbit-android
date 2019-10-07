package com.coinninja.coinkeeper.util.android.activity

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.thunderdome.model.RequestInvoice
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.Resources
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.ui.lightning.broadcast.BroadcastLightningPaymentActivity
import com.coinninja.coinkeeper.ui.lightning.deposit.LightningDepositActivity
import com.coinninja.coinkeeper.ui.lightning.loading.LightningLoadingOptionsDialog
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalActivity
import com.coinninja.coinkeeper.ui.lightning.withdrawal.LightningWithdrawalBroadcastActivity
import com.coinninja.coinkeeper.ui.market.MarketScreenActivity
import com.coinninja.coinkeeper.ui.payment.confirm.ConfirmPaymentActivity
import com.coinninja.coinkeeper.ui.payment.create.CreatePaymentActivity
import com.coinninja.coinkeeper.ui.payment.invite.InviteContactActivity
import com.coinninja.coinkeeper.ui.payment.request.LndInvoiceRequest
import com.coinninja.coinkeeper.ui.payment.request.LndInvoiceRequestActivity
import com.coinninja.coinkeeper.ui.payment.request.PayRequestActivity
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.ui.segwit.PerformSegwitUpgradeActivity
import com.coinninja.coinkeeper.ui.segwit.UpgradeToSegwitActivity
import com.coinninja.coinkeeper.ui.segwit.UpgradeToSegwitCompleteActivity
import com.coinninja.coinkeeper.ui.settings.SettingsActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.Shuffler
import com.coinninja.coinkeeper.util.TwitterUtil
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.uri.CoinNinjaUriBuilder
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter
import com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.TRANSACTION
import com.coinninja.coinkeeper.view.activity.*
import com.coinninja.matchers.ActivityMatchers.activityWithIntentStarted
import com.google.i18n.phonenumbers.Phonenumber
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNotNull
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.Shadows
import java.util.*

@RunWith(AndroidJUnit4::class)
class ActivityNavigationUtilTest {
    private val scenario: ActivityScenario<StartActivity> = ActivityScenario.launch(StartActivity::class.java)
    private val activity: AppCompatActivity
        get() {
            var activity: Activity? = null
            scenario.onActivity { a -> activity = a }
            return activity as AppCompatActivity
        }

    private fun createActivityNavigationUtil(): ActivityNavigationUtil {
        val coinNinjaUriBuilder = CoinNinjaUriBuilder()
        val dropbitUriBuilder = DropbitUriBuilder()
        val twitterUtil = TwitterUtil(ApplicationProvider.getApplicationContext(), Shuffler())
        return ActivityNavigationUtil(dropbitUriBuilder, coinNinjaUriBuilder, Mockito.mock(Analytics::class.java), twitterUtil)
    }

    @After
    fun tearDown() {
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun navigates_to_verify_phone_number() {
        val activityNavigationUtil = createActivityNavigationUtil()
        activityNavigationUtil.navigateToRegisterPhone(activity)

        val intent = Intent(activity, VerificationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun navigate_to_BackupRecoveryWordsStartActivity() {
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToBackupRecoveryWords(activity)

        val intent = Intent(activity, BackupRecoveryWordsStartActivity::class.java)
        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun navigates_to_home() {
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToHome(activity)

        val intent = Intent(activity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun navigate_to_SettingsActivity() {
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToSettings(activity)

        val intent = Intent(activity, SettingsActivity::class.java)
        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun navigate_to_CoinKeeperSupportActivity() {
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToSupport(activity)

        val intent = Intent(activity, CoinKeeperSupportActivity::class.java)
        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun navigate_to_verify_recovery_words_with_view_state() {
        val words = arrayOf("WORD1", "WORD2", "WORD3", "WORD4", "WORD5", "WORD6", "WORD7", "WORD8", "WORD9", "WORD10", "WORD11", "WORD12")
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToVerifyRecoveryWords(activity, words, DropbitIntents.EXTRA_BACKUP)

        val intent = Intent(activity, VerifyRecoverywordsActivity::class.java)
        intent.putExtra(VerifyRecoverywordsActivity.DATA_RECOVERY_WORDS, words)
        intent.putExtra(DropbitIntents.EXTRA_VIEW_STATE, DropbitIntents.EXTRA_BACKUP)
        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun shares_transaction_with_other_applications() {
        val activityNavigationUtil = createActivityNavigationUtil()
        val txid = "--txid--"
        val uri = activityNavigationUtil.coinNinjaUriBuilder.build(TRANSACTION, txid)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString())
        intent.addFlags(1)
        val chooser = Intent.createChooser(intent, Resources.getString(activity, R.string.share_transaction_intent_title))
        activityNavigationUtil.shareTransaction(activity, txid)

        assertThat(activity, activityWithIntentStarted(chooser))
    }

    @Test
    fun opens_transaction_id_on_block_explorer() {
        val txid = "--txid--"
        val uri = Uri.parse("https://coinninja.com/tx/--txid--")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.showTxidOnBlock(activity, txid)

        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun opens_address_on_block_explorer() {
        val address = "--address--"
        val uri = Uri.parse("https://coinninja.com/address/--address--")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.showAddressOnBlock(activity, address)

        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun explains_shared_memos() {
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.explainSharedMemos(activity)

        val intent = Intent(Intent.ACTION_VIEW, DropbitIntents.URI_SHARED_MEMOS)
        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun navigates_to_verify_phone_number_code() {
        val phoneNumber = Phonenumber.PhoneNumber()
        phoneNumber.nationalNumber = 3305555555L
        phoneNumber.countryCode = 1
        val number = PhoneNumber(phoneNumber)
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToVerifyPhoneNumberCode(activity, number)

        val intent = Intent(activity, VerifyPhoneVerificationCodeActivity::class.java)
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, number)
        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun navigates_to_buy_bitcoin_with_credit_card() {
        val uri = Uri.parse("https://coinninja.com/buybitcoin/creditcards")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToBuyBitcoinWithCreditCard(activity)

        assertThat(activity, activityWithIntentStarted(intent))
        verify(activityNavigationUtil.analytics).trackEvent(Analytics.EVENT_BUY_BITCOIN_CREDIT_CARD)
    }

    @Test
    fun navigates_to_buy_bitcoin_with_gift_card() {
        val uri = Uri.parse("https://coinninja.com/buybitcoin/giftcards")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToBuyBitcoinWithGiftCard(activity)

        assertThat(activity, activityWithIntentStarted(intent))
        verify(activityNavigationUtil.analytics).trackEvent(Analytics.EVENT_BUY_BITCOIN_GIFT_CARD)
    }

    @Test
    fun navigates_to_where_to_spend() {
        val uri = Uri.parse("https://coinninja.com/news/webview/load-online")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToWhereToSpend(activity)

        assertThat(activity, activityWithIntentStarted(intent))
        verify(activityNavigationUtil.analytics).trackEvent(Analytics.EVENT_SPEND_ONLINE)
    }

    @Test
    fun navigates_to_buy_gift_card() {
        val uri = Uri.parse("https://coinninja.com/spendbitcoin/giftcards")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigateToBuyGiftCard(activity)

        assertThat(activity, activityWithIntentStarted(intent))
        verify(activityNavigationUtil.analytics).trackEvent(Analytics.EVENT_SPEND_GIFT_CARDS)
    }

    @Test
    fun navigate_to_around_me() {
        val parameters = HashMap<CoinNinjaParameter, String>()
        parameters[CoinNinjaParameter.TYPE] = "atm"
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = 87.0
        location.longitude = 25.0
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigatesToMapWith(activity, parameters, location, Analytics.EVENT_BUY_BITCOIN_AT_ATM)

        val uri = Uri.parse("https://coinninja.com/news/webview/load-map?lat=87.0&long=25.0&type=atm")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        assertThat(activity, activityWithIntentStarted(intent))
        verify(activityNavigationUtil.analytics, times(1)).trackEvent(Analytics.EVENT_BUY_BITCOIN_AT_ATM)
    }

    @Test
    fun navigate_to_around_me__without_location() {
        val parameters = HashMap<CoinNinjaParameter, String>()
        parameters[CoinNinjaParameter.TYPE] = "atm"
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigatesToMapWith(activity, parameters, null, Analytics.EVENT_BUY_BITCOIN_AT_ATM)

        val uri = Uri.parse("https://coinninja.com/news/webview/load-map?type=atm")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        assertThat(activity, activityWithIntentStarted(intent))
        verify(activityNavigationUtil.analytics, times(1)).trackEvent(Analytics.EVENT_BUY_BITCOIN_AT_ATM)
    }

    @Test
    fun navigate_to_around_me__without_location_and_parameters() {
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.navigatesToMapWith(activity, null, null, Analytics.EVENT_BUY_BITCOIN_AT_ATM)

        val uri = Uri.parse("https://coinninja.com/news/webview/load-map")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        assertThat(activity, activityWithIntentStarted(intent))
        verify(activityNavigationUtil.analytics, times(1)).trackEvent(Analytics.EVENT_BUY_BITCOIN_AT_ATM)
    }

    @Test
    fun can_share_with_twitter() {
        val tweet = "pay me #bitcoin"
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_TEXT, tweet)
        intent.action = Intent.ACTION_VIEW
        val uri = Uri.parse("https://twitter.com/intent/tweet")
                .buildUpon()
                .appendQueryParameter("text", tweet)
                .build()
        intent.data = uri
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.shareWithTwitter(activity, tweet)

        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun navigates_to_learn_more_about_dropbit_dot_me() {
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.learnMoreAboutDropbitMe(activity)
        val uri = Uri.parse("https://dropbit.me")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun `shows transaction details for given txid`() {
        val txid = "--txid--"
        val activityNavigationUtil = createActivityNavigationUtil()
        val intent = Intent()
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(DropbitIntents.EXTRA_TRANSACTION_ID, txid)

        activityNavigationUtil.showTransactionDetail(activity, txid = txid)

        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun `shows transaction details for given transaction invite summary id`() {
        val recordId = 4L
        val activityNavigationUtil = createActivityNavigationUtil()
        val intent = Intent()
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(DropbitIntents.EXTRA_TRANSACTION_RECORD_ID, recordId)

        activityNavigationUtil.showTransactionDetail(activity, transactionInviteSummaryID = recordId)

        assertThat(activity, activityWithIntentStarted(intent))
    }

    @Test
    fun `does nothing when no transaction id provide`() {
        val activityNavigationUtil = createActivityNavigationUtil()

        activityNavigationUtil.showTransactionDetail(activity)

        Assert.assertNull(Shadows.shadowOf(activity).peekNextStartedActivityForResult())
    }

    @Test
    fun opensUrl() {
        createActivityNavigationUtil().also {
            val uri = Uri.parse("http://www.example.com")

            it.openUrl(activity, uri)

            assertThat(activity, activityWithIntentStarted(Intent(Intent.ACTION_VIEW, uri)))
        }
    }

    @Test
    fun shows_market_charts() {
        createActivityNavigationUtil().also {
            it.showMarketCharts(activity)

            assertThat(activity, activityWithIntentStarted(Intent(activity, MarketScreenActivity::class.java)))
        }
    }

    @Test
    fun shows_lightning_deposit_screen_with_no_amount() {
        createActivityNavigationUtil().also {
            it.showLoadLightningWith(activity)

            assertThat(activity, activityWithIntentStarted(Intent(activity, LightningDepositActivity::class.java)))
        }
    }

    @Test
    fun shows_lightning_deposit_screen_with_amount() {
        createActivityNavigationUtil().also {
            val amount = USDCurrency(5_00)
            it.showLoadLightningWith(activity, amount)

            val intent = Intent(activity, LightningDepositActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_AMOUNT, amount)
            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun shows_withdrawal_lighting_screen() {
        createActivityNavigationUtil().also {
            it.showWithdrawalLightning(activity)

            val intent = Intent(activity, LightningWithdrawalActivity::class.java)
            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun shows_loading_lightning_options() {
        createActivityNavigationUtil().also {
            it.showLoadLightningOptions(activity)

            assertNotNull(activity.supportFragmentManager.findFragmentByTag(LightningLoadingOptionsDialog::class.java.simpleName))
        }
    }

    @Test
    fun navigates_to_broadcast() {
        createActivityNavigationUtil().also {
            val broadcastDTO = BroadcastTransactionDTO(TransactionData())

            it.navigateToBroadcast(activity, broadcastDTO)

            val intent = Intent(activity, BroadcastActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_BROADCAST_DTO, broadcastDTO)
            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun shows_withdrawal_completed_screen() {
        createActivityNavigationUtil().also {
            val withdrawalRequest = WithdrawalRequest(BTCCurrency(100000), BTCCurrency(500), BTCCurrency(5000))
            it.showWithdrawalCompleted(activity, withdrawalRequest)

            val intent = Intent(activity, LightningWithdrawalBroadcastActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_WITHDRAWAL_REQUEST, withdrawalRequest)
            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_start_screen() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, StartActivity::class.java)

            it.navigateToStartActivity(activity)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_upgrade_segwit() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, UpgradeToSegwitActivity::class.java)

            it.navigateToUpgradeToSegwit(activity)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_upgrade_segwit__step_two() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, PerformSegwitUpgradeActivity::class.java)

            it.navigateToUpgradeToSegwitStepTwo(activity, null)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_upgrade_segwit__step_two__with_transaction_transfer() {
        createActivityNavigationUtil().also {
            val transactionData: TransactionData = TransactionData(emptyArray(), 10_000, 1_000, 0,
                    DerivationPath(49, 0, 0, 0, 0), "--address--")
            val intent = Intent(activity, PerformSegwitUpgradeActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_TRANSACTION_DATA, transactionData)

            it.navigateToUpgradeToSegwitStepTwo(activity, transactionData)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_upgrade_to_segwit_success() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, UpgradeToSegwitCompleteActivity::class.java)

            it.navigateToUpgradeToSegwitSuccess(activity)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_verification_activity() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, VerificationActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON, true)

            it.showVerificationActivity(activity)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_restore_wallet_activity() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, RestoreWalletActivity::class.java)

            it.navigateToRestoreWallet(activity)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_payment_reqeust_screen() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, PayRequestActivity::class.java)

            it.navigateToPaymentRequestScreen(activity)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigate_to_review_created_invoice() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, LndInvoiceRequestActivity::class.java)
            val lndInvoiceRequest = LndInvoiceRequest("--lnd-encoded_invoice", BTCCurrency(10_000), "--memo--")
            intent.putExtra(DropbitIntents.EXTRA_LND_INVOICE_REQUEST, lndInvoiceRequest)

            it.navigateToShowLndInvoice(activity, lndInvoiceRequest)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_create_payment_transaction() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, CreatePaymentActivity::class.java)

            it.navigateToPaymentCreateScreen(activity)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_create_payment_transaction__for_scan_intent() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, CreatePaymentActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_SHOULD_SCAN, true)

            it.navigateToPaymentCreateScreen(activity, withScan = true)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_create_payment_transaction__with_bitcoin_uri_intent() {
        createActivityNavigationUtil().also {
            val uri = "bitcoin:--address--"
            val bitcoinUri = mock<BitcoinUri>()
            whenever(bitcoinUri.toString()).thenReturn(uri)
            val intent = Intent(activity, CreatePaymentActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_BITCOIN_URI, uri)

            it.navigateToPaymentCreateScreen(activity, bitcoinUri = bitcoinUri)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun picks_contact_from_contact_picker() {
        createActivityNavigationUtil().also {
            val intent = Intent(activity, PickUserActivity::class.java)
            intent.action = DropbitIntents.ACTION_TWITTER_SELECTION

            it.startPickContactActivity(activity, DropbitIntents.ACTION_TWITTER_SELECTION)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_confirm_payment() {
        createActivityNavigationUtil().also {
            val holder = PaymentHolder(
                    evaluationCurrency = USDCurrency(10_000_00),
                    isSharingMemo = true,
                    publicKey = "--pub-key--",
                    memo = "--memo--",
                    defaultCurrencies = DefaultCurrencies(BTCCurrency(1), USDCurrency(10_000_00)),
                    toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = true)
            )
            val intent = Intent(activity, ConfirmPaymentActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, holder)

            it.navigateToConfirmPaymentScreen(activity, holder)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_pay_lightning_invoice() {
        createActivityNavigationUtil().also {
            val holder = PaymentHolder(
                    evaluationCurrency = USDCurrency(10_000_00),
                    isSharingMemo = true,
                    publicKey = "--pub-key--",
                    memo = "--memo--",
                    defaultCurrencies = DefaultCurrencies(BTCCurrency(1), USDCurrency(10_000_00)),
                    toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = true)
            )
            holder.requestInvoice = RequestInvoice(numSatoshis = 10843)
            holder.requestInvoice!!.encoded = "ld-encoded"

            val intent = Intent(activity, BroadcastLightningPaymentActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, holder)

            it.navigateToLightningBroadcast(activity, holder)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }

    @Test
    fun navigates_to_invite_contact() {
        createActivityNavigationUtil().also {
            val holder = PaymentHolder(
                    evaluationCurrency = USDCurrency(10_000_00),
                    isSharingMemo = true,
                    publicKey = "--pub-key--",
                    memo = "--memo--",
                    defaultCurrencies = DefaultCurrencies(BTCCurrency(1), USDCurrency(10_000_00)),
                    toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = true)
            )

            val intent = Intent(activity, InviteContactActivity::class.java)
            intent.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, holder)

            it.navigateToInviteContactScreen(activity, holder)

            assertThat(activity, activityWithIntentStarted(intent))
        }
    }
}