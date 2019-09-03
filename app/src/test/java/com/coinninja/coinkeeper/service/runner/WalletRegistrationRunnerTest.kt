package com.coinninja.coinkeeper.service.runner

import com.coinninja.coinkeeper.receiver.WalletRegistrationCompleteReceiver
import com.coinninja.coinkeeper.service.client.model.CNWallet
import com.coinninja.coinkeeper.service.client.model.WalletRegistrationPayload
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response


class WalletRegistrationRunnerTest {

    companion object {
        private const val JSON = "{\n" +
                "  \"id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "  \"public_key_string\": \"02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356,\n" +
                "  \"user_id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
                "  \"flags\": 1\n" +
                "}"
        private const val SIGN_VERIFICATION_KEY = "--Sign-Verification-Key--"
        private val CN_WALLET = Gson().fromJson(JSON, CNWallet::class.java)
        private val SUCCESSFUL_RESPONSE = Response.success(CN_WALLET)
    }

    private fun createRunner(): WalletRegistrationRunner {
        val runner = WalletRegistrationRunner(mock(), mock(), mock(), mock(), mock(), mock(), mock())
        whenever(runner.dataSigner.coinNinjaVerificationKey).thenReturn(SIGN_VERIFICATION_KEY)
        whenever(runner.apiClient.registerWallet(any())).thenReturn(SUCCESSFUL_RESPONSE)
        return runner
    }

    @Test
    fun notifies_system_that_new_wallet_registration_completed_successfully_given_wallet_id() {
        val runner = createRunner()
        whenever(runner.walletHelper.hasAccount()).thenReturn(true)

        runner.run()

        verify(runner.localBroadCastUtil).sendGlobalBroadcast(WalletRegistrationCompleteReceiver::class.java, DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE)
    }


    @Test
    fun notifies_system_that_new_wallet_registration_completed_successfully() {
        val runner = createRunner()

        runner.run()

        verify(runner.localBroadCastUtil)
                .sendGlobalBroadcast(WalletRegistrationCompleteReceiver::class.java, DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE)
    }

    @Test
    fun logs_undesired_responses() {
        val runner = createRunner()
        val body = ResponseBody.create(MediaType.parse("text"), "bad request")
        val response = Response.error<CNWallet>(400, body)
        whenever(runner.apiClient.registerWallet(any())).thenReturn(response)

        runner.run()

        verify(runner.logger).logError(any(), any(), eq(response))
        verify(runner.localBroadCastUtil, times(0)).sendGlobalBroadcast(any(), any())
    }

    @Test
    fun does_not_register_wallet_when_one_exists() {
        val runner = createRunner()
        whenever(runner.walletHelper.hasAccount()).thenReturn(true)

        runner.run()

        verify(runner.apiClient, times(0)).registerWallet(any())
    }

    @Test
    fun unsuccessful_cn_calls_end_runner() {
        val runner = createRunner()
        val body = ResponseBody.create(MediaType.parse("text"), "bad request")
        val response = Response.error<CNWallet>(400, body)
        whenever(runner.apiClient.registerWallet(any())).thenReturn(response)

        runner.run()

        verify(runner.walletHelper, times(0)).saveRegistration(any())
    }


    @Test
    fun saves_wallet_registration_in_wallet() {
        val runner = createRunner()
        whenever(runner.apiClient.registerWallet(any())).thenReturn(SUCCESSFUL_RESPONSE)

        runner.run()

        verify(runner.walletHelper).saveRegistration(CN_WALLET)
    }

    @Test
    fun registers_user_with_CN() {
        val runner = createRunner()
        whenever(runner.walletFlagsStorage.flags).thenReturn(1)

        runner.run()

        verify(runner.apiClient).registerWallet(WalletRegistrationPayload(SIGN_VERIFICATION_KEY, 1))
        verify(runner.analytics).setUserProperty(Analytics.PROPERTY_APP_V1, true)
    }

    @Test
    fun updates_flags_from_server_when_different() {
        val runner = createRunner()

        runner.processSuccess(SUCCESSFUL_RESPONSE, 0)

        verify(runner.walletFlagsStorage).flags = 1
        verify(runner.analytics).setUserProperty(Analytics.PROPERTY_APP_V1, true)
    }
}