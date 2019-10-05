package com.coinninja.coinkeeper.ui.segwit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.DateUtil
import app.dropbit.commons.util.isNotNull
import app.dropbit.commons.util.toRFC3339
import com.coinninja.coinkeeper.bitcoin.BroadcastResult
import com.coinninja.coinkeeper.bitcoin.TransactionBroadcaster
import com.coinninja.coinkeeper.cn.account.RemoteAddressCache
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.WalletFlags
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNWallet
import com.coinninja.coinkeeper.service.client.model.ReplaceWalletRequest
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep

@Mockable
class WalletUpgradeViewModel : ViewModel() {

    val upgradeState: MutableLiveData<UpgradeState> = MutableLiveData()

    internal lateinit var cnWalletManager: CNWalletManager
    internal lateinit var syncWalletManager: SyncWalletManager
    internal lateinit var dropbitAccountHelper: DropbitAccountHelper
    internal lateinit var remoteAddressCache: RemoteAddressCache
    internal lateinit var transactionBroadcaster: TransactionBroadcaster
    internal lateinit var hdWalletWrapper: HDWalletWrapper
    internal lateinit var cnClient: SignedCoinKeeperApiClient
    internal lateinit var serviceWorkUtil: ServiceWorkUtil
    internal lateinit var dateUtil: DateUtil

    internal var delay: Long = 700

    fun performStepOne() {
        viewModelScope.launch { updateState(executeStepOne()) }
    }

    fun performStepTwo() {
        viewModelScope.launch { updateState(executeStepTwo()) }
    }

    fun performStepThree(transactionData: TransactionData? = null) {
        viewModelScope.launch { updateState(executeStepThree(transactionData)) }
    }

    fun cleanUp() {
        viewModelScope.launch { updateState(executeCleanup()) }
    }

    internal suspend fun executeStepOne() = withContext(Dispatchers.IO) {
        gotoSleep()
        UpgradeState.StepOneCompleted
    }


    internal suspend fun executeStepTwo() = withContext(Dispatchers.IO) {
        if (dropbitAccountHelper.hasVerifiedAccount) {
            remoteAddressCache.removeAll()
        }

        gotoSleep()
        UpgradeState.StepTwoCompleted
    }

    internal suspend fun executeStepThree(transactionData: TransactionData?) = withContext(Dispatchers.IO) {
        val transferSuccess = if (transactionData.isNotNull()) {
            val broadcastResult = executeTransferWithRetry(transactionData!!)
            broadcastResult.isSuccess
        } else {
            true
        }

        if (transferSuccess) {
            executeWalletReplacement()
            gotoSleep()
            UpgradeState.StepThreeCompleted
        } else {
            UpgradeState.Error
        }
    }

    internal fun executeWalletReplacement() {
        cnClient.disableWallet()
        if (dropbitAccountHelper.hasVerifiedAccount) {
            val segwitWallet = cnWalletManager.segwitWalletForUpgrade
            val timestamp = dateUtil.timeInMillis().toRFC3339()
            cnClient.replaceWalletWith(
                    ReplaceWalletRequest(
                            publicKey = hdWalletWrapper.verificationKeyFor(segwitWallet),
                            timestamp = timestamp,
                            flags = WalletFlags.purpose84v2,
                            signature = hdWalletWrapper.sign(segwitWallet, timestamp)
                    )
            ).let {
                if (it.isSuccessful) {
                    val cnWallet = it.body() as CNWallet
                    dropbitAccountHelper.updateAccountIds(cnWallet.id)
                }
            }

        }
        cnWalletManager.replaceLegacyWithSegwit()
    }

    internal fun executeTransferWithRetry(transactionData: TransactionData, attempt: Int = 1): BroadcastResult {
        if (attempt > 3) return BroadcastResult()

        return transactionData.let { data ->
            transactionBroadcaster.broadcast(hdWalletWrapper.transactionFrom(data)).let { broadcastResult ->
                if (!broadcastResult.isSuccess) {
                    executeTransferWithRetry(transactionData, attempt + 1)
                } else {
                    broadcastResult
                }
            }
        }
    }

    internal suspend fun executeCleanup() = withContext(Dispatchers.IO) {
        syncWalletManager.syncNow()
        syncWalletManager.schedule60SecondSync()
        serviceWorkUtil.registerForPushNotifications()
        gotoSleep()
        UpgradeState.Finished
    }

    private fun gotoSleep() {
        if (delay > 0) sleep(delay)
    }

    internal suspend fun updateState(state: UpgradeState) = withContext(Dispatchers.Main) {
        upgradeState.value = state
    }
}