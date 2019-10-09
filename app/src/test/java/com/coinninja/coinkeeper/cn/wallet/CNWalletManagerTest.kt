package com.coinninja.coinkeeper.cn.wallet

import app.dropbit.commons.currency.BTCCurrency
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.receiver.WalletCreatedBroadCastReceiver
import com.coinninja.coinkeeper.service.client.CNUserAccount
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.analytics.AnalyticsBalanceRange
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
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
        whenever(manager.walletHelper.primaryWallet).thenReturn(mock())
        whenever(manager.walletHelper.balance).thenReturn(BTCCurrency(0L))
        whenever(manager.walletConfiguration.walletConfigurationFlags).thenReturn(18)
        whenever(manager.seedWordGenerator.generate()).thenReturn(validWords)

        return manager
    }

    @Test
    fun provides_access_to_check_if_wallet_upgrade_required() {
        val manager = createManager()

        whenever(manager.walletHelper.seedWords).thenReturn(null).thenReturn(validWords)
        whenever(manager.walletHelper.primaryWallet.purpose).thenReturn(49).thenReturn(84)
        whenever(manager.walletConfiguration.purpose).thenReturn(84)

        assertThat(manager.isSegwitUpgradeRequired).isFalse()
        assertThat(manager.isSegwitUpgradeRequired).isTrue()
        assertThat(manager.isSegwitUpgradeRequired).isFalse()

    }

    @Test
    fun provides_access_to_segwit_wallet_during_update() {
        val manager = createManager()
        val segwitWallet: Wallet = mock()
        whenever(manager.walletHelper.getOrCreateSegwitWalletForUpdate(validWords)).thenReturn(segwitWallet)

        assertThat(manager.segwitWalletForUpgrade).isEqualTo(segwitWallet)
    }

    @Test
    fun returns_false_if_wallet_last_sync_greater_than_0() {
        val manager = createManager()
        whenever(manager.walletHelper.primaryWallet.lastSync).thenReturn(0L).thenReturn(System.currentTimeMillis())

        assertTrue(manager.isFirstSync)
        assertFalse(manager.isFirstSync)
    }

    @Test
    fun setsLastSyncTimeToNow() {
        val manager = createManager()
        val time = System.currentTimeMillis()
        whenever(manager.dateUtil.getCurrentTimeInMillis()).thenReturn(time)

        manager.syncCompleted()

        verify(manager.walletHelper.primaryWallet).lastSync = time
        verify(manager.walletHelper.primaryWallet).update()
    }

    @Test
    fun knows_no_wallet_exists_when_no_wallet_no_words() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(null).thenReturn(emptyArray())
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
        val wallet:Wallet = mock()
        whenever(manager.walletHelper.primaryWallet).thenReturn(wallet)
        whenever(manager.walletHelper.seedWords).thenReturn(emptyArray())
        manager.skipBackup(validWords)

        verify(manager.walletHelper).saveWords(validWords)
        verify(manager.preferencesUtil).savePreference(CNWalletManager.PREFERENCE_SKIPPED_BACKUP, true)

        val ordered = inOrder(wallet)
        ordered.verify(wallet).flags = 18
        ordered.verify(wallet).update()
    }

    @Test
    fun verifying_words_do_not_save_words_when_they_are_already_saved() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(validWords)

        manager.userVerifiedWords(validWords)

        verify(manager.walletHelper, times(0)).saveWords(validWords)
    }

    @Test
    fun verifying_words_saves_words() {
        val manager = createManager()
        whenever(manager.walletHelper.seedWords).thenReturn(emptyArray())
        val wallet:Wallet = mock()
        whenever(manager.walletHelper.primaryWallet).thenReturn(wallet)

        manager.userVerifiedWords(validWords)

        verify(manager.walletHelper).saveWords(validWords)

        val ordered = inOrder(wallet)
        ordered.verify(wallet).flags = 18
        ordered.verify(wallet).update()
    }

    @Test
    fun caches_addresses_after_wallet_saved() {
        val manager = createManager()
        val inOrder = inOrder(manager.walletHelper, manager.accountManager, manager.localBroadCastUtil)
        whenever(manager.walletHelper.seedWords).thenReturn(emptyArray())

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
        whenever(manager.walletHelper.seedWords).thenReturn(emptyArray())

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

        assertThat(manager.generateRecoveryWords()).isEqualTo(validWords)
    }

    @Test
    fun notifies_system_that_wallet_was_created() {
        val manager = createManager()
        whenever(manager.bitcoinUtil.isValidBIP39Words(validWords)).thenReturn(true)
        whenever(manager.walletHelper.seedWords).thenReturn(emptyArray())

        manager.saveSeedWords(validWords)

        verify(manager.localBroadCastUtil).sendGlobalBroadcast(WalletCreatedBroadCastReceiver::class.java, DropbitIntents.ACTION_WALLET_CREATED)
    }

    @Test
    fun returns_true_when_user_has_btc_balance() {
        val manager = createManager()
        whenever(manager.walletHelper.balance).thenReturn(BTCCurrency(100L))

        assertTrue(manager.hasBalance)

        whenever(manager.walletHelper.balance).thenReturn(BTCCurrency(0L))
        assertFalse(manager.hasBalance)
    }

    @Test
    fun returns_users_phone_number_from_account() {
        val manager = createManager()
        whenever(manager.walletHelper.userAccount).thenReturn(mock())
        val phoneNumber = mock<PhoneNumber>()
        whenever(manager.walletHelper.userAccount.phoneNumber).thenReturn(phoneNumber)

        val actualPhone = manager.contact.phoneNumber
        assertThat(actualPhone).isEqualTo(phoneNumber)
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
        whenever(manager.walletHelper.balance).thenReturn(BTCCurrency(0))

        manager.updateBalances()

        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, false)
        verify(manager.analytics).setUserProperty(Analytics.PROPERTY_RELATIVE_WALLET_RANGE, AnalyticsBalanceRange.NONE.label)
    }

    @Test
    fun track_balance_ranges__with_balance() {
        val manager = createManager()
        whenever(manager.walletHelper.balance).thenReturn(BTCCurrency(99_999))

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

    @Test
    fun expose_marking_backup_as_skipped() {
        val manager = createManager()

        manager.markWalletBackupAsSkipped()

        verify(manager.preferencesUtil).savePreference(CNWalletManager.PREFERENCE_SKIPPED_BACKUP, true)
    }

    @Test
    fun upgrades_to_segwit() {
        val manager = createManager()
        val segwitWallet: Wallet = mock()
        val legacyWallet: Wallet = mock()
        whenever(manager.walletHelper.getOrCreateSegwitWalletForUpdate(any())).thenReturn(segwitWallet)
        whenever(manager.walletHelper.primaryWallet).thenReturn(legacyWallet)

        manager.replaceLegacyWithSegwit()

        verify(manager.preferencesUtil).savePreference(CNWalletManager.PREFERENCE_SKIPPED_BACKUP, true)
        verify(manager.walletHelper).rotateWallets(segwitWallet, legacyWallet)
    }

    @Test
    fun has_legacy_wallet_when_primary_is_segwit_and_legacy_is_present() {
        val manager = createManager()
        val legacyWallet: Wallet = mock()
        whenever(legacyWallet.purpose).thenReturn(49)
        val segwitWallet: Wallet = mock()
        whenever(segwitWallet.purpose).thenReturn(84)
        whenever(manager.walletHelper.primaryWallet)
                .thenReturn(null)
                .thenReturn(legacyWallet)
                .thenReturn(segwitWallet)

        whenever(manager.walletHelper.legacyWallet).thenReturn(legacyWallet)

        assertThat(manager.hasLegacyWallet).isFalse()
        assertThat(manager.hasLegacyWallet).isFalse()
        assertThat(manager.hasLegacyWallet).isTrue()
    }

    @Test
    fun delete_wallet_removes_all_data_and_clears_preferences() {
        val manager = createManager()

        manager.deleteWallet()

        verify(manager.walletHelper).deleteAll()
        verify(manager.preferencesUtil).removeAll()
    }
}