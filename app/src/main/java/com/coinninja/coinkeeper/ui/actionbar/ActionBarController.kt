package com.coinninja.coinkeeper.ui.actionbar

import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.MenuItemClickListener
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.currency.CryptoCurrency
import com.coinninja.coinkeeper.util.currency.FiatCurrency
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplaySyncView
import com.coinninja.coinkeeper.viewModel.WalletViewModel

@Mockable
class ActionBarController constructor(
        internal val walletViewModel: WalletViewModel
) {

    var menuItemClickListener: MenuItemClickListener? = null
    internal var isActionBarGone: Boolean = false
    internal var isUpEnabled: Boolean = false
    internal var isBalanceOn: Boolean = false
    internal var optionMenuLayout: Int? = null
    private var _title: String = ""

    fun setTheme(activity: AppCompatActivity, actionBarType: TypedValue) {
        when (actionBarType.resourceId) {
            R.id.actionbar_gone -> isActionBarGone = true

            R.id.actionbar_up_on_with_nav_bar,
            R.id.actionbar_up_on -> isUpEnabled = true

            R.id.actionbar_up_on_skip_on -> {
                isUpEnabled = true
                optionMenuLayout = R.menu.actionbar_light_skip_menu
            }
            R.id.actionbar_up_on_close_on -> {
                isUpEnabled = true
                optionMenuLayout = R.menu.actionbar_light_close_menu
            }
            R.id.actionbar_up_on_with_nav_bar_balance_on -> {
                isBalanceOn = true
            }

            R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on -> {
                isBalanceOn = true
                optionMenuLayout = R.menu.actionbar_light_charts_menu
            }

            R.id.actionbar_up_off_close_on -> {
                isUpEnabled = false
                optionMenuLayout = R.menu.actionbar_light_close_menu
            }

            R.id.actionbar_up_off -> {
                isUpEnabled = false
            }

            R.id.actionbar_up_off_skip_on -> {
                isUpEnabled = false
                optionMenuLayout = R.menu.actionbar_light_skip_menu
            }
            else -> throw IllegalStateException("R.attr.actionBarMenuType not set")
        }
        hideAppbarIfNecessary(activity)
        updateActionBarUpIndicator(activity)
        updateBalanceViewPreference(activity)
        displayTitle(activity)
    }

    private fun updateBalanceViewPreference(activity: AppCompatActivity) {
        activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)?.apply {
            visibility = if (isBalanceOn) {
                setupObserversFor(activity, this)
                View.VISIBLE
            } else {
                View.VISIBLE
            }
        }
    }

    private fun updateBalances(defaultCurrencyDisplayView: DefaultCurrencyDisplaySyncView, defaultCurrencies: DefaultCurrencies
                               , cryptoCurrency: CryptoCurrency, fiatCurrency: FiatCurrency) {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, cryptoCurrency, fiatCurrency)

    }

    private fun setupObserversFor(activity: AppCompatActivity, defaultCurrencyDisplayView: DefaultCurrencyDisplaySyncView) {
        walletViewModel.syncInProgress.observe(activity, Observer<Boolean> { isSyncing ->
            if (isSyncing == true) {
                defaultCurrencyDisplayView.showSyncingUI()
            } else {
                defaultCurrencyDisplayView.hideSyncingUI()
            }
        })
        walletViewModel.chainHoldings.observe(activity, Observer<CryptoCurrency> { holdings ->
            walletViewModel.defaultCurrencyPreference.value?.let { defaults ->
                walletViewModel.chainHoldingsWorth.value?.let { fiatCurrency ->
                    updateBalances(defaultCurrencyDisplayView, defaults, holdings, fiatCurrency)
                }
            }
        })
        walletViewModel.chainHoldingsWorth.observe(activity, Observer<FiatCurrency> { fiatCurrency ->
            walletViewModel.defaultCurrencyPreference.value?.let { defaults ->
                walletViewModel.chainHoldings.value?.let { holdings ->
                    updateBalances(defaultCurrencyDisplayView, defaults, holdings, fiatCurrency)
                }
            }
        })
        walletViewModel.defaultCurrencyPreference.observe(activity, Observer<DefaultCurrencies> { defaults ->
            walletViewModel.chainHoldingsWorth.value?.let { fiatCurrency ->
                walletViewModel.chainHoldings.value?.let { holdings ->
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
                R.id.action_chart_button -> {
                    menuItemClickListener?.onShowMarketData()
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

        if (isActionBarGone && _title.isNotEmpty()) {
            removeTitle(activity)
        } else {
            renderTitle(activity)
        }
    }

    private fun hideAppbarIfNecessary(activity: AppCompatActivity) {
        if (isActionBarGone) {
            activity.findViewById<View>(R.id.cn_appbar_layout_container)?.apply {
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
}
