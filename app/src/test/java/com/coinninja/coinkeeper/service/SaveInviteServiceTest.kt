package com.coinninja.coinkeeper.service

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.model.InvitedContact
import com.coinninja.coinkeeper.util.DropbitIntents
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class SaveInviteServiceTest {

    private fun createService(): SaveInviteService {
        val service = Robolectric.setupService(SaveInviteService::class.java)
        service.inviteTransactionSummaryHelper = mock(InviteTransactionSummaryHelper::class.java)
        service.cnWalletManager = mock(CNWalletManager::class.java)
        service.transactionNotificationManager = mock(TransactionNotificationManager::class.java)

        return service
    }

    private fun createCompletedInviteDTO(): CompletedInviteDTO {
        val identity = Identity(IdentityType.PHONE, "+13305551122", "--hash--", "Joe Blow")
        val invitedContact = InvitedContact(
                "--cn-id--",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "",
                "--hash--",
                "",
                ""
        )
        return CompletedInviteDTO(
                identity,
                340000L,
                100000L,
                100L,
                "--memo--",
                true,
                "",
                invitedContact
        )
    }

    @Test
    fun does_nothing_when_intent_does_not_have_completed_dto() {
        val service = createService()

        service.onHandleIntent(Intent())

        verifyZeroInteractions(service.inviteTransactionSummaryHelper)
        verifyZeroInteractions(service.cnWalletManager)
        verifyZeroInteractions(service.transactionNotificationManager)
    }

    @Test
    fun saves_invite_transaction_notification_and_updates_wallet() {
        val service = createService()
        val intent = Intent()
        val completedInviteDTO = createCompletedInviteDTO()
        intent.putExtra(DropbitIntents.EXTRA_COMPLETED_INVITE_DTO, completedInviteDTO)

        val orderedOperations = inOrder(service.inviteTransactionSummaryHelper,
                service.cnWalletManager, service.transactionNotificationManager)

        val inviteTransactionSummary = mock(InviteTransactionSummary::class.java)
        whenever(service.inviteTransactionSummaryHelper.acknowledgeInviteTransactionSummary(completedInviteDTO))
                .thenReturn(inviteTransactionSummary)

        service.onHandleIntent(intent)

        orderedOperations.verify(service.transactionNotificationManager)
                .saveTransactionNotificationLocally(inviteTransactionSummary, completedInviteDTO)
        orderedOperations.verify(service.cnWalletManager).updateBalances()
    }
}