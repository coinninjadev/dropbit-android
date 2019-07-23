package com.coinninja.coinkeeper.service.runner

import com.coinninja.coinkeeper.service.client.model.ReceivedInvite
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response


@RunWith(MockitoJUnitRunner::class)
class GetIncomingInviteRunnerTest {

    private fun createRunner(): GetIncomingInviteRunner = GetIncomingInviteRunner(mock(), mock())

    private val badResponse: Response<*>
        get() = Response.error<Any>(400, ResponseBody.create(MediaType.parse("application/json"),
                "[]"))

    @Test
    fun writes_two_invites_to_database_test() {
        val runner = createRunner()
        val testData = listOf(ReceivedInvite(), ReceivedInvite())
        val response = getResponse(testData)
        whenever(runner.client.receivedInvites).thenReturn(response)

        runner.run()

        verify(runner.inviteTransactionSummaryHelper, times(2)).saveReceivedInviteTransaction(any())
    }

    @Test
    fun server_fail_test() {
        val runner = createRunner()
        val response = badResponse
        whenever(runner.client.receivedInvites).thenReturn(response)

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

    private fun getResponse(responseData: Any): Response<*> {
        return Response.success(responseData, okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build())
    }

}