package com.coinninja.coinkeeper.cn.wallet;

import com.coinninja.bindings.SeedWordGenerator;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.receiver.WalletCreatedBroadCastReceiver;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.twitter.MyTwitterProfile;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class CNWalletManager {

    static final String PREFERENCE_SKIPPED_BACKUP = "preference_skipped_backup";
    private final AccountManager accountManager;
    private final DateUtil dateUtil;
    private final Analytics analytics;
    private WalletHelper walletHelper;
    private BitcoinUtil bitcoinUtil;
    private PreferencesUtil preferencesUtil;
    private SeedWordGenerator seedWordGenerator;
    private final InviteTransactionSummaryHelper inviteTransactionSummaryHelper;
    private LocalBroadCastUtil localBroadCastUtil;
    private MyTwitterProfile myTwitterProfile;

    @Inject
    public CNWalletManager(WalletHelper walletHelper, BitcoinUtil bitcoinUtil, AccountManager accountManager,
                           PreferencesUtil preferencesUtil, SeedWordGenerator seedWordGenerator,
                           InviteTransactionSummaryHelper inviteTransactionSummaryHelper, LocalBroadCastUtil localBroadCastUtil,
                           DateUtil dateUtil, Analytics analytics, MyTwitterProfile myTwitterProfile
    ) {
        this.walletHelper = walletHelper;
        this.bitcoinUtil = bitcoinUtil;
        this.accountManager = accountManager;
        this.preferencesUtil = preferencesUtil;
        this.seedWordGenerator = seedWordGenerator;
        this.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper;
        this.localBroadCastUtil = localBroadCastUtil;
        this.dateUtil = dateUtil;
        this.analytics = analytics;
        this.myTwitterProfile = myTwitterProfile;
    }

    public static int calcConfirmations(int currentBlockHeight, int transactionBlock) {
        return currentBlockHeight - transactionBlock + 1;
    }

    public void createWallet() {
        walletHelper.createWallet();
    }

    public Account getAccount() {
        return walletHelper.getUserAccount();
    }

    public Account updateAccount(CNUserAccount userAccount) {
        walletHelper.saveAccountRegistration(userAccount);
        return getAccount();
    }

    public String[] getRecoveryWords() {
        if (!hasWallet()) return null;

        return walletHelper.getSeedWords();
    }

    public void skipBackup(String[] recoveryWords) {
        saveSeedWords(recoveryWords);
        preferencesUtil.savePreference(PREFERENCE_SKIPPED_BACKUP, true);
    }

    public boolean hasSkippedBackup() {
        return preferencesUtil.contains(PREFERENCE_SKIPPED_BACKUP);
    }

    public boolean userVerifiedWords(String[] recoveryWords) {
        boolean result = saveSeedWords(recoveryWords);
        preferencesUtil.removePreference(PREFERENCE_SKIPPED_BACKUP);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, true);
        analytics.flush();
        return result;
    }

    public boolean hasWallet() {
        return walletHelper.getSeedWords() != null && walletHelper.getSeedWords().length == 12;
    }

    public String[] generateRecoveryWords() {
        return seedWordGenerator.generate();
    }

    public void syncCompleted() {
        Wallet wallet = walletHelper.getWallet();
        wallet.setLastSync(dateUtil.getCurrentTimeInMillis());
        wallet.update();
    }

    public boolean isFirstSync() {
        return walletHelper.getWallet().getLastSync() <= 0L;
    }

    public boolean hasBalance() {
        return walletHelper.getWallet().getBalance() > 0L;
    }

    public Contact getContact() {
        Account userAccount = walletHelper.getUserAccount();
        return new Contact(userAccount.getPhoneNumber(), "", true);
    }

    public void updateBalances() {
        walletHelper.updateBalances();
        walletHelper.updateSpendableBalances();
        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE);
    }

    public void deverifyAccount() {
        walletHelper.removeCurrentCnUserRegistration();
        inviteTransactionSummaryHelper.cancelPendingSentInvites();
        myTwitterProfile.clear();
        analytics.setUserProperty(Analytics.PROPERTY_TWITTER_VERIFIED, false);
        analytics.setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false);
        analytics.flush();
    }

    boolean saveSeedWords(String[] recoveryWords) {
        boolean savedSuccessfully;
        if (!isValid(recoveryWords)) {
            savedSuccessfully = false;
        } else if (isAlreadySaved(recoveryWords)) {
            savedSuccessfully = true;
        } else {
            walletHelper.saveWords(recoveryWords);
            accountManager.cacheAddresses();
            localBroadCastUtil.sendGlobalBroadcast(WalletCreatedBroadCastReceiver.class, DropbitIntents.ACTION_WALLET_CREATED);
            savedSuccessfully = true;
        }

        return savedSuccessfully;
    }

    private boolean isAlreadySaved(String[] recoveryWords) {
        if (!hasWallet()) return false;

        List<String> words = Arrays.asList(recoveryWords);

        String[] savedSeedWords = walletHelper.getSeedWords();

        if (words.containsAll(Arrays.asList(savedSeedWords))) {
            return true;
        } else {
            throw new RuntimeException("There are words already saved but do not match the words you are currently trying to save");
        }
    }

    private boolean isValid(String[] recoveryWords) {
        return bitcoinUtil.isValidBIP39Words(recoveryWords);
    }
}
