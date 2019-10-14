package com.coinninja.coinkeeper.ui.segwit

import android.content.DialogInterface
import android.os.Bundle
import android.widget.CheckBox
import androidx.lifecycle.Observer
import app.coinninja.cn.libbitcoin.model.TransactionData
import com.coinninja.android.helpers.check
import com.coinninja.android.helpers.uncheck
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.bitcoin.isFunded
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import javax.inject.Inject

class PerformSegwitUpgradeActivity : BaseActivity() {

    @Inject
    lateinit var walletUpgradeModelProvider: WalletUpgradeModelProvider

    lateinit var walletUpgradeViewModel: WalletUpgradeViewModel

    var state: UpgradeState = UpgradeState.NotStarted
    var transactionData: TransactionData? = null

    val stepOneCheckBox: CheckBox get() = findViewById(R.id.step_create_wallet)
    val stepTwoCheckBox: CheckBox get() = findViewById(R.id.step_update_to_segwit)
    val stepThreeCheckBox: CheckBox get() = findViewById(R.id.step_transfer)

    val onStatChangeObserver: Observer<UpgradeState> = Observer {
        state = it
        when (it) {
            UpgradeState.StepOneCompleted -> onStepOneCompleted()
            UpgradeState.StepTwoCompleted -> onStepTwoCompleted()
            UpgradeState.StepThreeCompleted -> onStepThreeCompleted()
            UpgradeState.Finished -> onUpgradeComplete()
            UpgradeState.Started -> onStarted()
            UpgradeState.Error -> onError()
            else -> onNotStarted()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrade_to_segwit_step_2)

        if (intent.hasExtra(DropbitIntents.EXTRA_TRANSACTION_DATA))
            transactionData = intent.getParcelableExtra(DropbitIntents.EXTRA_TRANSACTION_DATA)
    }

    override fun onStart() {
        super.onStart()
        walletUpgradeViewModel = walletUpgradeModelProvider.provide(this)
        walletUpgradeViewModel.upgradeState.observe(this, onStatChangeObserver)
        onStatChangeObserver.onChanged(state)
    }

    override fun onStop() {
        super.onStop()
        walletUpgradeViewModel.upgradeState.removeObserver(onStatChangeObserver)
    }

    private fun onNotStarted() {
        onStatChangeObserver.onChanged(UpgradeState.Started)
    }

    private fun onStarted() {
        uncheck(stepOneCheckBox, stepTwoCheckBox, stepThreeCheckBox)
        analytics.setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_STARTED, true)
        analytics.setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_COMPLETED, false)
        analytics.trackEvent(Analytics.EVENT_LIGHTNING_UPGRADE_STARTED)
        walletUpgradeViewModel.performStepOne()
    }

    private fun onStepOneCompleted() {
        check(stepOneCheckBox)
        analytics.trackEvent(Analytics.EVENT_LIGHTNING_UPGRADE_STEP_ONE_COMPLETE)
        walletUpgradeViewModel.performStepTwo()
    }

    private fun onStepTwoCompleted() {
        check(stepOneCheckBox, stepTwoCheckBox)
        analytics.trackEvent(Analytics.EVENT_LIGHTNING_UPGRADE_STEP_TWO_COMPLETE)
        analytics.setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_WITH_FUNDS, transactionData?.isFunded()
                ?: false)
        walletUpgradeViewModel.performStepThree(transactionData)
    }

    private fun onStepThreeCompleted() {
        check(stepOneCheckBox, stepTwoCheckBox, stepThreeCheckBox)
        if (transactionData?.isFunded() == true) {
            analytics.trackEvent(Analytics.EVENT_LIGHTNING_UPGRADE_STEP_THREE_COMPLETE)
        }
        walletUpgradeViewModel.cleanUp()
    }

    private fun onUpgradeComplete() {
        analytics.setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_COMPLETED, true)
        analytics.trackEvent(Analytics.EVENT_LIGHTNING_UPGRADE_COMPLETED)
        activityNavigationUtil.navigateToUpgradeToSegwitSuccess(this)
    }

    private fun onError() {
        val onClickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialog, which ->
            dialog?.dismiss()
            finish()
        }
        GenericAlertDialog.newInstance(
                getString(R.string.upgrade_failed_message),
                getString(R.string.ok),
                onClickListener
        ).show(supportFragmentManager, errorDialogTag)
    }

    private fun check(vararg checkBoxes: CheckBox) {
        checkBoxes.forEach {
            it.check()
        }
    }

    private fun uncheck(vararg checkBoxes: CheckBox) {
        checkBoxes.forEach {
            it.uncheck()
        }
    }

    companion object {
        const val errorDialogTag = "ERROR_DIALOG"
    }
}
