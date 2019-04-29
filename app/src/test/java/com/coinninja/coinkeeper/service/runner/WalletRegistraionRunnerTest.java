package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.receiver.WalletRegistrationCompleteReceiver;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WalletRegistraionRunnerTest {

    private final String JSON = "{\n" +
            "  \"id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
            "  \"public_key_string\": \"02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473\",\n" +
            "  \"created_at\": 1531921356,\n" +
            "  \"updated_at\": 1531921356,\n" +
            "  \"user_id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\"\n" +
            "}";
    private Gson GSON = new Gson();
    private final String SIGN_VERIFICATION_KEY = "--Sign-Verification-Key--";
    private final CNWallet CN_WALLET = GSON.fromJson(JSON, CNWallet.class);
    private final Response SUCCESSFUL_RESPONSE = Response.success(CN_WALLET);

    WalletRegistraionRunner runner;

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @Mock
    WalletHelper walletHelper;

    @Mock
    SignedCoinKeeperApiClient signedCoinKeeperClient;

    @Mock
    DataSigner dataSigner;

    @Mock
    CNLogger logger;

    @Before
    public void setUp() {
        when(dataSigner.getCoinNinjaVerificationKey()).thenReturn(SIGN_VERIFICATION_KEY);
        runner = new WalletRegistraionRunner(localBroadCastUtil, signedCoinKeeperClient, dataSigner, walletHelper, logger);

        String json = "{\n" +
                "  \"id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
                "  \"public_key_string\": \"02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921356\n" +
                "}";
        Gson gson = new Gson();
        CNWallet cnWallet = gson.fromJson(json, CNWallet.class);
        Response response = Response.success(cnWallet);
        when(signedCoinKeeperClient.registerWallet(anyString())).thenReturn(response);
    }

    @Test
    public void notifies_system_that_new_wallet_registration_completed_successfully_given_wallet_id() {
        when(walletHelper.hasAccount()).thenReturn(true);

        runner.run();

        verify(localBroadCastUtil).sendGlobalBroadcast(eq(WalletRegistrationCompleteReceiver.class),
                eq(DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE));

    }

    @Test
    public void notifies_system_that_new_wallet_registration_completed_successfully() {
        when(signedCoinKeeperClient.registerWallet(anyString())).thenReturn(SUCCESSFUL_RESPONSE);

        runner.run();

        verify(localBroadCastUtil).sendGlobalBroadcast(eq(WalletRegistrationCompleteReceiver.class),
                eq(DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE));
    }

    @Test
    public void logs_undesired_responses() {
        ResponseBody body = ResponseBody.create(MediaType.parse("text"), "bad request");
        Response response = Response.error(400, body);
        when(signedCoinKeeperClient.registerWallet(anyString())).thenReturn(response);

        runner.run();

        verify(logger).logError(anyString(), anyString(), eq(response));
        verify(localBroadCastUtil, times(0)).sendGlobalBroadcast(any(), any());
    }

    @Test
    public void does_not_register_wallet_when_one_exists() {
        when(walletHelper.hasAccount()).thenReturn(true);

        runner.run();

        verify(signedCoinKeeperClient, times(0)).registerWallet(any());
    }

    @Test
    public void unsuccessful_cn_calls_end_runner() {
        ResponseBody body = ResponseBody.create(MediaType.parse("text"), "bad request");
        Response response = Response.error(400, body);
        when(signedCoinKeeperClient.registerWallet(anyString())).thenReturn(response);

        runner.run();

        verify(walletHelper, times(0)).saveRegistration(any());
    }


    @Test
    public void saves_wallet_registration_in_wallet() {
        when(signedCoinKeeperClient.registerWallet(anyString())).thenReturn(SUCCESSFUL_RESPONSE);

        runner.run();

        verify(walletHelper).saveRegistration(CN_WALLET);
    }

    @Test
    public void registers_user_with_CN() {
        runner.run();

        verify(signedCoinKeeperClient).registerWallet(SIGN_VERIFICATION_KEY);
    }
}