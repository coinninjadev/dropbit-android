package com.coinninja.coinkeeper.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import app.dropbit.commons.currency.CryptoCurrency
import app.dropbit.commons.currency.FiatCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor
import com.coinninja.coinkeeper.interfaces.Authentication
import com.coinninja.coinkeeper.interfaces.PinEntry
import com.coinninja.coinkeeper.receiver.AuthenticationCompleteReceiver
import com.coinninja.coinkeeper.service.WalletCreationIntentService
import com.coinninja.coinkeeper.service.runner.HealthCheckTimerRunner
import com.coinninja.coinkeeper.service.tasks.CNHealthCheckTask
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController
import com.coinninja.coinkeeper.ui.actionbar.ActionbarControllerProvider
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerControllerProvider
import com.coinninja.coinkeeper.ui.market.OnMarketSelectionObserver
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.activity.*
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import com.coinninja.coinkeeper.viewModel.WalletViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import java.lang.ref.WeakReference
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), MenuItemClickListener, CNHealthCheckTask.HealthCheckCallback {
    // provided by injected providers
    lateinit var walletViewModel: WalletViewModel
    lateinit var actionBarController: ActionBarController
    internal lateinit var drawerController: DrawerController

    @Inject
    lateinit var drawerControllerProvider: DrawerControllerProvider
    @Inject
    lateinit var walletViewModelProvider: WalletViewModelProvider
    @Inject
    lateinit var actionbarControllerProvider: ActionbarControllerProvider
    @Inject
    lateinit var activityNavigationUtil: ActivityNavigationUtil
    @Inject
    lateinit var analytics: Analytics
    @Inject
    lateinit var actionBarType: TypedValue
    @Inject
    lateinit var cnWalletManager: CNWalletManager
    @Inject
    lateinit var pinEntry: PinEntry
    @Inject
    internal lateinit var authentication: Authentication
    @Inject
    internal lateinit var localBroadCastUtil: LocalBroadCastUtil
    @Inject
    internal lateinit var notificationsInteractor: InternalNotificationsInteractor
    @Inject
    internal lateinit var healthCheckRunner: HealthCheckTimerRunner
    @Inject
    internal lateinit var accountModeManager: AccountModeManager

    private val fragList = ArrayList<WeakReference<Fragment>>()
    internal val noInternetView: View? get() = findViewById(R.id.id_no_internet_message)
    internal val mute: View get() = findViewById(R.id.mute)
    internal val mutedMessage: TextView get() = findViewById(R.id.muted_message)
    internal val queue: ViewGroup get() = findViewById(R.id.message_queue)

    private val activeFragments: List<DialogFragment>
        get() {
            val activeFragments = ArrayList<DialogFragment>()
            for (ref in fragList) {
                ref.get()?.let {
                    if (it.isVisible && it is DialogFragment) {
                        activeFragments.add(it)
                    }
                }
            }
            return activeFragments
        }

    private var loadingDialog: AlertDialog? = null

    var hasForeGround = true

    internal val isLightningLockedObserver: Observer<Boolean> = Observer {
        onLightningLockedChanged(it)
    }

    internal val accountModeObserver: Observer<AccountMode> = Observer {
        onAccountModeChanged(it)
    }

    internal val lightningBalanceObserver: Observer<CryptoCurrency> = Observer {
        onLightningBalanceChanged(it)
    }

    internal val syncInProgressObserver: Observer<Boolean> = Observer {
        onSyncStatusChanged(it)
    }

    internal val defaultCurrencyPreferenceObserver: Observer<DefaultCurrencies> = Observer {
        defaultCurrencyChanged(it)
    }

    internal val holdingsWorthObserver: Observer<FiatCurrency> = Observer {
        onHoldingsWorthChanged(it)
    }

    internal val holdingsObserver: Observer<CryptoCurrency> = Observer {
        onHoldingsChanged(it)
    }

    internal val currencyModeChangeListener = object : ActionBarController.CurrencyModeChangeListener {
        override fun onChange() {
            walletViewModel.toggleDefaultCurrencyPreference()
        }
    }


    val latestPriceObserver: Observer<FiatCurrency> = Observer {
        onLatestPriceChanged(it)
    }

    @CallSuper
    open fun onLatestPriceChanged(currentPrice: FiatCurrency) {
    }

    @CallSuper
    open fun onAccountModeChanged(mode: AccountMode) {
        actionBarController.onAccountModeChange(this, mode)
    }

    @CallSuper
    open fun onLightningLockedChanged(isLightningLocked: Boolean) {
        actionBarController.onLightningLockedChange(this, isLightningLocked)
    }

    @CallSuper
    open fun onHoldingsChanged(balance: CryptoCurrency) {
        actionBarController.onHoldingsChanged(this, balance)
    }

    @CallSuper
    open fun onHoldingsWorthChanged(value: FiatCurrency) {
        actionBarController.onHoldingsWorthChanged(this, value)
    }

    @CallSuper
    open fun defaultCurrencyChanged(defaultCurrencies: DefaultCurrencies) {
        actionBarController.onDefaultCurrencyChanged(this, defaultCurrencies)
    }

    @CallSuper
    open fun onSyncStatusChanged(isSyncing: Boolean) {
        actionBarController.onSyncStatusChange(this, isSyncing)
    }

    @CallSuper
    open fun onLightningBalanceChanged(balance: CryptoCurrency) {
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AUTHENTICATION_REQUEST_CODE -> onAuthenticationResult(resultCode)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        actionBarController.inflateActionBarMenu(this, menu)
        actionBarController.menuItemClickListener = this
        return true
    }

    fun updateActivityLabel(string: String) = actionBarController.displayTitle(this, string)

    fun addTabToAppBar(@LayoutRes tabResourceId: Int, tabIndex: Int) {
        actionBarController.addTab(this, tabResourceId, tabIndex)
    }

    private fun observeMarketSelection(onMarketSelectionObserver: OnMarketSelectionObserver) =
            drawerController.observeMarketSelection(onMarketSelectionObserver)

    override fun onCloseClicked() = activityNavigationUtil.navigateToHome(this)
    override fun onSkipClicked() = activityNavigationUtil.navigateToHome(this)
    override fun onShowMarketData() {
        activityNavigationUtil.showMarketCharts(this)
        analytics.trackEvent(Analytics.EVENT_CHARTS_OPENED)
    }

    override fun setContentView(layoutResID: Int) {
        if (!BASE_VIEWS.contains(layoutResID)) {
            super.setContentView(R.layout.cn_base_layout)
            LayoutInflater.from(this).inflate(layoutResID, findViewById<ViewGroup>(R.id.cn_content_container))
        } else {
            super.setContentView(layoutResID)
        }
        setSupportActionBar(findViewById(R.id.toolbar))
        theme.resolveAttribute(R.attr.actionBarMenuType, actionBarType, true)
        actionBarController.setTheme(this, actionBarType)
        drawerController.inflateDrawer(this, actionBarType)
    }

    fun setLayoutDescription(motionScene: Int) {
        findViewById<ConstraintLayout>(R.id.cn_content_wrapper)?.loadLayoutDescription(motionScene)
    }

    fun mergeContentView(resourceId: Int) {
        setContentView(R.layout.cn_base_layout)
        findViewById<ViewGroup>(R.id.cn_content_wrapper)?.also {
            LayoutInflater.from(this).inflate(resourceId, it, true)
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        fragList.add(WeakReference(fragment))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        walletViewModel = walletViewModelProvider.provide(this)
        walletViewModel.checkLightningLock()
        actionBarController = actionbarControllerProvider.provide(activityNavigationUtil)
        drawerController = drawerControllerProvider.provide(walletViewModel, activityNavigationUtil)
        healthCheckRunner.setCallback(this)
    }


    override fun onResume() {
        super.onResume()
        hasForeGround = true
        if (cnWalletManager.hasSkippedBackup()) {
            drawerController.showBackupNowDrawerActions()
        }

        actionBarController.updateBalanceViewPreference(this)
        drawerController.renderBadgeForUnverifiedDeviceIfNecessary()
        observeMarketSelection(object : OnMarketSelectionObserver {
            override fun onShowMarket() {
                onShowMarketData()
            }
        })

        if (!PIN_IGNORE_LIST.contains(javaClass.name)) {
            checkAuth()
        }

        if (!MESSAGE_IGNORE_LIST.contains(javaClass.name)) {
            notificationsInteractor.startListeningForNotifications(this, true)
            checkForInternalNotifications()
        }

        if (authentication.isAuthenticated)
            healthCheckRunner.run()


        setupObservers()
    }

    private fun setupObservers() {
        walletViewModel.fetchBtcLatestPrice().observe(this, latestPriceObserver)
        walletViewModel.fetchLightningBalance().observe(this, lightningBalanceObserver)
        walletViewModel.currentPrice.observe(this, latestPriceObserver)
        walletViewModel.isLightningLocked.observe(this, isLightningLockedObserver)
        walletViewModel.accountMode.observe(this, accountModeObserver)
        walletViewModel.holdings.observe(this, holdingsObserver)
        walletViewModel.holdingsWorth.observe(this, holdingsWorthObserver)
        walletViewModel.syncInProgress.observe(this, syncInProgressObserver)
        walletViewModel.defaultCurrencyPreference.observe(this, defaultCurrencyPreferenceObserver)
        walletViewModel.loadHoldingBalances()
        walletViewModel.loadCurrencyDefaults()
        walletViewModel.currentMode()
        actionBarController.currencyModeChangeListener = currencyModeChangeListener
        walletViewModel.checkLightningLock()
    }

    private fun removeObservers() {
        walletViewModel.fetchBtcLatestPrice().removeObserver(latestPriceObserver)
        walletViewModel.fetchLightningBalance().removeObserver(lightningBalanceObserver)
        walletViewModel.currentPrice.removeObserver(latestPriceObserver)
        walletViewModel.isLightningLocked.removeObserver(isLightningLockedObserver)
        walletViewModel.holdings.removeObserver(holdingsObserver)
        walletViewModel.holdingsWorth.removeObserver(holdingsWorthObserver)
        walletViewModel.accountMode.removeObserver(accountModeObserver)
        walletViewModel.syncInProgress.removeObserver(syncInProgressObserver)
        walletViewModel.defaultCurrencyPreference.removeObserver(defaultCurrencyPreferenceObserver)
        actionBarController.currencyModeChangeListener = null
    }

    override fun onPause() {
        hasForeGround = false
        drawerController.closeDrawerNoAnimation()
        queue.removeAllViews()
        teardownMute()
        super.onPause()
        if (!MESSAGE_IGNORE_LIST.contains(javaClass.name)) {
            notificationsInteractor.stopListeningForNotifications()
        }
        queue.removeCallbacks(healthCheckRunner)
        analytics.onActivityStop(this)
        removeObservers()
    }


    fun showLoading() {
        loadingDialog = loadingDialog ?: AlertDialogBuilder.buildIndefiniteProgress(this).also {
            if (!it.isShowing) it.show()
        }
    }

    fun removeLoading() = loadingDialog?.dismiss().also {
        loadingDialog = null
    }

    @CallSuper
    open fun muteViews() {
        mute.apply {
            dismissAllDialogs()
            this.visibility = View.VISIBLE
            setOnTouchListener { _, _ -> true }
        }
    }

    @CallSuper
    open fun teardownMute() {
        mute.visibility = View.GONE
        mutedMessage.visibility = View.GONE
    }

    @CallSuper
    open fun muteViewsWithMessage(message: String) {
        muteViews()
        val mutedMessage: TextView = findViewById<TextView>(R.id.muted_message)
        mutedMessage.text = message
        mutedMessage.visibility = View.VISIBLE
    }

    @Deprecated("use activity navigation util", replaceWith = ReplaceWith("ActivityNavigationUtil"), level = DeprecationLevel.WARNING)
    protected fun navigateTo(intent: Intent) {
        startActivity(intent)
    }

    fun changeAccountMode(mode: AccountMode) {
        walletViewModel.setMode(mode)
    }

    open fun showNext() {
        onCompletion()
        val intent = intent
        if (intent != null && intent.hasExtra(DropbitIntents.EXTRA_NEXT)) {
            try {
                val nextClass = Class.forName(getIntent().getStringExtra(DropbitIntents.EXTRA_NEXT))
                showNext(Intent(this, nextClass))
            } catch (e: ClassNotFoundException) {
                showNext(Intent(this, StartActivity::class.java))
            }

        } else {
            showNext(Intent(this, StartActivity::class.java))
        }
    }

    private fun checkAuth() {
        val hasWallet = cnWalletManager.hasWallet

        if (!authentication.isAuthenticated) {
            if (!pinEntry.hasExistingPin() && !hasWallet) {
                activityNavigationUtil.navigateToStartActivity(this)
            } else if (!pinEntry.hasExistingPin() && hasWallet
                    && javaClass.name != CreatePinActivity::class.java.name) {
                startNewWalletFlow()
            } else if (pinEntry.hasExistingPin() && hasWallet) {
                authenticate()
            }
        }
    }

    fun onAuthenticationResult(resultCode: Int) {
        when (resultCode) {
            AppCompatActivity.RESULT_CANCELED -> {
                moveTaskToBack(true)
                finish()
            }
            AppCompatActivity.RESULT_OK -> if (!authentication.isAuthenticated) {
                authenticate()
            } else {
                broadcastAuthSuccessful()
            }
        }
    }

    fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onHealthSuccess() {
        scheduleHealthCheck()
        tearDownNoInternet()
    }

    override fun onHealthFail() {
        scheduleHealthCheck()
        onNoInternet()
    }

    private fun dismissAllDialogs() {
        for (fragment in activeFragments) {
            fragment.dismissAllowingStateLoss()
        }
    }

    private fun authenticate() {
        val intent = Intent(this, AuthenticateActivity::class.java)
        startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE)
    }

    private fun broadcastAuthSuccessful() {
        localBroadCastUtil.sendGlobalBroadcast(AuthenticationCompleteReceiver::class.java, DropbitIntents.ACTION_ON_USER_AUTH_SUCCESSFULLY)
    }

    protected fun startRecoveryFlow() {
        cnWalletManager.createWallet()
        activityNavigationUtil.navigateToRestoreWallet(this)
    }

    protected fun startInviteFlow() {
        val bundle = Bundle()
        bundle.putBoolean(DropbitIntents.EXTRA_HIDE_SKIP_BUTTON, true)
        startSignUpFlow(SignUpSelectionActivity::class.java.name, bundle)
    }

    protected fun startNewWalletFlow() {
        val bundle = Bundle()
        bundle.putBoolean(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON, true)
        startSignUpFlow(VerificationActivity::class.java.name, bundle)
    }

    private fun startSignUpFlow(className: String, bundle: Bundle?) {
        cnWalletManager.createWallet()
        val intent = Intent(this, CreatePinActivity::class.java)
        intent.putExtra(DropbitIntents.EXTRA_ON_COMPLETION, Intent(this, WalletCreationIntentService::class.java))
        intent.putExtra(DropbitIntents.EXTRA_NEXT, className)
        if (bundle != null) {
            intent.putExtra(DropbitIntents.EXTRA_NEXT_BUNDLE, bundle)
        }
        navigateTo(intent)
        finish()
    }

    private fun onCompletion() {
        if (intent == null || intent.extras == null || !intent.hasExtra(DropbitIntents.EXTRA_ON_COMPLETION)) {
            return
        }

        val completionIntent = intent.extras?.get(DropbitIntents.EXTRA_ON_COMPLETION) as Intent?
        if (completionIntent != null) {
            startService(completionIntent)
        }
    }

    private fun showNext(nextIntent: Intent) {
        finish()

        if (intent.hasExtra(DropbitIntents.EXTRA_NEXT_BUNDLE)) {
            val bundle = intent.getBundleExtra(DropbitIntents.EXTRA_NEXT_BUNDLE)
            nextIntent.replaceExtras(bundle)
        }

        startActivity(nextIntent)
    }

    private fun checkForInternalNotifications() {
        LocalBroadCastUtil(application).sendBroadcast(Intent(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE))
    }

    private fun tearDownNoInternet() {
        noInternetView?.let {
            (it.parent as ViewGroup).removeView(it)
            teardownMute()
        }
    }

    private fun onNoInternet() {
        if (findViewById<View>(R.id.id_no_internet_message) == null) {
            LayoutInflater.from(this).inflate(R.layout.no_internet_message, findViewById(R.id.message_queue))
            findViewById<View>(R.id.id_no_internet_message).findViewById<View>(R.id.component_message_action).setOnClickListener { onNetworkConfigClick() }
            muteViews()
        }
    }

    private fun onNetworkConfigClick() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun scheduleHealthCheck() {
        queue.postDelayed(healthCheckRunner, DropbitIntents.THIRTY_SECONDS)
    }

    companion object {

        internal const val AUTHENTICATION_REQUEST_CODE = 9999

        private val PIN_IGNORE_LIST = listOf<String>(
                CreatePinActivity::class.java.name,
                StartActivity::class.java.name,
                SignUpSelectionActivity::class.java.name,
                RestoreWalletActivity::class.java.name,
                RecoverWalletActivity::class.java.name,
                AuthenticateActivity::class.java.name,
                SplashActivity::class.java.name,
                TrainingActivity::class.java.name
        )

        private val MESSAGE_IGNORE_LIST = listOf<String>(
                VerifyPhoneVerificationCodeActivity::class.java.name,
                VerificationActivity::class.java.name,
                AuthenticateActivity::class.java.name,
                SignUpSelectionActivity::class.java.name
        )

        private val BASE_VIEWS = listOf(R.layout.cn_base_layout, R.layout.activity_market, R.layout.activity_home)

    }
}
