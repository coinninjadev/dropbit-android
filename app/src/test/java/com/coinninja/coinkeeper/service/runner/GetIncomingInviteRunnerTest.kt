package com.coinninja.coinkeeper.service.runner

import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response


class GetIncomingInviteRunnerTest {

    private fun createRunner(): GetIncomingInviteRunner = GetIncomingInviteRunner(mock(), mock(), mock(), mock())

    private fun <T> badResponse(): Response<T> = Response.error<T>(400, ResponseBody.create(MediaType.parse("application/json"),
            "[]"))

    @Test
    fun writes_two_invites_to_database_test() {
        val runner = createRunner()
        val testData = listOf(ReceivedInvite(address_type = "btc"), ReceivedInvite(address_type = "lightning"))
        val response = getResponse(testData)
        val btcInvite: InviteTransactionSummary = mock()
        whenever(btcInvite.type).thenReturn(Type.BLOCKCHAIN_RECEIVED)
        val lightingInvite: InviteTransactionSummary = mock()
        val toUser: UserIdentity = mock()
        val fromUser: UserIdentity = mock()
        whenever(lightingInvite.id).thenReturn(20)
        whenever(lightingInvite.type).thenReturn(Type.LIGHTNING_RECEIVED)
        whenever(lightingInvite.sentDate).thenReturn(1570413011317)
        whenever(lightingInvite.toUser).thenReturn(toUser)
        whenever(lightingInvite.fromUser).thenReturn(fromUser)
        whenever(toUser.id).thenReturn(5)
        whenever(fromUser.id).thenReturn(15)
        whenever(lightingInvite.toUser).thenReturn(toUser)
        whenever(runner.inviteTransactionSummaryHelper.saveReceivedInviteTransaction(any())).thenReturn(mock()).thenReturn(lightingInvite)
        whenever(runner.client.receivedInvites).thenReturn(response)

        runner.run()

        verify(runner.inviteTransactionSummaryHelper, times(2)).saveReceivedInviteTransaction(any())
        verify(runner.thunderDomeRepository).createSettlementForInvite(20, 5, 15, 1570413011317)
    }

    @Test
    fun server_fail_test() {
        val runner = createRunner()
        whenever(runner.client.receivedInvites).thenReturn(badResponse())

        runner.run()

        verify(runner.inviteTransactionSummaryHelper, times(0)).saveReceivedInviteTransaction(any())
    }

    @Test
    fun server_good_but_has_no_data_test() {
        val runner = createRunner()
        val testData = emptyList<ReceivedInvite>()
        val response = getResponse(testData)
        whenever(runner.client.receivedInvites).thenReturn(response)

        runner.run()

        verify(runner.inviteTransactionSummaryHelper, times(0)).saveReceivedInviteTransaction(any())
    }

    private fun <T> getResponse(responseData: T): Response<T> {
        return Response.success(responseData, okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build())
    }

}