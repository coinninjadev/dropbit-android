package com.coinninja.coinkeeper.view.activity

import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.coinninja.android.helpers.disable
import com.coinninja.android.helpers.enable
import com.coinninja.android.helpers.gone
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.R.*
import com.coinninja.coinkeeper.cn.wallet.service.CNServiceConnection
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import javax.inject.Inject

class RecoverWalletActivity : BaseActivity() {
    @Inject
    internal lateinit var bitcoinUtil: BitcoinUtil

    @Inject
    internal lateinit var cnServiceConnection: CNServiceConnection

    @Inject
    internal lateinit var serviceWorkUtil: ServiceWorkUtil

    internal val nextButton: Button get() = findViewById(id.ok)
    internal val icon: ImageView get() = findViewById(id.icon)
    internal val title: TextView get() = findViewById(id.title)
    internal val message: TextView get() = findViewById(id.message)
    internal val closeButton: View get() = findViewById(id.close)
    internal val intentFilter = IntentFilter().also {
        it.addAction(DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED)
        it.addAction(DropbitIntents.ACTION_SAVE_RECOVERY_WORDS)
        it.addAction(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS)
        it.addAction(DropbitIntents.ACTION_WALLET_ALREADY_UPGRADED)
        it.addAction(DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE)
        it.addAction(DropbitIntents.ACTION_WALLET_REQUIRES_UPGRADE)
    }

    internal var recoveryWords: Array<String> = emptyArray()
    internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            intent?.let {
                when {
                    DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED == it.action -> startSaveRecoveryWordsService()
                    DropbitIntents.ACTION_SAVE_RECOVERY_WORDS == it.action -> showSuccess()
                    DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS == it.action -> showFail()
                    DropbitIntents.ACTION_WALLET_REQUIRES_UPGRADE == it.action -> showUpgradeRequired()
                    DropbitIntents.ACTION_WALLET_ALREADY_UPGRADED == it.action -> showWalletAlreadyUpgraded()
                    DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE == it.action -> showWalletRegistrationComplete()
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_recover_wallet)
        if (intent.hasExtra(DropbitIntents.EXTRA_RECOVERY_WORDS)) {
            recoveryWords = intent.getStringArrayExtra(DropbitIntents.EXTRA_RECOVERY_WORDS)
                    ?: emptyArray()
        }
        serviceWorkUtil.bindToCNWalletService(cnServiceConnection)
        nextButton.disable()
    }

    override fun onResume() {
        super.onResume()
        if (!isValid(recoveryWords)) {
            showFail()
        } else {
            registerForLocalBroadcast()
            startSaveRecoveryWordsService()
        }
    }

    public override fun onPause() {
        super.onPause()
        unRegisterForLocalBroadcast()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unbindService(cnServiceConnection)
        } catch (e: Exception) {
        }
        cnServiceConnection.isBounded = false
    }

    internal fun registerForLocalBroadcast() {
        localBroadCastUtil.registerReceiver(receiver, intentFilter)
    }

    private fun startSaveRecoveryWordsService() {
        if (cnServiceConnection.isBounded) {
            val cnServices = cnServiceConnection.cnWalletServicesInterface
            cnServices?.saveSeedWords(recoveryWords)
        }
    }

    private fun unRegisterForLocalBroadcast() {
        localBroadCastUtil.unregisterReceiver(receiver)
    }

    internal fun showSuccess() {
        closeButton.gone()
        icon.setImageResource(drawable.ic_restore_success)
        icon.tag = drawable.ic_restore_success
        title.text = getText(string.recover_wallet_success_title)
        nextButton.background = resources.getDrawable(drawable.primary_button)
        message.setTextColor(resources.getColor(color.font_default))
        message.setText(string.recover_wallet_success_message)
        nextButton.setOnClickListener {
            activityNavigationUtil.showVerificationActivity(this)
            finish()
        }
        nextButton.setText(string.recover_wallet_success_button_text)
    }

    internal fun showFail() {
        title.text = getText(string.recover_wallet_error_title)
        icon.setImageResource(drawable.ic_restore_fail)
        icon.tag = drawable.ic_restore_fail
        nextButton.setText(string.recover_wallet_error_button_text)
        nextButton.background = resources.getDrawable(drawable.error_button)
        nextButton.setOnClickListener { v: View? -> navigateToRestore() }
        nextButton.enable()
        val close: View = findViewById(id.close)
        close.visibility = View.VISIBLE
        close.setOnClickListener { onClose() }
        message.setText(string.recover_wallet_error_message)
        message.setTextColor(resources.getColor(color.color_error))
    }

    private fun onClose() {
        activityNavigationUtil.navigateToStartActivity(this)
        finish()
    }


    private fun showWalletRegistrationComplete() {
        nextButton.enable()
    }

    private fun showWalletAlreadyUpgraded() {
        serviceWorkUtil.deleteWallet()
        val onclick = DialogInterface.OnClickListener { dialog, which ->
            activityNavigationUtil.navigateToStartActivity(this)
        }

        GenericAlertDialog.newInstance(
                getString(R.string.recovered_wallet_already_upgraded),
                getString(R.string.ok),
                onclick
        ).show(supportFragmentManager, dialogTag)
        analytics.trackEvent(Analytics.ENTERED_DEACTIVATED_WORDS)
    }

    private fun showUpgradeRequired() {
        val onclick = DialogInterface.OnClickListener { dialog, which ->
            activityNavigationUtil.navigateToUpgradeToSegwit(this)
            analytics.setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_FROM_RESTORE, true)
        }

        GenericAlertDialog.newInstance(
                getString(R.string.recovered_wallet_requires_upgrade),
                getString(R.string.ok),
                onclick
        ).show(supportFragmentManager, dialogTag)
    }

    private fun navigateToRestore() {
        activityNavigationUtil.navigateToRestoreWallet(this)
        finish()
    }

    private fun isValid(recoveryWords: Array<String>): Boolean {
        return bitcoinUtil.isValidBIP39Words(recoveryWords)
    }

    companion object {
        const val dialogTag: String = "DIALOG_TAG"
    }
}