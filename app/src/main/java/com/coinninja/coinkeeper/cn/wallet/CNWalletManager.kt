package com.coinninja.coinkeeper.cn.wallet

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.SeedWordGenerator
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.db.Account
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.receiver.WalletCreatedBroadCastReceiver
import com.coinninja.coinkeeper.service.client.CNUserAccount
import com.coinninja.coinkeeper.twitter.MyTwitterProfile
import com.coinninja.coinkeeper.util.DateUtil
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.analytics.AnalyticsBalanceRange
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import javax.inject.Inject

@Mockable
class CNWalletManager @Inject internal constructor(
        internal val walletHelper: WalletHelper,
        internal val bitcoinUtil: BitcoinUtil,
        internal val accountManager: AccountManager,
        internal val preferencesUtil: PreferencesUtil,
        internal val seedWordGenerator: SeedWordGenerator,
        internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
        internal val localBroadCastUtil: LocalBroadCastUtil,
        internal val dateUtil: DateUtil,
        internal val analytics: Analytics,
        internal val myTwitterProfile: MyTwitterProfile
) {

    val hasBalance: Boolean get() = walletHelper.balance.toLong() > 0L
    val hasWallet: Boolean get() = walletHelper.seedWords != null && walletHelper.seedWords!!.size == 12
    val account: Account get() = walletHelper.userAccount

    val recoveryWords: Array<String>? get() = if (!hasWallet) null else walletHelper.seedWords

    val isFirstSync: Boolean get() = walletHelper.wallet.lastSync <= 0L

    val contact: Contact
        get() {
            val userAccount = walletHelper.userAccount
            return Contact(userAccount.phoneNumber, "", true)
        }

    fun createWallet() = walletHelper.createWallet()

    fun generateRecoveryWords(): Array<String> = seedWordGenerator.generate()

    fun hasSkippedBackup(): Boolean = preferencesUtil.contains(PREFERENCE_SKIPPED_BACKUP)

    fun userVerifiedWords(recoveryWords: Array<String>): Boolean = saveSeedWords(recoveryWords).also {
        preferencesUtil.removePreference(PREFERENCE_SKIPPED_BACKUP)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, true)
        analytics.flush()
    }

    fun updateAccount(userAccount: CNUserAccount): Account {
        walletHelper.saveAccountRegistration(userAccount)
        return account
    }

    fun skipBackup(recoveryWords: Array<String>) {
        saveSeedWords(recoveryWords)
        preferencesUtil.savePreference(PREFERENCE_SKIPPED_BACKUP, true)
        analytics.setUserProperty(Analytics.PROPERTY_APP_V1, true)
    }

    fun syncCompleted() {
        val wallet = walletHelper.wallet
        wallet.lastSync = dateUtil.getCurrentTimeInMillis()
        wallet.update()
    }

    fun updateBalances() {
        walletHelper.updateBalances()
        walletHelper.updateSpendableBalances()
        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, hasBalance)
        analytics.setUserProperty(Analytics.PROPERTY_RELATIVE_WALLET_RANGE, AnalyticsBalanceRange.fromBalance(walletHelper.balance.toLong()).label)
    }

    fun deVerifyAccount() {
        walletHelper.removeCurrentCnUserRegistration()
        inviteTransactionSummaryHelper.cancelPendingSentInvites()
        myTwitterProfile.clear()
        analytics.setUserProperty(Analytics.PROPERTY_TWITTER_VERIFIED, false)
        analytics.setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false)
        analytics.flush()
    }

    private fun wordListIsValid(recoveryWords: Array<String>): Boolean = bitcoinUtil.isValidBIP39Words(recoveryWords)

    internal fun saveSeedWords(recoveryWords: Array<String>): Boolean {
        return if (!wordListIsValid(recoveryWords)) {
            false
        } else if (isAlreadySaved(recoveryWords)) {
            true
        } else {
            walletHelper.saveWords(recoveryWords)
            accountManager.cacheAddresses()
            localBroadCastUtil.sendGlobalBroadcast(WalletCreatedBroadCastReceiver::class.java, DropbitIntents.ACTION_WALLET_CREATED)
            true
        }
    }

    private fun isAlreadySaved(recoveryWords: Array<String>): Boolean {
        return if (hasWallet) {
            val words = listOf(*recoveryWords)
            val savedSeedWords = walletHelper.seedWords
            if (!words.containsAll(listOf(*savedSeedWords!!))) {
                throw RuntimeException("There are words already saved but do not match the words you are currently trying to save")
            }
            true
        } else {
            false
        }
    }

    companion object {

        internal val PREFERENCE_SKIPPED_BACKUP = "preference_skipped_backup"

        fun calcConfirmations(currentBlockHeight: Int, transactionBlock: Int): Int {
            return currentBlockHeight - transactionBlock + 1
        }
    }
}
