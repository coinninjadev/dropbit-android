package com.coinninja.coinkeeper.util.android.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import app.dropbit.annotations.Mockable
import com.coinninja.android.helpers.Resources
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.ui.account.verify.UserAccountVerificationActivity
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.ui.market.MarketScreenActivity
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.ui.settings.SettingsActivity
import com.coinninja.coinkeeper.ui.spending.BuyBitcoinActivity
import com.coinninja.coinkeeper.ui.spending.SpendBitcoinActivity
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.TwitterUtil
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.uri.CoinNinjaUriBuilder
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder
import com.coinninja.coinkeeper.util.uri.UriUtil
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.LATITUDE
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.LONGITUDE
import com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.*
import com.coinninja.coinkeeper.view.activity.CoinKeeperSupportActivity
import com.coinninja.coinkeeper.view.activity.TrainingActivity
import com.coinninja.coinkeeper.view.activity.VerifyPhoneVerificationCodeActivity
import com.coinninja.coinkeeper.view.activity.VerifyRecoverywordsActivity
import java.util.*
import javax.inject.Inject

@Mockable
class ActivityNavigationUtil @Inject constructor(
        internal val dropbitUriBuilder: DropbitUriBuilder,
        internal val coinNinjaUriBuilder: CoinNinjaUriBuilder,
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
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
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

    fun showDialogWithTag(fragmentManager: FragmentManager, dialogFragment: DialogFragment, tag: String) {
        dialogFragment.show(fragmentManager, tag)
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
}
