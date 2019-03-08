package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.RemoteAddressCache;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserPhoneConfirmationRunnerTest {

    private static final String CODE = "012983";
    private final static String VERIFIED_JSON = "{\n" +
            "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
            "  \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
            "  \"created_at\": \"2018-05-09T16:09:05.294Z\",\n" +
            "  \"updated_at\": \"2018-05-09T16:09:05.294Z\",\n" +
            "  \"status\": \"verified\",\n" +
            "  \"user_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
            "  \"verification_ttl\": \"2018-05-09T16:09:05.294Z\",\n" +
            "  \"verified_at\": \"2018-05-09T16:09:05.294Z\"\n" +
            "}";
    private final static String EXPIRED_JSON = "{\n" +
            "  \"id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
            "  \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
            "  \"created_at\": \"2018-05-09T16:09:05.294Z\",\n" +
            "  \"updated_at\": \"2018-05-09T16:09:05.294Z\",\n" +
            "  \"status\": \"expired\",\n" +
            "  \"user_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\",\n" +
            "  \"verification_ttl\": \"2018-05-09T16:09:05.294Z\",\n" +
            "  \"verified_at\": \"2018-05-09T16:09:05.294Z\"\n" +
            "}";

    @Mock
    private SignedCoinKeeperApiClient apiClient;

    @Mock
    private Analytics analytics;

    @Mock
    private Account account;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private RemoteAddressCache remoteAddressCache;

    @Mock
    CNLogger logger;

    @InjectMocks
    private UserPhoneConfirmationRunner runner;

    @Before
    public void setUp() {
        when(account.getStatus()).thenReturn(Account.Status.PENDING_VERIFICATION);
        Gson gson = new Gson();
        CNUserAccount cnAccount = gson.fromJson(VERIFIED_JSON, CNUserAccount.class);
        Response response = Response.success(cnAccount);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);
        runner.setCode(CODE);
    }

    @After
    public void tearDown() {
        apiClient = null;
        account = null;
        localBroadCastUtil = null;
        runner = null;
    }

    @Test
    public void sets_phone_verified_property_to_true_when_verified() {
        runner.run();

        verify(analytics).setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, true);
        verify(analytics).flush();
    }

    @Test
    public void broadcasts_http_failure_for_anyone_who_may_care() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("text"), "");
        Response response = Response.error(404, responseBody);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);

        runner.run();

        verify(localBroadCastUtil).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);
    }

    @Test
    public void error_409_sends_expired_broadcast() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "{\"error\":\"verification code invalid\",\"message\":\"Bad Request\",\"status\":400}");
        Response response = Response.error(409, responseBody);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);

        runner.run();

        verify(localBroadCastUtil).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE);
    }

    @Test
    public void broadcasts_invalid_code_submission() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "{\"error\":\"verification code invalid\",\"message\":\"Bad Request\",\"status\":400}");
        Response response = Response.error(400, responseBody);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);


        runner.run();

        verify(localBroadCastUtil).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__INVALID_CODE);
    }

    @Test
    public void broadcasts_failed_code_submissions() {
        Gson gson = new Gson();
        CNUserAccount cnAccount = gson.fromJson(EXPIRED_JSON, CNUserAccount.class);
        Response response = Response.success(cnAccount);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);


        runner.run();

        verify(localBroadCastUtil).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE);
    }

    @Test
    public void broadcasts_successful_account_creation() {
        runner.run();

        verify(localBroadCastUtil).sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__SUCCESS);
    }

    @Test
    public void only_save_verified_responses() {
        Gson gson = new Gson();
        CNUserAccount cnAccount = gson.fromJson(EXPIRED_JSON, CNUserAccount.class);
        Response response = Response.success(cnAccount);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);

        runner.run();

        verify(account, times(0)).setStatus(any(Account.Status.class));
        verify(account, times(0)).update();
    }

    @Test
    public void unsuccessful_api_calls_do_not_save() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("text"), "der");
        Response response = Response.error(400, responseBody);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);

        runner.run();

        verify(account, times(0)).setStatus(any(Account.Status.class));
        verify(account, times(0)).update();

    }

    @Test
    public void sends_addresses_to_coin_ninja() {
        runner.run();

        verify(remoteAddressCache).cacheAddresses();
    }

    @Test
    public void updates_account_on_successful_verification() {
        runner.run();

        verify(account).setStatus(Account.Status.VERIFIED);
        verify(account).update();
    }

    @Test
    public void will_not_run_if_user_is_not_pending_verification() {
        when(account.getStatus()).thenReturn(Account.Status.UNVERIFIED);

        runner.run();

        verify(apiClient, times(0)).verifyPhoneCode(CODE);
    }

    @Test
    public void calls_api_client_to_verify_confirmation_code() {

        runner.run();

        verify(apiClient).verifyPhoneCode(CODE);
    }

}
