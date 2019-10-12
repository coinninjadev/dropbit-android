package com.coinninja.coinkeeper.service

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.TransactionData
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.service.runner.SaveTransactionRunner
import com.coinninja.coinkeeper.util.DropbitIntents
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class BroadcastTransactionServiceTest {

    private fun createDTO(): CompletedBroadcastDTO {
        val broadcastActivityDTO = BroadcastTransactionDTO(
                mock(TransactionData::class.java),
                false, "--memo--",
                Identity(IdentityType.PHONE, "+13305553333", "--hash--", "Joe Blow"),
                null)
        return CompletedBroadcastDTO(broadcastActivityDTO, "--txid--")
    }

    @Test
    fun saves_transaction_locally() {
        val service = Robolectric.setupService(BroadcastTransactionService::class.java)
        service.runner = mock(SaveTransactionRunner::class.java)
        val intent = Intent()
        val completedBroadcastDTO = createDTO()
        intent.putExtra(DropbitIntents.EXTRA_COMPLETED_BROADCAST_DTO, completedBroadcastDTO)

        service.onHandleIntent(intent)

        val ordered = inOrder(service.runner)
        ordered.verify(service.runner).setCompletedBroadcastActivityDTO(completedBroadcastDTO)
        ordered.verify(service.runner).run()
    }
}

