package com.coinninja.coinkeeper.util.android.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.USDCurrency
import app.dropbit.commons.util.isNotNull
import com.coinninja.android.helpers.Resources
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.ui.account.verify.UserAccountVerificationActivity
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
import com.coinninja.coinkeeper.ui.spending.BuyBitcoinActivity
import com.coinninja.coinkeeper.ui.spending.SpendBitcoinActivity
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.TwitterUtil
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.uri.CoinNinjaUriBuilder
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder
import com.coinninja.coinkeeper.util.uri.UriUtil
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.LATITUDE
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.LONGITUDE
import com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.*
import com.coinninja.coinkeeper.view.activity.*
import java.util.*
import javax.inject.Inject

@Mockable
class ActivityNavigationUtil @Inject constructor(
        internal val dropbitUriBuilder: DropbitUriBuilder,
        internal val coinNinjaUriBuilder: CoinNinjaUriBuilder,
        internal val buyBitcoinUriBuilder: BuyBitcoinUriBuilder,
        internal val analytics: Analytics,
        internal val twitterUtil: TwitterUtil) {

    fun navigateToSettings(context: Context) {
        val intent = Intent(context, SettingsActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToUserVerification(context: Context) {
        val intent = Intent(context, UserAccountVerificationActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToSupport(context: Context) {
        val intent = Intent(context, CoinKeeperSupportActivity::class.java)
        context.startActivity(intent)
    }


    fun navigateToBackupRecoveryWords(context: Context) {
        context.startActivity(Intent(context, BackupRecoveryWordsStartActivity::class.java))
    }

    fun navigateToRegisterPhone(context: Context) {
        val intent = Intent(context, VerificationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun navigateToHome(context: Context) {
        val intent = Intent(context, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        context.startActivity(intent)
    }

    fun shareTransaction(context: Context, txid: String) {
        val uri = coinNinjaUriBuilder.build(TRANSACTION, txid)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString())
        val chooser = Intent.createChooser(intent, Resources.getString(context, R.string.share_transaction_intent_title))
        context.startActivity(chooser)
    }

    fun showAddressOnBlock(context: Context, address: String) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(ADDRESS, address), context as AppCompatActivity)
    }

    fun showTxidOnBlock(context: Context, txid: String) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(TRANSACTION, txid), context as AppCompatActivity)
    }

    fun explainSharedMemos(context: Context) {
        UriUtil.openUrl(DropbitIntents.URI_SHARED_MEMOS, context as AppCompatActivity)
    }

    fun navigateToVerifyPhoneNumberCode(context: Context, phoneNumber: PhoneNumber) {
        val intent = Intent(context, VerifyPhoneVerificationCodeActivity::class.java)
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, phoneNumber)
        context.startActivity(intent)
    }

    fun navigateToVerifyRecoveryWords(context: Context, seedWords: Array<String>, viewState: Int) {
        val intent = Intent(context, VerifyRecoverywordsActivity::class.java)
        intent.putExtra(VerifyRecoverywordsActivity.DATA_RECOVERY_WORDS, seedWords)
        intent.putExtra(DropbitIntents.EXTRA_VIEW_STATE, viewState)
        context.startActivity(intent)
    }

    fun navigateToBuyBitcoin(context: Context) {
        val intent = Intent(context, BuyBitcoinActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToLearnBitcoin(context: Context) {
        val intent = Intent(context, TrainingActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToSpendBitcoin(context: Context) {
        val intent = Intent(context, SpendBitcoinActivity::class.java)
        context.startActivity(intent)
    }

    fun navigateToBuyGiftCard(activity: Activity) {
        openUrl(activity, coinNinjaUriBuilder.build(SPEND_BITCOIN, "giftcards"))
        analytics.trackEvent(Analytics.EVENT_SPEND_GIFT_CARDS)
    }

    fun navigateToWhereToSpend(activity: Activity) {
        openUrl(activity, coinNinjaUriBuilder.build(NEWS, "webview", "load-online"))
        analytics.trackEvent(Analytics.EVENT_SPEND_ONLINE)
    }

    fun navigatesToMapWith(activity: Activity, parameters: HashMap<CoinNinjaParameter, String>?, location: Location?, analyticsEvent: String) {
        var parameters = parameters
        if (parameters == null)
            parameters = HashMap()

        if (location != null) {
            parameters[LATITUDE] = location.latitude.toString()
            parameters[LONGITUDE] = location.longitude.toString()
        }

        openUrl(activity, coinNinjaUriBuilder.build(NEWS, parameters, "webview", "load-map"))
        analytics.trackEvent(analyticsEvent)
    }

    fun navigateToBuyBitcoinWithCreditCard(activity: Activity) {
        openUrl(activity, coinNinjaUriBuilder.build(BUY_BITCOIN, "creditcards"))
        analytics.trackEvent(Analytics.EVENT_BUY_BITCOIN_CREDIT_CARD)
    }

    fun navigateToBuyBitcoinWithGiftCard(activity: Activity) {
        openUrl(activity, coinNinjaUriBuilder.build(BUY_BITCOIN, "giftcards"))
        analytics.trackEvent(Analytics.EVENT_BUY_BITCOIN_GIFT_CARD)
    }

    fun shareWithTwitter(activity: Activity, tweet: String) {
        activity.startActivity(twitterUtil.createTwitterIntent(activity, tweet))
    }

    fun learnMoreAboutDropbitMe(activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dropbit.me"))
        activity.startActivity(intent)
    }


    fun showTransactionDetail(activity: Activity, transactionInviteSummaryID: Long? = null, txid: String? = null) {
        if (transactionInviteSummaryID == null && txid.isNullOrEmpty()) return

        val intent = Intent(activity, TransactionDetailsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        txid?.let {
            intent.putExtra(DropbitIntents.EXTRA_TRANSACTION_ID, it)
        }

        transactionInviteSummaryID?.let {
            intent.putExtra(DropbitIntents.EXTRA_TRANSACTION_RECORD_ID, it)
        }

        activity.startActivity(intent)
    }


    fun openUrl(context: Context, uri: Uri) {
        Intent(Intent.ACTION_VIEW, uri).also {
            context.startActivity(it)
        }
    }

    fun showMarketCharts(activity: Activity) {
        Intent(activity, MarketScreenActivity::class.java).also { activity.startActivity(it) }
    }

    fun showLoadLightningWith(context: Context, usdCurrency: USDCurrency? = null) {
        Intent(context, LightningDepositActivity::class.java).also {
            usdCurrency?.let { amount ->
                it.putExtra(DropbitIntents.EXTRA_AMOUNT, amount)
            }
            context.startActivity(it)
        }
    }

    fun showWithdrawalLightning(context: Context) {
        Intent(context, LightningWithdrawalActivity::class.java).also {
            context.startActivity(it)
        }
    }

    fun navigateToBroadcast(activity: Activity, broadcastTransactionDTO: BroadcastTransactionDTO) {
        Intent(activity, BroadcastActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_BROADCAST_DTO, broadcastTransactionDTO)
            activity.startActivity(it)
        }
    }

    fun showLoadLightningOptions(context: AppCompatActivity) {
        context.supportFragmentManager.let {
            LightningLoadingOptionsDialog().show(it, LightningLoadingOptionsDialog::class.java.simpleName)
        }
    }

    fun showWithdrawalCompleted(activity: Activity, withdrawalRequest: WithdrawalRequest) {
        Intent(activity, LightningWithdrawalBroadcastActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_WITHDRAWAL_REQUEST, withdrawalRequest)
            activity.startActivity(it)
        }
    }

    fun navigateToStartActivity(activity: AppCompatActivity) {
        Intent(activity, StartActivity::class.java).also {
            val nullableView: View? = activity.findViewById<View>(R.id.img_logo)
            nullableView?.let { view ->
                val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(activity, view, activity.getString(R.string.logo_slide))
                activity.startActivity(it, options.toBundle())
            }

            if (nullableView == null)
                activity.startActivity(it)

        }
    }

    fun navigateToUpgradeToSegwit(activity: AppCompatActivity) {
        Intent(activity, UpgradeToSegwitActivity::class.java).also {
            activity.startActivity(it)
        }
    }

    fun navigateToUpgradeToSegwitStepTwo(activity: AppCompatActivity, transactionData: TransactionData? = null) {
        Intent(activity, PerformSegwitUpgradeActivity::class.java).also {
            transactionData?.let { data ->
                it.putExtra(DropbitIntents.EXTRA_TRANSACTION_DATA, data)
            }
            activity.startActivity(it)
        }
    }

    fun navigateToUpgradeToSegwitSuccess(activity: AppCompatActivity) {
        Intent(activity, UpgradeToSegwitCompleteActivity::class.java).also {
            activity.startActivity(it)
        }
    }

    fun showVerificationActivity(activity: AppCompatActivity) {
        Intent(activity, VerificationActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON, true)
            activity.startActivity(it)
        }
    }

    fun navigateToRestoreWallet(activity: AppCompatActivity) {
        Intent(activity, RestoreWalletActivity::class.java).also {
            activity.startActivity(it)
        }
    }

    fun navigateToPaymentRequestScreen(activity: Activity) {
        Intent(activity, PayRequestActivity::class.java).also {
            activity.startActivity(it)
        }
    }

    fun navigateToShowLndInvoice(activity: Activity, lndInvoiceRequest: LndInvoiceRequest) {
        Intent(activity, LndInvoiceRequestActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_LND_INVOICE_REQUEST, lndInvoiceRequest)
            activity.startActivity(it)
        }
    }

    @JvmOverloads
    fun navigateToPaymentCreateScreen(activity: Activity, withScan: Boolean = false, bitcoinUri: BitcoinUri? = null) {
        Intent(activity, CreatePaymentActivity::class.java).also {
            if (withScan) it.putExtra(DropbitIntents.EXTRA_SHOULD_SCAN, true)
            if (bitcoinUri.isNotNull()) it.putExtra(DropbitIntents.EXTRA_BITCOIN_URI, bitcoinUri.toString())
            activity.startActivity(it)
        }

    }

    fun startPickContactActivity(activity: Activity, action: String) {
        Intent(activity, PickUserActivity::class.java).also {
            it.action = action
            activity.startActivityForResult(it, DropbitIntents.REQUEST_PICK_CONTACT)
        }
    }

    fun navigateToConfirmPaymentScreen(activity: Activity, paymentHolder: PaymentHolder) {
        Intent(activity, ConfirmPaymentActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, paymentHolder)
            activity.startActivity(it)
        }
    }

    fun navigateToInviteSendScreen(activity: Activity, pendingInviteDTO: PendingInviteDTO) {
        Intent(activity, InviteSendActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_INVITE_DTO, pendingInviteDTO)
            activity.startActivity(it)
        }
    }

    fun navigateToLightningBroadcast(activity: Activity, paymentHolder: PaymentHolder) {
        Intent(activity, BroadcastLightningPaymentActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, paymentHolder)
            activity.startActivity(it)
        }
    }

    fun navigateToInviteContactScreen(activity: Activity, paymentHolder: PaymentHolder) {
        Intent(activity, InviteContactActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, paymentHolder)
            activity.startActivity(it)
        }
    }

    fun buyBitcoin(activity: Activity, address: String) {
        openUrl(activity, buyBitcoinUriBuilder.build(address))
    }
}
