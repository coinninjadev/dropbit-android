package com.coinninja.coinkeeper.ui.home

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.viewpager.widget.ViewPager
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.google.android.material.tabs.TabLayout
import javax.inject.Inject

class HomeActivity : BaseActivity() {

    companion object {
        const val currentPageKey = "current_page"
    }

    @Inject
    internal lateinit var activityNavigationUtil: ActivityNavigationUtil

    @Inject
    internal lateinit var homePagerAdapterProvider: HomePagerAdapterProvider
    internal var currentPage = 0

    internal val pager: ViewPager get() = findViewById(R.id.home_pager)
    internal val tabs: TabLayout get() = findViewById(R.id.appbar_tabs)

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