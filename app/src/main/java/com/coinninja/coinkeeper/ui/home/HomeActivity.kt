package com.coinninja.coinkeeper.ui.home

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.viewpager.widget.ViewPager
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class HomeActivity : BaseActivity() {

    companion object {
        const val currentPageKey = "current_page"
    }

    @Inject
    internal lateinit var homePagerAdapterProvider: HomePagerAdapterProvider

    @Inject
    internal lateinit var accountModeManger: AccountModeManager

    internal var currentPage = 0

    internal val pager: ViewPager get() = findViewById(R.id.home_pager)
    internal val tabs: TabLayout get() = findViewById(R.id.appbar_tabs)
    internal val onTabSelectedListener: TabLayout.OnTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tabs.selectedTabPosition) {
                1 -> accountModeManger.changeMode(AccountMode.LIGHTNING)
                else -> accountModeManger.changeMode(AccountMode.BLOCKCHAIN)
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

    private fun showDetailWithInitialIntent() {
        if (!intent.hasExtra(DropbitIntents.EXTRA_TRANSACTION_ID)) return

        activityNavigationUtil.showTransactionDetail(this, txid = intent.getStringExtra(DropbitIntents.EXTRA_TRANSACTION_ID))
        intent.removeExtra(DropbitIntents.EXTRA_TRANSACTION_ID)
    }

    private fun selectTabForMode() {
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