package com.coinninja.coinkeeper.ui.segwit

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.commons.currency.toBTCCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.android.helpers.uncheck
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver
import javax.inject.Inject

class UpgradeToSegwitActivity : BaseActivity() {

    @Inject
    internal lateinit var syncWalletManager: SyncWalletManager

    @Inject
    internal lateinit var syncManagerViewNotifier: SyncManagerViewNotifier

    @Inject
    internal lateinit var walletHelper: WalletHelper

    @Inject
    lateinit var fundingViewModelProvider: FundingViewModelProvider

    @Inject
    lateinit var hdWalletWrapper: HDWalletWrapper

    lateinit var fundingViewModel: FundingViewModel

    var syncCount = 0
    val upgradeButton: Button get() = findViewById(R.id.upgrade_button)
    val transferPermission: CheckBox get() = findViewById(R.id.transfer_permission)
    val upgradePermission: CheckBox get() = findViewById(R.id.new_words_permission)
    val syncProgressView: ProgressBar get() = findViewById(R.id.sync_progress)
    private val hasBalance: Boolean get() = !walletHelper.balance.isZero
    private var transactionData: TransactionData? = null

    val allChecked: Boolean
        get() = when {
            hasBalance && transferPermission.isChecked && upgradePermission.isChecked -> true
            !hasBalance && upgradePermission.isChecked -> true
            else -> false
        }

    val syncChangeObserver: SyncManagerChangeObserver = SyncManagerChangeObserver {
        if (!syncManagerViewNotifier.isSyncing && syncCount == 0) {
            syncWalletManager.syncNow()
            syncCount += 1
        } else if (!syncManagerViewNotifier.isSyncing) {
            onSyncCompleted()
        }
    }

    val onCheckedListener: CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        upgradeButton.isEnabled = allChecked
    }

    val fundingObserver: Observer<TransactionData> = Observer {
        transactionData = it
        updateTransferRates()
        showPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrade_to_segwit_step_1)
    }

    override fun onResume() {
        super.onResume()
        syncWalletManager.cancel30SecondSync()
        fundingViewModel = fundingViewModelProvider.provide(this)
        fundingViewModel.transactionData.observe(this, fundingObserver)
        upgradeButton.apply {
            isEnabled = false
            setOnClickListener { onUpgradeClicked() }
        }
        transferPermission.uncheck()
        upgradePermission.uncheck()
        transferPermission.gone()
        upgradePermission.gone()
        syncProgressView.show()
        syncWalletManager.syncNow()
        upgradePermission.setOnCheckedChangeListener(onCheckedListener)
        transferPermission.setOnCheckedChangeListener(onCheckedListener)
        syncManagerViewNotifier.observeSyncManagerChange(syncChangeObserver)
    }

    override fun onPause() {
        super.onPause()
        fundingViewModel.transactionData.removeObserver(fundingObserver)
    }

    private fun onUpgradeClicked() {
        activityNavigationUtil.navigateToUpgradeToSegwitStepTwo(this, transactionData)
    }

    private fun onSyncCompleted() {
        if (hasBalance) fundTransfer() else showPermissions()
    }

    private fun fundTransfer() {
        val wallet = cnWalletManager.segwitWalletForUpgrade
        val path = DerivationPath(
                wallet.purpose,
                wallet.coinType,
                wallet.accountIndex,
                1, 0
        )
        val metaAddress = hdWalletWrapper.getAddressForSegwitUpgrade(wallet, path)
        fundingViewModel.fundMaxForUpgrade(metaAddress.address)
    }

    private fun showPermissions() {
        syncProgressView.gone()
        upgradePermission.show()
        if (hasBalance) transferPermission.show()
    }

    private fun updateTransferRates() {
        transactionData?.let {
            val latestPrice = walletHelper.latestPrice
            transferPermission.text = getString(
                    R.string.transfer_wallet_funds_notice,
                    it.amount.toBTCCurrency().toUSD(latestPrice).toFormattedCurrency(),
                    it.feeAmount.toBTCCurrency().toUSD(latestPrice).toFormattedCurrency()
            )
        }
    }
}
