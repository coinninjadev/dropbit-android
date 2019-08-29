package com.coinninja.coinkeeper.cn.wallet

import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.receiver.WalletCreatedBroadCastReceiver
import com.coinninja.coinkeeper.service.client.CNUserAccount
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.analytics.AnalyticsBalanceRange
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class CNWalletManagerTest {
    companion object {
        private var invalidWords: Array<String> = arrayOf("word1", "word2", "word3", "word4", "word5", "word6", "word7", "word8", "word9", "word10", "word11", "word12")
        private var validWords: Array<String> = arrayOf("mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse")
    }

    private fun createManager(): CNWalletManager {
        val manager = CNWalletManager(mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock())
        whenever(manager.bitcoinUtil.isValidBIP39Words(validWords)).thenReturn(true)
        whenever(manager.bitcoinUtil.isValidBIP39Words(invalidWords)).thenReturn(false)
        whenever(manager.walletHelper.seedWords).thenReturn(validWords)
        whenever(manager.walletHelper.wallet).thenReturn(mock())

        return manager
    }

    @Test
    fun returns_false_if_wallet_last_sync_greater_than_0() {
        val manager = createManager()
        whenever(manager.walletHelper.wallet.lastSync).thenReturn(0L).thenReturn(System.currentTimeMillis())

        assertTrue(manager.isFirstSync)
        assertFalse(manager.isFirstSync)
    }

    @Test
    fun setsLastSyncTimeToNow() {
        val manager = createManager()
        val time = System.currentTimeMillis()
        whenever(manager.dateUtil.getCurrentTimeInMillis()).thenReturn(time)

        manager.syncCompleted()

        verify(manager.walletHelper.wallet).lastSync = time
        verify(manager.walletHelper.wallet).update()
    }

    @Test
    fun knows_no_wallet_exists_when_no_wallet_no_words() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(null).thenReturn(arrayOfNulls(0))
        assertFalse(manager.hasWallet)
        assertFalse(manager.hasWallet)
    }

    @Test
    fun knows_that_wallet_exists() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(validWords)

        assertTrue(manager.hasWallet)
    }

    @Test
    fun returns_false_when_skipped_backup_preference_not_recorded() {
        val manager = createManager()
        whenever(manager.preferencesUtil.contains(CNWalletManager.PREFERENCE_SKIPPED_BACKUP)).thenReturn(false)

        assertFalse(manager.hasSkippedBackup())
    }

    @Test
    fun returns_true_when_user_has_chosen_to_skip_backup() {
        val manager = createManager()
        whenever(manager.preferencesUtil.contains(CNWalletManager.PREFERENCE_SKIPPED_BACKUP)).thenReturn(true)

        assertTrue(manager.hasSkippedBackup())
    }

    @Test
    fun allows_user_to_skip_backup() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(arrayOfNulls(0))
        manager.skipBackup(validWords)

        verify(manager.walletHelper).saveWords(validWords)
        verify(manager.preferencesUtil).savePreference(CNWalletManager.PREFERENCE_SKIPPED_BACKUP, true)
        verify(manager.walletFlagsStorage).flags = WalletFlags.purpose49v1
    }

    @Test
    fun verifying_words_do_not_save_words_when_they_are_saved() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(validWords)

        manager.userVerifiedWords(validWords)

        verify(manager.walletHelper, times(0)).saveWords(validWords)
    }

    @Test
    fun verifying_words_saves_words() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(arrayOfNulls(0))

        manager.userVerifiedWords(validWords)

        verify(manager.walletHelper).saveWords(validWords)
    }

    @Test
    fun caches_addresses_after_wallet_saved() {
        val manager = createManager()
        val inOrder = inOrder(manager.walletHelper, manager.accountManager, manager.localBroadCastUtil)
        whenever(manager.walletHelper.seedWords).thenReturn(arrayOfNulls(0))

        manager.userVerifiedWords(validWords)

        inOrder.verify(manager.walletHelper).saveWords(validWords)
        inOrder.verify(manager.accountManager).cacheAddresses()
        inOrder.verify(manager.localBroadCastUtil).sendGlobalBroadcast(any(), any())
    }

    @Test
    fun report_that_user_has_backed_up_their_wallet() {
        val manager = createManager()
        manager.userVerifiedWords(validWords)

        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, true)
        verify(manager.analytics).flush()
    }

    @Test
    fun backing_up_wallet_removes_skipped_preference() {
        val manager = createManager()
        manager.userVerifiedWords(validWords)
        verify(manager.preferencesUtil).removePreference(CNWalletManager.PREFERENCE_SKIPPED_BACKUP)
    }

    @Test
    fun success_when_words_are_saved_test() {
        val manager = createManager()
        val sampleSeedWords = validWords
        whenever(manager.walletHelper.seedWords).thenReturn(arrayOfNulls(0))

        val savedSuccessfully = manager.saveSeedWords(sampleSeedWords)

        assertTrue(savedSuccessfully)
        verify(manager.bitcoinUtil).isValidBIP39Words(sampleSeedWords)
        verify(manager.walletHelper).saveWords(sampleSeedWords)
    }

    @Test
    fun success_when_words_are_already_in_the_list_test() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(validWords)
        whenever(manager.bitcoinUtil.isValidBIP39Words(validWords)).thenReturn(true)


        val savedSuccessfully = manager.saveSeedWords(validWords)

        assertTrue(savedSuccessfully)
        verify(manager.bitcoinUtil).isValidBIP39Words(validWords)
        verify(manager.walletHelper, times(0)).saveWords(any())
    }

    @Test
    fun fail_when_trying_to_save_invalid_words_test() {
        val manager = createManager()
        val sampleSeedWords = invalidWords

        val savedSuccessfully = manager.saveSeedWords(sampleSeedWords)

        assertFalse(savedSuccessfully)
        verify(manager.bitcoinUtil).isValidBIP39Words(sampleSeedWords)
        verify(manager.walletHelper, times(0)).saveWords(any())
    }

    @Test(expected = RuntimeException::class)
    fun crash_the_app_seed_words_already_saved_are_different_then_the_words_trying_to_save_test() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(invalidWords)
        val sampleSeedWords = validWords
        whenever(manager.bitcoinUtil.isValidBIP39Words(sampleSeedWords)).thenReturn(true)


        manager.saveSeedWords(sampleSeedWords)

        verify(manager.bitcoinUtil, times(0)).isValidBIP39Words(sampleSeedWords)
        verify(manager.walletHelper, times(0)).saveWords(any())
    }

    @Test
    fun can_generate_seed_words() {
        val manager = createManager()
        whenever(manager.seedWordGenerator.generate()).thenReturn(validWords)

        assertThat(manager.generateRecoveryWords(), equalTo(validWords))
    }

    @Test
    fun notifies_system_that_wallet_was_created() {
        val manager = createManager()
        whenever(manager.bitcoinUtil.isValidBIP39Words(validWords)).thenReturn(true)
        whenever(manager.walletHelper.seedWords).thenReturn(arrayOfNulls(0))

        manager.saveSeedWords(validWords)

        verify(manager.localBroadCastUtil).sendGlobalBroadcast(WalletCreatedBroadCastReceiver::class.java, DropbitIntents.ACTION_WALLET_CREATED)
    }

    @Test
    fun returns_true_when_user_has_btc_balance() {
        val manager = createManager()
        whenever(manager.walletHelper.balance).thenReturn(100L)

        assertTrue(manager.hasBalance)

        whenever(manager.walletHelper.balance).thenReturn(0L)
        assertFalse(manager.hasBalance)
    }

    @Test
    fun returns_users_phone_number_from_account() {
        val manager = createManager()
        whenever(manager.walletHelper.userAccount).thenReturn(mock())
        val phoneNumber = mock<PhoneNumber>()
        whenever(manager.walletHelper.userAccount.phoneNumber).thenReturn(phoneNumber)

        val actualPhone = manager.contact.phoneNumber
        assertThat(actualPhone, equalTo(phoneNumber))
    }

    @Test
    fun deVerifiesUsersAccount() {
        val manager = createManager()
        manager.deVerifyAccount()

        verify(manager.walletHelper).removeCurrentCnUserRegistration()
        verify(manager.inviteTransactionSummaryHelper).cancelPendingSentInvites()
        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false)
        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false)
        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_TWITTER_VERIFIED, false)
        verify(manager.myTwitterProfile).clear()
        verify(manager.analytics).flush()
    }

    @Test
    fun updating_wallet_balances_notifies_balance_change() {
        val manager = createManager()

        manager.updateBalances()

        verify(manager.walletHelper).updateBalances()
        verify(manager.walletHelper).updateSpendableBalances()
        verify(manager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE)
    }

    @Test
    fun track_balance_ranges__no_balance() {
        val manager = createManager()
        whenever(manager.walletHelper.balance).thenReturn(0)

        manager.updateBalances()

        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, false)
        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_RELATIVE_WALLET_RANGE, AnalyticsBalanceRange.NONE.label)
    }

    @Test
    fun track_balance_ranges__with_balance() {
        val manager = createManager()
        whenever(manager.walletHelper.balance).thenReturn(99_999)

        manager.updateBalances()

        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, true)
        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_RELATIVE_WALLET_RANGE, AnalyticsBalanceRange.UNDER_MILLI_BTC.label)
    }

    @Test
    fun updates_account_from_cn_user_account() {
        val manager = createManager()
        whenever(manager.walletHelper.userAccount).thenReturn(mock())
        val account = CNUserAccount()

        manager.updateAccount(account)

        verify(manager.walletHelper).saveAccountRegistration(account)
    }
}