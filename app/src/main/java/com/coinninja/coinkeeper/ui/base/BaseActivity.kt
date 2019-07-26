package com.coinninja.coinkeeper.ui.base

import android.content.Context
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController
import com.coinninja.coinkeeper.ui.market.OnMarketSelectionObserver
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), MenuItemClickListener {

    @Inject
    lateinit var actionBarController: ActionBarController
    @Inject
    lateinit var analytics: Analytics
    @Inject
    lateinit var actionBarType: TypedValue
    @Inject
    lateinit var cnWalletManager: CNWalletManager
    @Inject
    internal lateinit var drawerController: DrawerController
    @Inject
    internal lateinit var navigationUtil: ActivityNavigationUtil

    private var loadingDialog: AlertDialog? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        actionBarController.inflateActionBarMenu(this, menu)
        actionBarController.menuItemClickListener = this
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarController.onMenuItemClicked(item)) {
            true
        } else if (drawerController.onMenuItemClicked(item)) {
            true
        } else if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerController.isDrawerOpen) {
            drawerController.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    fun updateActivityLabel(string: String) = actionBarController.updateTitle(string)
    fun clearTitle() = actionBarController.updateTitle("")

    fun observeMarketSelection(onMarketSelectionObserver: OnMarketSelectionObserver) =
            drawerController.observeMarketSelection(onMarketSelectionObserver)

    override fun onCloseClicked() = navigationUtil.navigateToHome(this)
    override fun onSkipClicked() = navigationUtil.navigateToHome(this)
    override fun onShowMarketData() = navigationUtil.showMarketCharts(this)

    fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.cn_base_layout)
        LayoutInflater.from(this).inflate(layoutResID, findViewById<ViewGroup>(R.id.cn_content_container))
        setSupportActionBar(findViewById(R.id.toolbar))
        theme.resolveAttribute(R.attr.actionBarMenuType, actionBarType, true)
        actionBarController.setTheme(this, actionBarType)
        drawerController.inflateDrawer(this, actionBarType)
        actionBarController.displayTitle(this)
    }

    public override fun onPause() {
        super.onPause()
        drawerController.closeDrawerNoAnimation()
    }

    override fun onResume() {
        super.onResume()
        if (cnWalletManager.hasSkippedBackup()) {
            drawerController.showBackupNowDrawerActions()
        }

        drawerController.renderBadgeForUnverifiedDeviceIfNecessary()
        observeMarketSelection(object : OnMarketSelectionObserver {
            override fun onShowMarket() {
                onShowMarketData()
            }
        })
    }

    open fun onPriceReceived(price: USDCurrency) =
            drawerController.updatePriceOfBtcDisplay(price)

    fun showLoading() {
        loadingDialog = loadingDialog ?: AlertDialogBuilder.buildIndefiniteProgress(this).also {
            if (!it.isShowing) it.show()
        }
    }

    fun removeLoading() = loadingDialog?.dismiss().also {
        loadingDialog = null
    }
}
