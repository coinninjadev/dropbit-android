package com.coinninja.coinkeeper.ui.actionbar

import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.CryptoCurrency
import app.dropbit.commons.currency.FiatCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.ui.base.MenuItemClickListener
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplaySyncView
import com.google.android.material.tabs.TabLayout

@Mockable
class ActionBarController constructor(
        internal val activityNavigationUtil: ActivityNavigationUtil
) {


    companion object {
        val actionBarTypes: Array<Int> = arrayOf(
                // -- gone
                R.id.actionbar_gone,

                // -- up on
                R.id.actionbar_up_on,
                R.id.actionbar_up_on_balance_on,
                R.id.actionbar_up_on_skip_on,
                R.id.actionbar_up_on_with_nav_bar,
                R.id.actionbar_up_on_with_nav_bar_balance_on,
                R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on,
                // -- up off
                R.id.actionbar_up_off,
                R.id.actionbar_up_off_close_on,
                R.id.actionbar_up_off_skip_on
        )
    }

    var menuItemClickListener: MenuItemClickListener? = null
    internal var actionBarType = TypedValue().also {
        it.resourceId = R.id.actionbar_up_on
    }
    internal val isActionBarGone: Boolean get() = actionBarType.resourceId == R.id.actionbar_gone

    internal val isUpEnabled: Boolean
        get() = when (actionBarType.resourceId) {
            R.id.actionbar_up_on,
            R.id.actionbar_up_on_balance_on,
            R.id.actionbar_up_on_skip_on,
            R.id.actionbar_up_on_with_nav_bar,
            R.id.actionbar_up_on_with_nav_bar_balance_on,
            R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on -> true
            else -> false
        }

    internal val isBalanceOn: Boolean
        get() = when (actionBarType.resourceId) {
            R.id.actionbar_up_on_balance_on,
            R.id.actionbar_up_on_with_nav_bar_balance_on,
            R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on -> true

            else -> false
        }

    internal val isBalanceBelowTitle: Boolean
        get() = when (actionBarType.resourceId) {
            R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on -> true
            else -> false
        }


    internal val isChartsOn: Boolean
        get() = when (actionBarType.resourceId) {
            R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on -> true
            else -> false
        }


    internal val optionMenuLayout: Int?
        get() = when (actionBarType.resourceId) {
            R.id.actionbar_up_on_skip_on,
            R.id.actionbar_up_off_skip_on -> R.menu.actionbar_light_skip_menu

            R.id.actionbar_up_off_close_on -> R.menu.actionbar_light_close_menu
            else -> null
        }

    private var _title: String = ""

    private var accountMode: AccountMode = AccountMode.BLOCKCHAIN
    private var isCurrentlySyncing: Boolean = false
    private var holdingsWorth: FiatCurrency = USDCurrency(0)
    private var holdings: CryptoCurrency = BTCCurrency(0)
    private var defaultCurrencies: DefaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
    var currencyModeChangeListener: CurrencyModeChangeListener? = null

    fun setTheme(activity: AppCompatActivity, actionBarType: TypedValue) {
        if (!actionBarTypes.contains(actionBarType.resourceId)) {
            throw IllegalStateException("R.attr.actionBarMenuType not set")
        }
        this.actionBarType = actionBarType
        hideAppbarIfNecessary(activity)
        updateActionBarUpIndicator(activity)
        updateBalanceViewPreference(activity)
        showChartsIfNecessary(activity)
        displayTitle(activity)
        hideTabs(activity)
    }

    private fun hideTabs(activity: AppCompatActivity) {
        activity.findViewById<View>(R.id.appbar_tabs)?.visibility = View.GONE
    }

    private fun showChartsIfNecessary(activity: AppCompatActivity) {
        activity.findViewById<ImageButton>(R.id.appbar_charts)?.apply {
            if (isChartsOn) {
                visibility = View.VISIBLE
                setOnClickListener { menuItemClickListener?.onShowMarketData() }
            } else {
                this@ActionBarController.removeView(this)
            }
        }
    }

    fun updateBalanceViewPreference(activity: AppCompatActivity) {
        activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.appbar_balance)?.apply {
            this.accountMode = this@ActionBarController.accountMode
            renderValues(this@ActionBarController.defaultCurrencies, holdings, holdingsWorth)
            if (isCurrentlySyncing) {
                showSyncingUI()
            } else {
                hideSyncingUI()
            }
            if (isBalanceOn) {
                setupObserversFor(this)
                if (isBalanceBelowTitle) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                }
            } else {
                this@ActionBarController.removeView(this)
            }
        }
        activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.appbar_balance_large)?.apply {
            this.accountMode = this@ActionBarController.accountMode
            renderValues(this@ActionBarController.defaultCurrencies, holdings, holdingsWorth)
            if (isCurrentlySyncing) {
                showSyncingUI()
            } else {
                hideSyncingUI()
            }
            if (isBalanceOn) {
                if (isBalanceBelowTitle) {
                    setupObserversFor(this)
                    View.VISIBLE
                } else {
                    this@ActionBarController.removeView(this)
                }
            } else {
                this@ActionBarController.removeView(this)
            }
        }

        onLightningLockedChange(activity)
    }

    fun onLightningLockedChange(activity: AppCompatActivity, isLocked: Boolean = true) {
        activity.findViewById<ImageButton>(R.id.appbar_transfer_between_accounts)?.apply {
            if (isBalanceBelowTitle && isLocked) {
                show()
                setOnClickListener { activityNavigationUtil.showLoadLightningOptions(activity) }
            } else if (isBalanceBelowTitle && !isLocked) {
                postDelayed({
                    try {
                        this.gone()
                    } catch (e: Exception) {
                    }
                }, 300)
            } else {
                gone()
            }
        }
    }

    fun onAccountModeChange(activity: AppCompatActivity, mode: AccountMode) {
        accountMode = mode
        updateBalanceViewPreference(activity)
    }

    fun onSyncStatusChange(activity: AppCompatActivity, isSyncing: Boolean) {
        isCurrentlySyncing = isSyncing
        updateBalanceViewPreference(activity)
    }

    fun onHoldingsChanged(activity: AppCompatActivity, value: CryptoCurrency) {
        holdings = value
        updateBalanceViewPreference(activity)
    }

    fun onHoldingsWorthChanged(activity: AppCompatActivity, value: FiatCurrency) {
        holdingsWorth = value
        updateBalanceViewPreference(activity)
    }

    fun onDefaultCurrencyChanged(activity: AppCompatActivity, defaultCurrencies: DefaultCurrencies) {
        this.defaultCurrencies = defaultCurrencies
        updateBalanceViewPreference(activity)
    }

    private fun removeView(view: View) {
        (view.parent as ViewGroup).removeView(view)
    }


    private fun setupObserversFor(defaultCurrencyDisplayView: DefaultCurrencyDisplaySyncView) {
        defaultCurrencyDisplayView.setOnClickListener { currencyModeChangeListener?.onChange() }
    }


    fun inflateActionBarMenu(activity: AppCompatActivity, menu: Menu) {
        optionMenuLayout?.let {
            activity.menuInflater.inflate(it, menu)
        }
    }

    private fun updateActionBarUpIndicator(activity: AppCompatActivity) {
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(isUpEnabled)
    }

    fun onMenuItemClicked(item: MenuItem): Boolean {
        optionMenuLayout?.let {
            return when (item.itemId) {
                R.id.action_skip_btn -> {
                    menuItemClickListener?.onSkipClicked()
                    true
                }
                R.id.action_close_btn -> {
                    menuItemClickListener?.onCloseClicked()
                    true
                }
                else -> false
            }
        }
        return false
    }

    fun displayTitle(activity: AppCompatActivity, title: String? = null) {
        _title = title ?: activity.supportActionBar?.title.toString()
        activity.supportActionBar?.title = ""

        if (isActionBarGone || _title.isEmpty()) {
            removeTitle(activity)
        } else {
            renderTitle(activity)
        }
    }

    private fun hideAppbarIfNecessary(activity: AppCompatActivity) {
        if (isActionBarGone) {
            activity.findViewById<View>(R.id.appbar_group)?.apply {
                visibility = View.GONE
            }
        }
    }

    private fun renderTitle(activity: AppCompatActivity) {
        activity.findViewById<TextView>(R.id.appbar_title)?.apply {
            visibility = View.VISIBLE
            text = _title.toUpperCase()
        }
    }

    private fun removeTitle(activity: AppCompatActivity) {
        activity.findViewById<TextView>(R.id.appbar_title)?.apply {
            visibility = View.GONE
        }
    }

    fun addTab(activity: AppCompatActivity, @LayoutRes resourceId: Int, index: Int) {
        activity.findViewById<TabLayout>(R.id.appbar_tabs)?.apply {
            visibility = View.VISIBLE
            getTabAt(index)?.setCustomView(resourceId)
        }
    }

    interface CurrencyModeChangeListener {
        fun onChange()
    }
}
