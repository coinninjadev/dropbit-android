package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResendPhoneVerificationRunnerTest {


    private static final String SIGN_VERIFICATION_KEY = "---pub-sign-key---";

    String json = "{\n" +
            "\"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
            "\"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
            "\"created_at\": \"2018-05-09T16:09:05.294Z\",\n" +
            "\"updated_at\": \"2018-05-09T16:09:05.294Z\",\n" +
            "\"status\": \"pending-verification\",\n" +
            "\"user_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
            "\"verification_ttl\": \"2018-05-09T16:09:05.294Z\",\n" +
            "\"verified_at\": \"2018-05-09T16:09:05.294Z\"\n" +
            "}";
    @Mock
    private SignedCoinKeeperApiClient apiClient;
    @Mock
    private CNPhoneNumber CNPhoneNumber;
    @Mock
    private DataSigner dataSigner;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private Account account;
    @Mock
    private LocalBroadCastUtil localBroadCast;
    private CNUserAccount cnUserAccount;
    @InjectMocks
    private ResendPhoneVerificationRunner runner;

    @After
    public void tearDown() {
        apiClient = null;
        CNPhoneNumber = null;
        dataSigner = null;
        walletHelper = null;
        account = null;
        localBroadCast = null;
        cnUserAccount = null;
        runner = null;
    }

    @Before
    public void setUp() {
        when(dataSigner.getCoinNinjaVerificationKey()).thenReturn(SIGN_VERIFICATION_KEY);
        when(walletHelper.hasAccount()).thenReturn(true);
        when(walletHelper.getUserAccount()).thenReturn(account);
        Gson gson = new Gson();
        cnUserAccount = gson.fromJson(json, CNUserAccount.class);
        setResponse(200);
    }

    @Test
    public void reseeds_verification_on_execution() {
        setResponse(200);
        runner.setCNPhoneNumber(CNPhoneNumber);
        when(apiClient.resendVerification(CNPhoneNumber)).thenReturn(getResponse(200));

        runner.run();

        verify(apiClient).resendVerification(CNPhoneNumber);
    }

    @Test
    public void saves_user_account_when_resend_is_successful() {
        setResponse(200);
        runner.setCNPhoneNumber(CNPhoneNumber);
        when(apiClient.resendVerification(CNPhoneNumber)).thenReturn(getResponse(200));

        runner.run();

        verify(walletHelper).saveAccountRegistration(cnUserAccount, CNPhoneNumber);
    }

    @Test
    public void unsuccessful_resend_ends_runner() {
        runner.setCNPhoneNumber(CNPhoneNumber);
        when(apiClient.resendVerification(CNPhoneNumber)).thenReturn(Response.error(400,
                ResponseBody.create(MediaType.parse("text"), "bad request")));

        runner.run();

        verify(walletHelper, times(0)).saveAccountRegistration(any(), eq(CNPhoneNumber));
        verify(localBroadCast).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);

    }

    @Test
    public void send_code_via_sms_server_response_with_rate_limit_error_test() {
        runner.setCNPhoneNumber(CNPhoneNumber);
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "{\"error\":\"must wait 30 seconds before resending\",\"message\":\"Too Many Requests\",\"status\":429}");
        Response response = Response.error(429, responseBody);
        when(apiClient.resendVerification(CNPhoneNumber)).thenReturn(response);


        runner.run();

        verify(localBroadCast).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR);
    }

    @Test
    public void send_code_via_sms_server_response_with_blacklist_error_test() {
        runner.setCNPhoneNumber(CNPhoneNumber);
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "");
        Response response = Response.error(424, responseBody);
        when(apiClient.resendVerification(CNPhoneNumber)).thenReturn(response);


        runner.run();

        verify(localBroadCast).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR);
    }

    @Test
    public void send_code_via_sms_server_response_ok_test() {
        runner.setCNPhoneNumber(CNPhoneNumber);
        when(apiClient.resendVerification(CNPhoneNumber)).thenReturn(getResponse(200));

        runner.run();

        verify(localBroadCast).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT);
    }

    @NonNull
    private Response getResponse(int successCode) {
        return Response.success(cnUserAccount, new okhttp3.Response.Builder()
                .code(successCode)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    private void setResponse(int successCode) {
        Response response = getResponse(successCode);
        when(apiClient.registerUserAccount(any(CNPhoneNumber.class))).thenReturn(response);
    }
}