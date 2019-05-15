package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.RemoteAddressCache;
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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
    private DropbitAccountHelper dropbitAccountHelper;

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
        runner.setCode(CODE);
    }

    @After
    public void tearDown() {
        apiClient = null;
        dropbitAccountHelper = null;
        localBroadCastUtil = null;
        runner = null;
    }

    @Test
    public void saves_verified_responses() {
        Gson gson = new Gson();
        CNUserAccount cnAccount = gson.fromJson(VERIFIED_JSON, CNUserAccount.class);
        Response response = Response.success(cnAccount);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);

        runner.run();

        verify(dropbitAccountHelper).updateVerifiedAccount(cnAccount);
        verify(remoteAddressCache).cacheAddresses();
        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS);
        verify(analytics).setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, true);
        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, true);
        verify(analytics).flush();
    }

    @Test
    public void broadcasts_http_failure_for_anyone_who_may_care() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("text"), "");
        Response response = Response.error(404, responseBody);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);

        runner.run();

        verifyZeroInteractions(dropbitAccountHelper);
        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);
    }

    @Test
    public void only_save_verified_responses() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "{\"error\":\"verification code invalid\",\"message\":\"Bad Request\",\"status\":400}");
        Response response = Response.error(409, responseBody);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);

        runner.run();

        verifyZeroInteractions(dropbitAccountHelper);
        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE);
    }

    @Test
    public void unsuccessful_api_calls_do_not_save() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("text"), "der");
        Response response = Response.error(400, responseBody);
        when(apiClient.verifyPhoneCode(CODE)).thenReturn(response);

        runner.run();

        verifyZeroInteractions(dropbitAccountHelper);
        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE);
    }

}
