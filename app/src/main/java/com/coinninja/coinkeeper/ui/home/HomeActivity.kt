package com.coinninja.coinkeeper.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.viewpager.widget.ViewPager
import com.coinninja.android.helpers.hide
import com.coinninja.android.helpers.show
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.payment.PaymentBarFragment
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class HomeActivity : BaseActivity() {

    companion object {
        const val currentPageKey = "current_page"
        const val wyreTransferDialog: String = "wyreTransferDialog"
    }

    @Inject
    internal lateinit var homePagerAdapterProvider: HomePagerAdapterProvider

    @Inject
    internal lateinit var accountModeManger: AccountModeManager

    @Inject
    lateinit var syncWalletManager: SyncWalletManager

    internal var currentPage = 0

    internal var isLightningLocked = true

    internal val paymentBarFragment: PaymentBarFragment
        get() = supportFragmentManager.findFragmentByTag("paymentBarFragment") as PaymentBarFragment

    internal val pager: ViewPager get() = findViewById(R.id.home_pager)
    internal val tabs: TabLayout get() = findViewById(R.id.appbar_tabs)
    internal val onTabSelectedListener: TabLayout.OnTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            paymentBarFragment.show()
            when (tabs.selectedTabPosition) {
                1 -> {
                    changeAccountMode(AccountMode.LIGHTNING)
                    onLightningLockChanged()
                }
                else -> {
                    changeAccountMode(AccountMode.BLOCKCHAIN)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<View>(R.id.appbar_balance_large)?.apply {
            visibility = View.VISIBLE
        }
        findViewById<MotionLayout>(R.id.cn_content_wrapper)?.apply {
            updateState()
            rebuildScene()
        }

        pager.apply {
            adapter = homePagerAdapterProvider.provide(supportFragmentManager, lifecycle.currentState)
            setCurrentItem(currentPage, false)
            tabs.setupWithViewPager(this)
        }

        addTabToAppBar(R.layout.home_appbar_tab_2, 1)
        addTabToAppBar(R.layout.home_appbar_tab_1, 0)

        tabs.addOnTabSelectedListener(onTabSelectedListener)
        syncWalletManager.schedule30SecondSync()
        processCreationIntent()
    }

    override fun onResume() {
        super.onResume()
        showDetailWithInitialIntent()
        selectTabForMode()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let {
            if (it.containsKey(currentPageKey)) {
                currentPage = savedInstanceState.getInt(currentPageKey, 1)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        findViewById<ViewPager>(R.id.home_pager)?.also {
            outState.putInt(currentPageKey, it.currentItem)
        }
    }

    override fun onLightningLockedChanged(isLightningLocked: Boolean) {
        super.onLightningLockedChanged(isLightningLocked)
        this.isLightningLocked = isLightningLocked
        onLightningLockChanged()
    }

    private fun onLightningLockChanged() {
        if (isLightningLocked && accountModeManger.accountMode == AccountMode.LIGHTNING) {
            pager.postDelayed({
                if (isLightningLocked)
                    try {
                        paymentBarFragment.hide()
                    } catch (e: Exception) {
                    }
            }, 200)
        } else {
            pager.removeCallbacks(null)
            paymentBarFragment.show()
        }
    }

    private fun showDetailWithInitialIntent() {
        if (!intent.hasExtra(DropbitIntents.EXTRA_TRANSACTION_ID)) return

        activityNavigationUtil.showTransactionDetail(this, txid = intent.getStringExtra(DropbitIntents.EXTRA_TRANSACTION_ID))
        intent.removeExtra(DropbitIntents.EXTRA_TRANSACTION_ID)
    }

    private fun processCreationIntent() {
        intent.data?.let {
            if (it.scheme == "dropbit" && it.authority == "wyre") {
                it.getQueryParameter("transferId")?.let { transferId ->

                    val view = LayoutInflater.from(this).inflate(R.layout.dialog_wyre_transfer,
                            null, false)
                    view.findViewById<View>(R.id.track_button).setOnClickListener {
                        activityNavigationUtil.openUrl(this, Uri.parse("https://dash.sendwyre.com/track/${transferId}"))
                    }
                    val dialog = GenericAlertDialog.newInstance(view, true, true)
                    dialog.asWide()
                    dialog.show(supportFragmentManager, wyreTransferDialog)
                }
                intent.data = null
            }
        }
    }

    internal fun selectTabForMode() {
        when (accountModeManger.accountMode) {
            AccountMode.LIGHTNING -> {
                val tabs = tabs
                tabs.selectTab(tabs.getTabAt(1))
            }
            else -> {
                val tabs = tabs
                tabs.selectTab(tabs.getTabAt(0))
            }
        }
    }
}