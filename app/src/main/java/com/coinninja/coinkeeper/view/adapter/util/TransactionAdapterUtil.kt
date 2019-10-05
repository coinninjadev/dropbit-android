package com.coinninja.coinkeeper.view.adapter.util

import app.coinninja.cn.libbitcoin.HDWallet
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.di.interfaces.LightningDepositAddress
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.util.DateFormatUtil
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.InviteState
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.SendState
import javax.inject.Inject

@Mockable
class TransactionAdapterUtil @Inject constructor(
        internal val dateFormatter: DateFormatUtil,
        internal val bindableTransaction: BindableTransaction,
        @LightningDepositAddress internal val lightningDepositAddress: String
) {

    fun translateTransaction(summary: TransactionsInvitesSummary): BindableTransaction {
        bindableTransaction.reset()
        translateInvite(summary.inviteTransactionSummary)
        translateTransaction(summary.transactionSummary)
        setupContact(summary)
        return bindableTransaction
    }

    internal fun setupContact(transactionsInvitesSummary: TransactionsInvitesSummary) {
        setupIdentity(bindableTransaction.basicDirection, transactionsInvitesSummary.toUser, transactionsInvitesSummary.fromUser)
    }

    internal fun translateTransaction(transaction: TransactionSummary?) {
        transaction?.let {
            setSendState(transaction)
            translateMempoolState(transaction)
            translateTransactionDate(transaction.txTime)
            translateConfirmations(transaction.numConfirmations)
            bindableTransaction.txID = transaction.txid
            bindableTransaction.fee = transaction.fee
            bindableTransaction.historicalTransactionUSDValue = transaction.historicPrice
            setupTransactionNotification(bindableTransaction.basicDirection, transaction.transactionNotification)
        }
    }

    internal fun setSendState(transaction: TransactionSummary) {
        var isSend = false
        var isReceive = false
        for (fundingStat in transaction.funder) {
            if (fundingStat.address != null) {
                isSend = true
            }
        }

        var address: Address?
        for (targetStat in transaction.receiver) {
            address = targetStat.address
            if (address != null && address.changeIndex != HDWallet.INTERNAL) {
                isReceive = true
            }
        }

        when {
            transaction.isLightningWithdraw -> bindableTransaction.sendState = SendState.UNLOAD_LIGHTNING
            isSend && isReceive -> bindableTransaction.sendState = SendState.TRANSFER
            isSend -> bindableTransaction.sendState = SendState.SEND
            else -> bindableTransaction.sendState = SendState.RECEIVE
        }
    }

    internal fun translateMempoolState(transaction: TransactionSummary) {
        when (transaction.memPoolState) {
            MemPoolState.DOUBLE_SPEND, MemPoolState.ORPHANED, MemPoolState.FAILED_TO_BROADCAST -> {
                handleEdgeMempoolCondition(transaction)
            }
            MemPoolState.INIT -> return
            MemPoolState.PENDING, MemPoolState.ACKNOWLEDGE, MemPoolState.MINED -> buildTransactionDetails(transaction)
            else -> buildTransactionDetails(transaction)
        }
    }

    internal fun handleEdgeMempoolCondition(transaction: TransactionSummary) {
        translateTargets(transaction)
        if (bindableTransaction.sendState === SendState.RECEIVE) {
            bindableTransaction.sendState = SendState.FAILED_TO_BROADCAST_RECEIVE
        } else if (bindableTransaction.sendState === SendState.TRANSFER ||
                bindableTransaction.sendState === SendState.FAILED_TO_BROADCAST_TRANSFER) {
            bindableTransaction.sendState = SendState.FAILED_TO_BROADCAST_TRANSFER
        } else {
            bindableTransaction.sendState = SendState.FAILED_TO_BROADCAST_SEND
        }
    }


    internal fun buildTransactionDetails(transaction: TransactionSummary) {
        when (bindableTransaction.sendState) {
            SendState.TRANSFER -> {
                translateTargets(transaction)
            }
            SendState.UNLOAD_LIGHTNING, SendState.RECEIVE_CANCELED, SendState.RECEIVE -> translateReceive(transaction)
            SendState.LOAD_LIGHTNING, SendState.SEND_CANCELED, SendState.SEND -> translateSend(transaction)
            else -> {
            }
        }
        calculateTransactionValue(transaction)
    }

    internal fun calculateTransactionValue(transaction: TransactionSummary) {
        var value = 0L

        for (stat in transaction.receiver) {
            if ((bindableTransaction.sendState === SendState.SEND || bindableTransaction.sendState === SendState.LOAD_LIGHTNING)
                    && stat.address == null) {
                value += stat.value
            } else if ((bindableTransaction.sendState === SendState.RECEIVE || bindableTransaction.sendState === SendState.UNLOAD_LIGHTNING) && stat.address != null) {
                value += stat.value
            }
        }

        bindableTransaction.value = value

        if (transaction.memPoolState == MemPoolState.FAILED_TO_BROADCAST && value == 0L) {
            bindableTransaction.value = transaction.fee
            bindableTransaction.sendState = SendState.FAILED_TO_BROADCAST_TRANSFER
        }
    }

    internal fun translateReceive(transaction: TransactionSummary) {
        for (stat in transaction.receiver) {
            if (stat.address != null) {
                if (bindableTransaction.targetAddress?.isEmpty() != false) {
                    bindableTransaction.targetAddress = stat.addr
                }
            }
        }
        for (funder in transaction.funder) {
            if (funder.address == null) {
                bindableTransaction.fundingAddress = funder.addr
                break
            }
        }

        if (bindableTransaction.sendState !== SendState.RECEIVE_CANCELED
                && bindableTransaction.sendState !== SendState.UNLOAD_LIGHTNING
                && bindableTransaction.sendState !== SendState.SEND_CANCELED)
            bindableTransaction.sendState = SendState.RECEIVE
    }

    internal fun translateTargets(transaction: TransactionSummary) {

        if (transaction.receiver != null && transaction.receiver.isNotEmpty())
            bindableTransaction.targetAddress = transaction.receiver[0].addr


        if (transaction.funder != null && transaction.funder.isNotEmpty())
            bindableTransaction.fundingAddress = transaction.funder[0].addr

    }

    internal fun translateSend(transaction: TransactionSummary) {
        for (stat in transaction.receiver) {
            if (stat.address == null) {
                bindableTransaction.targetAddress = stat.addr
                break
            }
        }

        for (funder in transaction.funder) {
            if (funder.address != null) {
                bindableTransaction.fundingAddress = funder.addr
                break
            }
        }

        if (bindableTransaction.targetAddress == lightningDepositAddress)
            bindableTransaction.sendState = SendState.LOAD_LIGHTNING

        if (bindableTransaction.sendState !== SendState.RECEIVE_CANCELED
                && bindableTransaction.sendState !== SendState.SEND_CANCELED
                && bindableTransaction.sendState !== SendState.LOAD_LIGHTNING)
            bindableTransaction.sendState = SendState.SEND
    }

    internal fun translateConfirmations(confirmations: Int) {
        bindableTransaction.confirmationCount = confirmations
        when (confirmations) {
            0 -> bindableTransaction.confirmationState = BindableTransaction.ConfirmationState.UNCONFIRMED
            else -> bindableTransaction.confirmationState = BindableTransaction.ConfirmationState.CONFIRMED
        }
    }

    internal fun translateTransactionDate(txTime: Long?) {
        txTime?.let {
            if (txTime == 0L) {
                bindableTransaction.txTime = ""
            } else {
                bindableTransaction.txTime = dateFormatter.formatTime(txTime)
            }
        }
    }

    internal fun translateInvite(invite: InviteTransactionSummary?) {
        if (null == invite) return
        bindableTransaction.value = invite.valueSatoshis
        bindableTransaction.targetAddress = invite.address ?: ""
        bindableTransaction.historicalInviteUSDValue = invite.historicValue
        bindableTransaction.serverInviteId = invite.serverId
        translateTransactionDate(invite.sentDate)
        translateInviteState(invite)
        translateInviteIdentity(invite)
        translateInviteFee(invite)
        translateInviteType(invite)
        setupIdentity(bindableTransaction.basicDirection, invite.toUser, invite.fromUser)
        setupTransactionNotification(bindableTransaction.basicDirection, invite.transactionNotification)
    }

    internal fun setupTransactionNotification(direction: SendState, transactionNotification: TransactionNotification?) {
        transactionNotification?.let { notification ->
            bindableTransaction.memo = notification.memo
            bindableTransaction.isSharedMemo = notification.isShared

            setupIdentity(direction, notification.toUser, notification.fromUser)
        }
    }

    private fun setupIdentity(direction: SendState, toUser: UserIdentity?, fromUser: UserIdentity?): Unit? {
        return when (direction) {
            SendState.RECEIVE -> {
                fromUser?.let { user ->
                    bindableTransaction.identity = user.localeFriendlyDisplayIdentityText
                    bindableTransaction.identityType = user.type
                    bindableTransaction.profileUrl = user.avatar
                }
            }
            SendState.SEND -> {
                toUser?.let { user ->
                    bindableTransaction.identity = user.localeFriendlyDisplayIdentityText
                    bindableTransaction.identityType = user.type
                    bindableTransaction.profileUrl = user.avatar
                }
            }
            else -> {
            }
        }
    }

    internal fun translateInviteIdentity(invite: InviteTransactionSummary) {
        if (invite.type == Type.SENT) {
            bindableTransaction.identity = invite.localeFriendlyDisplayIdentityForReceiver
        } else {
            bindableTransaction.identity = invite.localeFriendlyDisplayIdentityForSender
        }
    }

    internal fun translateInviteFee(invite: InviteTransactionSummary) {
        var feeDisplay = invite.valueFeesSatoshis
        if (feeDisplay == null || feeDisplay < 1) {
            feeDisplay = 0L
        }
        bindableTransaction.fee = feeDisplay
    }

    internal fun translateInviteType(invite: InviteTransactionSummary) {
        when (invite.type) {
            Type.SENT -> {
                bindableTransaction.sendState = SendState.SEND
            }
            Type.RECEIVED -> {
                bindableTransaction.sendState = SendState.RECEIVE
            }
            else -> {

            }
        }

        if (bindableTransaction.inviteState === InviteState.EXPIRED || bindableTransaction.inviteState === InviteState.CANCELED) {
            if (invite.type == Type.SENT)
                bindableTransaction.sendState = SendState.SEND_CANCELED
            else
                bindableTransaction.sendState = SendState.RECEIVE_CANCELED
        }
    }

    internal fun translateInviteState(invite: InviteTransactionSummary) {
        when (invite.btcState) {
            BTCState.EXPIRED -> bindableTransaction.inviteState = InviteState.EXPIRED
            BTCState.CANCELED -> bindableTransaction.inviteState = InviteState.CANCELED
            else -> translatePendingInviteStateFromType(invite)
        }
    }

    internal fun translatePendingInviteStateFromType(invite: InviteTransactionSummary) {
        val address = invite.address
        if (invite.type == Type.SENT) translateSendInviteState(address)
        else if (invite.type == Type.RECEIVED) translateReceiveInviteState(address)
    }

    internal fun translateReceiveInviteState(address: String?) {
        if (null == address || address.isEmpty())
            bindableTransaction.inviteState = InviteState.RECEIVED_PENDING
        else
            bindableTransaction.inviteState = InviteState.RECEIVED_ADDRESS_PROVIDED
    }

    internal fun translateSendInviteState(address: String?) {
        if (null == address || address.isEmpty())
            bindableTransaction.inviteState = InviteState.SENT_PENDING
        else
            bindableTransaction.inviteState = InviteState.SENT_ADDRESS_PROVIDED
    }
}
