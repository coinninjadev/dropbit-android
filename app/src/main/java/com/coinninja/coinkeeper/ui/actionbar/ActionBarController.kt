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
import androidx.lifecycle.Observer
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.CryptoCurrency
import app.dropbit.commons.currency.FiatCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.ui.base.MenuItemClickListener
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplaySyncView
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import com.google.android.material.tabs.TabLayout

@Mockable
class ActionBarController constructor(
        internal val walletViewModel: WalletViewModel,
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
            if (isBalanceOn) {
                setupObserversFor(activity, this)
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
            if (isBalanceOn) {
                if (isBalanceBelowTitle) {
                    setupObserversFor(activity, this)
                    View.VISIBLE
                } else {
                    this@ActionBarController.removeView(this)
                }
            } else {
                this@ActionBarController.removeView(this)
            }
        }

        if (isBalanceBelowTitle) {
            activity.findViewById<ImageButton>(R.id.appbar_transfer_between_accounts)?.apply {
                visibility = View.VISIBLE
                setOnClickListener {activityNavigationUtil.showLoadLightningOptions(activity)}
            }
        }
    }

    private fun removeView(view: View) {
        (view.parent as ViewGroup).removeView(view)
    }

    private fun updateBalances(defaultCurrencyDisplayView: DefaultCurrencyDisplaySyncView, defaultCurrencies: DefaultCurrencies
                               , cryptoCurrency: CryptoCurrency, fiatCurrency: FiatCurrency) {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, cryptoCurrency, fiatCurrency)

    }

    private fun setupObserversFor(activity: AppCompatActivity, defaultCurrencyDisplayView: DefaultCurrencyDisplaySyncView) {
        walletViewModel.accountMode.observe(activity, Observer<AccountMode> { mode ->
            defaultCurrencyDisplayView.accountMode(mode)
        })
        walletViewModel.syncInProgress.observe(activity, Observer<Boolean> { isSyncing ->
            if (isSyncing == true) {
                defaultCurrencyDisplayView.showSyncingUI()
            } else {
                defaultCurrencyDisplayView.hideSyncingUI()
            }
        })
        walletViewModel.holdings.observe(activity, Observer<CryptoCurrency> { holdings ->
            walletViewModel.defaultCurrencyPreference.value?.let { defaults ->
                walletViewModel.holdingsWorth.value?.let { fiatCurrency ->
                    updateBalances(defaultCurrencyDisplayView, defaults, holdings, fiatCurrency)
                }
            }
        })
        walletViewModel.holdingsWorth.observe(activity, Observer<FiatCurrency> { fiatCurrency ->
            walletViewModel.defaultCurrencyPreference.value?.let { defaults ->
                walletViewModel.holdings.value?.let { holdings ->
                    updateBalances(defaultCurrencyDisplayView, defaults, holdings, fiatCurrency)
                }
            }
        })
        walletViewModel.defaultCurrencyPreference.observe(activity, Observer<DefaultCurrencies> { defaults ->
            walletViewModel.holdingsWorth.value?.let { fiatCurrency ->
                walletViewModel.holdings.value?.let { holdings ->
                    updateBalances(defaultCurrencyDisplayView, defaults, holdings, fiatCurrency)
                }
            }
        })

        defaultCurrencyDisplayView.setOnClickListener { walletViewModel.toggleDefaultCurrencyPreference() }
        walletViewModel.loadHoldingBalances()
        walletViewModel.loadCurrencyDefaults()
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
}
