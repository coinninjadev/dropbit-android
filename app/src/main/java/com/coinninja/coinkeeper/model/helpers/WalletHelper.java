package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.AccountDao;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.AddressDao;
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
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.query.WalletQueryManager;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.FeesManager;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

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
        Wallet wallet = getWallet();
        for (int i = 0; i < recoveryWords.length; i++) {
            wordHelper.saveWord(wallet.getId(), recoveryWords[i], i);
        }
    }

    public String[] getSeedWords() {
        if (null == getWallet()) {
            return null;
        }

        List<Word> words = daoSessionManager.getWordDao().queryBuilder().
                where(WordDao.Properties.WalletId.eq(getWallet().getId())).
                orderAsc(WordDao.Properties.SortOrder).list();
        List<String> stringList = new ArrayList<>();

        for (Word word :
                words) {
            stringList.add(word.getWord());
        }

        return stringList.toArray(new String[stringList.size()]);
    }

    public USDCurrency getLatestPrice() {
        return new USDCurrency(getWallet().getLastUSDPrice());
    }

    public void setLatestPrice(USDCurrency price) {
        Wallet wallet = getWallet();
        if (wallet != null && price.toLong() > 0L) {
            wallet.setLastUSDPrice(price.toLong());
            wallet.update();
        }
    }

    public TransactionFee getLatestFee() {
        return feesManager.fees();
    }

    public void setLatestFee(TransactionFee transactionFee) {
        if (getWallet() == null || transactionFee == null || transactionFee.getSlow() <= 0.0D) {
            return;
        }

        feesManager.setFees(transactionFee);
    }

    public Wallet getWallet() {
        return walletQueryManager.getWallet();
    }

    public boolean containsAddress(String address) {
        return daoSessionManager.getAddressDao().queryBuilder().
                where(AddressDao.Properties.Address.eq(address)).
                list().size() > 0;
    }

    public List<Address> addAddresses(List<GsonAddress> addresses, int changeIndex) {
        AddressDao addressDao = daoSessionManager.getAddressDao();
        List<String> savedAddresses = new ArrayList<>();

        for (GsonAddress address : addresses) {
            String addr = address.getAddress();

            if (savedAddresses.indexOf(addr) >= 0 || containsAddress(addr)) {
                savedAddresses.add(addr);
                continue;
            }
            savedAddresses.add(addr);

            Address dbAddress = new Address();
            dbAddress.setWalletId(getWallet().getId());
            dbAddress.setAddress(addr);
            dbAddress.setChangeIndex(changeIndex);
            dbAddress.setIndex(address.getDerivationIndex());
            addressDao.insert(dbAddress);
        }

        return addressDao.queryBuilder().
                where(AddressDao.Properties.Address.in(savedAddresses)).list();
    }

    public void linkStatsWithAddressBook() {
        daoSessionManager.runRaw("update TARGET_STAT set ADDRESS_ID = (select _id from ADDRESS where address = TARGET_STAT.ADDR)");
        daoSessionManager.runRaw("update FUNDING_STAT set ADDRESS_ID = (select _id from ADDRESS where address = FUNDING_STAT.ADDR);");
        daoSessionManager.runRaw("update TARGET_STAT set WALLET_ID = (select _id from WALLET limit 1)" +
                " where _id in (select _id from TARGET_STAT where WALLET_ID is null and ADDRESS_ID is not null)");
        daoSessionManager.runRaw("update FUNDING_STAT set WALLET_ID = (select _id from WALLET limit 1)" +
                " where _id in (select _id from FUNDING_STAT where WALLET_ID is null and ADDRESS_ID is not null);");
    }

    public TransactionSummary initTransactions(List<GsonAddress> addresses) {
        TransactionSummaryDao dao = daoSessionManager.getTransactionSummaryDao();
        List<String> txids = new ArrayList<>();
        TransactionSummary transaction = null;

        for (GsonAddress address : addresses) {
            String txid = address.getTransactionId();

            if (txids.indexOf(txid) > 0) {
                continue;
            }

            transaction = dao.queryBuilder().where(TransactionSummaryDao.Properties.
                    Txid.eq(txid)).limit(1).unique();

            if (transaction == null) {
                transaction = new TransactionSummary();
                transaction.setWalletId(getWallet().getId());
                transaction.setTxid(txid);
                transaction.setMemPoolState(MemPoolState.PENDING);

                dao.insert(transaction);
                dao.refresh(transaction);

            }
        }
        return transaction;
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

    public void saveAccountRegistration(CNUserAccount cnUserAccount, CNPhoneNumber phoneNumber) {
        if (!hasAccount()) return;

        Account account = getUserAccount();
        account.populateStatus(cnUserAccount.getStatus());
        account.setCnUserId(cnUserAccount.getId());
        account.setPhoneNumberHash(cnUserAccount.getPhoneNumberHash());
        account.setPhoneNumber(phoneNumber.toPhoneNumber());
        account.update();
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
        Wallet wallet = getWallet();
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

        Wallet wallet = getWallet();
        wallet.setBalance(Math.max(balance, 0));
        wallet.update();
    }

    public void updateSpendableBalances() {
        boolean useUnSpendableTargetStat = false;
        long balance = buildBalances(useUnSpendableTargetStat);

        Wallet wallet = getWallet();
        wallet.setSpendableBalance(Math.max(balance, 0));
        wallet.update();
    }

    public long buildBalances(boolean useUnSpendableTargetStat) {
        long balance = 0L;
        // invalidate memory caches
        Wallet wallet = getWallet();
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
            if (invite.getType() == Type.SENT &&
                    invite.getBtcState() == BTCState.UNFULFILLED) {
                long inviteTotalValue = calculateInviteValue(invite);
                balance -= inviteTotalValue;
            }
        }

        return balance;
    }

    public int getCurrentExternalIndex() {
        return getWallet().getExternalIndex();
    }

    public int getCurrentInternalIndex() {
        return getWallet().getInternalIndex();
    }

    public void setExternalIndex(int position) {
        Wallet wallet = getWallet();
        wallet.setExternalIndex(position);
        wallet.update();
    }

    public void setInternalIndex(int index) {
        Wallet wallet = getWallet();
        wallet.setInternalIndex(index);
        wallet.update();
    }

    public void updateBlockHeight(int blockheight) {
        Wallet wallet = getWallet();
        wallet.refresh();

        if (wallet.getBlockTip() < blockheight) {
            wallet.setBlockTip(blockheight);
            wallet.update();
            wallet.refresh();
        }
    }

    public int getBlockTip() {
        Wallet wallet = getWallet();
        wallet.refresh();
        return wallet.getBlockTip();
    }

    public List<InviteTransactionSummary> getIncompleteReceivedInvites() {

        QueryBuilder query = daoSessionManager.getInviteTransactionSummaryDao().queryBuilder().
                whereOr(InviteTransactionSummaryDao.Properties.Address.isNull(),
                        InviteTransactionSummaryDao.Properties.Address.eq(""))
                .where(
                        InviteTransactionSummaryDao.Properties.Type.eq(Type.RECEIVED.getId()),
                        InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.UNFULFILLED.getId())
                );

        return query.list();
    }

    public BTCCurrency getSpendableBalance() {
        return new BTCCurrency(getWallet().getSpendableBalance());
    }

    public long getBalance() {
        return getWallet().getBalance();
    }

    public TransactionSummary getTransactionByTxid(String txid) {
        return daoSessionManager.getTransactionSummaryDao().queryBuilder().where(TransactionSummaryDao.Properties.Txid.eq(txid)).unique();
    }

    long calculateInviteValue(InviteTransactionSummary invite) {
        String txID = invite.getBtcTransactionId();
        if (txID != null && !txID.isEmpty()) {
            return 0;//is invite has tx id its value is not used in user balance
        }

        Type type = invite.getType();
        long value = invite.getValueSatoshis();
        long fee = 0;

        if (type == Type.SENT) {
            fee = invite.getValueFeesSatoshis();
        }

        return value + fee;
    }

    private void clearRegistration(boolean clearId) {
        if (!hasAccount()) return;

        getWallet().refresh();
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
        Wallet wallet = getWallet();
        Account account = new Account();
        account.setWallet(wallet);
        account.setStatus(AccountStatus.UNVERIFIED);
        daoSessionManager.getAccountDao().insert(account);
        wallet = daoSessionManager.getWalletDao().queryBuilder().where(WalletDao.Properties.Id.eq(wallet.getId())).unique();
        wallet.refresh();
        return account;
    }

}
