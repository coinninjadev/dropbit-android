package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.AccountDao;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.WalletDao;
import com.coinninja.coinkeeper.model.db.Word;
import com.coinninja.coinkeeper.model.db.WordDao;
import com.coinninja.coinkeeper.model.db.enums.AccountStatus;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.query.WalletQueryManager;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.FeesManager;

import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.USDCurrency;

import static com.coinninja.coinkeeper.view.util.TransactionUtil.isTargetNotSpendable;

public class WalletHelper {
    private static final String TAG = WalletHelper.class.getSimpleName();
    public final DaoSessionManager daoSessionManager;
    private final WalletDao walletDao;

    private final WalletQueryManager walletQueryManager;
    private final FeesManager feesManager;
    private WordHelper wordHelper;

    public WalletHelper(DaoSessionManager daoSessionManager, WalletQueryManager walletQueryManager, WordHelper wordHelper, FeesManager feesManager) {
        this.daoSessionManager = daoSessionManager;
        this.feesManager = feesManager;
        walletDao = daoSessionManager.getWalletDao();
        this.walletQueryManager = walletQueryManager;
        this.wordHelper = wordHelper;
    }

    public void saveWords(String[] recoveryWords) {
        Wallet wallet = getPrimaryWallet();
        for (int i = 0; i < recoveryWords.length; i++) {
            wordHelper.saveWord(wallet.getId(), recoveryWords[i], i);
        }
    }

    public String[] getSeedWords() {
        Wallet wallet = getPrimaryWallet();
        if (wallet == null) {
            return null;
        }

        return getSeedWordsForWallet(wallet);
    }

    public String[] getSeedWordsForWallet(Wallet wallet) {
        List<Word> words = daoSessionManager.getWordDao().queryBuilder().
                where(WordDao.Properties.WalletId.eq(wallet.getId())).
                orderAsc(WordDao.Properties.SortOrder).list();
        List<String> stringList = new ArrayList<>();

        for (Word word :
                words) {
            stringList.add(word.getWord());
        }

        return stringList.toArray(new String[stringList.size()]);
    }

    public USDCurrency getLatestPrice() {
        Wallet primaryWallet = getPrimaryWallet();
        return (primaryWallet == null) ? new USDCurrency(0.0d) : new USDCurrency(primaryWallet.getLastUSDPrice());
    }

    public void setLatestPrice(USDCurrency price) {
        Wallet wallet = getPrimaryWallet();
        if (wallet != null && price.toLong() > 0L) {
            wallet.setLastUSDPrice(price.toLong());
            wallet.update();
        }
    }

    public void setLatestFee(TransactionFee transactionFee) {
        if (getPrimaryWallet() == null || transactionFee == null || transactionFee.getSlow() <= 0.0D) {
            return;
        }

        feesManager.setFees(transactionFee);
    }

    public Wallet getPrimaryWallet() {
        Wallet wallet = walletQueryManager.getPrimaryWallet();
        if (wallet == null) {
            wallet = definePrimary();
        }
        return wallet;
    }

    @Nullable
    public Wallet getSegwitWallet() {
        return walletQueryManager.getSegwitWallet();
    }


    @Nullable
    public Wallet getLegacyWallet() {
        return walletQueryManager.getLegacyWallet();
    }

    public void linkStatsWithAddressBook() {
        daoSessionManager.runRaw("update TARGET_STAT set ADDRESS_ID = (select _id from ADDRESS where address = TARGET_STAT.ADDR); ");
        daoSessionManager.runRaw("update TARGET_STAT set WALLET_ID = (select ADDRESS.WALLET_ID from ADDRESS where address = TARGET_STAT.ADDR);");
        daoSessionManager.runRaw("update FUNDING_STAT set ADDRESS_ID = (select _id from ADDRESS where address = FUNDING_STAT.ADDR);");
        daoSessionManager.runRaw("update FUNDING_STAT set WALLET_ID = (select ADDRESS.WALLET_ID from ADDRESS where address = FUNDING_STAT.ADDR);");
    }

    public LazyList<TransactionsInvitesSummary> getTransactionsLazily() {
        return daoSessionManager.getTransactionsInvitesSummaryDao().queryBuilder()
                .orderDesc(TransactionsInvitesSummaryDao.Properties.InviteTime,
                        TransactionsInvitesSummaryDao.Properties.BtcTxTime)
                .listLazyUncached();
    }

    public void saveRegistration(CNWallet cnWallet) {
        if (!hasAccount()) {
            Account account = getUserAccount();
            account.refresh();
            account.setCnWalletId(cnWallet.getId());
            account.update();
            account.refresh();
        }
    }

    public void createWallet() {
        daoSessionManager.createWallet();
    }

    public void saveAccountRegistration(CNUserAccount cnUserAccount) {
        if (!hasAccount()) return;

        Account account = getUserAccount();
        account.populateStatus(cnUserAccount.getStatus());
        account.setCnUserId(cnUserAccount.getId());
        account.update();
    }

    public USDCurrency btcChainWorth() {
        return getBalance().toUSD(getLatestPrice());
    }

    @NotNull
    public Wallet getOrCreateSegwitWalletForUpdate(String[] words) {
        Wallet segwitWallet = walletQueryManager.getSegwitWallet();
        if (segwitWallet == null) {
            segwitWallet = daoSessionManager.createWalletForUpdate(getPrimaryWallet().getUserId());
            for (int i = 0; i < words.length; i++) {
                wordHelper.saveWord(segwitWallet.getId(), words[i], i);
            }
        }
        return segwitWallet;
    }

    public void rotateAccount(@NotNull Wallet segwitWallet, @Nullable Wallet legacyWallet) {
        Account account = getUserAccount();
        if (account != null) {
            account.setWallet(segwitWallet);
            account.update();
        }

        if (legacyWallet != null) {
            legacyWallet.setIsPrimary(false);
            legacyWallet.update();
        }

        segwitWallet.setIsPrimary(true);
        segwitWallet.update();
    }

    public void deleteAll() {
        daoSessionManager.resetAll();
    }

    public void deleteAllTransactionData() {
        daoSessionManager.getInviteTransactionSummaryDao().deleteAll();
        daoSessionManager.getTransactionsInvitesSummaryDao().deleteAll();
        daoSessionManager.getTransactionSummaryDao().deleteAll();
        daoSessionManager.getTargetStatDao().deleteAll();
        daoSessionManager.getFundingStatDao().deleteAll();
        daoSessionManager.getAddressDao().deleteAll();
    }

    public void removeCurrentCnRegistration() {
        clearRegistration(true);
    }

    public void removeCurrentCnUserRegistration() {
        clearRegistration(false);
    }

    public boolean hasAccount() {
        Account account = getUserAccount();
        return account.getCnWalletId() != null;
    }

    public Account getUserAccount() {
        Wallet wallet = getPrimaryWallet();
        if (wallet == null) return new Account();

        Account account = daoSessionManager.getAccountDao().queryBuilder().where(AccountDao.Properties.WalletId.eq(wallet.getId())).limit(1).unique();
        if (account != null) {
            account.refresh();
        } else {
            account = createAccount();
        }
        return account;
    }

    public void updateBalances(Wallet wallet) {
        long balance = buildBalances(wallet, true);

        wallet.setBalance(Math.max(balance, 0));
        wallet.update();
    }

    public void updateSpendableBalances(Wallet wallet) {
        long balance = buildBalances(wallet, false);

        wallet.setSpendableBalance(Math.max(balance, 0));
        wallet.update();
    }

    public long buildBalances(Wallet wallet, boolean useUnSpendableTargetStat) {
        long balance = 0L;
        // invalidate memory caches
        wallet.refresh();
        wallet.resetFundingStats();
        wallet.resetTargetStats();
        wallet.resetInviteTransactionSummaries();

        List<TargetStat> targetStats = wallet.getTargetStats();
        List<FundingStat> fundingStats = wallet.getFundingStats();
        List<InviteTransactionSummary> invites = wallet.getInviteTransactionSummaries();

        for (TargetStat target : targetStats) {
            if (target.getState() == TargetStat.State.CANCELED) continue;

            boolean isNotSpendable = isTargetNotSpendable(target);

            if (isNotSpendable && !useUnSpendableTargetStat) {
                continue;
            } else {
                balance += target.getValue();
            }
        }

        for (FundingStat funding : fundingStats) {
            if (funding.getState() == FundingStat.State.CANCELED) continue;

            balance -= funding.getValue();
        }

        for (InviteTransactionSummary invite : invites) {
            if (invite.getType() == Type.BLOCKCHAIN_SENT &&
                    invite.getBtcState() == BTCState.UNFULFILLED) {
                long inviteTotalValue = calculateInviteValue(invite);
                balance -= inviteTotalValue;
            }
        }

        return balance;
    }

    public void updateBlockHeight(int blockheight) {
        Wallet wallet = getPrimaryWallet();
        wallet.refresh();

        if (wallet.getBlockTip() < blockheight) {
            wallet.setBlockTip(blockheight);
            wallet.update();
            wallet.refresh();
        }
    }

    public int getBlockTip() {
        Wallet wallet = getPrimaryWallet();
        wallet.refresh();
        return wallet.getBlockTip();
    }

    public List<InviteTransactionSummary> getIncompleteReceivedInvites() {

        QueryBuilder query = daoSessionManager.getInviteTransactionSummaryDao().queryBuilder().
                whereOr(InviteTransactionSummaryDao.Properties.Address.isNull(),
                        InviteTransactionSummaryDao.Properties.Address.eq(""))
                .whereOr(
                        InviteTransactionSummaryDao.Properties.Type.eq(Type.BLOCKCHAIN_RECEIVED.getId()),
                        InviteTransactionSummaryDao.Properties.Type.eq(Type.LIGHTNING_RECEIVED.getId())
                ).where(
                        InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.UNFULFILLED.getId())
                );

        return query.list();
    }

    public BTCCurrency getSpendableBalance() {
        return new BTCCurrency(getPrimaryWallet().getSpendableBalance());
    }

    public BTCCurrency getBalance() {
        Wallet primaryWallet = getPrimaryWallet();
        return (primaryWallet == null) ? new BTCCurrency(0) : new BTCCurrency(primaryWallet.getBalance());
    }

    public TransactionSummary getTransactionByTxid(String txid) {
        return daoSessionManager.getTransactionSummaryDao().queryBuilder().where(TransactionSummaryDao.Properties.Txid.eq(txid)).unique();
    }

    void saveAccountRegistration(CNUserAccount cnUserAccount, CNPhoneNumber phoneNumber) {
        if (!hasAccount()) return;

        Account account = getUserAccount();
        account.populateStatus(cnUserAccount.getStatus());
        account.setCnUserId(cnUserAccount.getId());
        account.setPhoneNumberHash(cnUserAccount.getPhoneNumberHash());
        account.setPhoneNumber(phoneNumber.toPhoneNumber());
        account.update();
    }

    long calculateInviteValue(InviteTransactionSummary invite) {
        String txID = invite.getBtcTransactionId();
        if (txID != null && !txID.isEmpty()) {
            return 0;//is invite has tx id its value is not used in user balance
        }

        Type type = invite.getType();
        long value = invite.getValueSatoshis();
        long fee = 0;

        if (type == Type.BLOCKCHAIN_SENT) {
            fee = invite.getValueFeesSatoshis();
        }

        return value + fee;
    }

    private Wallet definePrimary() {
        Wallet segwit = walletQueryManager.getSegwitWallet();
        Wallet legacy = walletQueryManager.getLegacyWallet();

        if (segwit != null && segwit.getLastSync() != 0) {
            if (legacy != null) {
                legacy.setIsPrimary(false);
                legacy.update();
            }
            segwit.setIsPrimary(true);
            segwit.update();
        } else if (legacy != null) {
            legacy.setIsPrimary(true);
            legacy.update();
        }
        return walletQueryManager.getPrimaryWallet();
    }

    private void clearRegistration(boolean clearId) {
        if (!hasAccount()) return;

        getPrimaryWallet().refresh();
        Account account = getUserAccount();
        account.refresh();

        if (clearId) {
            account.setCnWalletId(null);
        }
        account.setPhoneNumber(null);
        account.setPhoneNumberHash("");
        account.setCnUserId("");
        account.setStatus(AccountStatus.UNVERIFIED);
        account.update();
        daoSessionManager.getDropbitMeIdentityDao().deleteAll();
    }

    private Account createAccount() {
        Wallet wallet = getPrimaryWallet();
        Account account = new Account();
        account.setWallet(wallet);
        account.setStatus(AccountStatus.UNVERIFIED);
        daoSessionManager.getAccountDao().insert(account);
        wallet = daoSessionManager.getWalletDao().queryBuilder().where(WalletDao.Properties.Id.eq(wallet.getId())).unique();
        wallet.refresh();
        return account;
    }

}
