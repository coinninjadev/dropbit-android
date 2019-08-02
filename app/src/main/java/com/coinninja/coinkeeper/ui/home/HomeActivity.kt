package com.coinninja.coinkeeper.ui.home

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.market.OnMarketSelectionObserver
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.activity.base.BalanceBarActivity
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class HomeActivity : BalanceBarActivity() {

    companion object {
        const val currentPageKey = "current_page"
    }

    @Inject
    internal lateinit var activityNavigationUtil: ActivityNavigationUtil

    @Inject
    internal lateinit var homePagerAdapterProvider: HomePagerAdapterProvider
    var currentPage = 1

    val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageSelected(position: Int) {
            if (position == 0)
                analytics.trackEvent(Analytics.EVENT_CHARTS_OPENED)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        observeMarketSelection(object : OnMarketSelectionObserver {
            override fun onShowMarket() {
                showMarketPage()
            }
        })
        findViewById<ViewPager>(R.id.home_pager)?.apply {
            addOnPageChangeListener(this@HomeActivity.onPageChangeListener)
            adapter = homePagerAdapterProvider.provide(supportFragmentManager, lifecycle.currentState)
            setCurrentItem(currentPage, false)
        }.also {
            findViewById<TabLayout>(R.id.pager_tabs)?.apply {
                setupWithViewPager(it)
            }
        }
    }

    internal fun showMarketPage() {
        findViewById<ViewPager>(R.id.home_pager)?.apply {
            setCurrentItem(0, true)
        }
    }

    override fun onResume() {
        super.onResume()
        showDetailWithInitialIntent()
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

}