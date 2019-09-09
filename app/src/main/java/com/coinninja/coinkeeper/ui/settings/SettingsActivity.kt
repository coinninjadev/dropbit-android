package com.coinninja.coinkeeper.ui.settings

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import androidx.lifecycle.Observer
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.service.YearlyHighViewModel
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder
import com.coinninja.coinkeeper.util.uri.UriUtil
import com.coinninja.coinkeeper.util.uri.routes.DropbitRoute
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity
import com.coinninja.coinkeeper.view.activity.LicensesActivity
import com.coinninja.coinkeeper.view.activity.SplashActivity
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import javax.inject.Inject

class SettingsActivity : BaseActivity(), DialogInterface.OnClickListener {

    @Inject
    lateinit var dustProtectionPreference: DustProtectionPreference

    @Inject
    lateinit var deleteWalletPresenter: DeleteWalletPresenter

    @Inject
    lateinit var dropbitUriBuilder: DropbitUriBuilder

    @Inject
    lateinit var yearlyHighViewModel: YearlyHighViewModel

    val yearlyHighObserver = Observer<Boolean> { isSubscribed ->
        onYearlyHighSubscriptionChanged(isSubscribed)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE && supportFragmentManager.findFragmentByTag(TAG_CONFIRM_DELETE_WALLET) != null) {
            authorizeDelete()
        }

        dialog.dismiss()
    }

    internal fun onDeleted() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == DELETE_WALLET_REQUEST_CODE && resultCode == AuthorizedActionActivity.RESULT_AUTHORIZED) {
            deleteWalletPresenter.onDelete()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        setupRecoverWallet()
        setupDeleteWallet()
        setupLicenses()
        setupDustProtection()
        setupYearlyHighSubscription()
        setupAdjustableFees()
    }

    private fun setupAdjustableFees() {
        findViewById<Button>(R.id.adjustable_fees).setOnClickListener { adjustableFeesClicked() }
    }

    private fun adjustableFeesClicked() {
        startActivity(Intent(this, AdjustableFeesActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onPause() {
        super.onPause()
        (withId<View>(this, R.id.dust_protection_toggle) as Switch).setOnCheckedChangeListener(null)
    }

    internal fun authorizeDelete() {
        val authIntent = Intent(this, AuthorizedActionActivity::class.java)
        val authMessage = getString(R.string.authorize_delete_message)
        authIntent.putExtra(DropbitIntents.EXTRA_AUTHORIZED_ACTION_MESSAGE, authMessage)
        startActivityForResult(authIntent, DELETE_WALLET_REQUEST_CODE)
    }

    private fun setupDustProtection() {
        val toolTip = findViewById<View>(R.id.dust_protection_tooltip)
        toolTip.setOnClickListener { this.onDustProtectionTooltipPressed() }
        val view = findViewById<Switch>(R.id.dust_protection_toggle)
        view.isChecked = dustProtectionPreference.isDustProtectionEnabled
        view.setOnCheckedChangeListener { compoundButton, value ->
            this.onToggleDustProtection(compoundButton, value)
        }
    }

    private fun onDustProtectionTooltipPressed() {
        UriUtil.openUrl(dropbitUriBuilder.build(DropbitRoute.DUST_PROTECTION), this)
    }

    private fun onToggleDustProtection(compoundButton: CompoundButton, value: Boolean) {
        dustProtectionPreference.setProtection(value)
    }

    private fun onRecoverWalletClicked() {
        val intent = Intent(this, BackupRecoveryWordsStartActivity::class.java)
        startActivity(intent)
    }

    private fun deleteWallet(): Boolean {
        GenericAlertDialog.newInstance(null,
                getString(R.string.delete_wallet_dialog_text),
                getString(R.string.delete_wallet_positive),
                getString(R.string.delete_wallet_negative),
                this,
                false,
                false).show(supportFragmentManager, TAG_CONFIRM_DELETE_WALLET)
        return true
    }

    private fun setupLicenses() {
        findViewById<View>(R.id.open_source).setOnClickListener { v -> onLicenseClicked() }
    }

    private fun onLicenseClicked() {
        startActivity(Intent(this, LicensesActivity::class.java))
    }

    private fun setupRecoverWallet() {
        if (cnWalletManager.hasSkippedBackup()) {
            findViewById<View>(R.id.not_backed_up_message).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.not_backed_up_message).visibility = View.GONE
        }
        findViewById<View>(R.id.recover_wallet).setOnClickListener { onRecoverWalletClicked() }
    }

    private fun setupDeleteWallet() {
        findViewById<View>(R.id.delete_wallet).setOnClickListener { deleteWallet() }
        deleteWalletPresenter.setCallback { this.onDeleted() }
    }

    private fun onYearlyHighSubscriptionChanged(isSubscribed: Boolean) {
        val switch = findViewById<Switch>(R.id.yearly_high_subscription)
        if (switch.isChecked != isSubscribed) {
            switch.isChecked = isSubscribed
        }
        switch.setOnCheckedChangeListener { button, value ->
            yearlyHighViewModel.toggleSubscription(isSubscribed)
        }
    }

    private fun setupYearlyHighSubscription() {
        yearlyHighViewModel.isSubscribedToYearlyHigh.observe(this, yearlyHighObserver)
    }

    companion object {
        val TAG_CONFIRM_DELETE_WALLET = "tag_confirm_delete_wallet"
        val DELETE_WALLET_REQUEST_CODE = 12
    }
}
