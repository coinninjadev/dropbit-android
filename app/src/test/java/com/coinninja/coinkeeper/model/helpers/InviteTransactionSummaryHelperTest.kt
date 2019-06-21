package com.coinninja.coinkeeper.model.helpers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.bindings.TransactionBroadcastResult
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.model.query.InviteSummaryQueryManager
import com.coinninja.coinkeeper.service.client.model.InviteMetadata
import com.coinninja.coinkeeper.service.client.model.SentInvite
import com.coinninja.coinkeeper.util.DateUtil
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class InviteTransactionSummaryHelperTest {

    private fun createInviteTransactionSumamaryHelper(): InviteTransactionSummaryHelper {
        val helper = InviteTransactionSummaryHelper(
                mock(InviteSummaryQueryManager::class.java),
                mock(DaoSessionManager::class.java),
                mock(TransactionHelper::class.java),
                mock(DropbitAccountHelper::class.java),
                mock(UserIdentityHelper::class.java),
                mock(WalletHelper::class.java),
                mock(DateUtil::class.java)
        )

        whenever(helper.walletHelper.wallet).thenReturn(mock(Wallet::class.java))

        return helper
    }

    @Test
    fun `acknowledging invite creates join reference and copies some values`() {
        val helper = createInviteTransactionSumamaryHelper()
        val sentInvite = mock(SentInvite::class.java)
        val metaData = InviteMetadata()
        metaData.request_id = "--request-id--"
        whenever(sentInvite.metadata).thenReturn(metaData)
        whenever(sentInvite.created_at).thenReturn(1000000)
        whenever(sentInvite.id).thenReturn("--server-id--")
        whenever(sentInvite.status).thenReturn("new")
        val invite = mock(InviteTransactionSummary::class.java)
        val toUser = mock(UserIdentity::class.java)
        val fromUser = mock(UserIdentity::class.java)
        whenever(invite.toUser).thenReturn(toUser)
        whenever(invite.fromUser).thenReturn(fromUser)
        whenever(invite.btcState).thenReturn(BTCState.UNACKNOWLEDGED)
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId(metaData.request_id)).thenReturn(invite)
        val transactionsInvitesSummary = mock(TransactionsInvitesSummary::class.java)
        whenever(helper.daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary)

        helper.acknowledgeInviteTransactionSummary(sentInvite)

        verify(transactionsInvitesSummary).inviteTime = sentInvite.created_at
        verify(transactionsInvitesSummary).toUser = toUser
        verify(transactionsInvitesSummary).fromUser = fromUser
        verify(helper.daoSessionManager).insert(transactionsInvitesSummary)

        verify(invite).serverId = sentInvite.id
        verify(invite).btcState = BTCState.UNFULFILLED
        verify(invite).sentDate = sentInvite.created_at
        verify(invite).update()
    }

    @Test
    fun creates_new_temp_invite_when_server_id_absent_from_records() {
        val helper = createInviteTransactionSumamaryHelper()
        val invite = mock(InviteTransactionSummary::class.java)
        val transactionJoin = mock(TransactionsInvitesSummary::class.java)
        val orderedOperations = inOrder(helper.daoSessionManager, invite, transactionJoin)
        val requestId = "--request-id--"

        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId(requestId)).thenReturn(null)
        whenever(helper.daoSessionManager.newInviteTransactionSummary()).thenReturn(invite)
        whenever(helper.daoSessionManager.insert(invite)).thenReturn(1L)
        whenever(helper.daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionJoin)

        assertThat(helper.getOrCreateInviteSummaryWithServerId(requestId), equalTo(transactionJoin))
        orderedOperations.verify(invite).serverId = requestId
        orderedOperations.verify(helper.daoSessionManager).insert(invite)
        orderedOperations.verify(helper.daoSessionManager).insert(transactionJoin)
        orderedOperations.verify(invite).update()
    }

    @Test
    fun returns_existing_invite_join_record_when_server_id_exists_in_records() {
        val invite = mock(InviteTransactionSummary::class.java)
        val transactionJoin = mock(TransactionsInvitesSummary::class.java)
        val helper = createInviteTransactionSumamaryHelper()
        val requestId = "--request-id--"

        whenever(invite.transactionsInvitesSummary).thenReturn(transactionJoin)
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId(requestId)).thenReturn(invite)

        assertThat(helper.getOrCreateInviteSummaryWithServerId(requestId), equalTo(transactionJoin))
    }

    @Test
    fun saves_temporary_sent_invite() {
        val invite = mock(InviteTransactionSummary::class.java)
        val helper = createInviteTransactionSumamaryHelper()
        val pendingInviteDTO = createPendingToPhoneInviteDTO()
        val phoneIdentity = mock(DropbitMeIdentity::class.java)
        val toUser = mock(UserIdentity::class.java)
        val fromUser = mock(UserIdentity::class.java)
        whenever(helper.dropbitAccountHelper.identityForType(pendingInviteDTO.identity.identityType)).thenReturn(phoneIdentity)
        whenever(helper.userIdentityHelper.updateFrom(pendingInviteDTO.identity)).thenReturn(toUser)
        whenever(helper.userIdentityHelper.updateFrom(phoneIdentity)).thenReturn(fromUser)

        whenever(helper.daoSessionManager.newInviteTransactionSummary()).thenReturn(invite)

        val createdInvite = helper.saveTemporaryInvite(pendingInviteDTO)

        verify(invite).historicValue = 34000L
        verify(invite).toUser = toUser
        verify(invite).fromUser = fromUser
        verify(invite).btcState = BTCState.UNACKNOWLEDGED
        verify(invite).valueSatoshis = pendingInviteDTO.inviteAmount
        verify(invite).valueFeesSatoshis = pendingInviteDTO.inviteFee
        verify(invite).wallet = helper.walletHelper.wallet
        verify(invite).type = Type.SENT
        verify(invite).update()

        assertThat(createdInvite, equalTo(invite))
    }

    @Test
    fun updates_sent_invite_when_transaction_is_fulfilled() {
        val invite = mock(InviteTransactionSummary::class.java)
        val transactionsInvitesSummary = mock(TransactionsInvitesSummary::class.java)
        val helper = createInviteTransactionSumamaryHelper()
        val currentTimeMillis = System.currentTimeMillis()
        val transactionSummary = mock(TransactionSummary::class.java)
        val txid = "--txid--"
        val transactionBroadcastResult = mock(TransactionBroadcastResult::class.java)
        whenever(transactionBroadcastResult.txId).thenReturn(txid)
        whenever(transactionsInvitesSummary.inviteTransactionSummary).thenReturn(invite)
        whenever(helper.transactionHelper.createInitialTransaction(txid)).thenReturn(transactionSummary)
        whenever(helper.dateUtil.getCurrentTimeInMillis()).thenReturn(currentTimeMillis)
        val orderedOperations = inOrder(transactionsInvitesSummary, invite, transactionSummary)

        helper.updateFulfilledInvite(transactionsInvitesSummary, transactionBroadcastResult)

        orderedOperations.verify(invite).btcTransactionId = txid
        orderedOperations.verify(invite).btcState = BTCState.FULFILLED
        orderedOperations.verify(invite).update()
        orderedOperations.verify(transactionsInvitesSummary).inviteTxID = txid
        orderedOperations.verify(transactionsInvitesSummary).transactionTxID = txid
        orderedOperations.verify(transactionsInvitesSummary).inviteTime = 0L
        orderedOperations.verify(transactionsInvitesSummary).btcTxTime = currentTimeMillis
        orderedOperations.verify(transactionsInvitesSummary).update()
        orderedOperations.verify(transactionsInvitesSummary).transactionSummary = transactionSummary

    }

    private fun createPendingToPhoneInviteDTO(): PendingInviteDTO {
        val identity = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Blow")
        return PendingInviteDTO(identity,
                340000L,
                10000000L,
                100L,
                "--memo--",
                true,
                "--request-id--"
        )

    }
}