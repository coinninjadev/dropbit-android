package com.coinninja.coinkeeper.service.runner

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.DataSigner
import com.coinninja.coinkeeper.cn.wallet.WalletFlags
import com.coinninja.coinkeeper.cn.wallet.WalletFlagsStorage
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.receiver.WalletRegistrationCompleteReceiver
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNWallet
import com.coinninja.coinkeeper.service.client.model.WalletRegistrationPayload
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import retrofit2.Response
import javax.inject.Inject

@Mockable
class WalletRegistrationRunner @Inject constructor(
        internal val localBroadCastUtil: LocalBroadCastUtil,
        internal val apiClient: SignedCoinKeeperApiClient,
        internal val dataSigner: DataSigner,
        internal val walletHelper: WalletHelper,
        internal val logger: CNLogger,
        internal val analytics: Analytics,
        internal val walletFlagsStorage: WalletFlagsStorage

) : Runnable {

    override fun run() {
        if (walletHelper.hasAccount()) {
            notifyOfWalletRegistrationCompleted()
            return
        }

        val walletFlags = walletFlagsStorage.flags
        val response = apiClient.registerWallet(WalletRegistrationPayload(dataSigner.coinNinjaVerificationKey, walletFlags))
        if (response.code() == 200 || response.code() == 201) {
            processSuccess(response, walletFlags)
        } else {
            logger.logError(TAG, "|---- create user failed", response)
        }
    }

    internal fun processSuccess(response: Response<CNWallet>, currentFlags: Long) {
        walletHelper.saveRegistration(response.body())
        notifyOfWalletRegistrationCompleted()

        val serverFlags = response.body()?.flags ?: 0

        if (serverFlags != currentFlags)
            walletFlagsStorage.flags = serverFlags

        val walletVersion = WalletFlags(serverFlags).versionBit
        if (walletVersion > 0) {
            analytics.setUserProperty(Analytics.PROPERTY_APP_V1, true)
        } else {
            analytics.setUserProperty(Analytics.PROPERTY_APP_V1, false)
        }
    }

    private fun notifyOfWalletRegistrationCompleted() {
        localBroadCastUtil.sendGlobalBroadcast(WalletRegistrationCompleteReceiver::class.java, DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE)
    }

    companion object {

        private val TAG = WalletRegistrationRunner::class.java.name
    }
}
