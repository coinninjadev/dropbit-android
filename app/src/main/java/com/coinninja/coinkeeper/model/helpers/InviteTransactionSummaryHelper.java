package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.model.query.InviteSummaryQueryManager;
import com.coinninja.coinkeeper.service.client.model.SentInvite;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.coinninja.coinkeeper.model.db.enums.BTCState.UNACKNOWLEDGED;

public class InviteTransactionSummaryHelper {
    private final InviteSummaryQueryManager inviteSummaryQueryManager;
    private final WalletHelper walletHelper;
    private final DaoSessionManager daoSessionManager;
    private final TransactionHelper transactionHelper;
    private final DateUtil dateUtil;
    private PhoneNumberUtil phoneNumberUtil;

    @Inject
    InviteTransactionSummaryHelper(InviteSummaryQueryManager inviteSummaryQueryManager, WalletHelper walletHelper,
                                   DaoSessionManager daoSessionManager, TransactionHelper transactionHelper, DateUtil dateUtil, PhoneNumberUtil phoneNumberUtil) {
        this.inviteSummaryQueryManager = inviteSummaryQueryManager;
        this.walletHelper = walletHelper;
        this.daoSessionManager = daoSessionManager;
        this.transactionHelper = transactionHelper;
        this.dateUtil = dateUtil;
        this.phoneNumberUtil = phoneNumberUtil;
    }

    @NonNull
    TransactionsInvitesSummary getOrCreateInviteSummaryWithServerId(String cnId) {
        InviteTransactionSummary inviteTransactionSummary = inviteSummaryQueryManager.getInviteSummaryByCnId(cnId);
        if (null == inviteTransactionSummary)
            return createInviteTransactionSummaryWithParent(cnId);
        return inviteTransactionSummary.getTransactionsInvitesSummary();
    }

    @Nullable
    TransactionsInvitesSummary getInviteSummaryWithServerId(String cnId) {
        InviteTransactionSummary inviteTransactionSummary = inviteSummaryQueryManager.getInviteSummaryByCnId(cnId);
        if (null == inviteTransactionSummary)
            return null;
        return inviteTransactionSummary.getTransactionsInvitesSummary();
    }

    private TransactionsInvitesSummary createInviteTransactionSummaryWithParent(String cnId) {
        TransactionsInvitesSummary transactionsInvitesSummary = daoSessionManager.newTransactionInviteSummary();
        InviteTransactionSummary inviteTransactionSummary = createInviteTransactionSummary(cnId);

        long joinId = daoSessionManager.insert(transactionsInvitesSummary);
        long inviteId = daoSessionManager.insert(inviteTransactionSummary);
        inviteTransactionSummary.setTransactionsInvitesSummaryID(joinId);
        transactionsInvitesSummary.setInviteSummaryID(inviteId);
        transactionsInvitesSummary.update();
        inviteTransactionSummary.update();

        return transactionsInvitesSummary;
    }

    private InviteTransactionSummary createInviteTransactionSummary(String cnId) {
        InviteTransactionSummary inviteTransactionSummary = daoSessionManager.newInviteTransactionSummary();
        inviteTransactionSummary.setServerId(cnId);
        daoSessionManager.insert(inviteTransactionSummary);

        return inviteTransactionSummary;
    }

    public InviteTransactionSummary saveTemporaryInvite(PendingInviteDTO pendingInviteDTO) {
        InviteTransactionSummary invite = createInviteTransactionSummary(pendingInviteDTO.getRequestId());
        USDCurrency conversionCurrency = new USDCurrency(pendingInviteDTO.getBitcoinPrice());
        BTCCurrency btcCurrency = new BTCCurrency(pendingInviteDTO.getInviteAmount() + pendingInviteDTO.getInviteFee());
        USDCurrency totalUsdSpending = btcCurrency.toUSD(conversionCurrency);

        invite.setInviteName(pendingInviteDTO.getContact().getDisplayName());
        invite.setHistoricValue(totalUsdSpending.toLong());
        invite.setSenderPhoneNumber(walletHelper.getUserAccount().getPhoneNumber());
        invite.setReceiverPhoneNumber(pendingInviteDTO.getContact().getPhoneNumber());
        invite.setValueSatoshis(pendingInviteDTO.getInviteAmount());
        invite.setValueFeesSatoshis(pendingInviteDTO.getInviteFee());
        invite.setWallet(walletHelper.getWallet());
        invite.setBtcState(UNACKNOWLEDGED);
        invite.setType(Type.SENT);
        invite.update();

        return invite;
    }

    @Nullable
    public InviteTransactionSummary acknowledgeInviteTransactionSummary(CompletedInviteDTO completedInviteDTO) {
        TransactionsInvitesSummary transactionsInvitesSummary = daoSessionManager.newTransactionInviteSummary();
        transactionsInvitesSummary.setInviteTime(completedInviteDTO.getInvitedContact().getCreatedAt());
        daoSessionManager.insert(transactionsInvitesSummary);

        InviteTransactionSummary invite = inviteSummaryQueryManager.getInviteSummaryByCnId(completedInviteDTO.getRequestId());
        transactionsInvitesSummary.setInviteSummaryID(invite.getId());
        invite.setTransactionsInvitesSummary(transactionsInvitesSummary);
        invite.setServerId(completedInviteDTO.getCnId());
        invite.setSentDate(completedInviteDTO.getInvitedContact().getCreatedAt());
        invite.setBtcState(BTCState.from(completedInviteDTO.getInvitedContact().getStatus()));
        invite.update();
        transactionsInvitesSummary.update();

        return invite;
    }

    public void acknowledgeInviteTransactionSummary(SentInvite sentInvite) {
        TransactionsInvitesSummary transactionsInvitesSummary = daoSessionManager.newTransactionInviteSummary();

        InviteTransactionSummary invite = inviteSummaryQueryManager.getInviteSummaryByCnId(sentInvite.getMetadata().getRequest_id());

        if (invite.getBtcState() != UNACKNOWLEDGED) { return; }

        transactionsInvitesSummary.setInviteTime(sentInvite.getCreated_at());
        invite.setTransactionsInvitesSummary(transactionsInvitesSummary);
        invite.setServerId(sentInvite.getId());
        invite.setSentDate(sentInvite.getCreated_at());
        invite.setBtcState(BTCState.from(sentInvite.getStatus()));
        transactionsInvitesSummary.update();
        invite.update();
    }

    public void updateFulfilledInvite(TransactionsInvitesSummary transactionsInvitesSummary,
                                      TransactionBroadcastResult transactionBroadcastResult) {
        String txid = transactionBroadcastResult.getTxId();
        InviteTransactionSummary inviteTransactionSummary = transactionsInvitesSummary.getInviteTransactionSummary();
        inviteTransactionSummary.setBtcTransactionId(txid);
        inviteTransactionSummary.setBtcState(BTCState.FULFILLED);
        inviteTransactionSummary.update();

        transactionsInvitesSummary.setInviteTxID(txid);
        transactionsInvitesSummary.setTransactionTxID(txid);
        transactionsInvitesSummary.setInviteTime(0L);
        transactionsInvitesSummary.setBtcTxTime(dateUtil.getCurrentTimeInMillis());
        transactionsInvitesSummary.update();

        TransactionSummary transactionSummary = transactionHelper.createInitialTransaction(txid);
        transactionsInvitesSummary.setTransactionSummary(transactionSummary);
    }

    public InviteTransactionSummary getInviteSummaryById(String id) {
        return inviteSummaryQueryManager.getInviteSummaryByCnId(id);
    }

    public List<InviteTransactionSummary> getAllUnacknowledgedInvitations() {
        return daoSessionManager.getInviteTransactionSummaryDao().queryBuilder().where(InviteTransactionSummaryDao.Properties.BtcState.eq(UNACKNOWLEDGED.getId())).list();
    }

    public List<InviteTransactionSummary> getUnfulfilledSentInvites() {
        QueryBuilder<InviteTransactionSummary> inviteTransactionSummaryQueryBuilder = daoSessionManager.getInviteTransactionSummaryDao().queryBuilder();
        return inviteTransactionSummaryQueryBuilder
                .where(InviteTransactionSummaryDao.Properties.BtcState.eq(BTCState.UNFULFILLED.getId()),
                        InviteTransactionSummaryDao.Properties.Type.eq(Type.SENT.getId()))
                .list();

    }
}
