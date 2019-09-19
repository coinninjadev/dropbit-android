package com.coinninja.coinkeeper.service

import android.app.IntentService
import android.content.Intent
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.cn.service.PushNotificationDeviceManager
import com.coinninja.coinkeeper.cn.service.PushNotificationEndpointManager
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.twitter.MyTwitterProfile
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import dagger.android.AndroidInjection
import javax.inject.Inject

class DeleteWalletService @JvmOverloads constructor(name: String = TAG) : IntentService(name) {
    @Inject
    internal lateinit var cnWalletManager: CNWalletManager
    @Inject
    internal lateinit var pushNotificationDeviceManager: PushNotificationDeviceManager
    @Inject
    internal lateinit var pushNotificationEndpointManager: PushNotificationEndpointManager
    @Inject
    internal lateinit var apiClient: SignedCoinKeeperApiClient
    @Inject
    internal lateinit var analytics: Analytics
    @Inject
    internal lateinit var localBroadCastUtil: LocalBroadCastUtil
    @Inject
    internal lateinit var application: CoinKeeperApplication
    @Inject
    internal lateinit var syncWalletManager: SyncWalletManager
    @Inject
    internal lateinit var myTwitterProfile: MyTwitterProfile

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        localBroadCastUtil = LocalBroadCastUtil(this)
    }

    public override fun onHandleIntent(intent: Intent?) {
        syncWalletManager.cancelAllScheduledSync()
        if (pushNotificationEndpointManager.hasEndpoint()) {
            pushNotificationEndpointManager.unRegister()
            pushNotificationEndpointManager.removeEndpoint()
        }
        pushNotificationDeviceManager.removeCNDevice()
        apiClient.resetWallet()
        cnWalletManager.deleteWallet()
        resetAnalyticsProperties()
        myTwitterProfile.clear()
        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_ON_WALLET_DELETED)
    }

    private fun resetAnalyticsProperties() {
        analytics.trackEvent(Analytics.EVENT_WALLET_DELETE)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_WALLET, false)
        analytics.setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, false)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, false)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false)
        analytics.setUserProperty(Analytics.PROPERTY_TWITTER_VERIFIED, false)
        analytics.flush()
    }

    companion object {
        val TAG = DeleteWalletService::class.java.name
    }

}
