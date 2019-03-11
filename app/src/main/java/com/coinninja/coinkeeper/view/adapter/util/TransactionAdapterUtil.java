package com.coinninja.coinkeeper.view.adapter.util;

import android.content.Context;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.util.DateFormatUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.InviteState;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.SendState;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public class TransactionAdapterUtil {
    private final DateFormatUtil dateFormatter;
    private final Context context;
    private InviteTransactionSummary invite;
    private TransactionSummary transaction;
    private PhoneNumberUtil phoneNumberUtil;

    @Inject
    public TransactionAdapterUtil(DateFormatUtil dateFormatter, @ApplicationContext Context context,
                                  PhoneNumberUtil phoneNumberUtil) {
        this.dateFormatter = dateFormatter;
        this.context = context;
        this.phoneNumberUtil = phoneNumberUtil;
    }

    public BindableTransaction translateTransaction(TransactionsInvitesSummary summary) {
        BindableTransaction bindableTransaction = new BindableTransaction();

        invite = summary.getInviteTransactionSummary();
        transaction = summary.getTransactionSummary();

        translateInvite(bindableTransaction);
        translateTransaction(bindableTransaction);

        return bindableTransaction;
    }

    private void translateTransaction(BindableTransaction bindableTransaction) {
        if (null == transaction) return;

        translateTransactionDate(bindableTransaction, transaction.getTxTime());
        translateConfirmations(bindableTransaction, transaction.getNumConfirmations());
        bindableTransaction.setTxID(transaction.getTxid());
        bindableTransaction.setFee(transaction.getFee());
        setupContact(transaction, bindableTransaction);
        bindableTransaction.setHistoricalTransactionUSDValue(transaction.getHistoricPrice());
        setSendState(transaction, bindableTransaction);
        translateSendState(bindableTransaction);
        setupTransactionNotification(transaction.getTransactionNotification(), bindableTransaction);
    }

    private void setSendState(TransactionSummary transaction, BindableTransaction bindableTransaction) {
        if (transaction == null || invite != null) return;

        boolean isSend = false;
        boolean isReceive = false;
        for (FundingStat fundingStat : transaction.getFunder()) {
            if (fundingStat.getAddress() != null) {
                isSend = true;
            }
        }

        Address address;
        for (TargetStat targetStat : transaction.getReceiver()) {
            address = targetStat.getAddress();
            if (address != null && address.getChangeIndex() != HDWallet.INTERNAL) {
                isReceive = true;
            }
        }
        if (isSend && isReceive) {
            bindableTransaction.setSendState(SendState.TRANSFER);
        } else if (isSend) {
            bindableTransaction.setSendState(SendState.SEND);
        } else {
            bindableTransaction.setSendState(SendState.RECEIVE);
        }
    }


    private void translateSendState(BindableTransaction bindableTransaction) {
        if (transaction == null) return;

        switch (transaction.getMemPoolState()) {
            case DOUBLE_SPEND:
            case ORPHANED:
            case FAILED_TO_BROADCAST:
                translateTargets(bindableTransaction);
                if (bindableTransaction.getSendState() == SendState.RECEIVE) {
                    bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_RECEIVE);
                } else if (bindableTransaction.getSendState() == SendState.TRANSFER || bindableTransaction.getSendState() == SendState.FAILED_TO_BROADCAST_TRANSFER) {
                    bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_TRANSFER);
                } else {
                    bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_SEND);
                }
                return;
            case INIT:
                return;
            case PENDING:
            case ACKNOWLEDGE:
            case MINED:
            default:
                buildTransactionDetails(bindableTransaction);
        }
    }

    private void buildTransactionDetails(BindableTransaction bindableTransaction) {
        switch (bindableTransaction.getSendState()) {
            case TRANSFER:
                bindableTransaction.setSendState(SendState.TRANSFER);
                translateTargets(bindableTransaction);
                break;
            case RECEIVE_CANCELED:
            case RECEIVE:
                translateReceive(bindableTransaction);
                break;
            case SEND_CANCELED:
            case SEND:
                translateSend(bindableTransaction);
                break;
            default:


        }
        calculateTransactionValue(bindableTransaction, transaction);
    }

    private void calculateTransactionValue(BindableTransaction bindableTransaction, TransactionSummary transaction) {
        long value = 0L;

        for (TargetStat stat : transaction.getReceiver()) {
            if (bindableTransaction.getSendState() == SendState.SEND && stat.getAddress() == null) {
                value += stat.getValue();
            } else if (bindableTransaction.getSendState() == SendState.RECEIVE && stat.getAddress() != null) {
                value += stat.getValue();
            }
        }

        bindableTransaction.setValue(value);

        if (transaction.getMemPoolState() == MemPoolState.FAILED_TO_BROADCAST && value == 0) {
            bindableTransaction.setValue(transaction.getFee());
            bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_TRANSFER);
        }
    }

    private void translateReceive(BindableTransaction bindableTransaction) {
        for (TargetStat stat : transaction.getReceiver()) {
            if (stat.getAddress() != null) {
                if (bindableTransaction.getTargetAddress().isEmpty()) {
                    bindableTransaction.setTargetAddress(stat.getAddr());
                }
            }
        }
        for (FundingStat funder : transaction.getFunder()) {
            if (funder.getAddress() == null) {
                bindableTransaction.setFundingAddress(funder.getAddr());
                break;
            }
        }

        if (bindableTransaction.getSendState() != SendState.RECEIVE_CANCELED ||
                bindableTransaction.getSendState() != SendState.SEND_CANCELED)
            bindableTransaction.setSendState(SendState.RECEIVE);
    }

    private void translateTargets(BindableTransaction bindableTransaction) {

        if (transaction.getReceiver() != null && !transaction.getReceiver().isEmpty())
            bindableTransaction.setTargetAddress(transaction.getReceiver().get(0).getAddr());


        if (transaction.getFunder() != null && !transaction.getFunder().isEmpty())
            bindableTransaction.setFundingAddress(transaction.getFunder().get(0).getAddr());

    }

    private void translateSend(BindableTransaction bindableTransaction) {
        for (TargetStat stat : transaction.getReceiver()) {
            if (stat.getAddress() == null) {
                bindableTransaction.setTargetAddress(stat.getAddr());
                break;
            }
        }

        for (FundingStat funder : transaction.getFunder()) {
            if (funder.getAddress() != null) {
                bindableTransaction.setFundingAddress(funder.getAddr());
                break;
            }
        }


        if (bindableTransaction.getSendState() != SendState.RECEIVE_CANCELED ||
                bindableTransaction.getSendState() != SendState.SEND_CANCELED)
            bindableTransaction.setSendState(SendState.SEND);
    }

    private void translateConfirmations(BindableTransaction bindableTransaction,
                                        int confirmations) {

        bindableTransaction.setConfirmationCount(confirmations);
        switch (confirmations) {
            case 0:
                bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.UNCONFIRMED);
                break;
            default:
                bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.CONFIRMED);
        }
    }

    private void translateTransactionDate(BindableTransaction bindableTransaction, long txTime) {
        if (txTime == 0L) {
            bindableTransaction.setTxTime("");
        } else {
            bindableTransaction.setTxTime(dateFormatter.formatTime(txTime));
        }
    }

    public void setupContact(TransactionSummary transactionSummary, BindableTransaction bindableTransaction) {
        if (null != transactionSummary.getTransactionsInvitesSummary().getToName() &&
                !transactionSummary.getTransactionsInvitesSummary().getToName().isEmpty()) {
            bindableTransaction.setContactName(transactionSummary.getTransactionsInvitesSummary().getToName());
        }

        PhoneNumber toPhoneNumber = transactionSummary.getTransactionsInvitesSummary().getToPhoneNumber();
        if (null != toPhoneNumber) {
            bindableTransaction.setContactPhoneNumber(toPhoneNumber.displayTextForLocale());
        }
    }

    private void translateInvite(BindableTransaction bindableTransaction) {
        if (null == invite) return;
        bindableTransaction.setValue(invite.getValueSatoshis());
        if (invite.getAddress() != null)
            bindableTransaction.setTargetAddress(invite.getAddress());
        bindableTransaction.setHistoricalInviteUSDValue(invite.getHistoricValue());
        bindableTransaction.setServerInviteId(invite.getServerId());
        translateTransactionDate(bindableTransaction, invite.getSentDate());
        translateInviteState(bindableTransaction);
        translateInviteDisplayName(bindableTransaction);
        translateInviteFee(bindableTransaction);
        translateInviteType(bindableTransaction);
        TransactionNotification transactionNotification = invite.getTransactionNotification();
        setupTransactionNotification(transactionNotification, bindableTransaction);
    }

    private void setupTransactionNotification(TransactionNotification transactionNotification, BindableTransaction bindableTransaction) {
        if (transactionNotification == null || bindableTransaction == null) {
            return;
        }

        bindableTransaction.setMemo(transactionNotification.getMemo());
        bindableTransaction.setIsSharedMemo(transactionNotification.getIsShared());

        PhoneNumber sendersNumber = transactionNotification.getPhoneNumber();
        if (sendersNumber != null) {
            bindableTransaction.setContactPhoneNumber(sendersNumber.toNationalDisplayText());
        }
    }

    private void translateInviteDisplayName(BindableTransaction bindableTransaction) {
        String displayName = invite.getInviteName();
        if (displayName != null || !displayName.isEmpty()) {
            bindableTransaction.setContactName(displayName);
        }
    }

    @NonNull
    private void translateInviteFee(BindableTransaction bindableTransaction) {
        Long feeDisplay = invite.getValueFeesSatoshis();
        if (feeDisplay == null || feeDisplay < 1) {
            feeDisplay = 0L;
        }
        bindableTransaction.setFee(feeDisplay);
    }

    private void translateInviteType(BindableTransaction bindableTransaction) {
        Type type = invite.getType();

        switch (type) {
            case SENT:
                bindableTransaction.setContactPhoneNumber(invite.getReceiverPhoneNumber().displayTextForLocale());

                bindableTransaction.setSendState(SendState.SEND);
                break;
            case RECEIVED:
                bindableTransaction.setContactPhoneNumber(invite.getSenderPhoneNumber().displayTextForLocale());
                bindableTransaction.setSendState(SendState.RECEIVE);
                break;
        }

        if (bindableTransaction.getInviteState() == InviteState.EXPIRED || bindableTransaction.getInviteState() == InviteState.CANCELED) {
            if (type == Type.SENT)
                bindableTransaction.setSendState(SendState.SEND_CANCELED);
            else
                bindableTransaction.setSendState(SendState.RECEIVE_CANCELED);
        }
    }

    private void translateInviteState(BindableTransaction bindableTransaction) {
        if (null == invite) return;
        if (transaction != null) {
            bindableTransaction.setInviteState(InviteState.CONFIRMED);
            return;
        }

        switch (invite.getBtcState()) {
            case EXPIRED:
                bindableTransaction.setInviteState(InviteState.EXPIRED);
                break;
            case CANCELED:
                bindableTransaction.setInviteState(InviteState.CANCELED);
                break;
            default:
                translatePendingInviteStateFromType(bindableTransaction);
                break;
        }
    }

    private void translatePendingInviteStateFromType(BindableTransaction bindableTransaction) {
        String address = invite.getAddress();
        switch (invite.getType()) {
            case SENT:
                translateSendInviteState(bindableTransaction, address);
                break;
            case RECEIVED:
                translateReceiveInviteState(bindableTransaction, address);
                break;
        }
    }

    private void translateReceiveInviteState(BindableTransaction bindableTransaction, String address) {
        if (null == address || address.isEmpty())
            bindableTransaction.setInviteState(InviteState.RECEIVED_PENDING);
        else
            bindableTransaction.setInviteState(InviteState.RECEIVED_ADDRESS_PROVIDED);
    }

    private void translateSendInviteState(BindableTransaction bindableTransaction, String address) {
        if (null == address || address.isEmpty())
            bindableTransaction.setInviteState(InviteState.SENT_PENDING);
        else
            bindableTransaction.setInviteState(InviteState.SENT_ADDRESS_PROVIDED);
    }
}
