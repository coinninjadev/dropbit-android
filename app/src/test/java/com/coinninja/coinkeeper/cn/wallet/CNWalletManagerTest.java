package com.coinninja.coinkeeper.cn.wallet;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.receiver.WalletCreatedBroadCastReceiver;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CNWalletManagerTest {
    private String[] invalid_words = {"word1", "word2", "word3", "word4", "word5", "word6",
            "word7", "word8", "word9", "word10", "word11", "word12"};
    private String[] valid_words = {"mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse",
            "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse"};

    @Mock
    private DateUtil dateUtil;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private BitcoinUtil bitcoinUtil;

    @Mock
    private PreferencesUtil preferencesUtil;

    @Mock
    private SeedWordGenerator seedWordGenerator;

    @Mock
    private Analytics analytics;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private AccountManager accountManager;

    @Mock
    private TransactionHelper transactionHelper;

    @Mock
    PhoneNumberUtil phoneNumberUtil;

    @InjectMocks
    private CNWalletManager cnWalletManager;

    private PhoneNumber phoneNumber;

    @Before
    public void setUp() throws Exception {
        when(bitcoinUtil.isValidBIP39Words(valid_words)).thenReturn(true);
        when(bitcoinUtil.isValidBIP39Words(invalid_words)).thenReturn(false);
        when(walletHelper.getSeedWords()).thenReturn(valid_words);
        phoneNumber = new PhoneNumber("+12223334444");
    }

    @After
    public void tearDown() {
        invalid_words = null;
        valid_words = null;
        dateUtil = null;
        walletHelper = null;
        bitcoinUtil = null;
        preferencesUtil = null;
        seedWordGenerator = null;
        analytics = null;
        localBroadCastUtil = null;
        cnWalletManager = null;
        accountManager = null;
    }

    @Test
    public void returns_false_if_wallet_last_sync_greater_than_0() {
        long time = System.currentTimeMillis();
        Wallet wallet = mock(Wallet.class);
        when(walletHelper.getWallet()).thenReturn(wallet);
        when(wallet.getLastSync()).thenReturn(0L).thenReturn(time);

        assertTrue(cnWalletManager.isFirstSync());
        assertFalse(cnWalletManager.isFirstSync());
    }

    @Test
    public void setsLastSyncTimeToNow() {
        long time = System.currentTimeMillis();
        Wallet wallet = mock(Wallet.class);
        when(dateUtil.getCurrentTimeInMillis()).thenReturn(time);
        when(walletHelper.getWallet()).thenReturn(wallet);

        cnWalletManager.syncCompleted();

        verify(wallet).setLastSync(time);
        verify(wallet).update();
    }


    @Test
    public void knows_that_wallet_exists() {
        when(walletHelper.getSeedWords()).thenReturn(valid_words);

        assertTrue(cnWalletManager.hasWallet());
    }

    @Test
    public void knows_that_wallet_does_not_exists() {
        when(walletHelper.getSeedWords()).thenReturn(new String[0]);

        assertFalse(cnWalletManager.hasWallet());
    }

    @Test
    public void returns_false_when_skipped_backup_preference_not_recorded() {
        when(preferencesUtil.contains(CNWalletManager.PREFERENCE_SKIPPED_BACKUP)).thenReturn(false);

        assertFalse(cnWalletManager.hasSkippedBackup());
    }

    @Test
    public void returns_true_when_user_has_chosen_to_skip_backup() {
        when(preferencesUtil.contains(CNWalletManager.PREFERENCE_SKIPPED_BACKUP)).thenReturn(true);

        assertTrue(cnWalletManager.hasSkippedBackup());
    }

    @Test
    public void allows_user_to_skip_backup() {
        when(walletHelper.getSeedWords()).thenReturn(new String[0]);
        cnWalletManager.skipBackup(valid_words);

        verify(walletHelper).saveWords(valid_words);
        verify(preferencesUtil).savePreference(CNWalletManager.PREFERENCE_SKIPPED_BACKUP, true);
    }

    @Test
    public void verifying_words_do_not_save_words_when_they_are_saved() {
        when(walletHelper.getSeedWords()).thenReturn(valid_words);

        cnWalletManager.userVerifiedWords(valid_words);

        verify(walletHelper, times(0)).saveWords(valid_words);
    }

    @Test
    public void verifying_words_saves_words() {
        when(walletHelper.getSeedWords()).thenReturn(new String[0]);

        cnWalletManager.userVerifiedWords(valid_words);

        verify(walletHelper).saveWords(valid_words);
    }

    @Test
    public void caches_addresses_after_wallet_saved() {
        InOrder inOrder = inOrder(walletHelper, accountManager, localBroadCastUtil);
        when(walletHelper.getSeedWords()).thenReturn(new String[0]);

        cnWalletManager.userVerifiedWords(valid_words);

        inOrder.verify(walletHelper).saveWords(valid_words);
        inOrder.verify(accountManager).cacheAddresses();
        inOrder.verify(localBroadCastUtil).sendGlobalBroadcast(eq(WalletCreatedBroadCastReceiver.class), anyString());
    }

    @Test
    public void report_that_user_has_backed_up_their_wallet() {
        cnWalletManager.userVerifiedWords(valid_words);

        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, true);
        verify(analytics).flush();
    }

    @Test
    public void backing_up_wallet_removes_skipped_preference() {
        cnWalletManager.userVerifiedWords(valid_words);
        verify(preferencesUtil).removePreference(CNWalletManager.PREFERENCE_SKIPPED_BACKUP);
    }

    @Test
    public void success_when_words_are_saved_test() {
        String[] sampleSeedWords = valid_words;
        when(walletHelper.getSeedWords()).thenReturn(new String[0]);

        boolean savedSuccessfully = cnWalletManager.saveSeedWords(sampleSeedWords);

        assertTrue(savedSuccessfully);
        verify(bitcoinUtil).isValidBIP39Words(sampleSeedWords);
        verify(walletHelper).saveWords(sampleSeedWords);
    }

    @Test
    public void success_when_words_are_already_in_the_list_test() {
        when(walletHelper.getSeedWords()).thenReturn(valid_words);
        when(bitcoinUtil.isValidBIP39Words(valid_words)).thenReturn(true);


        boolean savedSuccessfully = cnWalletManager.saveSeedWords(valid_words);

        assertTrue(savedSuccessfully);
        verify(bitcoinUtil).isValidBIP39Words(valid_words);
        verify(walletHelper, times(0)).saveWords(any());
    }

    @Test
    public void fail_when_trying_to_save_invalid_words_test() {
        String[] sampleSeedWords = invalid_words;

        boolean savedSuccessfully = cnWalletManager.saveSeedWords(sampleSeedWords);

        assertFalse(savedSuccessfully);
        verify(bitcoinUtil).isValidBIP39Words(sampleSeedWords);
        verify(walletHelper, times(0)).saveWords(any());
    }

    @Test(expected = RuntimeException.class)
    public void crash_the_app_seed_words_already_saved_are_different_then_the_words_trying_to_save_test() {
        when(walletHelper.getSeedWords()).thenReturn(invalid_words);
        String[] sampleSeedWords = valid_words;
        when(bitcoinUtil.isValidBIP39Words(sampleSeedWords)).thenReturn(true);


        cnWalletManager.saveSeedWords(sampleSeedWords);

        verify(bitcoinUtil, times(0)).isValidBIP39Words(sampleSeedWords);
        verify(walletHelper, times(0)).saveWords(any());
    }

    @Test
    public void can_generate_seed_words() {
        when(seedWordGenerator.generate()).thenReturn(valid_words);

        assertThat(cnWalletManager.generateRecoveryWords(), equalTo(valid_words));
    }

    @Test
    public void notifies_system_that_wallet_was_created() {
        when(bitcoinUtil.isValidBIP39Words(valid_words)).thenReturn(true);
        when(walletHelper.getSeedWords()).thenReturn(new String[0]);

        cnWalletManager.saveSeedWords(valid_words);

        verify(localBroadCastUtil).
                sendGlobalBroadcast(WalletCreatedBroadCastReceiver.class, Intents.ACTION_WALLET_CREATED);
    }

    @Test
    public void returns_true_when_user_has_btc_balance() {
        Wallet wallet = mock(Wallet.class);
        when(wallet.getBalance()).thenReturn(100L);
        when(walletHelper.getWallet()).thenReturn(wallet);

        assertTrue(cnWalletManager.hasBalance());

        when(wallet.getBalance()).thenReturn(0L);
        assertFalse(cnWalletManager.hasBalance());
    }

    @Test
    public void returns_users_phone_number_from_account() {
        Account account = mock(Account.class);
        when(walletHelper.getUserAccount()).thenReturn(account);
        when(account.getPhoneNumber()).thenReturn(phoneNumber);

        PhoneNumber actualPhone = cnWalletManager.getContact().getPhoneNumber();
        assertThat(actualPhone, equalTo(this.phoneNumber));
    }

    @Test
    public void deverifiesUsersAccount() {
        cnWalletManager.deverifyAccount();

        verify(walletHelper).removeCurrentCnUserRegistration();
        verify(transactionHelper).cancelPendingSentInvites();
        verify(analytics).setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false);
        verify(analytics).flush();
    }

    @Test
    public void updating_wallet_balances_notifies_balance_change() {
        cnWalletManager.updateBalances();

        verify(walletHelper).updateBalances();
        verify(walletHelper).updateSpendableBalances();
        verify(localBroadCastUtil).sendBroadcast(Intents.ACTION_WALLET_SYNC_COMPLETE);
    }
}