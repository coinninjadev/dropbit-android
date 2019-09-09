package com.coinninja.coinkeeper.ui.actionbar.managers

import android.app.Activity
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.FiatCurrency
import com.coinninja.android.helpers.Resources
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.di.interfaces.BuildVersionName
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.ui.market.OnMarketSelectionObserver
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.ui.BadgeRenderer
import com.coinninja.coinkeeper.view.widget.DrawerLayout
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import com.google.android.material.navigation.NavigationView
import java.util.*

@Mockable
class DrawerController(
        internal val badgeRenderer: BadgeRenderer,
        internal val activityNavigationUtil: ActivityNavigationUtil,
        @BuildVersionName internal val versionName: String,
        internal val dropbitAccountHelper: DropbitAccountHelper,
        internal val walletViewModel: WalletViewModel
) {

    internal var drawerLayout: DrawerLayout? = null
    internal var currentPriceObserver: Observer<in FiatCurrency> = Observer { this.updatePriceOfBtcDisplay(it) }
    private var onMarketSelectionObserver: OnMarketSelectionObserver? = null

    val isDrawerOpen: Boolean get() = drawerLayout?.isDrawerOpen(GravityCompat.START) ?: false

    fun inflateDrawer(activity: AppCompatActivity, actionBarType: TypedValue) {
        if (drawerThemes.contains(actionBarType.resourceId)) {
            val root = activity.findViewById<View>(R.id.cn_content_wrapper)
            wrapBaseLayoutWithDrawer(activity, root)
            inflate(activity)
            setupPrice(activity)
            setupNavigationView()
            setupDrawerButtons()
            displayAppVersion()
        }
    }

    fun openDrawer() = drawerLayout?.openDrawer(GravityCompat.START)

    fun closeDrawer() = drawerLayout?.closeDrawer(GravityCompat.START)

    fun closeDrawerNoAnimation() = drawerLayout?.closeDrawer(GravityCompat.START, false)

    fun renderBadgeForUnverifiedDeviceIfNecessary() = drawerLayout?.let {
        if (!dropbitAccountHelper.hasVerifiedAccount) {
            badgeRenderer.renderBadge(it.findViewById<ImageView>(R.id.contact_phone))
            badgeRenderer.renderBadge(it.findViewById<Toolbar>(R.id.toolbar))
        }
    }

    fun displayAppVersion() = drawerLayout?.let {
        it.findViewById<TextView>(R.id.drawer_action_footer_version).apply {
            text = Resources.getString(context, R.string.app_version_label, versionName)
        }
    }


    fun showBackupNowDrawerActions() = drawerLayout?.let {
        badgeRenderer.renderBadge(it.findViewById<ImageView>(R.id.setting_icon))
        badgeRenderer.renderBadge(it.findViewById<Toolbar>(R.id.toolbar))
        it.findViewById<Button>(R.id.drawer_backup_now).apply {
            visibility = View.VISIBLE
            setOnClickListener { activityNavigationUtil.navigateToBackupRecoveryWords(context) }
        }
    }

    fun onMenuItemClicked(item: MenuItem): Boolean =
            if (drawerLayout != null && item.itemId == android.R.id.home) {
                openDrawer()
                true
            } else {
                false
            }

    fun observeMarketSelection(onMarketSelectionObserver: OnMarketSelectionObserver) {
        this.onMarketSelectionObserver = onMarketSelectionObserver
    }

    private fun setupPrice(activity: AppCompatActivity) {
        walletViewModel.currentPrice.observe(activity, currentPriceObserver)
        walletViewModel.loadHoldingBalances()
    }

    private fun updatePriceOfBtcDisplay(price: FiatCurrency?) {
        price?.let { fiat ->
            if (!price.isZero)
                drawerLayout?.findViewById<TextView>(R.id.drawer_action_price_text)?.apply {
                    text = fiat.toFormattedCurrency()
                }
        }
    }

    private fun wrapBaseLayoutWithDrawer(activity: AppCompatActivity, root: View) {
        drawerLayout = DrawerLayout(activity, false)
        drawerLayout?.apply {
            fitsSystemWindows = true
            val screen = root.parent as ViewGroup
            screen.removeView(root)
            screen.addView(drawerLayout)
            addView(root)
        }
    }

    private fun inflate(activity: AppCompatActivity) {
        activity.layoutInflater.inflate(R.layout.cn_drawer_layout, drawerLayout, true)
    }

    private fun setupDrawerButtons() {
        drawerLayout?.findViewById<View>(R.id.drawer_setting)?.setOnClickListener {
            activityNavigationUtil.navigateToSettings(it.context)
        }
        drawerLayout?.findViewById<View>(R.id.drawer_support)?.setOnClickListener {
            activityNavigationUtil.navigateToSupport(it.context)
        }
        drawerLayout?.findViewById<View>(R.id.drawer_where_to_buy)?.setOnClickListener {
            activityNavigationUtil.navigateToSpendBitcoin(it.context)
        }
        drawerLayout?.findViewById<View>(R.id.drawer_phone)?.setOnClickListener {
            activityNavigationUtil.navigateToUserVerification(it.context)
        }
        drawerLayout?.findViewById<View>(R.id.buy_bitcoin_drawer)?.setOnClickListener {
            activityNavigationUtil.navigateToBuyBitcoin(it.context)
        }
        drawerLayout?.findViewById<View>(R.id.drawer_action_price_text)?.setOnClickListener { onShowMarket() }
    }

    private fun onShowMarket() = drawerLayout?.let {
        val drawerToggle = object : ActionBarDrawerToggle(it.context as Activity, it,
                it.findViewById(R.id.toolbar), R.string.drawer_open_descritpion, R.string.drawer_close_descritpion) {

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                onMarketSelectionObserver?.onShowMarket()
                it.removeDrawerListener(this)
            }
        }

        it.addDrawerListener(drawerToggle)
        closeDrawer()
    }


    private fun setupNavigationView() =
            drawerLayout?.findViewById<NavigationView>(R.id.drawer_action_view)?.apply {
                itemIconTintList = null
                setNavigationItemSelectedListener { menuItem ->
                    menuItem.isChecked = true
                    closeDrawer()
                    true
                }

            }

    companion object {

        private val drawerThemes: HashSet<*>

        init {
            val set = HashSet<Int>()
            set.add(R.id.actionbar_up_on_with_nav_bar)
            set.add(R.id.actionbar_up_on_with_nav_bar_balance_on)
            set.add(R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on)
            drawerThemes = set
        }
    }
}
