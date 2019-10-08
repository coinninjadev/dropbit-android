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
    private final DaoSessionManager daoSessionManager;
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

    public TransactionFee getLatestFee() {
        return feesManager.fees();
    }

    public void setLatestFee(TransactionFee transactionFee) {
        if (getPrimaryWallet() == null || transactionFee == null || transactionFee.getSlow() <= 0.0D) {
            return;
        }

        feesManager.setFees(transactionFee);
    }

    public Wallet getPrimaryWallet() {
        return walletQueryManager.getPrimaryWallet();
    }

    @Nullable
    public Wallet getLegacyWallet() {
        return walletQueryManager.getLegacyWallet();
    }

    public void linkStatsWithAddressBook() {
        daoSessionManager.runRaw("update TARGET_STAT set ADDRESS_ID = (select _id from ADDRESS where address = TARGET_STAT.ADDR)");
        daoSessionManager.runRaw("update FUNDING_STAT set ADDRESS_ID = (select _id from ADDRESS where address = FUNDING_STAT.ADDR);");
        daoSessionManager.runRaw("update TARGET_STAT set WALLET_ID = (select _id from WALLET limit 1)" +
                " where _id in (select _id from TARGET_STAT where WALLET_ID is null and ADDRESS_ID is not null)");
        daoSessionManager.runRaw("update FUNDING_STAT set WALLET_ID = (select _id from WALLET limit 1)" +
                " where _id in (select _id from FUNDING_STAT where WALLET_ID is null and ADDRESS_ID is not null);");
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

    public void rotateWallets(@NotNull Wallet newPrimary, Wallet oldPrimary) {
        Account account = getUserAccount();
        if (account != null) {
            account.setWallet(newPrimary);
            account.update();
        }
        daoSessionManager.getTransactionsInvitesSummaryDao().deleteAll();
        daoSessionManager.getInviteTransactionSummaryDao().deleteAll();
        daoSessionManager.getTransactionSummaryDao().deleteAll();
        daoSessionManager.getTargetStatDao().deleteAll();
        daoSessionManager.getFundingStatDao().deleteAll();
        daoSessionManager.getAddressDao().deleteAll();
        long oldId = oldPrimary.getId();
        long newId = newPrimary.getId();
        long replacementId = newId + 1;
        daoSessionManager.runRaw(String.format("UPDATE Wallet set _ID = %s where _ID = %s", replacementId, oldId));
        daoSessionManager.runRaw(String.format("UPDATE Word set WALLET_ID = %s where WALLET_ID = %s", replacementId, oldId));
        oldPrimary.setId(replacementId);
        oldPrimary.update();
    }

    public void deleteAll() {
        daoSessionManager.resetAll();
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

    public void updateBalances() {
        boolean useUnSpendableTargetStat = true;
        long balance = buildBalances(useUnSpendableTargetStat);

        Wallet wallet = getPrimaryWallet();
        wallet.setBalance(Math.max(balance, 0));
        wallet.update();
    }

    public void updateSpendableBalances() {
        boolean useUnSpendableTargetStat = false;
        long balance = buildBalances(useUnSpendableTargetStat);

        Wallet wallet = getPrimaryWallet();
        wallet.setSpendableBalance(Math.max(balance, 0));
        wallet.update();
    }

    public long buildBalances(boolean useUnSpendableTargetStat) {
        long balance = 0L;
        // invalidate memory caches
        Wallet wallet = getPrimaryWallet();
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

    public int getCurrentExternalIndex() {
        return getPrimaryWallet().getExternalIndex();
    }

    public int getCurrentInternalIndex() {
        return getPrimaryWallet().getInternalIndex();
    }

    public void setExternalIndex(int position) {
        Wallet wallet = getPrimaryWallet();
        wallet.setExternalIndex(position);
        wallet.update();
    }

    public void setInternalIndex(int index) {
        Wallet wallet = getPrimaryWallet();
        wallet.setInternalIndex(index);
        wallet.update();
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
