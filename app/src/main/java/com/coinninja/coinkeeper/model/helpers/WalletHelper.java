package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.AccountDao;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.AddressDao;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.WalletDao;
import com.coinninja.coinkeeper.model.db.Word;
import com.coinninja.coinkeeper.model.db.WordDao;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.coinninja.coinkeeper.view.util.TransactionUtil.IS_TARGETSTAT_NOT_SPENDABLE;

public class WalletHelper {
    private static final String TAG = WalletHelper.class.getSimpleName();
    private final DaoSessionManager daoSessionManager;
    private final WalletDao walletDao;
    PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();

    private WordHelper wordHelper;

    public WalletHelper(DaoSessionManager daoSessionManager, WordHelper wordHelper) {
        this.daoSessionManager = daoSessionManager;
        walletDao = daoSessionManager.getWalletDao();
        this.wordHelper = wordHelper;
    }

    public void saveWords(String[] recoveryWords) {
        Wallet wallet = getWallet();
        wallet.refresh();
        verifyCanSave(wallet);
        for (int i = 0; i < recoveryWords.length; i++) {
            wordHelper.saveWord(wallet.getId(), recoveryWords[i], i);
        }
    }

    private void verifyCanSave(Wallet wallet) {
        List<Word> words = wallet.getWords();
        List<Address> addresses = wallet.getAddressses();
        if ((words != null && words.size() > 0) || (addresses != null && addresses.size() > 0)) {
            throw new IllegalAccessError("Wallet already has words!! This is not safe");
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
        getWallet().setLastUSDPrice(price.toLong());
        getWallet().update();
    }


    public TransactionFee getLatestFee() {
        double lastFee = 0D;

        String lastFeeString = getWallet().getLastFee();
        if (lastFeeString != null) {
            lastFee = Double.parseDouble(lastFeeString);
        }
        return new TransactionFee(lastFee, lastFee, lastFee);
    }

    public void setLatestFee(TransactionFee transactionFee) {
        getWallet().setLastFee(String.valueOf(transactionFee.getMin()));
    }

    public Wallet getWallet() {

        return walletDao.queryBuilder().orderAsc().limit(1).unique();
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

    private Account createAccount() {
        Wallet wallet = getWallet();
        Account account = new Account();
        account.setWallet(wallet);
        account.setStatus(Account.Status.UNVERIFIED);
        daoSessionManager.getAccountDao().insert(account);
        wallet = daoSessionManager.getWalletDao().queryBuilder().where(WalletDao.Properties.Id.eq(wallet.getId())).unique();
        wallet.refresh();
        return account;
    }

    public void saveAccountRegistration(CNUserAccount cnUserAccount, CNPhoneNumber phoneNumber) {
        //todo: hasAccount() actually inserts a new account into the database
        if (!hasAccount()) return;

        Account account = getUserAccount();
        account.populateStatus(cnUserAccount.getStatus());
        account.setCnUserId(cnUserAccount.getId());
        account.setPhoneNumberHash(cnUserAccount.getPhoneNumberHash());
        account.setPhoneNumber(new PhoneNumber(phoneNumber));
        account.update();
    }

    public void saveAccountRegistration(String cnUserId, Account.Status status, String phoneNumberHash, String phoneNumber) {
        if (!hasAccount()) return;

        getWallet().refresh();
        Account account = getUserAccount();
        account.refresh();
        account.setStatus(status);
        account.setCnUserId(cnUserId);
        account.setPhoneNumberHash(phoneNumberHash);
        account.setPhoneNumber(new PhoneNumber(phoneNumber));

        account.update();
    }

    public void removeCurrentCnRegistration() {
        clearRegistration(true);
    }

    public void removeCurrentCnUserRegistration() {
        clearRegistration(false);
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
        account.setStatus(Account.Status.UNVERIFIED);
        account.update();
    }


    public boolean hasAccount() {
        Account account = getUserAccount();
        return account.getCnWalletId() != null;
    }

    public Account getUserAccount() {
        Account account = daoSessionManager.getAccountDao().queryBuilder().where(AccountDao.Properties.WalletId.eq(getWallet().getId())).limit(1).unique();
        if (account != null) {
            account.refresh();
        } else {
            account = createAccount();
        }
        return account;
    }

    public boolean hasVerifiedAccount() {
        return hasAccount() && getUserAccount().getStatus() == Account.Status.VERIFIED;
    }

    public void updateUserID(CNUserAccount cnUserAccount) {
        if (!hasAccount()) return;
        Account account = getUserAccount();
        account.setCnUserId(cnUserAccount.getId());
        account.setPhoneNumberHash(cnUserAccount.getPhoneNumberHash());
        account.update();
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
            if (target.getState() == TargetStat.State.CANCELED) continue;//skip canceled stats

            boolean isNotSpendable = IS_TARGETSTAT_NOT_SPENDABLE(target);

            if (isNotSpendable && !useUnSpendableTargetStat) {
                continue;
            } else {
                balance += target.getValue();
            }
        }

        for (FundingStat funding : fundingStats) {
            if (funding.getState() == FundingStat.State.CANCELED) continue;//skip canceled stats

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

}
