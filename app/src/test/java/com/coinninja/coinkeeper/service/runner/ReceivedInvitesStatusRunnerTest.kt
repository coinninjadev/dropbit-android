package com.coinninja.coinkeeper.service.runner

import com.coinninja.coinkeeper.service.client.model.ReceivedInvite
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response

class ReceivedInvitesStatusRunnerTest {
    private fun createRunner(): ReceivedInvitesStatusRunner = ReceivedInvitesStatusRunner(mock(), mock(), mock(), mock(), mock())

    @Test
    fun updates_user_profile_to_account_for_dropbit_receive() {
        val runner = createRunner()
        whenever(runner.client.receivedInvites).thenReturn(getResponse(Companion.fulfilledReceivedRequest))

        runner.run()

        verify(runner.analytics).setUserProperty(Analytics.PROPERTY_HAS_RECEIVED_DROPBIT, true)
    }

    @Test
    fun save_completed_invite_test() {
        val runner = createRunner()
        whenever(runner.client.receivedInvites).thenReturn(getResponse(Companion.fulfilledReceivedRequest))

        runner.run()

        verify(runner.inviteTransactionSummaryHelper).updateFulfilledInviteByCnId("a1bb1d88-bfc8-4085-8966-e0062278237c", "7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03")
    }

    @Test
    fun not_save_uncompleted_invite_test() {
        val runner = createRunner()
        whenever(runner.client.receivedInvites).thenReturn(getResponse(Companion.un_fulfilledReceivedRequest))


        runner.run()

        verify(runner.inviteTransactionSummaryHelper, times(0)).updateFulfilledInviteByCnId(any(), any())
    }

    @Test
    fun not_save_bad_response_from_server_invite_test() {
        val runner = createRunner()
        whenever(runner.client.receivedInvites).thenReturn(badResponse())

        runner.run()

        verify(runner.inviteTransactionSummaryHelper, times(0)).updateFulfilledInviteByCnId(any(), any())
        verify(runner.logger).logError(ReceivedInvitesStatusRunner.TAG, ReceivedInvitesStatusRunner.RECEIVED_INVITE_FAILED, runner.client.receivedInvites)
    }

    private fun <T> getResponse(jsonArray: String): Response<T> {
        return Response.success(Gson().fromJson<T>(jsonArray, object : TypeToken<List<ReceivedInvite>>() {

        }.type), okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build())
    }

    companion object {
        private fun <T> badResponse(): Response<T> = Response.error<T>(400, ResponseBody.create(MediaType.parse("application/json"),
                "[]"))

        private const val fulfilledReceivedRequest = "[\n" +
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
                "    \"status\": \"completed\",\n" +
                "    \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "  }\n" +
                "]"
        private const val un_fulfilledReceivedRequest = "[\n" +
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
                "    \"txid\": \"\",\n" +
                "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
                "  }\n" +
                "]"
    }

}