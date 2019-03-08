package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.query.InviteSummaryQueryManager;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

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

    TransactionsInvitesSummary getOrCreateInviteSummaryWithServerId(String cnId) {
        InviteTransactionSummary inviteTransactionSummary = inviteSummaryQueryManager.getInviteSummaryByCnId(cnId);
        if (null == inviteTransactionSummary)
            return createInviteTransactionSummaryWithParent(cnId);
        return inviteTransactionSummary.getTransactionsInvitesSummary();
    }

    private TransactionsInvitesSummary createInviteTransactionSummaryWithParent(String cnId) {
        TransactionsInvitesSummary transactionsInvitesSummary = daoSessionManager.newTransactionInviteSummary();
        InviteTransactionSummary inviteTransactionSummary = daoSessionManager.newInviteTransactionSummary();
        inviteTransactionSummary.setServerId(cnId);
        long inviteId = daoSessionManager.insert(inviteTransactionSummary);
        long joinId = daoSessionManager.insert(transactionsInvitesSummary);
        transactionsInvitesSummary.setInviteSummaryID(inviteId);
        transactionsInvitesSummary.update();
        inviteTransactionSummary.setTransactionsInvitesSummaryID(joinId);
        inviteTransactionSummary.update();
        return transactionsInvitesSummary;
    }

    public InviteTransactionSummary saveCompletedSentInvite(CompletedInviteDTO completedInviteDTO) {
        TransactionsInvitesSummary transactionsInvitesSummary = getOrCreateInviteSummaryWithServerId(completedInviteDTO.getCnId());
        transactionsInvitesSummary.setInviteTime(completedInviteDTO.getInvitedContact().getCreatedAt());
        transactionsInvitesSummary.update();
        InviteTransactionSummary invite = transactionsInvitesSummary.getInviteTransactionSummary();
        USDCurrency conversionCurrency = new USDCurrency(completedInviteDTO.getBitcoinPrice());
        BTCCurrency btcCurrency = new BTCCurrency(completedInviteDTO.getInviteAmount() + completedInviteDTO.getInviteFee());
        USDCurrency totalUsdSpending = btcCurrency.toUSD(conversionCurrency);

        invite.setInviteName(completedInviteDTO.getContact().getDisplayName());
        invite.setHistoricValue(totalUsdSpending.toLong());
        invite.setSenderPhoneNumber(walletHelper.getUserAccount().getPhoneNumber());
        invite.setReceiverPhoneNumber(completedInviteDTO.getContact().getPhoneNumber());
        invite.setSentDate(completedInviteDTO.getInvitedContact().getCreatedAt());
        invite.setValueSatoshis(completedInviteDTO.getInviteAmount());
        invite.setValueFeesSatoshis(completedInviteDTO.getInviteFee());
        invite.setWallet(walletHelper.getWallet());
        invite.setBtcState(BTCState.from(completedInviteDTO.getInvitedContact().getStatus()));
        invite.setType(Type.SENT);
        invite.update();

        return invite;
    }

    public void updateFulfilledInvite(TransactionsInvitesSummary transactionsInvitesSummary,
                                      TransactionBroadcastResult transactionBroadcastResult) {

        String txid = transactionBroadcastResult.getTxId();
        TransactionSummary transactionSummary = transactionHelper.createInitialTransaction(txid);
        InviteTransactionSummary inviteTransactionSummary = transactionsInvitesSummary.getInviteTransactionSummary();
        inviteTransactionSummary.setBtcTransactionId(txid);
        inviteTransactionSummary.setBtcState(BTCState.FULFILLED);
        transactionsInvitesSummary.setInviteTxID(txid);
        transactionsInvitesSummary.setTransactionTxID(txid);
        transactionsInvitesSummary.setTransactionSummary(transactionSummary);
        transactionsInvitesSummary.setInviteTime(0L);
        transactionsInvitesSummary.setBtcTxTime(dateUtil.getCurrentTimeInMillis());
        inviteTransactionSummary.update();
        transactionsInvitesSummary.update();

    }
}
