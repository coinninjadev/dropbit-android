package com.coinninja.coinkeeper.model.helpers

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.bitcoin.BroadcastResult
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.service.client.model.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class InviteTransactionSummaryHelperTest {

    private fun createHelper(): InviteTransactionSummaryHelper {
        val helper = InviteTransactionSummaryHelper(mock(), mock(), mock(), mock(), mock(),
                mock(), mock(), mock()
        )

        whenever(helper.walletHelper.primaryWallet).thenReturn(mock())

        return helper
    }

    @Test
    fun cancelsByCnId() {
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId("--cn-id--")).thenReturn(invite)

        helper.cancelInviteByCnId("--cn-id--")

        verify(invite).btcState = BTCState.CANCELED
        verify(invite).btcTransactionId = ""
        verify(invite).update()
    }

    @Test
    fun cancels_invite() {
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()

        helper.cancelInvite(invite)

        val ordered = inOrder(invite)
        ordered.verify(invite).btcState = BTCState.CANCELED
        ordered.verify(invite).btcTransactionId = ""
        ordered.verify(invite).update()
    }

    @Test
    fun cancels_al_pending_invites() {
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        val invite2: InviteTransactionSummary = mock()
        val pendingInvites = listOf(invite, invite2)
        whenever(helper.inviteSummaryQueryManager.unfulfilledSentInvites).thenReturn(pendingInvites)

        helper.cancelPendingSentInvites()

        verify(invite).btcState = BTCState.CANCELED
        verify(invite).update()
        verify(invite2).btcState = BTCState.CANCELED
        verify(invite2).update()
    }

    @Test
    fun acknowledging_from_invite_DTO_invite_creates_join_reference_and_copies_some_values() {
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        whenever(invite.toUser).thenReturn(mock())
        whenever(invite.fromUser).thenReturn(mock())
        whenever(invite.type).thenReturn(Type.BLOCKCHAIN_SENT)
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId("--request-id--")).thenReturn(invite)

        val createdAt = System.currentTimeMillis()
        whenever(helper.dateUtil.getCurrentTimeInMillis()).thenReturn(createdAt)
        val completedInviteDTO = CompletedInviteDTO(mock(),
                requestId = "--request-id--",
                invitedContact = InvitedContact("--server-id--", 0, 0,
                        null, null, "new", "1")
        )

        helper.acknowledgeInviteTransactionSummary(completedInviteDTO)

        verify(invite).sentDate = createdAt
        verify(invite).btcState = BTCState.UNFULFILLED
        verify(invite).update()
        verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(invite)

        val ordered = inOrder(invite, helper.transactionInviteSummaryHelper)
        ordered.verify(invite).serverId = "--server-id--"
        ordered.verify(invite).sentDate = createdAt
        ordered.verify(invite).btcState = BTCState.UNFULFILLED
        ordered.verify(invite).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(invite)
    }

    @Test
    fun acknowledging_observed_invite_creates_join_reference_and_copies_some_values() {
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        whenever(invite.toUser).thenReturn(mock())
        whenever(invite.fromUser).thenReturn(mock())
        whenever(invite.type).thenReturn(Type.BLOCKCHAIN_SENT)
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId("--request-id--")).thenReturn(invite)

        val createdAt = System.currentTimeMillis()
        whenever(helper.dateUtil.getCurrentTimeInMillis()).thenReturn(createdAt)
        val sentInvite = SentInvite().apply {
            id = "--server-id--"
            metadata = InviteMetadata().apply {
                request_id = "--request-id--"

            }
        }

        helper.acknowledgeInviteTransactionSummary(sentInvite)

        verify(invite).sentDate = createdAt
        verify(invite).btcState = BTCState.UNFULFILLED
        verify(invite).update()
        verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(invite)

        val ordered = inOrder(invite, helper.transactionInviteSummaryHelper)
        ordered.verify(invite).serverId = sentInvite.id
        ordered.verify(invite).sentDate = createdAt
        ordered.verify(invite).btcState = BTCState.UNFULFILLED
        ordered.verify(invite).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(invite)
    }

    @Test
    fun acknowledging_observed_invite_does_not_create_join_for_LIGHTNING_sends() {
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        whenever(invite.toUser).thenReturn(mock())
        whenever(invite.fromUser).thenReturn(mock())
        whenever(invite.type).thenReturn(Type.LIGHTNING_SENT)
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId("--request-id--")).thenReturn(invite)

        val createdAt = System.currentTimeMillis()
        whenever(helper.dateUtil.getCurrentTimeInMillis()).thenReturn(createdAt)
        val sentInvite = SentInvite().apply {
            id = "--server-id--"
            metadata = InviteMetadata().apply {
                request_id = "--request-id--"

            }
        }

        helper.acknowledgeInviteTransactionSummary(sentInvite)

        verify(invite).sentDate = createdAt
        verify(invite).btcState = BTCState.UNFULFILLED
        verify(invite).update()
        verify(helper.transactionInviteSummaryHelper, times(0)).getOrCreateParentSettlementFor(invite)

        val ordered = inOrder(invite)
        ordered.verify(invite).serverId = sentInvite.id
        ordered.verify(invite).sentDate = createdAt
        ordered.verify(invite).btcState = BTCState.UNFULFILLED
        ordered.verify(invite).update()
    }

    @Test
    fun creates_new_temp_invite_whenever_server_id_absent_from_records() {
        val helper = createHelper()
        val invite = mock<InviteTransactionSummary>()
        val transactionJoin = mock<TransactionsInvitesSummary>()
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
    fun returns_existing_invite_join_record_whenever_server_id_exists_in_records() {
        val invite = mock<InviteTransactionSummary>()
        val transactionJoin = mock<TransactionsInvitesSummary>()
        val helper = createHelper()
        val requestId = "--request-id--"

        whenever(invite.transactionsInvitesSummary).thenReturn(transactionJoin)
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId(requestId)).thenReturn(invite)

        assertThat(helper.getOrCreateInviteSummaryWithServerId(requestId), equalTo(transactionJoin))
    }

    @Test
    fun saves_temporary_sent_invite__blockchain() {
        val invite = mock<InviteTransactionSummary>()
        val helper = createHelper()
        val pendingInviteDTO = createPendingToPhoneInviteDTO()
        val phoneIdentity = mock<DropbitMeIdentity>()
        val toUser = mock<UserIdentity>()
        val fromUser = mock<UserIdentity>()
        whenever(helper.dropbitAccountHelper.identityForType(pendingInviteDTO.identity.identityType)).thenReturn(phoneIdentity)
        whenever(helper.userIdentityHelper.updateFrom(pendingInviteDTO.identity)).thenReturn(toUser)
        whenever(helper.userIdentityHelper.updateFrom(phoneIdentity)).thenReturn(fromUser)

        whenever(helper.daoSessionManager.newInviteTransactionSummary()).thenReturn(invite)

        val createdInvite = helper.saveTemporaryInvite(pendingInviteDTO)

        verify(invite).historicValue = 340_00L
        verify(invite).toUser = toUser
        verify(invite).fromUser = fromUser
        verify(invite).btcState = BTCState.UNACKNOWLEDGED
        verify(invite).valueSatoshis = pendingInviteDTO.inviteAmount
        verify(invite).valueFeesSatoshis = pendingInviteDTO.inviteFee
        verify(invite).wallet = helper.walletHelper.primaryWallet
        verify(invite).type = Type.BLOCKCHAIN_SENT
        verify(invite).serverId = "--request-id--"
        verify(invite).update()

        assertThat(createdInvite, equalTo(invite))
    }

    @Test
    fun saves_temporary_sent_invite__lightning() {
        val invite = mock<InviteTransactionSummary>()
        val helper = createHelper()
        val paymentHolder = PaymentHolder(evaluationCurrency = USDCurrency(10_000))
        paymentHolder.toUser = Identity(IdentityType.PHONE, "+13305551111")
        paymentHolder.updateValue(BTCCurrency(100_000))
        paymentHolder.requestId = "--request-id--"
        paymentHolder.accountMode = AccountMode.LIGHTNING

        val phoneIdentity = mock<DropbitMeIdentity>()
        val toUser = mock<UserIdentity>()
        val fromUser = mock<UserIdentity>()
        whenever(helper.dropbitAccountHelper.identityForType(paymentHolder.toUser!!.identityType)).thenReturn(phoneIdentity)
        whenever(helper.userIdentityHelper.updateFrom(paymentHolder.toUser!!)).thenReturn(toUser)
        whenever(helper.userIdentityHelper.updateFrom(phoneIdentity)).thenReturn(fromUser)

        whenever(helper.daoSessionManager.newInviteTransactionSummary()).thenReturn(invite)

        val createdInvite = helper.saveTemporaryInvite(paymentHolder)

        verify(invite).historicValue = paymentHolder.fiat.toLong()
        verify(invite).toUser = toUser
        verify(invite).fromUser = fromUser
        verify(invite).btcState = BTCState.UNACKNOWLEDGED
        verify(invite).valueSatoshis = paymentHolder.crypto.toLong()
        verify(invite).valueFeesSatoshis = paymentHolder.transactionData.feeAmount
        verify(invite).wallet = helper.walletHelper.primaryWallet
        verify(invite).type = Type.LIGHTNING_SENT
        verify(invite).serverId = "--request-id--"
        verify(invite).update()

        assertThat(createdInvite, equalTo(invite))
    }

    @Test
    fun updates_sent_invite_whenever_transaction_is_fulfilled() {
        val txid = "--txid--"
        val helper = createHelper()
        val invite = mock<InviteTransactionSummary>()
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        val transactionBroadcastResult = mock<BroadcastResult>()
        whenever(transactionBroadcastResult.txid).thenReturn(txid)
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)
        whenever(invite.type).thenReturn(Type.BLOCKCHAIN_SENT)
        val orderedOperations = inOrder(invite, helper.transactionInviteSummaryHelper)

        helper.updateFulfilledInvite(invite, txid)

        orderedOperations.verify(invite).btcTransactionId = txid
        orderedOperations.verify(invite).btcState = BTCState.FULFILLED
        orderedOperations.verify(invite).update()
        orderedOperations.verify(helper.transactionInviteSummaryHelper).populateWith(transactionsInvitesSummary, invite)
    }

    @Test
    fun updates_received_invites_as_fulfilled() {
        val cnId = "--cn-id--"
        val txid = "--txid--"
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        whenever(helper.getInviteSummaryByCnId(cnId)).thenReturn(invite)
        whenever(invite.type).thenReturn(Type.BLOCKCHAIN_SENT)
        val settlement: TransactionsInvitesSummary = mock()
        whenever(invite.transactionsInvitesSummary).thenReturn(settlement)

        helper.updateFulfilledInviteByCnId(cnId, txid)

        val orderedOperations = inOrder(invite, helper.transactionInviteSummaryHelper)
        orderedOperations.verify(invite).btcTransactionId = txid
        orderedOperations.verify(invite).btcState = BTCState.FULFILLED
        orderedOperations.verify(invite).update()
        orderedOperations.verify(helper.transactionInviteSummaryHelper).populateWith(settlement, invite)
    }

    @Test
    fun cancels_pending_sent_invites() {
        val helper = createHelper()
        val invite1: InviteTransactionSummary = mock()
        val invite2: InviteTransactionSummary = mock()
        whenever(helper.unfulfilledSentInvites).thenReturn(listOf(invite1, invite2))

        helper.cancelPendingSentInvites()

        verify(invite1).btcState = BTCState.CANCELED
        verify(invite1).update()

        verify(invite2).btcState = BTCState.CANCELED
        verify(invite2).update()
    }

    @Test
    fun saves_received_invite__creates_settlement_for_blockchain() {
        val helper = createHelper()
        val createdAt = System.currentTimeMillis() / 1000
        val receivedInvite = ReceivedInvite(
                id = "--server-id--",
                created_at = createdAt,
                updated_at = createdAt,
                address = "--address--",
                request_ttl = "--ttl--",
                sender = "--sender--",
                status = "completed",
                txid = "--txid--",
                address_type = "btc",
                metadata = InviteMetadata(
                        MetadataAmount(26236L, 100L),
                        MetadataContact("phone", "--sender-phone--"),
                        MetadataContact("phone", "--receiver-phone--")
                )
        )

        val invite: InviteTransactionSummary = mock()
        val wallet: Wallet = mock()
        whenever(helper.walletHelper.primaryWallet).thenReturn(wallet)
        whenever(helper.inviteSummaryQueryManager.getOrCreate("--server-id--")).thenReturn(invite)
        val fromUser: UserIdentity = mock()
        val toUser: UserIdentity = mock()
        whenever(helper.userIdentityHelper.updateFrom(receivedInvite.metadata.sender)).thenReturn(fromUser)
        whenever(helper.userIdentityHelper.updateFrom(receivedInvite.metadata.receiver)).thenReturn(toUser)

        helper.saveReceivedInviteTransaction(receivedInvite)

        verify(invite).historicValue = 100L
        verify(invite).valueSatoshis = 26236L
        verify(invite).valueFeesSatoshis = 0L
        verify(invite).btcState = BTCState.FULFILLED
        verify(invite).type = Type.BLOCKCHAIN_RECEIVED
        verify(invite).wallet = wallet
        verify(invite).btcTransactionId = "--txid--"
        verify(invite).address = "--address--"
        verify(invite).sentDate = createdAt * 1000
        verify(invite).toUser = toUser
        verify(invite).fromUser = fromUser
        verify(invite).update()

        verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(invite)
    }

    @Test
    fun saves_received_invite__does_not_create_parent_settlement_when_lightning() {
        val helper = createHelper()
        val createdAt = System.currentTimeMillis() / 1000
        val receivedInvite = ReceivedInvite(
                id = "--server-id--",
                created_at = createdAt,
                updated_at = createdAt,
                address = "--address--",
                request_ttl = "--ttl--",
                sender = "--sender--",
                status = "completed",
                txid = "--txid--",
                address_type = "lightning",
                metadata = InviteMetadata(
                        MetadataAmount(26236L, 100L),
                        MetadataContact("phone", "--sender-phone--"),
                        MetadataContact("phone", "--receiver-phone--")
                )
        )

        val invite: InviteTransactionSummary = mock()
        val wallet: Wallet = mock()
        whenever(helper.walletHelper.primaryWallet).thenReturn(wallet)
        whenever(helper.inviteSummaryQueryManager.getOrCreate("--server-id--")).thenReturn(invite)
        val fromUser: UserIdentity = mock()
        val toUser: UserIdentity = mock()
        whenever(helper.userIdentityHelper.updateFrom(receivedInvite.metadata.sender)).thenReturn(fromUser)
        whenever(helper.userIdentityHelper.updateFrom(receivedInvite.metadata.receiver)).thenReturn(toUser)

        helper.saveReceivedInviteTransaction(receivedInvite)

        verify(invite).historicValue = 100L
        verify(invite).valueSatoshis = 26236L
        verify(invite).valueFeesSatoshis = 0L
        verify(invite).btcState = BTCState.FULFILLED
        verify(invite).type = Type.LIGHTNING_RECEIVED
        verify(invite).wallet = wallet
        verify(invite).btcTransactionId = "--txid--"
        verify(invite).address = "--address--"
        verify(invite).sentDate = createdAt * 1000
        verify(invite).toUser = toUser
        verify(invite).fromUser = fromUser
        verify(invite).update()

        verify(helper.transactionInviteSummaryHelper, times(0)).getOrCreateParentSettlementFor(invite)
    }

    @Test
    fun updates_invite_with_address() {
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByCnId("--server-id--")).thenReturn(invite)

        helper.updateInviteAddressTransaction("--server-id--", "--address--")

        verify(invite).address = "--address--"
        verify(invite).update()
    }

    @Test
    fun updates_with_sent_invite_state() {
        val helper = createHelper()
        val sentInvite = SentInvite().apply {
            id = "--cn-id--"
            addressPubKey = "--pub-key--"
            address = "--address--"
        }
        val invite: InviteTransactionSummary = mock()
        whenever(helper.getInviteSummaryByCnId("--cn-id--")).thenReturn(invite)
        whenever(invite.type).thenReturn(Type.BLOCKCHAIN_SENT)

        helper.updateInviteAddressTransaction(sentInvite)

        verify(invite).address = "--address--"
        verify(invite).pubkey = "--pub-key--"
        verify(invite).update()
        verify(helper.transactionInviteSummaryHelper).updateSentTimeFrom(invite)
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