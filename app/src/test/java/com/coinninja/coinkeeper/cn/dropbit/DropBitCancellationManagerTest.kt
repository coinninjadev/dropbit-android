package com.coinninja.coinkeeper.cn.dropbit

import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response
import java.util.*

class DropBitCancellationManagerTest {
    private fun createManager(): DropBitCancellationManager = DropBitCancellationManager(mock(), mock(), mock())

    @Test
    fun markUnfulfilledAsCanceled() {
        val service = createManager()
        val sampleInvitesList = ArrayList<InviteTransactionSummary>()
        val tempInvite1: InviteTransactionSummary = mock()
        val tempInvite2: InviteTransactionSummary = mock()
        whenever(tempInvite1.serverId).thenReturn("some invite id 1")
        whenever(tempInvite2.serverId).thenReturn("some invite id 2")

        sampleInvitesList.add(tempInvite1)
        sampleInvitesList.add(tempInvite2)

        whenever(service.inviteTransactionSummaryHelper.unfulfilledSentInvites).thenReturn(sampleInvitesList)

        val response = getResponse(sentInviteJSON)

        whenever(service.client.updateInviteStatusCanceled(any())).thenReturn(response)

        service.markUnfulfilledAsCanceled()

        verify(service.inviteTransactionSummaryHelper).cancelInviteByCnId("some invite id 1")
        verify(service.inviteTransactionSummaryHelper).cancelInviteByCnId("some invite id 2")
        verify(service.cnWalletManager, atLeast(1)).updateBalances()
    }

    @Test
    fun markAsCanceled() {
        val service = createManager()
        val sampleInvitesList = ArrayList<InviteTransactionSummary>()
        val tempInvite1: InviteTransactionSummary = mock()
        val tempInvite2: InviteTransactionSummary = mock()
        whenever(tempInvite1.serverId).thenReturn("some invite id 1")
        whenever(tempInvite2.serverId).thenReturn("some invite id 2")

        sampleInvitesList.add(tempInvite1)
        sampleInvitesList.add(tempInvite2)
        val response = getResponse(sentInviteJSON)

        whenever(service.client.updateInviteStatusCanceled(any())).thenReturn(response)

        service.markAsCanceled(sampleInvitesList)

        verify(service.inviteTransactionSummaryHelper).cancelInviteByCnId("some invite id 1")
        verify(service.inviteTransactionSummaryHelper).cancelInviteByCnId("some invite id 2")

    }

    @Test
    fun markAsCanceled_server_error() {
        val service = createManager()
        val sampleInvitesList = ArrayList<InviteTransactionSummary>()
        val tempInvite1: InviteTransactionSummary = mock()
        val tempInvite2: InviteTransactionSummary = mock()
        whenever(tempInvite1.serverId).thenReturn("some invite id 1")
        whenever(tempInvite2.serverId).thenReturn("some invite id 2")

        sampleInvitesList.add(tempInvite1)
        sampleInvitesList.add(tempInvite2)
        val badResponse = badResponse

        whenever(service.client.updateInviteStatusCanceled(any())).thenReturn(badResponse)

        service.markAsCanceled(sampleInvitesList)

        verify(service.inviteTransactionSummaryHelper, times(0)).cancelInviteByCnId("some invite id 1")
        verify(service.inviteTransactionSummaryHelper, times(0)).cancelInviteByCnId("some invite id 2")

    }

    @Test
    fun markAsCanceled_single() {
        val service = createManager()
        val tempInvite1: InviteTransactionSummary = mock()
        whenever(tempInvite1.serverId).thenReturn("some invite id 1")
        val response = getResponse(sentInviteJSON)

        whenever(service.client.updateInviteStatusCanceled(any())).thenReturn(response)

        service.markAsCanceled(tempInvite1)

        verify(service.inviteTransactionSummaryHelper).cancelInviteByCnId("some invite id 1")
        verify(service.cnWalletManager, times(1)).updateBalances()
    }

    @Test
    fun markAsCanceled_single_server_error() {
        val service = createManager()
        val tempInvite1: InviteTransactionSummary = mock()
        whenever(tempInvite1.serverId).thenReturn("some invite id 1")

        val badResponse = badResponse

        whenever(service.client.updateInviteStatusCanceled(any())).thenReturn(badResponse)

        service.markAsCanceled(tempInvite1)

        verify(service.inviteTransactionSummaryHelper, times(0)).cancelInviteByCnId("some invite id 1")
    }

    @Test
    fun markAsCanceled_string() {
        val service = createManager()
        val serverInviteId = "some invite id 1"

        val tempInvite1: InviteTransactionSummary = mock()
        whenever(tempInvite1.serverId).thenReturn(serverInviteId)

        val response = getResponse(sentInviteJSON)

        whenever(service.inviteTransactionSummaryHelper.getInviteSummaryByCnId(serverInviteId)).thenReturn(tempInvite1)
        whenever(service.client.updateInviteStatusCanceled(any())).thenReturn(response)

        service.markAsCanceled(serverInviteId)

        verify(service.inviteTransactionSummaryHelper).cancelInviteByCnId(serverInviteId)
    }

    @Test
    fun markAsCanceled_string_server_error() {
        val service = createManager()
        val serverInviteId = "some invite id 1"

        val tempInvite1: InviteTransactionSummary = mock()
        whenever(tempInvite1.serverId).thenReturn(serverInviteId)

        val badResponse = badResponse
        whenever(service.client.updateInviteStatusCanceled(any())).thenReturn(badResponse)

        service.markAsCanceled(tempInvite1)

        verify(service.inviteTransactionSummaryHelper, times(0)).cancelInviteByCnId(serverInviteId)
    }

    @Test
    fun markAsCanceled_string_null() {
        val service = createManager()
        val serverInviteId = "some invite id 1"
        whenever(service.inviteTransactionSummaryHelper.getInviteSummaryByCnId(serverInviteId)).thenReturn(null)

        service.markAsCanceled(serverInviteId)

        verify(service.inviteTransactionSummaryHelper, times(0)).cancelInviteByCnId(serverInviteId)
    }

    @Test
    fun markAsCanceled_emptyList() {
        val service = createManager()
        val sampleInvitesList = ArrayList<InviteTransactionSummary>()

        service.markAsCanceled(sampleInvitesList)

        verify(service.client, times(0)).updateInviteStatusCanceled(any())
        verify(service.inviteTransactionSummaryHelper, times(0)).cancelInviteByCnId("some invite id 1")
        verify(service.inviteTransactionSummaryHelper, times(0)).cancelInviteByCnId("some invite id 2")

    }

    private fun getResponse(responseData: String): Response<*> {
        return Response.success(responseData, okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build())
    }

    companion object {
        private val badResponse: Response<*>
            get() = Response.error<Any>(400, ResponseBody.create(MediaType.parse("application/json"),
                    "[]"))

        private const val sentInviteJSON = "[\n" +
                "  {\n" +
                "    \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
                "    \"created_at\": 1531921356,\n" +
                "    \"updated_at\": 1531921356,\n" +
                "    \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
                "    \"metadata\": {\n" +
                "      \"amount\": {\n" +
                "        \"btc\": 120000000,\n" +
                "        \"usd\": 8292280\n" +
                "      },\n" +
                "      \"sender\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      },\n" +
                "      \"receiver\": {\n" +
                "        \"country_code\": 1,\n" +
                "        \"phone_number\": \"5554441234\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
                "    \"request_ttl\": 1531921356,\n" +
                "    \"status\": \"new\",\n" +
                "    \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                "    \"user_id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "  }\n" +
                "]"
    }


}