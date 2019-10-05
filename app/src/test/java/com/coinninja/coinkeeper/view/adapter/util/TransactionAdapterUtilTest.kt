package com.coinninja.coinkeeper.view.adapter.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.HDWallet
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.*
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times

@RunWith(AndroidJUnit4::class)
class TransactionAdapterUtilTest {
    private fun createUtil(): TransactionAdapterUtil {
        val util = TransactionAdapterUtil(mock(), mock(), "--deposit-address--")
        whenever(util.bindableTransaction.basicDirection).thenReturn(SendState.SEND)
        return util
    }

    private fun createMockTransaction(): TransactionSummary {
        val transaction = mock(TransactionSummary::class.java)
        val fundingStat = mock(FundingStat::class.java)
        val targetStat = mock(TargetStat::class.java)
        val changeStat = mock(TargetStat::class.java)
        whenever(transaction.funder).thenReturn(listOf(fundingStat))
        whenever(transaction.receiver).thenReturn(listOf(targetStat, changeStat))
        whenever(transaction.receiver.get(0).addr).thenReturn("-- to addr 1 --")
        whenever(transaction.receiver.get(0).value).thenReturn(1000L)
        whenever(transaction.receiver.get(1).addr).thenReturn("-- change addr 1 --")
        whenever(transaction.receiver.get(1).value).thenReturn(2000L)
        whenever(transaction.funder.get(0).addr).thenReturn("-- from addr 1 --")
        whenever(transaction.funder.get(0).value).thenReturn(5000)
        whenever(transaction.isLightningWithdraw).thenReturn(false)
        return transaction
    }

    private fun createMockInvite(): InviteTransactionSummary {
        val invite = mock(InviteTransactionSummary::class.java)
        whenever(invite.address).thenReturn("--address--")
        whenever(invite.valueSatoshis).thenReturn(10000)
        whenever(invite.valueFeesSatoshis).thenReturn(100)
        whenever(invite.historicValue).thenReturn(1000)
        whenever(invite.serverId).thenReturn("--server-invite-id--")
        return invite
    }

    private fun mockSentTransaction(): TransactionSummary {
        val transaction = createMockTransaction()
        val address = mock(Address::class.java)
        whenever(address.changeIndex).thenReturn(HDWallet.INTERNAL)
        whenever(transaction.receiver.get(1).address).thenReturn(address)
        whenever(transaction.funder.get(0).address).thenReturn(mock(Address::class.java))
        return transaction
    }

    private fun mockTransferredTransaction(): TransactionSummary {
        val transaction = createMockTransaction()
        whenever(transaction.receiver.get(0).address).thenReturn(mock(Address::class.java))
        val address = mock(Address::class.java)
        whenever(address.changeIndex).thenReturn(HDWallet.INTERNAL)
        whenever(transaction.receiver.get(1).address).thenReturn(address)
        whenever(transaction.funder.get(0).address).thenReturn(mock(Address::class.java))
        return transaction
    }

    private fun mockReceivedTransaction(): TransactionSummary {
        val transaction = createMockTransaction()
        whenever(transaction.receiver.get(0).address).thenReturn(mock(Address::class.java))
        return transaction
    }

    @Test
    fun `resets bindable transaction is translated`() {
        val util = createUtil()

        util.translateTransaction(mock(TransactionsInvitesSummary::class.java))

        verify(util.bindableTransaction).reset()
    }

    @Test
    fun `binds txid`() {
        val util = createUtil()
        val transaction = mockSentTransaction()
        whenever(transaction.txid).thenReturn("--txid--")

        util.translateTransaction(transaction)

        verify(util.bindableTransaction).txID = "--txid--"
    }

    @Test
    fun `binds fee for transaction`() {
        val util = createUtil()
        val transaction = mockSentTransaction()
        whenever(transaction.fee).thenReturn(100L)

        util.translateTransaction(transaction)

        verify(util.bindableTransaction).fee = 100L
    }

    @Test
    fun `binds historical rate for transaction`() {
        val util = createUtil()
        val transaction = mockSentTransaction()
        whenever(transaction.historicPrice).thenReturn(5000L)

        util.translateTransaction(transaction)

        verify(util.bindableTransaction).historicalTransactionUSDValue = 5000L
    }

    // Identity Assignment
    @Test
    fun `sets identity to receiver when transaction is sent`() {
        val util = createUtil()
        whenever(util.bindableTransaction.basicDirection).thenReturn(SendState.SEND)
        val summary = mock(TransactionsInvitesSummary::class.java)
        val toUser = mock(UserIdentity::class.java)
        whenever(toUser.type).thenReturn(IdentityType.PHONE)
        whenever(summary.toUser).thenReturn(toUser)
        whenever(toUser.localeFriendlyDisplayIdentityText).thenReturn("--to user identity")

        util.setupContact(summary)

        verify(util.bindableTransaction).identity = "--to user identity"
    }

    @Test
    fun `sets identity to sender when transaction received`() {
        val util = createUtil()
        whenever(util.bindableTransaction.basicDirection).thenReturn(SendState.RECEIVE)
        val summary = mock(TransactionsInvitesSummary::class.java)
        val fromUser = mock(UserIdentity::class.java)
        whenever(summary.fromUser).thenReturn(fromUser)
        whenever(fromUser.type).thenReturn(IdentityType.PHONE)
        whenever(fromUser.localeFriendlyDisplayIdentityText).thenReturn("--to user identity")

        util.setupContact(summary)

        verify(util.bindableTransaction).identity = "--to user identity"
    }

    @Test
    fun `sets identity type when setting contact -- Send`() {
        val util = createUtil()
        whenever(util.bindableTransaction.basicDirection).thenReturn(SendState.SEND)
        val summary = mock(TransactionsInvitesSummary::class.java)
        val toUser = mock(UserIdentity::class.java)
        whenever(summary.toUser).thenReturn(toUser)

        whenever(toUser.type).thenReturn(IdentityType.PHONE)
        util.setupContact(summary)
        verify(util.bindableTransaction).identityType = IdentityType.PHONE

        whenever(toUser.type).thenReturn(IdentityType.TWITTER)
        util.setupContact(summary)
        verify(util.bindableTransaction).identityType = IdentityType.TWITTER
    }

    @Test
    fun `sets identity type when setting contact -- Receive`() {
        val util = createUtil()
        whenever(util.bindableTransaction.basicDirection).thenReturn(SendState.RECEIVE)
        val summary = mock(TransactionsInvitesSummary::class.java)
        val fromUser = mock(UserIdentity::class.java)
        whenever(summary.fromUser).thenReturn(fromUser)

        whenever(fromUser.type).thenReturn(IdentityType.PHONE)
        util.setupContact(summary)
        verify(util.bindableTransaction).identityType = IdentityType.PHONE

        whenever(fromUser.type).thenReturn(IdentityType.TWITTER)
        util.setupContact(summary)
        verify(util.bindableTransaction).identityType = IdentityType.TWITTER
    }

    // Send State Assignment
    @Test
    fun `infers send state to SEND when one input and one output`() {
        val util = createUtil()
        val transaction = mockSentTransaction()

        util.setSendState(transaction)

        verify(util.bindableTransaction).sendState = SendState.SEND
    }

    @Test
    fun `infers send state to RECEIVE when one input and no outputs`() {
        val util = createUtil()
        val transaction = mockReceivedTransaction()

        util.setSendState(transaction)

        verify(util.bindableTransaction).sendState = SendState.RECEIVE
    }

    @Test
    fun `infers send state to TRANSFER when one input and one output`() {
        val util = createUtil()
        val transaction = mockTransferredTransaction()

        util.setSendState(transaction)

        verify(util.bindableTransaction).sendState = SendState.TRANSFER
    }

    // MempoolStates
    @Test
    fun `send edge conditions`() {
        val util = createUtil()
        val transaction = mockSentTransaction()
        whenever(util.bindableTransaction.sendState).thenReturn(SendState.SEND)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.DOUBLE_SPEND)
        util.translateMempoolState(transaction)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.ORPHANED)
        util.translateMempoolState(transaction)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.FAILED_TO_BROADCAST)
        util.translateMempoolState(transaction)

        verify(util.bindableTransaction, times(3)).sendState = SendState.FAILED_TO_BROADCAST_SEND
    }

    @Test
    fun `transfer edge conditions`() {
        val util = createUtil()
        val transaction = mockTransferredTransaction()
        whenever(util.bindableTransaction.sendState).thenReturn(SendState.TRANSFER)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.DOUBLE_SPEND)
        util.translateMempoolState(transaction)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.ORPHANED)
        util.translateMempoolState(transaction)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.FAILED_TO_BROADCAST)
        util.translateMempoolState(transaction)

        verify(util.bindableTransaction, times(3)).sendState = SendState.FAILED_TO_BROADCAST_TRANSFER
    }

    @Test
    fun receive_edge_conditions() {
        val util = createUtil()
        val transaction = mockReceivedTransaction()
        whenever(util.bindableTransaction.sendState).thenReturn(SendState.RECEIVE)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.DOUBLE_SPEND)
        util.translateMempoolState(transaction)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.ORPHANED)
        util.translateMempoolState(transaction)

        whenever(transaction.memPoolState).thenReturn(MemPoolState.FAILED_TO_BROADCAST)
        util.translateMempoolState(transaction)

        verify(util.bindableTransaction, times(3)).sendState = SendState.FAILED_TO_BROADCAST_RECEIVE
    }

    @Test
    fun builds_Transaction_details_for_LOAD_LIGHTNING() {
        val util = createUtil()
        val transaction = mockSentTransaction()
        whenever(transaction.receiver.get(0).addr).thenReturn("--deposit-address--")
        whenever(util.bindableTransaction.targetAddress).thenReturn("--deposit-address--")

        util.setSendState(transaction)
        util.translateSend(transaction)

        verify(util.bindableTransaction).targetAddress = "--deposit-address--"

        val ordered = inOrder(util.bindableTransaction)
        ordered.verify(util.bindableTransaction).sendState = SendState.SEND
        ordered.verify(util.bindableTransaction).sendState = SendState.LOAD_LIGHTNING
    }

    @Test
    fun builds_Transaction_details_for_WITHDRAW_LIGHTNING() {
        val util = createUtil()
        val transaction = mockReceivedTransaction()
        whenever(transaction.isLightningWithdraw).thenReturn(true)

        util.setSendState(transaction)

        whenever(util.bindableTransaction.sendState).thenReturn(SendState.UNLOAD_LIGHTNING)
        util.translateReceive(transaction)

        verify(util.bindableTransaction).sendState = SendState.UNLOAD_LIGHTNING
        verify(util.bindableTransaction, times(0)).sendState = SendState.RECEIVE
    }

    @Test
    fun builds_Transaction_details_for_LOAD_LIGHTNING_sets_addresses() {
        val util = createUtil()
        val transaction = mockSentTransaction()
        whenever(transaction.receiver.get(0).addr).thenReturn("--deposit-address--")
        whenever(util.bindableTransaction.sendState).thenReturn(SendState.LOAD_LIGHTNING)

        util.translateMempoolState(transaction)

        verify(util.bindableTransaction).targetAddress = "--deposit-address--"
        verify(util.bindableTransaction).fundingAddress = "-- from addr 1 --"
        verify(util.bindableTransaction).value = 1000L
    }

    @Test
    fun `builds Transaction details for WITHDRAW_LIGHTNING sets addresses`() {
        val util = createUtil()
        val transaction = mockReceivedTransaction()
        whenever(util.bindableTransaction.sendState).thenReturn(SendState.UNLOAD_LIGHTNING)

        util.translateMempoolState(transaction)

        verify(util.bindableTransaction).targetAddress = "-- to addr 1 --"
        verify(util.bindableTransaction).fundingAddress = "-- from addr 1 --"
        verify(util.bindableTransaction).value = 1000L
    }

    @Test
    fun `builds Transaction details for RECEIVE`() {
        val util = createUtil()
        val transaction = mockReceivedTransaction()
        whenever(util.bindableTransaction.sendState).thenReturn(SendState.RECEIVE)

        util.translateMempoolState(transaction)

        verify(util.bindableTransaction).targetAddress = "-- to addr 1 --"
        verify(util.bindableTransaction).fundingAddress = "-- from addr 1 --"
        verify(util.bindableTransaction).value = 1000L
    }

    @Test
    fun `builds Transaction details for SEND`() {
        val util = createUtil()
        val transaction = mockSentTransaction()
        whenever(util.bindableTransaction.sendState).thenReturn(SendState.SEND)

        util.translateMempoolState(transaction)

        verify(util.bindableTransaction).targetAddress = "-- to addr 1 --"
        verify(util.bindableTransaction).fundingAddress = "-- from addr 1 --"
        verify(util.bindableTransaction).value = 1000L
    }

    @Test
    fun `builds Transaction details for TRANSFER`() {
        val util = createUtil()
        val transaction = mockTransferredTransaction()
        whenever(util.bindableTransaction.sendState).thenReturn(SendState.TRANSFER)

        util.translateMempoolState(transaction)

        verify(util.bindableTransaction).targetAddress = "-- to addr 1 --"
        verify(util.bindableTransaction).fundingAddress = "-- from addr 1 --"
        verify(util.bindableTransaction).value = 0
    }

    @Test
    fun `sets confirmation state`() {
        val util = createUtil()

        util.translateConfirmations(0)
        verify(util.bindableTransaction).confirmationState = ConfirmationState.UNCONFIRMED
        verify(util.bindableTransaction).confirmationCount = 0

        util.translateConfirmations(1)
        verify(util.bindableTransaction).confirmationState = ConfirmationState.CONFIRMED
        verify(util.bindableTransaction).confirmationCount = 1
    }

    @Test
    fun `formats tx time`() {
        val util = createUtil()

        util.translateTransactionDate(0)
        verify(util.bindableTransaction).txTime = ""

        whenever(util.dateFormatter.formatTime(100000000)).thenReturn("--formatted-date-time")
        util.translateTransactionDate(100000000)
        verify(util.bindableTransaction).txTime = "--formatted-date-time"
    }

    @Test
    fun `sets values from tx notification from transaction for SEND`() {
        val util = createUtil()
        val transactionNotification = mock(TransactionNotification::class.java)
        val toUser = mock(UserIdentity::class.java)
        whenever(toUser.type).thenReturn(IdentityType.UNKNOWN)
        whenever(toUser.localeFriendlyDisplayIdentityText).thenReturn("Some Receivers Name")
        whenever(transactionNotification.toUser).thenReturn(toUser)
        whenever(transactionNotification.memo).thenReturn("for the tacos")
        whenever(transactionNotification.isShared).thenReturn(true)


        util.setupTransactionNotification(SendState.SEND, transactionNotification)

        verify(util.bindableTransaction).memo = "for the tacos"
        verify(util.bindableTransaction).isSharedMemo = true
        verify(util.bindableTransaction).identity = "Some Receivers Name"
    }

    @Test
    fun `sets identity type from tx notification from transaction for SEND`() {
        val util = createUtil()
        val transactionNotification = mock(TransactionNotification::class.java)
        val toUser = mock(UserIdentity::class.java)
        whenever(toUser.localeFriendlyDisplayIdentityText).thenReturn("Some Receivers Name")
        whenever(transactionNotification.toUser).thenReturn(toUser)
        whenever(transactionNotification.memo).thenReturn("for the tacos")
        whenever(transactionNotification.isShared).thenReturn(true)

        whenever(toUser.type).thenReturn(IdentityType.PHONE)
        util.setupTransactionNotification(SendState.SEND, transactionNotification)
        verify(util.bindableTransaction).identityType = IdentityType.PHONE

        whenever(toUser.type).thenReturn(IdentityType.TWITTER)
        util.setupTransactionNotification(SendState.SEND, transactionNotification)
        verify(util.bindableTransaction).identityType = IdentityType.TWITTER
    }

    @Test
    fun `sets values from tx notification from transaction for RECEIVE`() {
        val util = createUtil()
        val transactionNotification = mock(TransactionNotification::class.java)
        val fromUser = mock(UserIdentity::class.java)
        whenever(fromUser.localeFriendlyDisplayIdentityText).thenReturn("Some Senders Name")
        whenever(transactionNotification.fromUser).thenReturn(fromUser)
        whenever(transactionNotification.memo).thenReturn("for the tacos")
        whenever(transactionNotification.isShared).thenReturn(true)

        whenever(fromUser.type).thenReturn(IdentityType.PHONE)
        util.setupTransactionNotification(SendState.RECEIVE, transactionNotification)
        verify(util.bindableTransaction).identityType = IdentityType.PHONE

        whenever(fromUser.type).thenReturn(IdentityType.TWITTER)
        util.setupTransactionNotification(SendState.RECEIVE, transactionNotification)
        verify(util.bindableTransaction).identityType = IdentityType.TWITTER
    }

    @Test
    fun `sets values from tx notification from transaction for TRANSFER`() {
        val util = createUtil()
        val transactionNotification = mock(TransactionNotification::class.java)
        val fromUser = mock(UserIdentity::class.java)
        whenever(transactionNotification.fromUser).thenReturn(fromUser)
        whenever(transactionNotification.memo).thenReturn("for the tacos")
        whenever(transactionNotification.isShared).thenReturn(false)

        util.setupTransactionNotification(SendState.TRANSFER, transactionNotification)

        verify(util.bindableTransaction).memo = "for the tacos"
        verify(util.bindableTransaction).isSharedMemo = false
        verify(util.bindableTransaction, times(0)).identity = any()
    }

    // Invites

    @Test
    fun `binds values from invite`() {
        val util = createUtil()
        val invite = createMockInvite()

        util.translateInvite(invite)

        verify(util.bindableTransaction).value = 10000
        verify(util.bindableTransaction).targetAddress = "--address--"
        verify(util.bindableTransaction).serverInviteId = "--server-invite-id--"
        verify(util.bindableTransaction).historicalInviteUSDValue = 1000
    }

    @Test
    fun `sets display identity for invite`() {
        val util = createUtil()
        val invite = createMockInvite()
        whenever(invite.localeFriendlyDisplayIdentityForReceiver).thenReturn("to user name")
        whenever(invite.localeFriendlyDisplayIdentityForSender).thenReturn("from user name")

        whenever(invite.type).thenReturn(Type.SENT)
        util.translateInviteIdentity(invite)
        verify(util.bindableTransaction).identity = "to user name"

        whenever(invite.type).thenReturn(Type.RECEIVED)
        util.translateInviteIdentity(invite)
        verify(util.bindableTransaction).identity = "from user name"
    }

    @Test
    fun `binds fee from invite`() {
        val util = createUtil()
        val invite = createMockInvite()

        util.translateInviteFee(invite)

        verify(util.bindableTransaction).fee = 100
    }

    @Test
    fun `binds invite state`() {
        val util = createUtil()
        val invite = createMockInvite()

        whenever(invite.btcState).thenReturn(BTCState.EXPIRED)
        util.translateInviteState(invite)
        verify(util.bindableTransaction).inviteState = InviteState.EXPIRED

        whenever(invite.btcState).thenReturn(BTCState.CANCELED)
        util.translateInviteState(invite)
        verify(util.bindableTransaction).inviteState = InviteState.CANCELED

        whenever(invite.btcState).thenReturn(BTCState.UNFULFILLED)
        whenever(invite.type).thenReturn(Type.SENT)
        whenever(invite.address).thenReturn("")
        util.translateInviteState(invite)
        verify(util.bindableTransaction).inviteState = InviteState.SENT_PENDING

        whenever(invite.address).thenReturn("--address--")
        whenever(invite.type).thenReturn(Type.SENT)
        util.translateInviteState(invite)
        verify(util.bindableTransaction).inviteState = InviteState.SENT_ADDRESS_PROVIDED

        whenever(invite.type).thenReturn(Type.RECEIVED)
        whenever(invite.address).thenReturn("")
        util.translateInviteState(invite)
        verify(util.bindableTransaction).inviteState = InviteState.RECEIVED_PENDING

        whenever(invite.address).thenReturn("--address--")
        whenever(invite.type).thenReturn(Type.RECEIVED)
        util.translateInviteState(invite)
        verify(util.bindableTransaction).inviteState = InviteState.RECEIVED_ADDRESS_PROVIDED
    }

    @Test
    fun `binds invite type normal`() {
        val util = createUtil()
        val invite = createMockInvite()

        whenever(invite.btcState).thenReturn(BTCState.FULFILLED)
        whenever(invite.type).thenReturn(Type.RECEIVED)
        util.translateInviteType(invite)
        verify(util.bindableTransaction).sendState = SendState.RECEIVE

        whenever(invite.type).thenReturn(Type.SENT)
        util.translateInviteType(invite)
        verify(util.bindableTransaction).sendState = SendState.SEND
    }

    @Test
    fun `binds invite type expired`() {
        val util = createUtil()
        val invite = createMockInvite()

        whenever(util.bindableTransaction.inviteState).thenReturn(InviteState.EXPIRED)
        whenever(invite.type).thenReturn(Type.RECEIVED)
        util.translateInviteType(invite)
        verify(util.bindableTransaction).sendState = SendState.RECEIVE_CANCELED

        whenever(invite.btcState).thenReturn(BTCState.EXPIRED)
        whenever(invite.type).thenReturn(Type.SENT)
        util.translateInviteType(invite)
        verify(util.bindableTransaction).sendState = SendState.SEND_CANCELED
    }


    @Test
    fun `binds invite type canceled`() {
        val util = createUtil()
        val invite = createMockInvite()

        whenever(util.bindableTransaction.inviteState).thenReturn(InviteState.CANCELED)
        whenever(invite.type).thenReturn(Type.RECEIVED)
        util.translateInviteType(invite)
        verify(util.bindableTransaction).sendState = SendState.RECEIVE_CANCELED

        whenever(invite.btcState).thenReturn(BTCState.CANCELED)
        whenever(invite.type).thenReturn(Type.SENT)
        util.translateInviteType(invite)
        verify(util.bindableTransaction).sendState = SendState.SEND_CANCELED

    }

}