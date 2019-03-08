package com.coinninja.coinkeeper.service.runner;

import android.app.Service;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class RegisterCNPhoneNumberRunnableTest {

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
    private Service service;
    @Mock
    private CoinKeeperApplication application;
    @Mock
    private UserHelper user;
    @Mock
    private DataSigner dataSigner;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private Account account;
    @Mock
    private LocalBroadCastUtil localBroadCast;
    @Mock
    ResendPhoneVerificationRunner resendPhoneVerificationRunner;
    private RegisterPhoneNumberRunnable runner;
    private CNUserAccount cnUserAccount;


    @Before
    public void setUp() {
        when(service.getApplication()).thenReturn(application);
        when(application.getUser()).thenReturn(user);
        when(application.getSecuredClient()).thenReturn(apiClient);
        when(application.getLocalBroadCastUtil()).thenReturn(localBroadCast);
        when(user.getWalletHelper()).thenReturn(walletHelper);
        when(dataSigner.getCoinNinjaVerificationKey()).thenReturn(SIGN_VERIFICATION_KEY);
        when(walletHelper.hasAccount()).thenReturn(true);
        when(walletHelper.getUserAccount()).thenReturn(account);

        runner = new RegisterPhoneNumberRunnable(service);
        runner.setResendRunner(resendPhoneVerificationRunner);

        Gson gson = new Gson();
        cnUserAccount = gson.fromJson(json, CNUserAccount.class);
    }

    private void setResponse(int successCode) {
        Response response = getResponse(successCode);
        when(apiClient.registerUserAccount(any(CNPhoneNumber.class))).thenReturn(response);
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

    @Test
    public void does_not_register_user_when_user_does_not_exist() {
        when(walletHelper.hasAccount()).thenReturn(false);

        runner.run();

        verify(apiClient, times(0)).registerUserAccount(any());
    }

    @Test
    public void unsuccessful_cn_calls_end_runner() {
        ResponseBody body = ResponseBody.create(MediaType.parse("text"), "bad request");
        Response response = Response.error(400, body);
        when(apiClient.registerUserAccount(any(CNPhoneNumber.class))).thenReturn(response);

        runner.run();

        verify(walletHelper, times(0)).saveAccountRegistration(any(), eq(CNPhoneNumber));
        verify(localBroadCast).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);

    }


    @Test
    public void resends_verification_on_success_result() {
        setResponse(200);
        runner.setCNPhoneNumber(CNPhoneNumber);
        when(apiClient.resendVerification(CNPhoneNumber)).thenReturn(getResponse(200));

        runner.run();

        verify(resendPhoneVerificationRunner).setCNPhoneNumber(CNPhoneNumber);
        verify(resendPhoneVerificationRunner).run();
    }

    @Test
    public void saves_account_upon_200() {
        setResponse(200);

        runner.run();

        verify(walletHelper).updateUserID(cnUserAccount);
    }

    @Test
    public void broadcasts_on_201() {
        setResponse(201);

        runner.run();

        verify(walletHelper).saveAccountRegistration(cnUserAccount, null);

        verify(localBroadCast).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT);

    }

    @Test
    public void instructs_api_client_to_verify_given_number() {
        setResponse(201);

        runner.setCNPhoneNumber(CNPhoneNumber);

        runner.run();

        verify(apiClient).registerUserAccount(CNPhoneNumber);
    }

    @Test
    public void create_new_account_server_response_ok_test() {
        setResponse(201);

        runner.setCNPhoneNumber(CNPhoneNumber);
        when(apiClient.resendVerification(CNPhoneNumber)).thenReturn(getResponse(200));

        runner.run();

        verify(localBroadCast).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT);
    }

}