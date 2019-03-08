package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.AddressDao;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.FundingStatDao;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class AddressHelper {
    private DaoSessionManager daoSessionManager;
    private final WalletHelper walletHelper;

    @Inject
    public AddressHelper(DaoSessionManager daoSessionManager, WalletHelper walletHelper) {
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
    }

    public void updateSpentTransactions() {
        List<TargetStat> targets = daoSessionManager.getTargetStatDao().queryBuilder().where(
                TargetStatDao.Properties.AddressId.isNotNull(),
                TargetStatDao.Properties.FundingId.isNull()).list();

        for (TargetStat target : targets) {
            FundingStat fundingStat = daoSessionManager.getFundingStatDao().queryBuilder().where(
                    FundingStatDao.Properties.Position.eq(target.getPosition()),
                    FundingStatDao.Properties.Value.eq(target.getValue()),
                    FundingStatDao.Properties.FundedTransaction.eq(target.getTransaction().getTxid())
            ).unique();

            if (fundingStat != null) {
                target.refresh();
                target.setFundingStat(fundingStat);
                target.update();

                fundingStat.refresh();
                fundingStat.setTargetStat(target);
                fundingStat.update();
            }
        }
    }

    public boolean hasReceivedTransaction() {
        Boolean hasReceived = false;
        List<Address> list = daoSessionManager.getAddressDao().queryBuilder().where(
                AddressDao.Properties.ChangeIndex.eq(0)
        ).list();

        for (Address address : list) {
            if (address.getTargets().size() > 0) {
                for (TargetStat target : address.getTargets()) {
                    TransactionSummary transaction = target.getTransaction();
                    if (transaction == null || transaction.getTransactionsInvitesSummary() == null)
                        continue;

                    if (transaction.getTransactionsInvitesSummary().getInviteTransactionSummary() == null) {
                        hasReceived = true;
                        break;
                    }
                }
            }
        }

        return hasReceived;
    }

    public int getAddressCountFor(int chainIndex) {
        return (int) daoSessionManager.getAddressDao().queryBuilder()
                .where(AddressDao.Properties.ChangeIndex.eq(chainIndex)).count();
    }

    public void saveAddress(int chainIndex, int derivationIndex, String addr) {
        Address address = daoSessionManager.getAddressDao().queryBuilder()
                .where(
                        AddressDao.Properties.ChangeIndex.eq(chainIndex),
                        AddressDao.Properties.Index.eq(derivationIndex),
                        AddressDao.Properties.Address.eq(addr)
                )
                .unique();

        if (null != address) return;

        address = new Address();
        address.setAddress(addr);
        address.setWallet(walletHelper.getWallet());
        address.setChangeIndex(chainIndex);
        address.setIndex(derivationIndex);
        daoSessionManager.getAddressDao().insert(address);
    }

    public List<Address> getUnusedAddressesFor(int chainIndex) {
        return daoSessionManager.getAddressDao().queryRaw("" +
                " LEFT JOIN TARGET_STAT on T._id = TARGET_STAT.ADDRESS_ID" +
                " LEFT JOIN FUNDING_STAT on T._id = FUNDING_STAT.ADDRESS_ID" +
                " WHERE T.CHANGE_INDEX = " + String.valueOf(chainIndex) +
                " AND  FUNDING_STAT.ADDRESS_ID IS NULL " +
                " AND TARGET_STAT.ADDRESS_ID IS NULL "
        );
    }

    public Address get(String publicKey){
        return daoSessionManager.getAddressDao().queryBuilder()
                .where(AddressDao.Properties.Address.eq(publicKey)).unique();
    }

    public int getLargestDerivationIndexReportedFor(int chainIndex) {
        List<Address> chainAddresses = daoSessionManager.getAddressDao().queryRaw("" +
                " LEFT JOIN TARGET_STAT on T._id = TARGET_STAT.ADDRESS_ID" +
                " LEFT JOIN FUNDING_STAT on T._id = FUNDING_STAT.ADDRESS_ID" +
                " WHERE T.CHANGE_INDEX = " + String.valueOf(chainIndex) +
                " AND FUNDING_STAT.ADDRESS_ID IS NOT NULL " +
                " OR TARGET_STAT.ADDRESS_ID IS NOT NULL "
        );
        Comparator<Address> comparator = (Address e1, Address e2) ->
                Integer.compare(e1.getIndex(), e2.getIndex());

        Collections.sort(chainAddresses, comparator);
        Collections.reverse(chainAddresses);

        return chainAddresses.size() > 0 ? chainAddresses.get(0).getIndex() : 0;
    }

    public Address getAddressFor(DerivationPath path) {
        return daoSessionManager.getAddressDao().queryBuilder()
                .where(AddressDao.Properties.ChangeIndex.eq(path.getChange()),
                        AddressDao.Properties.Index.eq(path.getIndex()))
                .unique();
    }
}
