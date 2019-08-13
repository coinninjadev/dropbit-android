package com.coinninja.coinkeeper.cn.dropbit

import com.coinninja.coinkeeper.service.client.model.SentInvite
import com.coinninja.failedHttpResponse
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Response


class DropbitTwitterInviteTweetSuppressionCheckTest {

    @Test
    fun negative_http_response_from_server_instructs_manual_mention() = runBlocking {
        val inviteId = "--invite-id--"
        val callback: ShouldManuallyMentionCallback = mock()
        val suppressionCheck = DropbitTwitterInviteTweetSuppressionCheck(mock())
        whenever(suppressionCheck.signedCoinKeeperApiClient.patchSuppressionForWalletAddressRequest(inviteId))
                .thenReturn(failedHttpResponse())


        suppressionCheck.checkShouldManuallyMention(inviteId, callback)

        verify(callback).onManualShouldManuallyMention(true)
    }

    @Test
    fun duplicate_response_instructs_not_to_mention_manually() = runBlocking {
        val inviteId = "--invite-id--"
        val callback: ShouldManuallyMentionCallback = mock()
        val suppressionCheck = DropbitTwitterInviteTweetSuppressionCheck(mock())
        whenever(suppressionCheck.signedCoinKeeperApiClient.patchSuppressionForWalletAddressRequest(inviteId))
                .thenReturn(Response.success(SentInvite(id = inviteId, delivery_id = "duplicate")))


        suppressionCheck.checkShouldManuallyMention(inviteId, callback)

        verify(callback).onManualShouldManuallyMention(false)
    }

    @Test
    fun non_duplicate_response_instructs_to_mention_manually() = runBlocking {
        val inviteId = "--invite-id--"
        val callback: ShouldManuallyMentionCallback = mock()
        val suppressionCheck = DropbitTwitterInviteTweetSuppressionCheck(mock())
        whenever(suppressionCheck.signedCoinKeeperApiClient.patchSuppressionForWalletAddressRequest(inviteId))
                .thenReturn(Response.success(SentInvite(id = inviteId)))


        suppressionCheck.checkShouldManuallyMention(inviteId, callback)

        verify(callback).onManualShouldManuallyMention(true)
    }
}