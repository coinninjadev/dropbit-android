package com.coinninja.coinkeeper.ui.payment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.ui.payment.request.RequestDialogFragment
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.util.PaymentUtil
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import com.coinninja.coinkeeper.util.crypto.uri.UriException
import com.coinninja.coinkeeper.view.fragment.ConfirmPayDialogFragment
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder
import com.coinninja.coinkeeper.view.widget.PaymentBarView
import javax.inject.Inject

class PaymentBarFragment : BaseFragment(), PaymentBarCallbacks {

    @Inject
    internal lateinit var feesManager: FeesManager

    @Inject
    internal lateinit var localBroadcastUtil: LocalBroadCastUtil

    @Inject
    internal lateinit var activityNavigationUtil: ActivityNavigationUtil

    @Inject
    internal lateinit var paymentUtil: PaymentUtil

    @Inject
    internal lateinit var bitcoinUtil: BitcoinUtil

    @Inject
    internal lateinit var walletHelper: WalletHelper

    @Inject
    internal lateinit var currencyPreference: CurrencyPreference

    internal val paymentHolder: PaymentHolder = PaymentHolder()

    internal val paymentBarView: PaymentBarView? get() = findViewById(R.id.payment_bar)
    internal val payDialogFragment: PayDialogFragment? get() = childFragmentManager.findFragmentByTag(PayDialogFragment::class.java.simpleName) as PayDialogFragment?

    internal var intentFilter = IntentFilter(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE)

    internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (DropbitIntents.ACTION_WALLET_SYNC_COMPLETE == intent.action) {
                paymentHolder.spendableBalance = walletHelper.spendableBalance
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment_bar, container, false)
    }

    override fun onStart() {
        super.onStart()
        localBroadcastUtil.registerReceiver(receiver, intentFilter)
        paymentHolder.spendableBalance = walletHelper.spendableBalance
        creationIntent.data?.let { uri ->
            launchPayScreenWithBitcoinUriIfNecessary(uri)
        }

    }

    override fun onResume() {
        super.onResume()
        paymentBarView?.apply {
            setOnRequestPressedObserver { this@PaymentBarFragment.onRequestButtonPressed() }
            setOnSendPressedObserver { this@PaymentBarFragment.showPayDialogWithDefault() }
            setOnScanPressedObserver { this@PaymentBarFragment.onQrScanPressed() }
        }
    }

    override fun onStop() {
        super.onStop()
        localBroadcastUtil.unregisterReceiver(receiver)
    }

    override fun onQrScanPressed() {
        showPayDialog(true)
    }

    override fun confirmPaymentFor(paymentUtil: PaymentUtil) {
        dismissPayDialog()
        val confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(paymentUtil, this)
        confirmPayDialogFragment.isCancelable = false
        activityNavigationUtil.showDialogWithTag(childFragmentManager, confirmPayDialogFragment, ConfirmPayDialogFragment::class.java.simpleName)
    }

    override fun confirmPaymentFor(paymentUtil: PaymentUtil, identity: Identity) {
        dismissPayDialog()
        val confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(identity, paymentUtil, this)
        confirmPayDialogFragment.isCancelable = false
        activityNavigationUtil.showDialogWithTag(childFragmentManager, confirmPayDialogFragment, ConfirmPayDialogFragment::class.java.simpleName)
    }


    override fun confirmInvite(paymentUtil: PaymentUtil, identity: Identity) {
        dismissPayDialog()
        val confirmPayDialogFragment = ConfirmPayDialogFragment.newInstance(identity, paymentUtil, this)
        confirmPayDialogFragment.isCancelable = false
        activityNavigationUtil.showDialogWithTag(childFragmentManager, confirmPayDialogFragment, ConfirmPayDialogFragment::class.java.simpleName)
    }

    override fun cancelPayment(dialogFragment: DialogFragment) {
        dialogFragment.dismiss()
        currencyPreference.reset()
        paymentHolder.defaultCurrencies = currencyPreference.currenciesPreference
        paymentUtil.setAddress(null)
        paymentHolder.clearPayment()
        paymentUtil.clearFunding()
    }

    private fun showPayDialogWithBitcoinUri(uri: BitcoinUri) {
        showPayDialog(uri)
    }

    internal fun showPayDialogWithDefault() {
        showPayDialog(false)
    }

    private fun launchPayScreenWithBitcoinUriIfNecessary(uri: Uri) {
        try {
            val bitcoinUri = bitcoinUtil.parse(uri.toString())
            showPayDialogWithBitcoinUri(bitcoinUri)
            creationIntent.data = null
        } catch (e: UriException) {
            AlertDialogBuilder.build(context, "Invalid bitcoin request received. Please try again").show()
        }
    }

    private fun onRequestButtonPressed() {
        val requestDialog = RequestDialogFragment()
        activityNavigationUtil.showDialogWithTag(childFragmentManager, requestDialog, RequestDialogFragment::class.java.simpleName)
    }


    private fun dismissPayDialog() {
        payDialogFragment?.dismiss()
    }

    private fun showPayDialog(shouldShowScan: Boolean) {
        resetPaymentUtilForPayDialogFragment()
        val payDialog = PayDialogFragment.newInstance(paymentUtil, this, shouldShowScan)
        activityNavigationUtil.showDialogWithTag(childFragmentManager, payDialog, PayDialogFragment::class.java.simpleName)
    }

    private fun showPayDialog(bitcoinUri: BitcoinUri) {
        resetPaymentUtilForPayDialogFragment()
        val payDialog = PayDialogFragment.newInstance(paymentUtil, this, bitcoinUri)
        activityNavigationUtil.showDialogWithTag(childFragmentManager, payDialog, PayDialogFragment::class.java.simpleName)
    }

    private fun resetPaymentUtilForPayDialogFragment() {
        currencyPreference.reset()
        paymentHolder.defaultCurrencies = currencyPreference.currenciesPreference
        paymentHolder.evaluationCurrency = walletHelper.latestPrice
        paymentHolder.spendableBalance = walletHelper.spendableBalance
        paymentUtil.setFee(feesManager.currentFee())
        paymentUtil.paymentHolder = paymentHolder
        paymentHolder.clearPayment()
    }
}
