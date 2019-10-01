package com.coinninja.coinkeeper.ui.payment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeChangeObserver
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder
import com.coinninja.coinkeeper.view.widget.PaymentBarView
import javax.inject.Inject

class PaymentBarFragment : BaseFragment() {


    @Inject
    internal lateinit var activityNavigationUtil: ActivityNavigationUtil

    @Inject
    lateinit var bitcoinUriBuilder: BitcoinUri.Builder

    @Inject
    internal lateinit var accountModeManager: AccountModeManager

    internal val accountModeChangeObserver = object : AccountModeChangeObserver {
        override fun onAccountModeChanged(accountMode: AccountMode) {
            view?.let {
                (it as PaymentBarView).accountMode = accountMode
            }
        }
    }

    internal val sendButton: View get() = view?.findViewById(R.id.send_btn)!!
    internal val requestButton: View get() = view?.findViewById(R.id.request_btn)!!
    internal val scanButton: View get() = view?.findViewById(R.id.scan_btn)!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment_bar, container)
    }

    override fun onStart() {
        super.onStart()
        creationIntent.data?.let { uri ->
            launchPayScreenWithBitcoinUriIfNecessary(uri)
        }

    }

    override fun onResume() {
        super.onResume()
        accountModeManager.observeChanges(accountModeChangeObserver)
        accountModeChangeObserver.onAccountModeChanged(accountModeManager.accountMode)
        view?.also {
            (it as PaymentBarView).apply {

                it.setOnRequestPressedObserver(object : PaymentBarView.OnRequestPressedObserver {
                    override fun onRequestPressed() {
                        this@PaymentBarFragment.onRequestButtonPressed()
                    }
                })
                it.setOnSendPressedObserver(object : PaymentBarView.OnSendPressedObserver {
                    override fun onSendPressed() {
                        this@PaymentBarFragment.onSendPressed()
                    }
                })
                it.setOnScanPressedObserver(object : PaymentBarView.OnScanPressedObserver {
                    override fun onScanPressed() {
                        this@PaymentBarFragment.onQrScanPressed()
                    }

                })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        accountModeManager.removeObserver(accountModeChangeObserver)
    }

    private fun onQrScanPressed() {
        activity?.let {
            activityNavigationUtil.navigateToPaymentCreateScreen(it, withScan = true)
        }
    }

    private fun onSendPressed() {
        activity?.let {
            activityNavigationUtil.navigateToPaymentCreateScreen(it)
        }
    }

    private fun launchPayScreenWithBitcoinUriIfNecessary(uri: Uri) {
        val bitcoinUri = bitcoinUriBuilder.parse(uri.toString())
        creationIntent.data = null
        if (bitcoinUri.isValidPaymentAddress) {
            activity?.let {
                activityNavigationUtil.navigateToPaymentCreateScreen(it, bitcoinUri = bitcoinUri)
            }
        } else {
            AlertDialogBuilder.build(context, "Invalid bitcoin request received. Please try again").show()
        }
    }

    private fun onRequestButtonPressed() {
        activity?.let {
            activityNavigationUtil.navigateToPaymentRequestScreen(it)
        }
    }
}
