package com.coinninja.coinkeeper.model.helpers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.AddressDao;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.FundingStatDao;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao.Properties;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.db.UserIdentity;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;
import com.coinninja.coinkeeper.service.client.model.SentInvite;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.coinninja.coinkeeper.service.client.model.VIn;
import com.coinninja.coinkeeper.service.client.model.VOut;
import com.coinninja.coinkeeper.util.DateUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class TransactionHelper {

    private static final String TAG = TransactionHelper.class.getSimpleName();
    private final WalletHelper walletHelper;
    private final TransactionInviteSummaryHelper transactionInviteSummaryHelper;
    private final DropbitAccountHelper dropbitAccountHelper;
    private final UserIdentityHelper userIdentityHelper;
    private DateUtil dateUtil;
    private DaoSessionManager daoSessionManager;

    @Inject
    public TransactionHelper(DaoSessionManager daoSessionManager, WalletHelper walletHelper,
                             TransactionInviteSummaryHelper transactionInviteSummaryHelper,
                             DropbitAccountHelper dropbitAccountHelper,
                             UserIdentityHelper userIdentityHelper, DateUtil dateUtil) {
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
        this.transactionInviteSummaryHelper = transactionInviteSummaryHelper;
        this.dropbitAccountHelper = dropbitAccountHelper;
        this.userIdentityHelper = userIdentityHelper;
        this.dateUtil = dateUtil;
    }

    public List<TransactionSummary> getIncompleteTransactions() {
        TransactionSummaryDao dao = getTransactionDao();
        return dao.queryBuilder()
                .whereOr(Properties.MemPoolState.eq(MemPoolState.INIT.getId()),
                        Properties.MemPoolState.eq(MemPoolState.PENDING.getId()),
                        Properties.MemPoolState.eq(MemPoolState.ACKNOWLEDGE.getId())
                ).list();
    }

    public List<TransactionSummary> getRequiringNotificationCheck() {
        TransactionSummaryDao dao = getTransactionDao();
        return dao.queryBuilder().where(Properties.SoughtNotification.eq(false)).list();
    }

    public List<TransactionSummary> getTransactionsWithoutHistoricPricing() {
        TransactionSummaryDao dao = getTransactionDao();
        return dao.queryBuilder()
                .where(Properties.HistoricPrice.eq(0L)
                ).list();
    }

    public List<TransactionSummary> getPendingMindedTransactions() {
        TransactionSummaryDao dao = getTransactionDao();
        return dao.queryBuilder().where(
                Properties.MemPoolState.eq(MemPoolState.MINED.getId()),
                Properties.NumConfirmations.lt(6)).list();
    }

    public List<TransactionSummary> getPendingTransactionsOlderThan(long olderThanSeconds) {
        TransactionSummaryDao dao = getTransactionDao();
        long timeInThePast = calculatePastTimeFromNow(olderThanSeconds);

        return dao.queryBuilder().whereOr(
                Properties.MemPoolState.eq(MemPoolState.PENDING.getId()),
                Properties.MemPoolState.eq(MemPoolState.INIT.getId()))
                .where(Properties.TxTime.le(timeInThePast)).list();
    }

    public List<TransactionSummary> getTransactionsWithoutFees() {
        TransactionSummaryDao dao = getTransactionDao();
        return dao.queryBuilder().where(Properties.Fee.eq(0L)).list();
    }

    @SuppressWarnings("UnnecessaryContinue")
    public void updateTransactions(List<TransactionDetail> fetchedTransactions, int currentBlockHeight) {
        for (TransactionDetail detail : fetchedTransactions) {
            TransactionSummary transaction = getTransactionDao().queryBuilder().
                    where(Properties.Txid.eq(detail.getTransactionId())).
                    limit(1).unique();

            if (transaction == null) continue;

            transaction.setMemPoolState(MemPoolState.ACKNOWLEDGE);
            if (detail.getBlockheight() > 0) {
                transaction.setNumConfirmations(CNWalletManager.calcConfirmations(currentBlockHeight, detail.getBlockheight()));
            }
            transaction.update();
            transaction.refresh();


            try {
                saveTransaction(transaction, detail);
            } catch (Exception ex) {
                continue;
            }
        }

    }

    public void updateFeesFor(List<TransactionStats> transactionStats) {
        TransactionSummaryDao dao = getTransactionDao();
        for (TransactionStats stats : transactionStats) {
            TransactionSummary transaction = dao.queryBuilder().where(Properties.Txid.eq(stats.getTransactionId())).limit(1).unique();
            if (transaction != null) {
                transaction.refresh();
                transaction.setFee(stats.getFees());
                transaction.update();
            }
        }
    }

    public void saveReceivedInviteTransaction(Wallet wallet, ReceivedInvite receivedInvite) {
        UserIdentity fromUser = userIdentityHelper.updateFrom(receivedInvite.getMetadata().getSender());
        UserIdentity toUser = userIdentityHelper.updateFrom(receivedInvite.getMetadata().getReceiver());
        saveInviteTransaction(
                wallet.getId(),
                receivedInvite.getId(),
                Type.RECEIVED,
                fromUser,
                toUser,
                receivedInvite.getMetadata().getAmount().getUsd(),
                receivedInvite.getCreated_at(),
                receivedInvite.getStatus(),
                receivedInvite.getMetadata().getAmount().getBtc(),
                0L,
                receivedInvite.getAddress(),
                receivedInvite.getTxid());
    }

    void addInviteToTransInvitesSummary(InviteTransactionSummary invite) {
        TransactionsInvitesSummaryDao transInvitesDao = daoSessionManager.getTransactionsInvitesSummaryDao();

        TransactionsInvitesSummary transactionsInvitesSummary = transInvitesDao.queryBuilder().
                where(TransactionsInvitesSummaryDao.Properties.InviteSummaryID.eq(invite.getId())).
                limit(1).unique();

        String btcTxID = invite.getBtcTransactionId();

        if (btcTxID != null && !btcTxID.isEmpty()) {
            transactionsInvitesSummary = joinInviteToTransaction(transactionsInvitesSummary, btcTxID);
        }

        if (transactionsInvitesSummary == null) {
            transactionsInvitesSummary = new TransactionsInvitesSummary();
            transInvitesDao.insert(transactionsInvitesSummary);
        }

        transactionsInvitesSummary.setInviteTransactionSummary(invite);
        transactionsInvitesSummary.setInviteSummaryID(invite.getId());

        if (transactionsInvitesSummary.getBtcTxTime() > 0) {
            transactionsInvitesSummary.setInviteTime(0);
        } else {
            transactionsInvitesSummary.setInviteTime(invite.getSentDate());
        }

        if (invite.getBtcState() == BTCState.EXPIRED || invite.getBtcState() == BTCState.CANCELED) {
            transactionsInvitesSummary.setBtcTxTime(invite.getSentDate());
            transactionsInvitesSummary.setInviteTime(0);
        }

        transactionsInvitesSummary.setInviteTxID(invite.getBtcTransactionId());
        invite.setTransactionsInvitesSummary(transactionsInvitesSummary);
        invite.setTransactionsInvitesSummaryID(transactionsInvitesSummary.getId());

        invite.update();
        invite.refresh();
        transactionsInvitesSummary.update();
        transactionsInvitesSummary.refresh();
        transInvitesDao.refresh(transactionsInvitesSummary);
    }

    public void updateInviteAddressTransaction(String id, String address) {
        InviteTransactionSummaryDao inviteDao = daoSessionManager.getInviteTransactionSummaryDao();

        InviteTransactionSummary invite = inviteDao.queryBuilder().
                where(InviteTransactionSummaryDao.Properties.ServerId.eq(id)).
                limit(1).unique();

        if (invite == null) {
            Log.e(TAG, "unable to update invite: " + address);
            return;
        }

        invite.refresh();
        invite.setAddress(address);
        invite.update();
        invite.refresh();
    }

    public InviteTransactionSummary updateInviteAddressTransaction(SentInvite sentInvite) {
        InviteTransactionSummary invite = getInviteTransactionSummary(sentInvite);
        if (invite == null) return null;

        invite.refresh();
        String status = sentInvite.getStatus();
        if ("expired".equals(status)) {
            invite.setBtcState(BTCState.EXPIRED);
            updateInviteTimeCompleteTransactionInviteSummary(invite);
        } else if ("new".equals(status)) {
            invite.setBtcState(BTCState.UNFULFILLED);
        } else if ("canceled".equals(status)) {
            invite.setBtcState(BTCState.CANCELED);
            updateInviteTimeCompleteTransactionInviteSummary(invite);
        } else if ("completed".equals(status)) {
            invite.setBtcState(BTCState.FULFILLED);
        }

        invite.setPubkey(sentInvite.getAddressPubKey());
        invite.setAddress(sentInvite.getAddress());
        invite.update();
        invite.refresh();
        return invite;
    }

    @Nullable
    public InviteTransactionSummary getInviteTransactionSummary(SentInvite sentInvite) {
        InviteTransactionSummary invite = getInviteTransactionSummary(sentInvite.getId());

        if (invite == null) {
            Log.e(TAG, "unable to update invite: " + sentInvite.getAddress());
            return null;
        }
        return invite;
    }

    public void updateInviteTxIDTransaction(Wallet wallet, String inviteServerID, String txID) {
        InviteTransactionSummaryDao inviteDao = daoSessionManager.getInviteTransactionSummaryDao();

        InviteTransactionSummary invite = inviteDao.queryBuilder().
                where(InviteTransactionSummaryDao.Properties.ServerId.eq(inviteServerID))
                .unique();

        if (invite == null) {
            Log.e(TAG, "unable to update invite: " + txID);
            return;
        }

        if (invite.getBtcState() == null || invite.getBtcState() == BTCState.UNFULFILLED) {

            invite.setBtcTransactionId(txID);
            invite.setBtcState(BTCState.FULFILLED);
            invite.update();
            invite.refresh();

            addInviteToTransInvitesSummary(invite);

            saveLocalTransaction(wallet, txID, invite);
        }
    }

    public void updateInviteAsCanceled(String inviteServerID) {
        InviteTransactionSummaryDao inviteDao = daoSessionManager.getInviteTransactionSummaryDao();

        InviteTransactionSummary invite = inviteDao.queryBuilder().
                where(InviteTransactionSummaryDao.Properties.ServerId.eq(inviteServerID)).
                limit(1).unique();

        if (invite == null) {
            return;
        }

        invite.setBtcTransactionId("");
        invite.setBtcState(BTCState.CANCELED);
        invite.update();
        invite.refresh();

        addInviteToTransInvitesSummary(invite);
    }

    public List<InviteTransactionSummary> gatherUnfulfilledInviteTrans() {
        InviteTransactionSummaryDao inviteDao = daoSessionManager.getInviteTransactionSummaryDao();

        return inviteDao.queryBuilder()
                .where(InviteTransactionSummaryDao.Properties.Type.eq(Type.SENT.getId()),
                        InviteTransactionSummaryDao.Properties.Address.isNotNull(),
                        InviteTransactionSummaryDao.Properties.Address.notEq(""))
                .whereOr(
                        InviteTransactionSummaryDao.Properties.BtcTransactionId.isNull(),
                        InviteTransactionSummaryDao.Properties.BtcTransactionId.eq(""))
                .where(InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.UNFULFILLED.getId()))
                .list();

    }

    public InviteTransactionSummary getInviteTransactionSummary(String inviteServerID) {
        InviteTransactionSummaryDao inviteDao = daoSessionManager.getInviteTransactionSummaryDao();

        return inviteDao.queryBuilder().
                where(InviteTransactionSummaryDao.Properties.ServerId.eq(inviteServerID)).
                limit(1).unique();
    }

    public List<InviteTransactionSummary> getInvitesWithTxID() {
        InviteTransactionSummaryDao inviteDao = daoSessionManager.getInviteTransactionSummaryDao();

        return inviteDao.queryBuilder().where(
                InviteTransactionSummaryDao.Properties.BtcTransactionId.isNotNull(),
                InviteTransactionSummaryDao.Properties.BtcTransactionId.notEq("")).list();
    }

    public TransactionSummary getTransactionWithTxID(String txID) {
        TransactionSummaryDao transactionTableDao = daoSessionManager.getTransactionSummaryDao();

        return transactionTableDao.queryBuilder().where(
                Properties.Txid.eq(txID)).limit(1).unique();
    }

    public void joinInviteToTx(@NonNull InviteTransactionSummary invite, @NonNull TransactionSummary transaction) {

        TransactionsInvitesSummaryDao joinTableDao = daoSessionManager.getTransactionsInvitesSummaryDao();
        TransactionsInvitesSummary joinContainingRealTX = joinTableDao.queryBuilder()
                .where(TransactionsInvitesSummaryDao.Properties.TransactionSummaryID.eq(transaction.getId())).limit(1).unique();
        TransactionsInvitesSummary joinContainingInvite = joinTableDao.queryBuilder()
                .where(TransactionsInvitesSummaryDao.Properties.InviteSummaryID.eq(invite.getId())).limit(1).unique();

        if (joinContainingRealTX == joinContainingInvite) return;

        dropEntry(joinContainingInvite);
        dropEntry(joinContainingRealTX);


        TransactionsInvitesSummary newJoinTable = daoSessionManager.newTransactionInviteSummary();
        daoSessionManager.insert(newJoinTable);

        newJoinTable.setInviteTransactionSummary(invite);
        newJoinTable.setInviteTxID(invite.getBtcTransactionId());

        newJoinTable.setTransactionTxID(transaction.getTxid());
        newJoinTable.setTransactionSummary(transaction);

        if (transaction.getTxTime() > 0) {
            newJoinTable.setBtcTxTime(transaction.getTxTime());
            newJoinTable.setInviteTime(0);
        } else {
            newJoinTable.setInviteTime(invite.getSentDate());
        }

        newJoinTable.update();
        newJoinTable.refresh();

        invite.setTransactionsInvitesSummaryID(newJoinTable.getId());
        invite.update();
        invite.refresh();

        transaction.setTransactionsInvitesSummaryID(newJoinTable.getId());
        transaction.update();
        transaction.refresh();
    }

    public String markTransactionSummaryAsFailedToBroadcast(String txid) {
        TransactionSummary transaction = getTransactionDao().queryBuilder().
                where(Properties.Txid.eq(txid)).
                limit(1).unique();

        if (transaction == null) return null;

        markTargetStatsAsCanceled(transaction.getReceiver());
        markFundingStatsAsCanceled(transaction.getFunder());

        transaction.setMemPoolState(MemPoolState.FAILED_TO_BROADCAST);
        transaction.update();
        transaction.refresh();

        return renameTXIDToFailed(txid);
    }

    public void markTransactionSummaryAsAcknowledged(String txid) {
        TransactionSummary transaction = getTransactionDao().queryBuilder().
                where(Properties.Txid.eq(txid)).
                limit(1).unique();

        if (transaction == null) return;

        transaction.setMemPoolState(MemPoolState.ACKNOWLEDGE);
        transaction.update();
        transaction.refresh();

    }

    public TransactionSummary createInitialTransaction(String txid) {
        return createInitialTransaction(txid, null);
    }

    public TransactionSummary createInitialTransactionForCompletedBroadcast(CompletedBroadcastDTO completedBroadcastActivityDTO) {
        return createInitialTransaction(completedBroadcastActivityDTO.getTransactionId(), completedBroadcastActivityDTO.getIdentity());
    }

    void saveOut(TransactionSummary transaction, VOut out) {
        TargetStatDao dao = daoSessionManager.getTargetStatDao();
        String[] addresses = out.getScriptPubKey().getAddresses();

        if (null != addresses && addresses.length == 0)
            throw new IllegalArgumentException();

        TargetStat target = dao.queryBuilder().where(TargetStatDao.Properties.Tsid.eq(transaction.getId()),
                TargetStatDao.Properties.Value.eq(out.getValue()),
                TargetStatDao.Properties.Position.eq(out.getIndex()),
                TargetStatDao.Properties.Addr.eq(addresses[0])).
                unique();

        Address dbAddress = daoSessionManager.getAddressDao().queryBuilder().where(AddressDao.Properties.Address.eq(addresses[0])).unique();

        if (null != target) {
            target.refresh();
        } else {
            target = new TargetStat();
            target.__setDaoSession(daoSessionManager.getDaoSession());
        }

        if (null != dbAddress) {
            target.setAddress(dbAddress);
            target.setWallet(transaction.getWallet());
        }

        MemPoolState transactionState = transaction.getMemPoolState();
        TargetStat.State targetState;
        switch (transactionState) {
            case ACKNOWLEDGE:
            case MINED:
                targetState = TargetStat.State.ACKNOWLEDGE;
                break;
            case FAILED_TO_BROADCAST:
            case DOUBLE_SPEND:
            case ORPHANED:
                targetState = TargetStat.State.CANCELED;
                break;
            default:
                targetState = TargetStat.State.PENDING;
        }

        target.setAddr(addresses[0]);
        target.setPosition(out.getIndex());
        target.setTransaction(transaction);
        target.setValue(out.getValue());
        target.setState(targetState);
        target.setTxTime(transaction.getTxTime());
        dao.save(target);
    }

    void saveIn(TransactionSummary transaction, VIn in) {
        FundingStatDao dao = daoSessionManager.getFundingStatDao();

        String[] addresses = in.getPreviousOutput().getScriptPubKey().getAddresses();
        if (null != addresses && addresses.length == 0)
            throw new IllegalArgumentException();

        FundingStat funder = dao.queryBuilder().where(
                FundingStatDao.Properties.Tsid.eq(transaction.getId()),
                FundingStatDao.Properties.FundedTransaction.eq(in.getTransactionId()),
                FundingStatDao.Properties.Value.eq(in.getPreviousOutput().getValue()),
                FundingStatDao.Properties.Position.eq(in.getPreviousOutput().getIndex()),
                FundingStatDao.Properties.Addr.eq(in.getPreviousOutput().getScriptPubKey().getAddresses()[0]))
                .unique();

        Address dbAddress = daoSessionManager.getAddressDao().queryBuilder().where(AddressDao.Properties.Address.eq(addresses[0])).unique();

        if (funder == null) {
            funder = new FundingStat();
            funder.__setDaoSession(daoSessionManager.getDaoSession());
        } else {
            funder.refresh();
        }

        if (dbAddress != null) {
            funder.setAddress(dbAddress);
            funder.setWallet(transaction.getWallet());
        }

        MemPoolState transactionState = transaction.getMemPoolState();
        FundingStat.State fundingState;
        switch (transactionState) {
            case ACKNOWLEDGE:
            case MINED:
                fundingState = FundingStat.State.ACKNOWLEDGE;
                break;
            case FAILED_TO_BROADCAST:
            case DOUBLE_SPEND:
            case ORPHANED:
                fundingState = FundingStat.State.CANCELED;
                break;
            default:
                fundingState = FundingStat.State.PENDING;
        }

        funder.setAddr(addresses[0]);
        funder.setPosition(in.getPreviousOutput().getIndex());
        funder.setTransaction(transaction);
        funder.setFundedTransaction(in.getTransactionId());
        funder.setState(fundingState);
        funder.setValue(in.getPreviousOutput().getValue());
        dao.save(funder);
        dao.refresh(funder);
    }

    void saveTransaction(TransactionSummary transaction, TransactionDetail detail) {
        if (detail.getBlocktimeMillis() > 0L) {
            transaction.setTxTime(detail.getBlocktimeMillis());
        } else if (detail.getTimeMillis() > 0L) {
            transaction.setTxTime(detail.getTimeMillis());
        } else {
            transaction.setTxTime(detail.getReceivedTimeMillis());
        }

        transaction.setBlockhash(detail.getBlockhash());
        transaction.setBlockheight(detail.getBlockheight());
        if (detail.isInBlock()) {
            transaction.setMemPoolState(MemPoolState.MINED);
        }
        transaction.update();
        transaction.refresh();

        try {
            for (VIn in : detail.getVInList()) {
                saveIn(transaction, in);
            }

            for (VOut out : detail.getVOutList()) {
                saveOut(transaction, out);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }


        daoSessionManager.clearCacheFor(transaction);
        transaction.setNumInputs(transaction.getFunder().size());
        transaction.setNumOutputs(transaction.getReceiver().size());
        transaction.update();
        transaction.refresh();

        try {
            addTransactionToTransInvitesSummary(transaction);
        } catch (Exception ex) {
            Log.e(TAG, "Transaction To InvitesSummary for txID: " + transaction.getTxid());
        }
    }

    InviteTransactionSummary saveInviteTransaction(long walletId,
                                                   String inviteServerID,
                                                   Type type,
                                                   UserIdentity fromUser,
                                                   UserIdentity toUser,
                                                   long historicUSAValue,
                                                   Long sentDate,
                                                   String inviteStatus,
                                                   Long valueSatoshis,
                                                   Long fee,
                                                   String address,
                                                   String btcTxID) {

        InviteTransactionSummaryDao inviteDao = daoSessionManager.getInviteTransactionSummaryDao();


        InviteTransactionSummary invite = inviteDao.
                queryBuilder().
                where(InviteTransactionSummaryDao.Properties.ServerId.eq(inviteServerID)).
                limit(1)
                .unique();

        if (invite == null) {
            invite = new InviteTransactionSummary();
            invite.setServerId(inviteServerID);
            inviteDao.insert(invite);
        } else {
            TransactionsInvitesSummary transactionsInvitesSummary = invite.getTransactionsInvitesSummary();
            if (transactionsInvitesSummary != null) {
                TransactionSummary tx = transactionsInvitesSummary.getTransactionSummary();
                if (tx != null && tx.getMemPoolState() == MemPoolState.FAILED_TO_BROADCAST) {
                    return null;
                }
            }
        }

        BTCState state = BTCState.from(inviteStatus);
        invite.setBtcState(state);
        invite.setHistoricValue(historicUSAValue);
        invite.setToUser(toUser);
        invite.setFromUser(fromUser);
        invite.setSentDate(sentDate);
        invite.setValueSatoshis(valueSatoshis);
        invite.setValueFeesSatoshis(fee);
        invite.setWalletId(walletId);
        invite.setAddress(address);
        invite.setBtcTransactionId(btcTxID);
        invite.setType(type);

        invite.update();
        invite.refresh();

        if (invite.getTransactionsInvitesSummary() == null) {
            addInviteToTransInvitesSummary(invite);
        }

        TransactionsInvitesSummary summary = invite.getTransactionsInvitesSummary();
        summary.setToUser(toUser);
        summary.setFromUser(fromUser);
        summary.update();

        return invite;
    }

    @Deprecated()
    void addTransactionToTransInvitesSummary(TransactionSummary transaction) {
        TransactionsInvitesSummaryDao transInvitesDao = daoSessionManager.getTransactionsInvitesSummaryDao();

        TransactionsInvitesSummary transactionsInvitesSummary = transInvitesDao.queryBuilder().whereOr(
                TransactionsInvitesSummaryDao.Properties.TransactionSummaryID.eq(transaction.getId()),
                TransactionsInvitesSummaryDao.Properties.InviteTxID.eq(transaction.getTxid()))
                .limit(1).unique();

        if (transactionsInvitesSummary == null) {
            transactionsInvitesSummary = new TransactionsInvitesSummary();
            transInvitesDao.insert(transactionsInvitesSummary);
        }

        transactionsInvitesSummary.setTransactionSummary(transaction);
        transactionsInvitesSummary.setTransactionSummaryID(transaction.getId());
        if (transaction.getTxTime() > 0L) {
            transactionsInvitesSummary.setBtcTxTime(transaction.getTxTime());
            transactionsInvitesSummary.setInviteTime(0);
        }
        transactionsInvitesSummary.setTransactionTxID(transaction.getTxid());

        transaction.setTransactionsInvitesSummary(transactionsInvitesSummary);
        transaction.setTransactionsInvitesSummaryID(transactionsInvitesSummary.getId());

        transaction.update();
        transaction.refresh();
        transactionsInvitesSummary.update();
        transactionsInvitesSummary.refresh();
        transInvitesDao.refresh(transactionsInvitesSummary);
    }

    protected TransactionsInvitesSummary joinInviteToTransaction(TransactionsInvitesSummary transactionsInvitesSummary, String btcTxID) {
        TransactionsInvitesSummaryDao transInvitesDao = daoSessionManager.getTransactionsInvitesSummaryDao();

        if (transactionsInvitesSummary == null) {
            return null;//nothing to join
        }


        TransactionSummary tx = transactionsInvitesSummary.getTransactionSummary();
        InviteTransactionSummary invite = transactionsInvitesSummary.getInviteTransactionSummary();
        if (tx != null && invite != null) {
            return transactionsInvitesSummary;//its already joined
        }


        dropEntry(transactionsInvitesSummary);//there must be two entries for this invite, lets remove this one and find the second one
        return transInvitesDao.queryBuilder().
                where(TransactionsInvitesSummaryDao.Properties.TransactionTxID.eq(btcTxID)).
                limit(1).unique();
    }

    protected void saveLocalTransaction(Wallet wallet, String txID, InviteTransactionSummary invite) {
        TransactionSummaryDao dao = getTransactionDao();
        TransactionSummary transaction = dao.queryBuilder().
                where(Properties.Txid.eq(txID)).
                limit(1).unique();

        if (null == transaction) {
            transaction = new TransactionSummary();
            transaction.setTxid(txID);
            transaction.setMemPoolState(MemPoolState.INIT);
            transaction.setWallet(wallet);
            long currentInitTime = dateUtil.getCurrentTimeInMillis();
            transaction.setTxTime(currentInitTime);

            dao.insert(transaction);
            dao.refresh(transaction);
        }
    }

    protected long calculatePastTimeFromNow(long olderThanSeconds) {
        long currentTimeInMillis = dateUtil.getCurrentTimeInMillis();
        long olderThanTimeInMillis = TimeUnit.SECONDS.toMillis(olderThanSeconds);

        return currentTimeInMillis - olderThanTimeInMillis;
    }

    private TransactionSummaryDao getTransactionDao() {
        return daoSessionManager.getTransactionSummaryDao();
    }

    private InviteTransactionSummaryDao getInviteTransactionDao() {
        return daoSessionManager.getInviteTransactionSummaryDao();
    }

    private TransactionsInvitesSummaryDao getTransactionsInvitesSummaryDao() {
        return daoSessionManager.getTransactionsInvitesSummaryDao();
    }

    private void dropEntry(TransactionsInvitesSummary transInvite) {
        TransactionsInvitesSummaryDao transInvitesDao = daoSessionManager.getTransactionsInvitesSummaryDao();
        if (transInvite == null) {
            return;
        }
        transInvitesDao.delete(transInvite);
    }

    private void updateInviteTimeCompleteTransactionInviteSummary(InviteTransactionSummary invite) {
        TransactionsInvitesSummaryDao query = daoSessionManager.getTransactionsInvitesSummaryDao();
        TransactionsInvitesSummary summary = query.queryBuilder()
                .where(TransactionsInvitesSummaryDao.Properties.InviteSummaryID.eq(invite.getId()))
                .limit(1).unique();

        if (null != summary) {
            summary.setInviteTime(0);
            summary.setBtcTxTime(invite.getSentDate());
            summary.update();
        }
    }

    private void markTargetStatsAsCanceled(List<TargetStat> receivers) {
        for (TargetStat targetStat : receivers) {
            targetStat.setState(TargetStat.State.CANCELED);

            targetStat.update();
            targetStat.refresh();

            removeFundingStatTargetStatRelationship(targetStat);
        }
    }

    private void markFundingStatsAsCanceled(List<FundingStat> funders) {
        for (FundingStat fundingStat : funders) {
            fundingStat.setState(FundingStat.State.CANCELED);

            fundingStat.update();
            fundingStat.refresh();

            removeFundingStatTargetStatRelationship(fundingStat);
        }

    }

    private void removeFundingStatTargetStatRelationship(FundingStat fundingStat) {
        TargetStatDao dao = daoSessionManager.getTargetStatDao();

        TargetStat targetStat = dao.queryBuilder().where(TargetStatDao.Properties.FundingId.eq(fundingStat.getId())).
                limit(1).unique();

        if (targetStat == null) return;

        targetStat.setFundingStat(null);
        targetStat.update();
        targetStat.refresh();
    }

    private void removeFundingStatTargetStatRelationship(TargetStat targetStat) {
        FundingStatDao dao = daoSessionManager.getFundingStatDao();

        FundingStat fundingStat = dao.queryBuilder().where(FundingStatDao.Properties.TargetId.eq(targetStat.getId())).
                limit(1).unique();

        if (fundingStat == null) return;

        fundingStat.setTargetStat(null);
        fundingStat.update();
        fundingStat.refresh();
    }

    private String renameTXIDToFailed(String txid) {
        long currentTime = dateUtil.getCurrentTimeInMillis();
        String newTxId = String.format("failedToBroadcast_%s_%s", currentTime, txid);
        renameTransSummary(txid, newTxId);
        renameInviteSummary(txid, newTxId);
        renameTransInviteSummary(txid, newTxId);
        return newTxId;
    }

    private void renameTransSummary(String originalTxId, String newTxId) {
        TransactionSummary transaction = getTransactionDao().queryBuilder().
                where(Properties.Txid.eq(originalTxId)).
                limit(1).unique();

        if (transaction == null) return;

        transaction.setTxid(newTxId);
        transaction.update();
        transaction.refresh();
    }

    private void renameInviteSummary(String originalTxId, String newTxId) {
        InviteTransactionSummary invite = getInviteTransactionDao().queryBuilder().
                where(InviteTransactionSummaryDao.Properties.BtcTransactionId.eq(originalTxId)).
                limit(1).unique();

        if (invite == null) return;

        invite.setBtcTransactionId(newTxId);
        invite.update();
        invite.refresh();
    }

    private void renameTransInviteSummary(String originalTxId, String newTxId) {
        TransactionsInvitesSummary transactionsInvitesSummary = getTransactionsInvitesSummaryDao().queryBuilder().
                where(TransactionsInvitesSummaryDao.Properties.InviteTxID.eq(originalTxId)).
                limit(1).unique();

        if (transactionsInvitesSummary == null) return;

        transactionsInvitesSummary.setInviteTxID(newTxId);
        transactionsInvitesSummary.update();
        transactionsInvitesSummary.refresh();
    }


    private TransactionSummary createInitialTransaction(String transactionId, Identity identity) {
        TransactionSummary transactionSummary = daoSessionManager.newTransactionSummary();
        transactionSummary.setTxid(transactionId);
        transactionSummary.setWallet(walletHelper.getWallet());
        transactionSummary.setMemPoolState(MemPoolState.PENDING);
        transactionSummary.setNumConfirmations(0);
        transactionSummary.setTxTime(dateUtil.getCurrentTimeInMillis());

        daoSessionManager.insert(transactionSummary);

        TransactionsInvitesSummary transactionInviteSummary = transactionInviteSummaryHelper.getOrCreateTransactionInviteSummaryFor(transactionSummary);
        addUserIdentitiesToTransaction(identity, transactionInviteSummary);
        transactionInviteSummary.update();
        return transactionSummary;
    }

    private void addUserIdentitiesToTransaction(Identity identity, TransactionsInvitesSummary transactionInviteSummary) {
        if (identity == null) return;

        DropbitMeIdentity myIdentity = dropbitAccountHelper.identityForType(identity.getIdentityType());
        if (myIdentity != null) {
            UserIdentity fromUser = userIdentityHelper.updateFrom(myIdentity);
            transactionInviteSummary.setFromUser(fromUser);
        }

        UserIdentity toUser = userIdentityHelper.updateFrom(identity);
        transactionInviteSummary.setToUser(toUser);
    }

}
