package com.coinninja.coinkeeper.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.coinninja.coinkeeper.R.id
import com.coinninja.coinkeeper.R.layout
import com.coinninja.coinkeeper.receiver.StartupCompleteReceiver
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents

class SplashActivity : BaseActivity() {

    internal var displayDelayRunnable = Runnable { runOnUiThread { showNextActivity() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_splash)
        window.exitTransition = null
        window.enterTransition = null
        authentication.forceDeAuthenticate()
    }

    override fun onResume() {
        super.onResume()
        findViewById<View>(id.img_logo).postDelayed(displayDelayRunnable, 500)
    }

    public override fun onPause() {
        super.onPause()
        findViewById<View>(id.img_logo).removeCallbacks(displayDelayRunnable)
    }

    override fun onStart() {
        super.onStart()
        localBroadCastUtil.sendGlobalBroadcast(StartupCompleteReceiver::class.java,
                DropbitIntents.ACTION_ON_APPLICATION_FOREGROUND_STARTUP)
    }

    @SuppressLint("NewApi")
    private fun showNextActivity() =
            when {
                cnWalletManager.hasWallet && cnWalletManager.isSegwitUpgradeRequired -> {
                    activityNavigationUtil.navigateToUpgradeToSegwit(this)
                }
                cnWalletManager.hasWallet && !cnWalletManager.isSegwitUpgradeRequired -> {
                    activityNavigationUtil.navigateToHome(this)
                }
                else -> activityNavigationUtil.navigateToStartActivity(this)

            }
}
