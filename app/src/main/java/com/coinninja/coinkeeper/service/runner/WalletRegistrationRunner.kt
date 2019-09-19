package com.coinninja.coinkeeper.service.runner

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
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
        internal val hdWallet: HDWalletWrapper,
        internal val walletHelper: WalletHelper,
        internal val logger: CNLogger,
        internal val analytics: Analytics,
        internal val walletFlagsStorage: WalletFlagsStorage

) : Runnable {

    override fun run() {
        if (walletHelper.hasAccount()) {
            notifyOfWalletRegistrationCompleted(walletFlagsStorage.flags)
            return
        }

        val walletFlags = walletFlagsStorage.flags
        val response = apiClient.registerWallet(WalletRegistrationPayload(hdWallet.verificationKey, walletFlags))
        if (response.code() == 200 || response.code() == 201) {
            processSuccess(response, walletFlags)
        } else {
            logger.logError(TAG, "|---- create user failed", response)
        }
    }

    internal fun processSuccess(response: Response<CNWallet>, currentFlags: Long) {
        walletHelper.saveRegistration(response.body())

        val serverFlags = response.body()?.flags ?: 0

        if (serverFlags != currentFlags)
            walletFlagsStorage.flags = serverFlags

        analytics.setUserProperty(Analytics.PROPERTY_WALLET_VERSION, WalletFlags(serverFlags).versionBit)
        notifyOfWalletRegistrationCompleted(serverFlags)
    }

    internal fun notifyOfWalletRegistrationCompleted(flags: Long) {
        val walletFlags = WalletFlags(flags)
        when {
            walletFlags.isActive() && !walletFlags.hasVersion(WalletFlags.v2) ->
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_WALLET_REQUIRES_UPGRADE)
            !walletFlags.isActive() && !walletFlags.hasVersion(WalletFlags.v2) ->
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_WALLET_ALREADY_UPGRADED)
            else -> {
                localBroadCastUtil.sendGlobalBroadcast(WalletRegistrationCompleteReceiver::class.java, DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE)
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE)
            }
        }
    }

    companion object {

        private val TAG = WalletRegistrationRunner::class.java.name
    }
}
