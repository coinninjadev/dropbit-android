package com.coinninja.coinkeeper.ui.actionbar

import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
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

    internal val optionMenuLayout: Int?
        get() = when (actionBarType.resourceId) {
            R.id.actionbar_up_on_skip_on,
            R.id.actionbar_up_off_skip_on -> R.menu.actionbar_light_skip_menu

            R.id.actionbar_up_off_close_on -> R.menu.actionbar_light_close_menu

            R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on -> R.menu.actionbar_light_charts_menu

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
        displayTitle(activity)
    }

    private fun updateBalanceViewPreference(activity: AppCompatActivity) {
        activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)?.apply {
            visibility = if (isBalanceOn) {
                setupObserversFor(activity, this)
                View.VISIBLE
            } else {
                View.GONE
            }

            if (isBalanceOn && isBalanceBelowTitle) {
                val constraintSet = ConstraintSet()
                val constraintLayout = activity.findViewById<ConstraintLayout>(R.id.cn_appbar_extensions)
                constraintSet.clone(constraintLayout)
                constraintSet.connect(R.id.balance, ConstraintSet.START, R.id.guideline2, ConstraintSet.START)
                constraintSet.connect(R.id.balance, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.connect(R.id.balance, ConstraintSet.TOP, R.id.appbar_title, ConstraintSet.BOTTOM)

                constraintSet.applyTo(constraintLayout)


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

        if (isActionBarGone || _title.isEmpty()) {
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
